package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.PageRequestDTO;
import com.scut.industrial_software.model.dto.TaskCreateDTO;
import com.scut.industrial_software.model.entity.ModTasks;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jhx1301
 * @since 2025-05-28
 */
public interface IModTasksService extends IService<ModTasks> {

    /**
     * 获取共享任务分页列表
     * 
     * @param projectId 项目ID
     * @param requestDTO 分页请求参数
     * @return 分页列表
     */
    ApiResult<?> getSharedTasksPage(Integer projectId, PageRequestDTO requestDTO);
    
    /**
     * 获取私人任务分页列表
     * 
     * @param projectId 项目ID
     * @param requestDTO 分页请求参数
     * @return 分页列表
     */
    ApiResult<?> getPrivateTasksPage(Integer projectId, PageRequestDTO requestDTO);
    
    /**
     * 创建新共享任务
     * 
     * @param projectId 项目ID
     * @param createDTO 创建参数
     * @return 创建结果
     */
    ApiResult<?> createSharedTask(Integer projectId, TaskCreateDTO createDTO);
    
    /**
     * 创建新私人任务
     * 
     * @param projectId 项目ID
     * @param createDTO 创建参数
     * @return 创建结果
     */
    ApiResult<?> createPrivateTask(Integer projectId, TaskCreateDTO createDTO);
    
    /**
     * 删除任务
     * 
     * @param taskId 任务ID
     * @return 操作结果
     */
    ApiResult<?> deleteTask(String taskId);

    ApiResult<?> startTask(String taskId);

    ApiResult<?> getTaskStatus(String taskId);

    ApiResult<?> stopTask(String taskId);
}
