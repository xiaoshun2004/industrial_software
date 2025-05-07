package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scut.industrial_software.mapper.SubprogramFileMapper;
import com.scut.industrial_software.model.entity.SubprogramFile;
import com.scut.industrial_software.service.ISubprogramFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class SubprogramFileServiceImpl extends ServiceImpl<SubprogramFileMapper, SubprogramFile> implements ISubprogramFileService {

    @Value("${files.upload.path}")
    private String fileUploadPath;

    // 注入 Mapper
    @Autowired
    private SubprogramFileMapper subprogramFileMapper;

    // 子程序文件上传逻辑
    @Override
    public ApiResult<String> uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return ApiResult.failed("文件不能为空");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        String subprogramPath = fileUploadPath + "/subprograms/" + uuid + fileExtension;

        File dest = new File(subprogramPath);
        dest.getParentFile().mkdirs(); // 确保子目录存在
        file.transferTo(dest);

        // 获取当前时间并转换为 DATETIME 格式
        String uploadTime = DateUtils.getCurrentDatetime();

        // 存储到数据库
        SubprogramFile subprogramFile = SubprogramFile.builder()
                .fileName(originalFilename)
                .filePath(subprogramPath)
                .uploadTime(uploadTime)  // 设置当前时间戳
                .build();

        this.save(subprogramFile);  // MyBatis-Plus 会自动调用 insert 操作

        return ApiResult.success("文件上传成功");
    }

    // 子程序文件下载逻辑
    @Override
    public String downloadFile(String fileName) {
        // 从数据库查询文件路径
        SubprogramFile subprogramFile = subprogramFileMapper.selectOne(new QueryWrapper<SubprogramFile>().eq("file_name", fileName));

        // 如果文件记录不存在，返回空或错误消息
        if (subprogramFile == null) {
            return null;  // 或者抛出异常
        }

        // 返回存储在数据库中的文件绝对路径
        return subprogramFile.getFilePath();
    }
}
