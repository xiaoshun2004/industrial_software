package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiErrorCode;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.common.constant.Constant;
import com.scut.industrial_software.common.exception.ApiException;
import com.scut.industrial_software.mapper.FileMetaMapper;
import com.scut.industrial_software.model.dto.FileQueryDTO;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.model.entity.FileMeta;
import com.scut.industrial_software.model.vo.FileMetaVO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.service.IFileMetaService;
import com.scut.industrial_software.utils.TransFileSizeUtil;
import com.scut.industrial_software.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * 文件元数据表 服务实现类
 * </p>
 */
@Slf4j
@Service
public class FileMetaServiceImpl extends ServiceImpl<FileMetaMapper, FileMeta> implements IFileMetaService {

    @Value("${files.upload.path}")
    private String uploadPath;

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 上传文件（使用）
    public FileMetaVO uploadFile(String dbType, String fileName,MultipartFile file) {
        // 如果文件为空的情况
        if (file == null || file.isEmpty()) {
            throw new ApiException("文件不能为空");
        }
        // dbType参数不为指定的值
        if(Constant.dbTypes.stream().noneMatch(dbType::equals)){
            throw new ApiException(ApiErrorCode.INVALID_DATABASE_TYPE);
        }
        // 如果文件的类型不支持，返回错误

        try {
            // 确保上传目录存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 获取当前登录用户
            UserDTO currentUser = UserHolder.getUser();
            Long userId = currentUser != null ? currentUser.getId() : null;
            String username = currentUser != null ? currentUser.getName() : "unknown";

            // 生成文件UUID和保存路径
            String originalFilename = file.getOriginalFilename();
            String fileExtension = StringUtils.getFilenameExtension(originalFilename);
            String fileUuid = UUID.randomUUID().toString().replace("-", "");
            String storedFilename = fileUuid + (fileExtension != null ? "." + fileExtension : "");
            String filePath = uploadPath + File.separator + storedFilename;

            FileMeta fileMeta;

            // 创建新文件记录
            fileMeta = new FileMeta();
            fileMeta.setFileUuid(fileUuid)
                    .setFileName(fileName + (fileExtension != null ? "." + fileExtension : ""))
                    .setFilePath(filePath)
                    .setFileSize(file.getSize())
                    .setFileType(file.getContentType())
                    .setCreatorId(userId)
                    .setCreatorName(username)
                    .setStorageLocation("LOCAL_DISK")
                    .setCreateTime(LocalDateTime.now())
                    .setUpdateTime(LocalDateTime.now())
                    .setDbType(dbType);

            // 保存文件到磁盘
            file.transferTo(new File(filePath));

            // 保存或更新数据库记录
            saveOrUpdate(fileMeta);

            // 转换为VO并返回
            FileMetaVO fileMetaVO = new FileMetaVO();
            fileMetaVO.setId(fileMeta.getFileUuid());
            fileMetaVO.setFileName(fileMeta.getFileName());
            fileMetaVO.setFileSize(TransFileSizeUtil.transFileSize(fileMeta.getFileSize()));
            fileMetaVO.setUpdateTime(fileMeta.getUpdateTime());
            return fileMetaVO;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new ApiException(ApiErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    //下载文件（使用）
    public byte[] downloadFile(String field) {
        FileMeta fileMeta = baseMapper.selectOne(new LambdaQueryWrapper<FileMeta>().eq(FileMeta::getFileUuid, field));
        if (fileMeta == null) {
            throw new ApiException(ApiErrorCode.RESOURCE_NOT_FOUND);
        }

        try {
            Path path = Paths.get(fileMeta.getFilePath());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("文件下载失败", e);
            throw new ApiException(ApiErrorCode.FILE_DOWNLOAD_FAILED);
        }
    }

    @Override
    public FileMetaVO getFileInfo(String field) {
        FileMeta fileMeta = baseMapper.selectOne(new LambdaQueryWrapper<FileMeta>().eq(FileMeta::getFileUuid, field));
        if (fileMeta == null) {
            throw new ApiException(ApiErrorCode.RESOURCE_NOT_FOUND);
        }
        FileMetaVO fileMetaVO = new FileMetaVO();
        fileMetaVO.setFileName(fileMeta.getFileName());
        fileMetaVO.setId(fileMeta.getFileUuid());
        fileMetaVO.setFileSize(TransFileSizeUtil.transFileSize(fileMeta.getFileSize()));
        fileMetaVO.setUpdateTime(fileMeta.getUpdateTime());
        return fileMetaVO;
    }

    @Override
    // 获取当前用户的文件列表（使用）
    public PageVO<FileMetaVO> getMyFiles(FileQueryDTO queryDTO) {
        // 获取当前登录用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED);
        }

        // 检查dbType参数是否有效
        if(Constant.dbTypes.stream().noneMatch(queryDTO.getDbType()::equals)){
            throw new ApiException(ApiErrorCode.INVALID_DATABASE_TYPE);
        }

        // 构建分页参数
        Page<FileMeta> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 查询当前用户的文件
        IPage<FileMeta> filePage = baseMapper.selectPageByCreatorIdAndDbType(page, currentUser.getId(), queryDTO.getDbType());

        // 如果没有查询到文件，返回文件不存在
        if (filePage.getRecords().isEmpty()) {
            throw new ApiException(ApiErrorCode.RESOURCE_NOT_FOUND);
        }

        // 转换为VO
        List<FileMetaVO> records = filePage.getRecords().stream()
                .map(fileMeta -> {
                    FileMetaVO vo = new FileMetaVO();
                    vo.setId(fileMeta.getFileUuid());
                    vo.setFileName(fileMeta.getFileName());
                    vo.setFileSize(TransFileSizeUtil.transFileSize(fileMeta.getFileSize()));
                    vo.setUpdateTime(fileMeta.getUpdateTime());
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 构建分页结果
        return PageVO.build(records, filePage.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /*
    @Override
    public PageVO<FileMetaVO> searchFiles(FileQueryDTO queryDTO) {
        // 构建分页参数
        Page<FileMeta> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 根据关键字查询文件
        IPage<FileMeta> filePage = baseMapper.selectPageByKeyword(page, queryDTO.getKeyword());
        
        // 转换为VO
        List<FileMetaVO> records = filePage.getRecords().stream()
                .map(fileMeta -> {
                    FileMetaVO vo = new FileMetaVO();
                    BeanUtils.copyProperties(fileMeta, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 构建分页结果
        return PageVO.build(records, filePage.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(Long id) {
        FileMeta fileMeta = getById(id);
        if (fileMeta == null) {
            throw new ApiException("文件不存在");
        }

        // 获取当前登录用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser != null && !currentUser.getId().equals(fileMeta.getCreatorId())) {
            throw new ApiException("您没有权限删除此文件");
        }

        // 删除物理文件
        File file = new File(fileMeta.getFilePath());
        if (file.exists()) {
            file.delete();
        }

        // 删除数据库记录
        return removeById(id);
    }
    */
} 