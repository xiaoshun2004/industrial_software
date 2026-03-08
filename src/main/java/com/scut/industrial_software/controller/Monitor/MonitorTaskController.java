package com.scut.industrial_software.controller.Monitor;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.MonitorTasksPageRequestDTO;
import com.scut.industrial_software.service.IMonitorTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/Monitoring/tasks")
public class MonitorTaskController {

    @Autowired
    private IMonitorTaskService taskService;

    @GetMapping
    public ApiResult<?> getTasksPage(@ModelAttribute @Validated MonitorTasksPageRequestDTO pageRequestDTO){
        log.info("获取任务列表: pageNum={}, pageSize={}, keyword={}, status={}, serverId={}",
                pageRequestDTO.getPageNum(),
                pageRequestDTO.getPageSize(),
                pageRequestDTO.getKeyword(),
                pageRequestDTO.getStatus(),
                pageRequestDTO.getServerId());
        return taskService.getTasksPage(pageRequestDTO);
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
