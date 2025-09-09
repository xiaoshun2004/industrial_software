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

    CompletableFuture<String> startProgram();

    MonitorVO monitorProgram(Long pid);

    void scheduledMonitor();
}
