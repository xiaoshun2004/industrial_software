package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.vo.MonitorVO;

import java.util.concurrent.CompletableFuture;

/**
 * <p>
 *  任务监控服务类
 * </p>
 *
 * @author guo
 * @since 2025-09-02
 */
public interface IMonitorService {

    ApiResult<MonitorVO> startProgram(String taskId);

     void monitorProgram(Long pid);

    void scheduledMonitor();

    ApiResult<MonitorVO> stopProgram(String taskId);

    ApiResult<MonitorVO> getProgramStatus(String taskId);
}
