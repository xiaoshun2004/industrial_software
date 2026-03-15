package com.scut.industrial_software.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.MonitorTasksPageRequestDTO;
import com.scut.industrial_software.model.entity.ModTasks;

public interface IMonitorTaskService extends IService<ModTasks> {

    ApiResult<?> getTasksPage(MonitorTasksPageRequestDTO requestDTO);

    ApiResult<?> updateTaskPriority(String taskId, Integer priority);
}
