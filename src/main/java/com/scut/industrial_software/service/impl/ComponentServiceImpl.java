package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.config.ComponentStoreProperties;
import com.scut.industrial_software.mapper.ComponentsMapper;
import com.scut.industrial_software.model.entity.Components;
import com.scut.industrial_software.service.IComponentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
public class ComponentServiceImpl extends ServiceImpl<ComponentsMapper, Components> implements IComponentService {

    // private String resourcePath = "D:/resource/";

    @Autowired
    private ComponentStoreProperties componentStoreProperties;

    @Override
    public ResponseEntity<Resource> downloadModule(String dynamicsDirection, String moduleType, String resourceType) {
        log.info("下载模块: 动力学方向 {}, 模块类型 {}, 资源类型 {}", dynamicsDirection, moduleType, resourceType);

        try {
            String fileName = dynamicsDirection + "_" + moduleType;

            // 相对路径
            // String relativePath = dynamicsDirection + "/" + moduleType + "/";
            // 如果动力学方向为冲击，并且模块类型为求解器，则添加资源类型区别
            if(dynamicsDirection.equals("impact") && moduleType.equals("solver")){
                // relativePath += resourceType + "/";
                fileName += "_" + resourceType;
            }

            // 安全路径解析 ———— 防止路径遍历攻击
            /*Path basePath = Paths.get(resourcePath).normalize().toAbsolutePath();
            Path resolvedPath = basePath.resolve(relativePath).normalize();

            if(!resolvedPath.startsWith(basePath)){
                log.info("非法路径尝试:{} -> {}", relativePath,resolvedPath);
                return ResponseEntity.badRequest().build();
            }

            // 完整路径 以及 文件名
            Path filePath = resolvedPath.resolve("main.exe").normalize();
            String fileNameWithExtension = fileName + ".exe";

            if(!filePath.toFile().exists()){
                log.info("文件不存在:{}", filePath);
                return ResponseEntity.notFound().build();
            }*/

            // 直接使用映射关系获取安装包路径
            String installDir = componentStoreProperties.getPath(fileName);
            if (installDir == null || installDir.isBlank()) {
                log.info("未知的模块请求或未配置路径: {}", fileName);
                return ResponseEntity.notFound().build();
            }

            // 安全路径解析，防止路径遍历并校验文件存在
            // Path basePath = Paths.get(installDir).normalize().toAbsolutePath();
            Path filePath = Paths.get(installDir).normalize().toAbsolutePath();

            /*if (!filePath.startsWith(basePath)) {
                log.warn("检测到潜在的路径遍历攻击: {}", filePath);
                return ResponseEntity.badRequest().build();
            }*/
            if (!filePath.toFile().isFile()) {
                log.info("文件不存在或不是普通文件: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);

            // 设置响应头（支持中文文件名）
            HttpHeaders headers = new HttpHeaders();
            String encodedFileName = URLEncoder.encode(installDir, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");

            log.info("正在下载前处理/求解器模块:{}", filePath);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(filePath.toFile().length())
                    .body(resource);
        } catch (Exception e) {
            log.info("下载模块失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<Resource> downloadPostprocessingModule() {
        log.info("下载后通用处理模块");
        try {
            String fileName = "postProcessing";

            /*
            Path resolvedPath = Paths.get(resourcePath).normalize().toAbsolutePath().resolve("后处理/").normalize();
            Path filePath = resolvedPath.resolve("main.exe");*/

            String postInstallPath = componentStoreProperties.getPath(fileName);

            if(postInstallPath == null || postInstallPath.isBlank()){
                log.info("未知的后处理模块请求或未配置路径: {}", fileName);
                return ResponseEntity.notFound().build();
            }
            Path filePath = Paths.get(postInstallPath).normalize().toAbsolutePath();

            if(!filePath.toFile().exists()){
                log.info("文件不存在:{}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);

            // 创建响应头
            HttpHeaders headers = new HttpHeaders();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");

            log.info("正在下载通用后处理模块:{}", filePath);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(filePath.toFile().length())
                    .body(resource);
        } catch (Exception e) {
            log.info("下载模块失败",e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public ApiResult<?> getModuleList(){
        log.info("正在获取模块列表");

        List<Components> result = baseMapper.selectList(null);

        return ApiResult.success(result);
    }
}
