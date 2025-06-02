package com.scut.industrial_software.mapper;

import com.scut.industrial_software.model.entity.ModTasks;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author jhx1301
 * @since 2025-05-28
 */
public interface ModTasksMapper extends BaseMapper<ModTasks> {

    /**
     * 分页查询共享任务
     * 
     * @param page 分页参数
     * @param projectId 项目ID
     * @param keyword 搜索关键字
     * @return 分页结果
     */
    IPage<ModTasks> selectSharedTasksByPage(Page<ModTasks> page, @Param("projectId") Integer projectId, @Param("keyword") String keyword);
    
    /**
     * 分页查询私人任务
     * 
     * @param page 分页参数
     * @param projectId 项目ID
     * @param keyword 搜索关键字
     * @return 分页结果
     */
    IPage<ModTasks> selectPrivateTasksByPage(Page<ModTasks> page, @Param("projectId") Integer projectId, @Param("keyword") String keyword);
}
