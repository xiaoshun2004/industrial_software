package com.scut.industrial_software.controller;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
public class UpLoadController {
    //注入依赖：AliOSSUtiles工具类
    @Autowired
    private AliOSSUtils aliOSSUtils;

    @PostMapping("/upload")
    public ApiResult<String> uplaod(MultipartFile zip) throws IOException {
        log.info("上传过来的参数：{}", zip.getOriginalFilename());

        //调用阿里云OSS工具类【AliOSSUtiles】进行文件上传
        String url = aliOSSUtils.upload(zip);
        log.info("文件上传完成，url是：{}", url);

        return ApiResult.success(url);
    }
}

