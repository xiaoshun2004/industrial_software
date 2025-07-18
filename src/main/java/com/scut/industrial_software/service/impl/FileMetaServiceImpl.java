package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.exception.ApiException;
import com.scut.industrial_software.mapper.FileMetaMapper;
import com.scut.industrial_software.model.dto.FileQueryDTO;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.model.entity.FileMeta;
import com.scut.industrial_software.model.vo.FileMetaVO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.service.IFileMetaService;
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
    public FileMetaVO uploadOrSaveFile(MultipartFile file, Long id) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("文件不能为空");
        }

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

            if (id != null) {
                // 更新已有文件
                fileMeta = getById(id);
                if (fileMeta == null) {
                    throw new ApiException("文件不存在");
                }

                // 检查权限
                if (userId != null && !userId.equals(fileMeta.getCreatorId())) {
                    throw new ApiException("您没有权限修改此文件");
                }

                // 删除旧文件
                File oldFile = new File(fileMeta.getFilePath());
                if (oldFile.exists()) {
                    oldFile.delete();
                }

                // 更新文件元数据
                fileMeta.setFileName(originalFilename)
                        .setFilePath(filePath)
                        .setFileSize(file.getSize())
                        .setFileType(file.getContentType());
            } else {
                // 创建新文件记录
                fileMeta = new FileMeta();
                fileMeta.setFileUuid(fileUuid)
                        .setFileName(originalFilename)
                        .setFilePath(filePath)
                        .setFileSize(file.getSize())
                        .setFileType(file.getContentType())
                        .setCreatorId(userId)
                        .setCreatorName(username)
                        .setStorageLocation("LOCAL_DISK");
            }

            // 保存文件到磁盘
            file.transferTo(new File(filePath));

            // 保存或更新数据库记录
            saveOrUpdate(fileMeta);

            // 转换为VO并返回
            FileMetaVO fileMetaVO = new FileMetaVO();
            BeanUtils.copyProperties(fileMeta, fileMetaVO);
            return fileMetaVO;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new ApiException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(Long id) {
        FileMeta fileMeta = getById(id);
        if (fileMeta == null) {
            throw new ApiException("文件不存在");
        }

        try {
            Path path = Paths.get(fileMeta.getFilePath());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("文件下载失败", e);
            throw new ApiException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public FileMetaVO getFileInfo(Long id) {
        FileMeta fileMeta = getById(id);
        if (fileMeta == null) {
            throw new ApiException("文件不存在");
        }

        FileMetaVO fileMetaVO = new FileMetaVO();
        BeanUtils.copyProperties(fileMeta, fileMetaVO);
        return fileMetaVO;
    }

    @Override
    public PageVO<FileMetaVO> getMyFiles(FileQueryDTO queryDTO) {
        // 获取当前登录用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            throw new ApiException("用户未登录");
        }

        // 构建分页参数
        Page<FileMeta> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 查询当前用户的文件
        IPage<FileMeta> filePage = baseMapper.selectPageByCreatorId(page, currentUser.getId());
        
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
} 