package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.ComponentsMapper;
import com.scut.industrial_software.model.entity.Components;
import com.scut.industrial_software.service.IComponentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
public class ComponentServiceImpl extends ServiceImpl<ComponentsMapper, Components> implements IComponentService {

    //从yaml文件读取资源路径，暂时先写死
    private final String resourcePath = "D:/resources/";

    @Override
    public ResponseEntity<Resource> downloadModule(Integer componentId,
                                                   @RequestHeader(value = "Range", required = false) String rangeHeader) {

        log.info("正在下载的组件ID为：{}, Range头: {}", componentId, rangeHeader);

        // 1. 参数校验
        if (componentId == null || componentId <= 0) {
            log.warn("无效的组件ID: {}", componentId);
            return ResponseEntity.badRequest().build();
        }

        try {
            // 2. 构建相对路径 & 安全校验
            String relativePath = String.valueOf(componentId);
            Path basePath = Paths.get(resourcePath).normalize().toAbsolutePath();
            Path resolvedPath = basePath.resolve(relativePath).normalize();

            if (!resolvedPath.startsWith(basePath)) {
                log.error("非法路径尝试: {} -> {}", relativePath, resolvedPath);
                return ResponseEntity.badRequest().build();
            }

            // 3. 定位文件
            Path filePath = resolvedPath.resolve("install.exe").normalize();
            File file = filePath.toFile();

            if (!file.exists()) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 4. 查询文件名（数据库操作放在前面，避免后续流操作时持锁）
            String componentName = baseMapper.selectById(componentId).getName();
            String fileNameWithExtension = componentName + "安装包.exe";
            String encodedFileName = URLEncoder.encode(fileNameWithExtension, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            long fileSize = file.length();

            // 5. 判断是否支持Range请求（断点续传）
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(filePath, fileSize, encodedFileName, rangeHeader);
            }

            // 6. 普通下载响应
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileSize)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")  // 告知客户端支持断点续传
                    .body(resource);

        } catch (Exception e) {
            log.error("下载模块失败，componentId: {}", componentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 处理Range请求（断点续传核心逻辑）
     */
    private ResponseEntity<Resource> handleRangeRequest(Path filePath, long fileSize,
                                                        String encodedFileName, String rangeHeader) throws IOException {

        // 解析Range头: "bytes=0-999" 或 "bytes=500-"
        String range = rangeHeader.replace("bytes=", "");
        String[] ranges = range.split("-");

        long start = Long.parseLong(ranges[0].trim());
        long end = (ranges.length > 1 && !ranges[1].trim().isEmpty()) ?
                Long.parseLong(ranges[1].trim()) : fileSize - 1;

        // 边界校验
        if (start < 0 || end >= fileSize || start > end) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                    .build();
        }

        long contentLength = end - start + 1;

        // 使用RandomAccessFile实现部分读取
        RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r");
        raf.seek(start);

        // 自定义InputStream，限制只读取指定范围
        InputStream rangeInputStream = new InputStream() {
            private long remaining = contentLength;

            @Override
            public int read() throws IOException {
                if (remaining <= 0) return -1;
                remaining--;
                return raf.read();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (remaining <= 0) return -1;
                int toRead = (int) Math.min(len, remaining);
                int bytesRead = raf.read(b, off, toRead);
                if (bytesRead > 0) {
                    remaining -= bytesRead;
                }
                return bytesRead;
            }

            @Override
            public void close() throws IOException {
                raf.close();
            }
        };

        // 返回206 Partial Content
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(contentLength)
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .body(new InputStreamResource(rangeInputStream) {
                    @Override
                    public String getFilename() {
                        return encodedFileName;
                    }
                });
    }

    public ApiResult<?> getModuleList(){
        log.info("正在获取模块列表");

        List<Components> result = baseMapper.selectList(null);

        return ApiResult.success(result);
    }
}
