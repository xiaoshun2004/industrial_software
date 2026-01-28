package com.scut.industrial_software.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.entity.Task;

public interface IMonitorTaskService extends IService<Task> {

    ApiResult<?> getTasksPage(Integer pageNum, Integer pageSize, String keyword, String status, Integer serverId);
}
