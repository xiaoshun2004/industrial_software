package com.scut.industrial_software.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.FileQueryDTO;
import com.scut.industrial_software.model.entity.FileMeta;
import com.scut.industrial_software.model.vo.FileMetaVO;
import com.scut.industrial_software.model.vo.PageVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Part;
import java.io.InputStream;

import java.io.InputStream;

/**
 * <p>
 * 文件元数据表 服务类
 * </p>
 */
public interface IFileMetaService extends IService<FileMeta> {

    /**
     * 上传或保存文件
     *
     * @param dbType 文件隶属的数据库类型
     * @param file 文件
     * @param fileName 用户指定的文件名
     * @return 文件元数据
     */
    FileMetaVO uploadFile(String dbType, String fileName, MultipartFile file);

    /**
     * 流式上传文件
     *
     * @param dbType 文件隶属的数据库类型
     * @param file  文件
     * @param fileName  用户指定的文件名
     * @return 文件元数据
     */
    FileMetaVO uploadFileStream(String dbType, String fileName, MultipartFile file);

    /**
     * 下载文件
     *
     * @param field 文件唯一字符串ID
     * @return 文件字节数组
     */
    byte[] downloadFile(String field);

    /**
     * 获取文件信息
     *
     * @param field 文件的字符串ID
     * @return 文件元数据
     */
    FileMetaVO getFileInfo(String field);

    /**
     * 获取当前用户的文件列表√
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
    //PageVO<FileMetaVO> searchFiles(FileQueryDTO queryDTO);

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 是否成功
     */
    boolean deleteFile(String id);
} 