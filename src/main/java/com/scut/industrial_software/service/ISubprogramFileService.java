package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.entity.SubprogramFile;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * <p>
 * 子程序文件 服务类接口
 * </p>
 *
 * @since 2025-03-29
 */
public interface ISubprogramFileService extends IService<SubprogramFile> {

    // 上传文件方法
    ApiResult<String> uploadFile(MultipartFile file) throws Exception;

    // 下载文件方法
    String downloadFile(String fileName);
}
