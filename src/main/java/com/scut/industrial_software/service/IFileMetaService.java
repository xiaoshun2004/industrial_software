package com.scut.industrial_software.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scut.industrial_software.model.dto.FileQueryDTO;
import com.scut.industrial_software.model.entity.FileMeta;
import com.scut.industrial_software.model.vo.FileMetaVO;
import com.scut.industrial_software.model.vo.PageVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件元数据表 服务类
 * </p>
 */
public interface IFileMetaService extends IService<FileMeta> {

    /**
     * 上传或保存文件
     *
     * @param file 文件
     * @param id 文件ID（为null时表示新上传，否则表示覆盖保存）
     * @return 文件元数据
     */
    FileMetaVO uploadOrSaveFile(MultipartFile file, Long id);

    /**
     * 下载文件
     *
     * @param id 文件ID
     * @return 文件字节数组
     */
    byte[] downloadFile(Long id);

    /**
     * 获取文件信息
     *
     * @param id 文件ID
     * @return 文件元数据
     */
    FileMetaVO getFileInfo(Long id);

    /**
     * 获取当前用户的文件列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageVO<FileMetaVO> getMyFiles(FileQueryDTO queryDTO);

    /**
     * 根据关键字搜索文件
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageVO<FileMetaVO> searchFiles(FileQueryDTO queryDTO);

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 是否成功
     */
    boolean deleteFile(Long id);
} 