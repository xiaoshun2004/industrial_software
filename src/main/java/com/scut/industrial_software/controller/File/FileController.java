package com.scut.industrial_software.controller.File;

import org.springframework.http.HttpHeaders;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.service.ISimulationFileService;
import com.scut.industrial_software.service.ISubprogramFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author hj
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    private ISubprogramFileService subprogramFileService;

    @Autowired
    private ISimulationFileService simulationFileService;

    // 上传子程序文件
    @PostMapping("/upload/subprogram")
    public ApiResult<String> uploadSubprogramFile(@RequestParam("file") MultipartFile file) throws Exception {
        return subprogramFileService.uploadFile(file);
    }

    // 上传仿真输入文件
    @PostMapping("/upload/simulation")
    public ApiResult<String> uploadSimulationFile(@RequestParam("file") MultipartFile file) throws Exception {
        return simulationFileService.uploadFile(file);
    }

    // 下载子程序文件
    @GetMapping("/download/subprogram/{fileName}")
    public ResponseEntity<FileSystemResource> downloadSubprogramFile(@PathVariable String fileName)  {
        // 获取文件的存储路径
        String filePath = subprogramFileService.downloadFile(fileName);

        // 如果没有查询到文件路径，返回 404
        if (filePath == null) {
            return ResponseEntity.notFound().build();
        }

        // 创建文件对象
        File file = new File(filePath);

        // 如果文件不存在，返回 404
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 创建 FileSystemResource 以便传输文件
        FileSystemResource resource = new FileSystemResource(file);

        // 设置文件的下载响应头
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)  // 设置内容类型为二进制流
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())  // 设置下载文件名
                .body(resource);  // 返回文件资源
    }

    // 下载仿真输入文件
    @GetMapping("/download/simulation/{fileName}")
    public ResponseEntity<FileSystemResource> downloadSimulationFile(@PathVariable String fileName)  {
        // 获取文件路径
        String filePath = simulationFileService.downloadFile(fileName);

        // 如果没有查询到文件路径，返回 404
        if (filePath == null) {
            return ResponseEntity.notFound().build();
        }

        // 创建文件对象
        File file = new File(filePath);

        // 如果文件不存在，返回 404
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 创建 FileSystemResource 以便传输文件
        FileSystemResource resource = new FileSystemResource(file);

        // 设置文件的下载响应头
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)  // 设置内容类型为二进制流
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())  // 设置下载文件名
                .body(resource);  // 返回文件资源
    }
}


