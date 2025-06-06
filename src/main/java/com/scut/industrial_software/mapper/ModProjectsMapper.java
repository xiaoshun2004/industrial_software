package com.scut.industrial_software.mapper;

import com.scut.industrial_software.model.entity.ModProjects;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author jhx1301
 * @since 2025-05-28
 */
public interface ModProjectsMapper extends BaseMapper<ModProjects> {

    /**
     * 分页查询共享项目
     * 
     * @param page 分页参数
     * @param keyword 搜索关键字
     * @param userOrgId 用户所属组织ID (null表示显示无组织的项目)
     * @return 分页结果
     */
    IPage<Map<String, Object>> selectSharedProjectsByPage(Page<Map<String, Object>> page, @Param("keyword") String keyword, @Param("userOrgId") Integer userOrgId);
    
    /**
     * 分页查询私人项目
     * 
     * @param page 分页参数
     * @param keyword 搜索关键字
     * @param creator 创建者
     * @return 分页结果
     */
    IPage<Map<String, Object>> selectPrivateProjectsByPage(Page<Map<String, Object>> page, @Param("keyword") String keyword, @Param("creator") Integer creator);
}
