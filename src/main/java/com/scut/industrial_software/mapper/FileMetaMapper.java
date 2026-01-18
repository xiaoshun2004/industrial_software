package com.scut.industrial_software.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scut.industrial_software.model.entity.FileMeta;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 文件元数据表 Mapper 接口
 * </p>
 */
@Mapper
public interface FileMetaMapper extends BaseMapper<FileMeta> {

    /**
     * 根据创建者ID分页查询文件列表
     * 
     * @param page 分页参数
     * @param creatorId 创建者ID
     * @return 分页结果
     */
    IPage<FileMeta> selectPageByCreatorId(Page<FileMeta> page, @Param("creatorId") Long creatorId);

    /**
     * 根据创建者ID和文件隶属数据库类型分页查询文件列表
     *
     * @param page 分页参数
     * @param creatorId 创建者ID
     * @param dbType 数据库类型
     * @return 分页结果
     */
    IPage<FileMeta> selectPageByCreatorIdAndDbType(Page<FileMeta> page, @Param("creatorId") Integer creatorId, @Param("dbType") String dbType);

    /**
     * 根据关键字分页模糊查询文件列表
     * 
     * @param page 分页参数
     * @param keyword 关键字
     * @return 分页结果
     */
    IPage<FileMeta> selectPageByKeyword(Page<FileMeta> page, @Param("keyword") String keyword);
} 