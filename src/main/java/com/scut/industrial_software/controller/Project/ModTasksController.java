package com.scut.industrial_software.controller.Project;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.PageRequestDTO;
import com.scut.industrial_software.model.dto.TaskCreateDTO;
import com.scut.industrial_software.service.IModTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  任务管理控制器
 * </p>
 *
 * @author jhx1301
 * @since 2025-05-28
 */
@RestController
@RequestMapping("/modTasks")
public class ModTasksController {
    
    @Autowired
    private IModTasksService modTasksService;
    
    /**
     * 获取共享任务分页列表
     */
    @PostMapping("/shared/{projectId}/page")
    public ApiResult<?> getSharedTasksPage(@PathVariable Integer projectId,
                                         @RequestBody PageRequestDTO requestDTO) {
        return modTasksService.getSharedTasksPage(projectId, requestDTO);
    }
    
    /**
     * 获取私人任务分页列表
     */
    @PostMapping("/private/{projectId}/page")
    public ApiResult<?> getPrivateTasksPage(@PathVariable Integer projectId,
                                          @RequestBody PageRequestDTO requestDTO) {
        return modTasksService.getPrivateTasksPage(projectId, requestDTO);
    }
    
    /**
     * 创建新共享任务
     */
    @PostMapping("/shared/{projectId}/create")
    public ApiResult<?> createSharedTask(@PathVariable Integer projectId,
                                       @RequestBody TaskCreateDTO createDTO) {
        return modTasksService.createSharedTask(projectId, createDTO);
    }
    
    /**
     * 创建新私人任务
     */
    @PostMapping("/private/{projectId}/create")
    public ApiResult<?> createPrivateTask(@PathVariable Integer projectId,
                                        @RequestBody TaskCreateDTO createDTO) {
        return modTasksService.createPrivateTask(projectId, createDTO);
    }
    
    /**
     * 删除任务
     */
    @DeleteMapping("/delete/{taskId}")
    public ApiResult<?> deleteTask(@PathVariable Integer taskId) {
        return modTasksService.deleteTask(taskId);
    }
}
