package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.ComponentsMapper;
import com.scut.industrial_software.model.entity.Components;
import com.scut.industrial_software.service.IComponentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class ComponentServiceImpl extends ServiceImpl<ComponentsMapper, Components> implements IComponentService {

    //从yaml文件读取资源路径，暂时先写死
    private final String resourcePath = "D:/resources/";
    private static final String INSTALL_FILE_NAME = "install.exe";
    private static final String BATCH_ZIP_FILE_NAME = "components-install-packages.zip";

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
            Path filePath = resolvedPath.resolve(INSTALL_FILE_NAME).normalize();
            File file = filePath.toFile();

            if (!file.exists()) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 4. 查询文件名（数据库操作放在前面，避免后续流操作时持锁）
            Components component = baseMapper.selectById(componentId);
            if (component == null) {
                log.warn("组件不存在，componentId: {}", componentId);
                return ResponseEntity.notFound().build();
            }
            String fileNameWithExtension = buildInstallPackageName(component, componentId);
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

    @Override
    public ResponseEntity<StreamingResponseBody> downloadModules(List<Integer> componentIds) {
        log.info("正在批量下载组件安装包，componentIds: {}", componentIds);

        if (componentIds == null || componentIds.isEmpty() || componentIds.stream().anyMatch(id -> id == null || id <= 0)) {
            log.warn("批量下载组件参数无效: {}", componentIds);
            return ResponseEntity.badRequest().build();
        }

        try {
            Path basePath = Paths.get(resourcePath).normalize().toAbsolutePath();
            List<ComponentPackage> packages = new ArrayList<>();
            Set<String> usedEntryNames = new HashSet<>();

            for (Integer componentId : new LinkedHashSet<>(componentIds)) {
                Components component = baseMapper.selectById(componentId);
                if (component == null) {
                    log.warn("批量下载组件不存在，componentId: {}", componentId);
                    return ResponseEntity.notFound().build();
                }

                Path filePath = resolveInstallPackagePath(basePath, componentId);
                if (filePath == null) {
                    log.warn("批量下载组件路径非法，componentId: {}", componentId);
                    return ResponseEntity.badRequest().build();
                }

                if (!Files.isRegularFile(filePath)) {
                    log.warn("批量下载组件安装包不存在，componentId: {}, filePath: {}", componentId, filePath);
                    return ResponseEntity.notFound().build();
                }

                String entryName = uniqueZipEntryName(buildInstallPackageName(component, componentId), componentId, usedEntryNames);
                packages.add(new ComponentPackage(filePath, entryName));
            }

            StreamingResponseBody body = outputStream -> {
                byte[] buffer = new byte[8192];
                try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
                    for (ComponentPackage componentPackage : packages) {
                        ZipEntry entry = new ZipEntry(componentPackage.entryName());
                        entry.setSize(Files.size(componentPackage.filePath()));
                        zipOutputStream.putNextEntry(entry);

                        try (InputStream inputStream = Files.newInputStream(componentPackage.filePath())) {
                            int length;
                            while ((length = inputStream.read(buffer)) != -1) {
                                zipOutputStream.write(buffer, 0, length);
                            }
                        }

                        zipOutputStream.closeEntry();
                    }
                }
            };

            String encodedFileName = URLEncoder.encode(BATCH_ZIP_FILE_NAME, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .body(body);

        } catch (Exception e) {
            log.error("批量下载组件安装包失败，componentIds: {}", componentIds, e);
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

    private Path resolveInstallPackagePath(Path basePath, Integer componentId) {
        String relativePath = String.valueOf(componentId);
        Path resolvedPath = basePath.resolve(relativePath).normalize();

        if (!resolvedPath.startsWith(basePath)) {
            log.error("非法路径尝试: {} -> {}", relativePath, resolvedPath);
            return null;
        }

        return resolvedPath.resolve(INSTALL_FILE_NAME).normalize();
    }

    private String buildInstallPackageName(Components component, Integer componentId) {
        String componentName = component.getName();
        if (componentName == null || componentName.isBlank()) {
            componentName = "component-" + componentId;
        }

        return sanitizeFileName(componentName.trim()) + "安装包.exe";
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String uniqueZipEntryName(String fileName, Integer componentId, Set<String> usedEntryNames) {
        if (usedEntryNames.add(fileName)) {
            return fileName;
        }

        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        String extension = dotIndex > 0 ? fileName.substring(dotIndex) : "";
        String candidate = baseName + "-" + componentId + extension;

        int suffix = 2;
        while (!usedEntryNames.add(candidate)) {
            candidate = baseName + "-" + componentId + "-" + suffix + extension;
            suffix++;
        }

        return candidate;
    }

    private record ComponentPackage(Path filePath, String entryName) {
    }

    public ApiResult<?> getModuleList(){
        log.info("正在获取模块列表");

        List<Components> result = baseMapper.selectList(null);

        return ApiResult.success(result);
    }
}
