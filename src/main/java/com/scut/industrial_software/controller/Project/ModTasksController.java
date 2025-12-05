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
     * 获取任务分页列表
     */
    @PostMapping("/{accessType}/{projectId}/page")
    public ApiResult<?> getTasksPage(@PathVariable String accessType,
                                     @PathVariable Integer projectId,
                                     // @RequestBody将客户端的JSON请求体反序列化为Java对象
                                     @RequestBody PageRequestDTO requestDTO) {
        return "shared".equals(accessType) ?
                modTasksService.getSharedTasksPage(projectId, requestDTO) :
                modTasksService.getPrivateTasksPage(projectId, requestDTO);
    }
    /**
     * 创建新任务
     */
    @PostMapping("/{accessType}/{projectId}/create")
    public ApiResult<?> createTask(@PathVariable String accessType,
                                   @PathVariable Integer projectId,
                                   @RequestBody TaskCreateDTO createDTO) {
        return "shared".equals(accessType) ?
                modTasksService.createSharedTask(projectId, createDTO) :
                modTasksService.createPrivateTask(projectId, createDTO);
    }

    // 以下方法不需要区分访问类型，保持原样
    /**
     * 删除任务
     */
    @DeleteMapping("/delete/{taskId}")
    public ApiResult<?> deleteTask(@PathVariable String taskId) {
        return modTasksService.deleteTask(taskId);
    }

    /**
     * 开始任务
     */
    @PutMapping("/start/{taskId}")
    public ApiResult<?> startTask(@PathVariable String taskId) {
        return modTasksService.startTask(taskId);
    }

    /**
     * 查询任务状态，专门为监控任务轮询设置的获取状态接口
     */
    @GetMapping("/status/{taskId}")
    public ApiResult<?> getTaskStatus(@PathVariable String taskId) {
        return modTasksService.getTaskStatus(taskId);
    }

    /**
     * 停止任务
     */
    @PutMapping("/stop/{taskId}")
    public ApiResult<?> stopTask(@PathVariable String taskId) {
        return modTasksService.stopTask(taskId);
    }


}
