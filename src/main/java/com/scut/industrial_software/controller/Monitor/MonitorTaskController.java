package com.scut.industrial_software.controller.Monitor;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.service.IMonitorTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/Monitoring/tasks")
public class MonitorTaskController {

    @Autowired
    private IMonitorTaskService taskService;

    @PostMapping
    public ApiResult<?> getTasksPage(@RequestParam Integer pageNum,
                                     @RequestParam Integer pageSize,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) Integer serverId){
        return taskService.getTasksPage(pageNum, pageSize, keyword, status, serverId);
    }

    @PutMapping("/{taskId}/priority")
    public ApiResult<?> updateTaskPriority(@PathVariable String taskId){
        return ApiResult.success();
    }

    @PostMapping("/allocate")
    public ApiResult<?> allocateResources(){
        return ApiResult.success();
    }
}
