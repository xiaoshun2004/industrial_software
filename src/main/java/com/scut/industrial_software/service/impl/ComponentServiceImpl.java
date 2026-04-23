package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.config.ComponentDownloadProperties;
import com.scut.industrial_software.mapper.ComponentsMapper;
import com.scut.industrial_software.model.constant.RedisConstants;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.model.entity.Components;
import com.scut.industrial_software.service.IComponentService;
import com.scut.industrial_software.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class ComponentServiceImpl extends ServiceImpl<ComponentsMapper, Components> implements IComponentService {

    private static final String INSTALL_FILE_NAME = "install.exe";
    private static final String BATCH_ZIP_FILE_NAME = "components-install-packages.zip";
    private static final String SINGLE_DOWNLOAD_PATH_TEMPLATE = "/components/install/%d/download?token=%s";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComponentDownloadProperties componentDownloadProperties;

    @Override
    public ResponseEntity<Resource> downloadModule(Integer componentId, String rangeHeader) {
        return downloadModuleInternal(componentId, rangeHeader);
    }

    // 生成单组件下载的临时Token，前端可以使用该Token调用下载接口进行安装，适用于大型组件的安装包
    @Override
    public ApiResult<?> createSingleDownloadToken(Integer componentId) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return ApiResult.unauthorized("请先登录");
        }
        if (componentId == null || componentId <= 0) {
            return ApiResult.failed("组件ID无效");
        }

        try {
            Components component = baseMapper.selectById(componentId);
            if (component == null) {
                return ApiResult.resourceNotFound("组件不存在");
            }

            Path filePath = resolveInstallPackagePath(getResourceBasePath(), componentId);
            if (filePath == null) {
                return ApiResult.failed("组件安装包路径非法");
            }
            if (!Files.isRegularFile(filePath)) {
                return ApiResult.resourceNotFound("组件安装包不存在");
            }

            String token = UUID.randomUUID().toString().replace("-", "");
            storeSingleDownloadToken(token, currentUser.getId(), componentId);

            Map<String, String> result = new HashMap<>();
            result.put("downloadUrl",
                    buildAbsoluteDownloadUrl(String.format(SINGLE_DOWNLOAD_PATH_TEMPLATE, componentId, token)));

            log.info("生成单组件下载临时Token成功，userId: {}, componentId: {}", currentUser.getId(), componentId);
            return ApiResult.success(result);
        } catch (Exception e) {
            log.error("生成单组件下载临时Token失败，componentId: {}", componentId, e);
            return ApiResult.internalServerError("生成组件下载链接失败");
        }
    }

    // 使用临时Token下载组件安装包，前端需要提供之前生成的Token，后端会验证Token的有效性后允许下载，适用于大型组件的安装包
    @Override
    public ResponseEntity<Resource> downloadModuleByToken(Integer componentId, String token, String rangeHeader) {
        if (componentId == null || componentId <= 0 || token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SingleDownloadTokenPayload payload = loadSingleDownloadToken(token);
            if (payload == null) {
                log.warn("单组件下载临时Token无效或已过期，componentId: {}", componentId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (!componentId.equals(payload.getComponentId())) {
                log.warn("单组件下载临时Token与组件不匹配，tokenComponentId: {}, requestComponentId: {}",
                        payload.getComponentId(), componentId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return downloadModuleInternal(componentId, rangeHeader);
        } catch (Exception e) {
            log.error("通过临时Token下载单组件失败，componentId: {}", componentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 内部方法，处理组件安装包的下载逻辑，支持断点续传
    private ResponseEntity<Resource> downloadModuleInternal(Integer componentId, String rangeHeader) {
        log.info("正在下载组件安装包，componentId: {}, Range: {}", componentId, rangeHeader);

        if (componentId == null || componentId <= 0) {
            log.warn("无效的组件ID: {}", componentId);
            return ResponseEntity.badRequest().build();
        }

        try {
            Path basePath = getResourceBasePath();
            Path filePath = resolveInstallPackagePath(basePath, componentId);
            if (filePath == null) {
                return ResponseEntity.badRequest().build();
            }

            File file = filePath.toFile();
            if (!file.exists()) {
                log.warn("组件安装包不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Components component = baseMapper.selectById(componentId);
            if (component == null) {
                log.warn("组件不存在，componentId: {}", componentId);
                return ResponseEntity.notFound().build();
            }

            String fileNameWithExtension = buildInstallPackageName(component, componentId);
            String encodedFileName = URLEncoder.encode(fileNameWithExtension, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            long fileSize = file.length();

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(filePath, fileSize, encodedFileName, rangeHeader);
            }

            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileSize)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(resource);
        } catch (Exception e) {
            log.error("下载组件安装包失败，componentId: {}", componentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 批量下载组件安装包，前端提供多个组件ID，后端将对应的安装包打包成一个ZIP文件进行下载
    @Override
    public ResponseEntity<StreamingResponseBody> downloadModules(List<Integer> componentIds) {
        log.info("姝ｅ湪鎵归噺涓嬭浇缁勪欢瀹夎鍖咃紝componentIds: {}", componentIds);

        if (componentIds == null || componentIds.isEmpty() || componentIds.stream().anyMatch(id -> id == null || id <= 0)) {
            log.warn("鎵归噺涓嬭浇缁勪欢鍙傛暟鏃犳晥: {}", componentIds);
            return ResponseEntity.badRequest().build();
        }

        try {
            Path basePath = getResourceBasePath();
            List<ComponentPackage> packages = new ArrayList<>();
            Set<String> usedEntryNames = new HashSet<>();

            for (Integer componentId : new LinkedHashSet<>(componentIds)) {
                Components component = baseMapper.selectById(componentId);
                if (component == null) {
                    log.warn("鎵归噺涓嬭浇缁勪欢涓嶅瓨鍦紝componentId: {}", componentId);
                    return ResponseEntity.notFound().build();
                }

                Path filePath = resolveInstallPackagePath(basePath, componentId);
                if (filePath == null) {
                    log.warn("鎵归噺涓嬭浇缁勪欢璺緞闈炴硶锛宑omponentId: {}", componentId);
                    return ResponseEntity.badRequest().build();
                }

                if (!Files.isRegularFile(filePath)) {
                    log.warn("鎵归噺涓嬭浇缁勪欢瀹夎鍖呬笉瀛樺湪锛宑omponentId: {}, filePath: {}", componentId, filePath);
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
            log.error("鎵归噺涓嬭浇缁勪欢瀹夎鍖呭け璐ワ紝componentIds: {}", componentIds, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 处理断点续传的请求，根据 Range 头部信息返回对应的文件片段，支持单个范围请求
     */
    private ResponseEntity<Resource> handleRangeRequest(Path filePath, long fileSize,
                                                        String encodedFileName, String rangeHeader) throws IOException {

        String range = rangeHeader.replace("bytes=", "");
        String[] ranges = range.split("-");

        long start = Long.parseLong(ranges[0].trim());
        long end = (ranges.length > 1 && !ranges[1].trim().isEmpty()) ?
                Long.parseLong(ranges[1].trim()) : fileSize - 1;

        if (start < 0 || end >= fileSize || start > end) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                    .build();
        }

        long contentLength = end - start + 1;
        RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r");
        raf.seek(start);

        InputStream rangeInputStream = new InputStream() {
            private long remaining = contentLength;

            // 关闭InputStream时同时关闭RandomAccessFile，确保资源正确释放
            @Override
            public int read() throws IOException {
                if (remaining <= 0) {
                    return -1;
                }
                remaining--;
                return raf.read();
            }

            // 重写read(byte[], int, int)方法以提高读取效率，支持一次读取多个字节
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (remaining <= 0) {
                    return -1;
                }
                int toRead = (int) Math.min(len, remaining);
                int bytesRead = raf.read(b, off, toRead);
                if (bytesRead > 0) {
                    remaining -= bytesRead;
                }
                return bytesRead;
            }
            // 其他InputStream方法可以根据需要重写，例如available()、close()等，确保正确处理流的状态和资源释放
            @Override
            public void close() throws IOException {
                raf.close();
            }
        };
        // 返回206 Partial Content响应，包含Content-Range和Accept-Ranges头部信息，指示支持断点续传
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

    // 根据组件ID解析安装包的文件路径，确保路径安全，防止路径遍历攻击
    private Path resolveInstallPackagePath(Path basePath, Integer componentId) {
        String relativePath = String.valueOf(componentId);
        Path resolvedPath = basePath.resolve(relativePath).normalize();

        if (!resolvedPath.startsWith(basePath)) {
            log.error("非法路径尝试: {} -> {}", relativePath, resolvedPath);
            return null;
        }

        return resolvedPath.resolve(INSTALL_FILE_NAME).normalize();
    }

    // 获取资源文件的根路径，确保配置项存在且合法
    private Path getResourceBasePath() {
        String sourceRoot = componentDownloadProperties.getSourceRoot();
        if (sourceRoot == null || sourceRoot.isBlank()) {
            throw new IllegalStateException("component.download.source-root 未配置");
        }
        return Paths.get(sourceRoot).normalize().toAbsolutePath();
    }

    // 存储单组件下载的临时Token，关联用户ID和组件ID，并设置过期时间，使用Redis作为存储介质
    private void storeSingleDownloadToken(String token, Integer userId, Integer componentId) throws IOException {
        SingleDownloadTokenPayload payload = new SingleDownloadTokenPayload(userId, componentId);
        int tokenExpireMinutes = componentDownloadProperties.getTokenExpireMinutes() == null
                || componentDownloadProperties.getTokenExpireMinutes() <= 0
                ? 10
                : componentDownloadProperties.getTokenExpireMinutes();

        stringRedisTemplate.opsForValue().set(
                RedisConstants.COMPONENT_SINGLE_DOWNLOAD_TOKEN_PREFIX + token,
                objectMapper.writeValueAsString(payload),
                tokenExpireMinutes,
                TimeUnit.MINUTES
        );
    }

    // 加载单组件下载的临时Token，验证Token的有效性并解析出关联的用户ID和组件ID，供下载接口使用
    private SingleDownloadTokenPayload loadSingleDownloadToken(String token) throws IOException {
        String payloadJson = stringRedisTemplate.opsForValue()
                .get(RedisConstants.COMPONENT_SINGLE_DOWNLOAD_TOKEN_PREFIX + token);
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        return objectMapper.readValue(payloadJson, SingleDownloadTokenPayload.class);
    }

    // 构建组件安装包的绝对下载URL，供前端使用生成的临时Token调用下载接口进行安装
    private String buildAbsoluteDownloadUrl(String path) {
        String publicBaseUrl = componentDownloadProperties.getPublicBaseUrl();
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            throw new IllegalStateException("component.download.public-base-url 未配置");
        }

        String normalizedBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBaseUrl + normalizedPath;
    }

    // 构建组件安装包的文件名，优先使用组件名称，如果组件名称不可用则使用默认格式，确保文件名合法且具有正确的扩展名
    private String buildInstallPackageName(Components component, Integer componentId) {
        String componentName = component.getName();
        if (componentName == null || componentName.isBlank()) {
            componentName = "component-" + componentId;
        }

        return sanitizeFileName(componentName.trim()) + "安装包.exe";
    }

    // 对文件名进行清理，替换掉可能导致文件系统问题的特殊字符，确保生成的文件名在各种操作系统上都能正常使用
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    // 生成唯一的ZIP条目名称，避免在批量下载时不同组件的安装包文件名冲突，确保每个组件的安装包在ZIP文件中都有一个唯一的条目名称
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

    public ApiResult<?> getModuleList() {

        log.info("正在获取组件列表");

        List<Components> result = baseMapper.selectList(null);

        return ApiResult.success(result);
    }

    // 单组件下载临时Token的载荷类，包含用户ID和组件ID，用于存储在Redis中并在下载时验证Token的有效性和权限
    private static class SingleDownloadTokenPayload {
        private Integer userId;
        private Integer componentId;

        public SingleDownloadTokenPayload() {
        }

        public SingleDownloadTokenPayload(Integer userId, Integer componentId) {
            this.userId = userId;
            this.componentId = componentId;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public Integer getComponentId() {
            return componentId;
        }

        public void setComponentId(Integer componentId) {
            this.componentId = componentId;
        }
    }
}
