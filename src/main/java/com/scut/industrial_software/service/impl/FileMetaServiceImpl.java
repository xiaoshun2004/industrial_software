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
import com.scut.industrial_software.model.constant.RedisConstants;
import com.scut.industrial_software.model.dto.FileQueryDTO;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.model.entity.FileMeta;
import com.scut.industrial_software.model.vo.FileMetaVO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.service.IFileMetaService;
import com.scut.industrial_software.utils.TransFileSizeUtil;
import com.scut.industrial_software.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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

    private static final long MAX_PREVIEW_IMAGE_SIZE = 10 * 1024 * 1024L;
    private static final Set<String> SUPPORTED_PREVIEW_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Value("${files.upload.path}")
    private String uploadPath;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 上传文件（使用）
    public FileMetaVO uploadFile(String dbType, String fileName, MultipartFile file, MultipartFile previewImage) {
        // 如果文件为空的情况
        if (file == null || file.isEmpty()) {
            throw new ApiException("文件不能为空");
        }
        // dbType参数不为指定的值
        if(Constant.dbTypes.stream().noneMatch(dbType::equals)){
            throw new ApiException(ApiErrorCode.INVALID_DATABASE_TYPE);
        }
        // 如果文件的类型不支持，返回错误

        validatePreviewImage(previewImage);

        String filePath = null;
        String previewImagePath = null;

        try {
            // 确保上传目录存在
            ensureUploadDirectoryExists();

            // 获取当前登录用户
            UserDTO currentUser = UserHolder.getUser();
            Integer userId = currentUser != null ? currentUser.getId() : null;
            String username = currentUser != null ? currentUser.getName() : "unknown";

            // 生成文件UUID和保存路径
            String originalFilename = file.getOriginalFilename();
            String fileExtension = StringUtils.getFilenameExtension(originalFilename);
            String fileUuid = UUID.randomUUID().toString().replace("-", "");
            String storedFilename = fileUuid + (fileExtension != null ? "." + fileExtension : "");
            filePath = uploadPath + File.separator + storedFilename;

            // 创建新文件记录
            FileMeta fileMeta = new FileMeta();
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

            if (hasPreviewImage(previewImage)) {
                String previewImageId = UUID.randomUUID().toString().replace("-", "");
                String previewExtension = StringUtils.getFilenameExtension(previewImage.getOriginalFilename());
                String previewStoredFilename = previewImageId + (previewExtension != null ? "." + previewExtension : "");
                previewImagePath = uploadPath + File.separator + previewStoredFilename;

                previewImage.transferTo(new File(previewImagePath));
                fileMeta.setPreviewImageId(previewImageId)
                        .setPreviewImagePath(previewImagePath)
                        .setPreviewImageType(previewImage.getContentType())
                        .setPreviewImageSize(previewImage.getSize());
            }

            // 保存或更新数据库记录
            saveOrUpdate(fileMeta);

            // 转换为VO并返回
            return buildFileMetaVO(fileMeta);

        } catch (Exception e) {
            log.error("文件上传失败", e);
            deleteIfExists(filePath);
            deleteIfExists(previewImagePath);
            if (e instanceof ApiException apiException) {
                throw apiException;
            }
            throw new ApiException(ApiErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileMetaVO uploadFileStream(String dbType, String fileName, MultipartFile file) {
        // 如果文件为空的情况
        if (file == null || file.getSize() == 0) {
            throw new ApiException("文件不能为空");
        }
        log.info("1");
        // dbType参数不为指定的值
        if (Constant.dbTypes.stream().noneMatch(dbType::equals)) {
            throw new ApiException(ApiErrorCode.INVALID_DATABASE_TYPE);
        }

        // 如果文件的类型不支持，返回错误
        // 这里可以添加文件类型验证逻辑

        try {
            // 确保上传目录存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }


            // 获取当前登录用户
            UserDTO currentUser = UserHolder.getUser();
            Integer userId = currentUser != null ? currentUser.getId() : null;
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

            // 流式保存文件到磁盘
            InputStream inputStream = file.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(filePath);

                byte[] buffer = new byte[8192]; // 8KB缓冲区
                int bytesRead;
                long totalBytesRead = 0;

            // 流式读取和写入
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

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
        return buildFileMetaVO(getFileMetaByUuid(field));
    }

    @Override
    public String getPreviewContentType(String field) {
        FileMeta fileMeta = getFileMetaByUuid(field);
        if (!StringUtils.hasText(fileMeta.getPreviewImagePath())) {
            throw new ApiException("预览图未找到");
        }
        return fileMeta.getPreviewImageType();
    }

    @Override
    public byte[] downloadPreview(String field) {
        FileMeta fileMeta = getFileMetaByUuid(field);
        if (!StringUtils.hasText(fileMeta.getPreviewImagePath())) {
            throw new ApiException("预览图未找到");
        }

        try {
            Path path = Paths.get(fileMeta.getPreviewImagePath());
            if (!Files.exists(path)) {
                throw new ApiException("预览图未找到");
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("预览图下载失败", e);
            throw new ApiException(ApiErrorCode.FILE_DOWNLOAD_FAILED);
        }
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
        String keyword = StringUtils.hasText(queryDTO.getKeyword()) ? queryDTO.getKeyword().trim() : null;

        // 查询当前用户的文件
        IPage<FileMeta> filePage = baseMapper.selectPageByCreatorIdAndDbType(page, currentUser.getId(), queryDTO.getDbType(), keyword);

        // 转换为VO
        List<FileMetaVO> records = filePage.getRecords().stream()
                .map(this::buildFileMetaVO)
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
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(String id) {
        FileMeta fileMeta = getFileMetaByUuid(id);

        // 获取当前登录用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser != null && !currentUser.getId().equals(fileMeta.getCreatorId())) {
            throw new ApiException(ApiErrorCode.FORBIDDEN);
        }

        //根据fileMeta实例，删除数据库记录
        boolean IsRemove = removeById(fileMeta.getId());

        if(!IsRemove){
            throw new ApiException("数据库记录删除失败");
        }

        // 删除物理文件
        File file = new File(fileMeta.getFilePath());
        if (file.exists()) {
            boolean deleted = file.delete();
            if(!deleted){
                throw new ApiException("物理文件删除失败");
            }
        }

        // 删除数据库记录
        if (StringUtils.hasText(fileMeta.getPreviewImagePath())) {
            File previewFile = new File(fileMeta.getPreviewImagePath());
            if (previewFile.exists()) {
                boolean deleted = previewFile.delete();
                if(!deleted){
                    throw new ApiException("预览图删除失败");
                }
            }
        }

        return true;
    }

    private FileMeta getFileMetaByUuid(String field) {
        FileMeta fileMeta = baseMapper.selectOne(new LambdaQueryWrapper<FileMeta>().eq(FileMeta::getFileUuid, field));
        if (fileMeta == null) {
            throw new ApiException(ApiErrorCode.RESOURCE_NOT_FOUND);
        }
        return fileMeta;
    }

    private FileMetaVO buildFileMetaVO(FileMeta fileMeta) {
        FileMetaVO fileMetaVO = new FileMetaVO();
        fileMetaVO.setFileName(fileMeta.getFileName());
        fileMetaVO.setId(fileMeta.getFileUuid());
        fileMetaVO.setFileSize(TransFileSizeUtil.transFileSize(fileMeta.getFileSize()));
        fileMetaVO.setUpdateTime(fileMeta.getUpdateTime());
        fileMetaVO.setHasPreview(StringUtils.hasText(fileMeta.getPreviewImagePath()));
        fileMetaVO.setPreviewImageId(fileMeta.getPreviewImageId());
        return fileMetaVO;
    }

    private boolean hasPreviewImage(MultipartFile previewImage) {
        return previewImage != null && !previewImage.isEmpty();
    }

    private void validatePreviewImage(MultipartFile previewImage) {
        if (!hasPreviewImage(previewImage)) {
            return;
        }
        if (!SUPPORTED_PREVIEW_IMAGE_TYPES.contains(previewImage.getContentType())) {
            throw new ApiException("预览图格式不支持");
        }
        if (previewImage.getSize() > MAX_PREVIEW_IMAGE_SIZE) {
            throw new ApiException("预览图过大");
        }
    }

    private void ensureUploadDirectoryExists() throws IOException {
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    private void deleteIfExists(String path) {
        if (!StringUtils.hasText(path)) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            log.warn("文件删除失败: {}", path, e);
        }
    }

    @Override
    public ApiResult<Object> checkFiles(String md5, Long totalChunks) {
        // 需要实现将最近一个未收到的文件分片索引号返回
        // 0. 字段异常判空
        if(!StringUtils.hasText(md5)){
            throw new ApiException("文件标识符不能为空");
        }
        if(totalChunks == null || totalChunks <= 0){
            throw new ApiException("文件分片总数必须为正整数");
        }
        // 1. 缓存的key值
        String key = RedisConstants.FILE_UPLOAD_CHUNK_PREFIX + md5;
        // 2. 获取顺序排列下来第一个缺失的索引号
        long firstMissing = findFirstMissingChunkKey(key, totalChunks);
        // 3. 统计后端已上传数量

        // 4. 返回{md5,firstMissing,count}信息给前端网页

        return null;
    }

    private long findFirstMissingChunkKey(String key, long totalChunks) {
        // 1. 每次扫描1024位
        final long batchSize = 1024;
        long start = 0;
        while(start < totalChunks){
            long end =  Math.min(start + batchSize, totalChunks);

            for(long i = start; i < end; i++){
                Boolean bit = stringRedisTemplate.opsForValue().getBit(key, i);
                if(Boolean.FALSE.equals(bit)){
                    return i;
                }
            }

            start = end;
        }
        // 如果所有分片都上传完
        return -1L;
    }

    @Override
    public ApiResult<Object> uploadChunk(String md5, Integer chunkIndex, MultipartFile file) {
        return null;
    }

    @Override
    public ApiResult<Object> mergeChunk(String md5, String fileName, Long totalChunks) {
        return null;
    }

}
