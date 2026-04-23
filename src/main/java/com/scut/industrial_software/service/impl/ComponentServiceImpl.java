package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.config.ComponentDownloadProperties;
import com.scut.industrial_software.mapper.ComponentsMapper;
import com.scut.industrial_software.model.constant.RedisConstants;
import com.scut.industrial_software.model.dto.ComponentBatchDownloadRequestDTO;
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
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class ComponentServiceImpl extends ServiceImpl<ComponentsMapper, Components> implements IComponentService {

    private static final String INSTALL_FILE_NAME = "install.exe";
    private static final String BATCH_ZIP_FILE_NAME = "components-install-packages.zip";
    private static final String SINGLE_DOWNLOAD_PATH_TEMPLATE = "/components/install/%d/download?token=%s";
    private static final String BATCH_STREAM_PATH_TEMPLATE = "/components/install/batch/stream?token=%s";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComponentDownloadProperties componentDownloadProperties;

    @Override
    public ApiResult<?> createSingleDownloadToken(Integer componentId) {
        try {
            UserDTO currentUser = requireCurrentUser();
            resolveSinglePackage(componentId);

            String token = generateToken();
            storeJsonToken(
                    RedisConstants.COMPONENT_SINGLE_DOWNLOAD_TOKEN_PREFIX,
                    token,
                    new SingleDownloadTokenPayload(currentUser.getId(), componentId)
            );

            Map<String, String> result = new HashMap<>();
            result.put("downloadUrl",
                    buildAbsoluteDownloadUrl(String.format(SINGLE_DOWNLOAD_PATH_TEMPLATE, componentId, token)));

            log.info("生成单组件下载临时Token成功，userId: {}, componentId: {}", currentUser.getId(), componentId);
            return ApiResult.success(result);
        } catch (ComponentDownloadException e) {
            if (e.getStatus() == HttpStatus.NOT_FOUND) {
                return ApiResult.resourceNotFound(e.getMessage());
            }
            if (e.getStatus() == HttpStatus.UNAUTHORIZED) {
                return ApiResult.unauthorized(e.getMessage());
            }
            return ApiResult.failed(e.getMessage());
        } catch (Exception e) {
            log.error("生成单组件下载临时Token失败，componentId: {}", componentId, e);
            return ApiResult.internalServerError("生成组件下载链接失败");
        }
    }

    @Override
    public ResponseEntity<Resource> downloadModuleByToken(Integer componentId, String token, String rangeHeader) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SingleDownloadTokenPayload payload = readReusableToken(
                    RedisConstants.COMPONENT_SINGLE_DOWNLOAD_TOKEN_PREFIX,
                    token,
                    SingleDownloadTokenPayload.class
            );
            if (payload == null || !componentId.equals(payload.componentId())) {
                log.warn("单组件下载临时Token无效、已过期或与组件不匹配，componentId: {}", componentId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return buildSingleDownloadResponse(resolveSinglePackage(componentId), rangeHeader);
        } catch (ComponentDownloadException e) {
            return ResponseEntity.status(e.getStatus()).build();
        } catch (Exception e) {
            log.error("通过临时Token下载单组件失败，componentId: {}", componentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ApiResult<?> createBatchStreamToken(ComponentBatchDownloadRequestDTO requestDTO) {
        try {
            UserDTO currentUser = requireCurrentUser();
            List<ComponentPackage> packages = resolveBatchPackages(requestDTO == null ? null : requestDTO.getComponentIds());

            String token = generateToken();
            storeJsonToken(
                    RedisConstants.COMPONENT_BATCH_STREAM_TOKEN_PREFIX,
                    token,
                    new BatchStreamTokenPayload(currentUser.getId(), toComponentIds(packages))
            );

            Map<String, String> result = new HashMap<>();
            result.put("downloadUrl",
                    buildAbsoluteDownloadUrl(String.format(BATCH_STREAM_PATH_TEMPLATE, token)));

            log.info("生成批量下载流式临时Token成功，userId: {}, componentIds: {}", currentUser.getId(), toComponentIds(packages));
            return ApiResult.success(result);
        } catch (ComponentDownloadException e) {
            if (e.getStatus() == HttpStatus.NOT_FOUND) {
                return ApiResult.resourceNotFound(e.getMessage());
            }
            if (e.getStatus() == HttpStatus.UNAUTHORIZED) {
                return ApiResult.unauthorized(e.getMessage());
            }
            return ApiResult.failed(e.getMessage());
        } catch (Exception e) {
            log.error("生成批量下载流式临时Token失败", e);
            return ApiResult.internalServerError("生成批量下载链接失败");
        }
    }

    @Override
    public ResponseEntity<StreamingResponseBody> downloadModulesByToken(String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            BatchStreamTokenPayload payload = consumeOneTimeToken(
                    RedisConstants.COMPONENT_BATCH_STREAM_TOKEN_PREFIX,
                    token,
                    BatchStreamTokenPayload.class
            );
            if (payload == null || payload.componentIds() == null || payload.componentIds().isEmpty()) {
                log.warn("批量下载流式临时Token无效或已过期");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return buildBatchDownloadResponse(resolveBatchPackages(payload.componentIds()));
        } catch (ComponentDownloadException e) {
            return ResponseEntity.status(e.getStatus()).build();
        } catch (Exception e) {
            log.error("通过临时Token批量下载组件失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ApiResult<?> getModuleList() {
        log.info("正在获取组件列表");
        return ApiResult.success(baseMapper.selectList(null));
    }

    private UserDTO requireCurrentUser() {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new ComponentDownloadException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return currentUser;
    }

    private ResponseEntity<Resource> buildSingleDownloadResponse(ComponentPackage componentPackage, String rangeHeader)
            throws IOException {
        File file = componentPackage.filePath().toFile();
        String encodedFileName = encodeFileName(componentPackage.entryName());
        long fileSize = file.length();

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            return handleRangeRequest(componentPackage.filePath(), fileSize, encodedFileName, rangeHeader);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileSize)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(new FileSystemResource(file));
    }

    private ResponseEntity<StreamingResponseBody> buildBatchDownloadResponse(List<ComponentPackage> packages) {
        StreamingResponseBody body = outputStream -> {
            byte[] buffer = new byte[8192];
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
                zipOutputStream.setLevel(Deflater.NO_COMPRESSION);
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

        String encodedFileName = encodeFileName(BATCH_ZIP_FILE_NAME);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .body(body);
    }

    private ComponentPackage resolveSinglePackage(Integer componentId) {
        validateComponentId(componentId);

        Components component = baseMapper.selectById(componentId);
        if (component == null) {
            throw new ComponentDownloadException(HttpStatus.NOT_FOUND, "组件不存在");
        }

        Path filePath = resolveInstallPackagePath(getResourceBasePath(), componentId);
        if (filePath == null) {
            throw new ComponentDownloadException(HttpStatus.BAD_REQUEST, "组件安装包路径非法");
        }
        if (!Files.isRegularFile(filePath)) {
            throw new ComponentDownloadException(HttpStatus.NOT_FOUND, "组件安装包不存在");
        }

        return new ComponentPackage(filePath, buildInstallPackageName(component, componentId), componentId);
    }

    private List<ComponentPackage> resolveBatchPackages(List<Integer> componentIds) {
        List<Integer> normalizedIds = normalizeComponentIds(componentIds);
        List<ComponentPackage> packages = new ArrayList<>();
        Set<String> usedEntryNames = new HashSet<>();

        for (Integer componentId : normalizedIds) {
            ComponentPackage basePackage = resolveSinglePackage(componentId);
            String uniqueEntryName = uniqueZipEntryName(basePackage.entryName(), componentId, usedEntryNames);
            packages.add(new ComponentPackage(basePackage.filePath(), uniqueEntryName, componentId));
        }

        return packages;
    }

    private List<Integer> normalizeComponentIds(List<Integer> componentIds) {
        if (componentIds == null || componentIds.isEmpty()) {
            throw new ComponentDownloadException(HttpStatus.BAD_REQUEST, "组件ID列表不能为空");
        }

        List<Integer> normalizedIds = new ArrayList<>();
        for (Integer componentId : new LinkedHashSet<>(componentIds)) {
            validateComponentId(componentId);
            normalizedIds.add(componentId);
        }
        return normalizedIds;
    }

    private void validateComponentId(Integer componentId) {
        if (componentId == null || componentId <= 0) {
            throw new ComponentDownloadException(HttpStatus.BAD_REQUEST, "组件ID无效");
        }
    }

    private List<Integer> toComponentIds(List<ComponentPackage> packages) {
        return packages.stream().map(ComponentPackage::componentId).toList();
    }

    private void storeJsonToken(String keyPrefix, String token, Object payload) throws IOException {
        stringRedisTemplate.opsForValue().set(
                keyPrefix + token,
                objectMapper.writeValueAsString(payload),
                getTokenExpireMinutes(),
                TimeUnit.MINUTES
        );
    }

    private <T> T readReusableToken(String keyPrefix, String token, Class<T> payloadType) throws IOException {
        String payloadJson = stringRedisTemplate.opsForValue().get(keyPrefix + token);
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        return objectMapper.readValue(payloadJson, payloadType);
    }

    private <T> T consumeOneTimeToken(String keyPrefix, String token, Class<T> payloadType) throws IOException {
        String payloadJson = stringRedisTemplate.opsForValue().getAndDelete(keyPrefix + token);
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        return objectMapper.readValue(payloadJson, payloadType);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private int getTokenExpireMinutes() {
        Integer tokenExpireMinutes = componentDownloadProperties.getTokenExpireMinutes();
        return tokenExpireMinutes == null || tokenExpireMinutes <= 0 ? 10 : tokenExpireMinutes;
    }

    private Path getResourceBasePath() {
        String sourceRoot = componentDownloadProperties.getSourceRoot();
        if (sourceRoot == null || sourceRoot.isBlank()) {
            throw new IllegalStateException("component.download.source-root 未配置");
        }
        return Paths.get(sourceRoot).normalize().toAbsolutePath();
    }

    private Path resolveInstallPackagePath(Path basePath, Integer componentId) {
        Path resolvedPath = basePath.resolve(String.valueOf(componentId)).normalize();
        if (!resolvedPath.startsWith(basePath)) {
            log.error("非法路径尝试: {} -> {}", componentId, resolvedPath);
            return null;
        }
        return resolvedPath.resolve(INSTALL_FILE_NAME).normalize();
    }

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

    private String encodeFileName(String fileName) {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private ResponseEntity<Resource> handleRangeRequest(Path filePath, long fileSize,
                                                        String encodedFileName, String rangeHeader) throws IOException {
        String range = rangeHeader.replace("bytes=", "");
        String[] ranges = range.split("-");

        long start = Long.parseLong(ranges[0].trim());
        long end = (ranges.length > 1 && !ranges[1].trim().isEmpty())
                ? Long.parseLong(ranges[1].trim())
                : fileSize - 1;

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

            @Override
            public int read() throws IOException {
                if (remaining <= 0) {
                    return -1;
                }
                remaining--;
                return raf.read();
            }

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

            @Override
            public void close() throws IOException {
                raf.close();
            }
        };

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

    private record ComponentPackage(Path filePath, String entryName, Integer componentId) {
    }

    private record SingleDownloadTokenPayload(Integer userId, Integer componentId) {
    }

    private record BatchStreamTokenPayload(Integer userId, List<Integer> componentIds) {
    }

    private static class ComponentDownloadException extends RuntimeException {
        private final HttpStatus status;

        private ComponentDownloadException(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }
}
