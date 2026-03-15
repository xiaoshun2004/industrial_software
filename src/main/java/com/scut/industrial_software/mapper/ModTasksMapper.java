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

    /**
     * 管理端任务监控分页
     */
    IPage<ModTasks> selectMonitorTasksByPage(Page<ModTasks> page,
                                             @Param("keyword") String keyword,
                                             @Param("status") String status,
                                             @Param("serverId") Integer serverId);

    /**
     * 仅在 pending 状态允许修改优先级
     */
    int updateTaskPriorityWhenPending(@Param("taskId") Integer taskId,
                                      @Param("priority") Integer priority);

    /**
     * 条件状态流转，避免并发覆盖
     */
    int updateTaskStatusConditionally(@Param("taskId") Integer taskId,
                                      @Param("fromStatus") String fromStatus,
                                      @Param("toStatus") String toStatus);

    /**
     * 查询某一求解器类型当前运行数量
     */
    Integer countRunningByType(@Param("type") String type);

    /**
     * 按优先级和创建时间查询待调度任务
     */
    IPage<ModTasks> selectPendingTasksForSchedule(Page<ModTasks> page,
                                                  @Param("type") String type);
}
