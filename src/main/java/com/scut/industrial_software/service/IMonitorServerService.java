package com.scut.industrial_software.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.entity.Server;

public interface IMonitorServerService extends IService<Server> {

    ApiResult<?> getServerPage(Integer pageNum, Integer pageSize, String keyword, String status, String type);

    ApiResult<?> adjustServerResources(String serverId, Integer cpuCores, Integer memory);
}
