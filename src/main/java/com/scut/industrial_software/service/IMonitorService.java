package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.TaskRuntimeSnapshotDTO;
import com.scut.industrial_software.model.vo.MonitorVO;

import java.util.Map;

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

    void scheduledMonitor();

    ApiResult<MonitorVO> stopProgram(String taskId);

    ApiResult<MonitorVO> getProgramStatus(String taskId);

    /**
     * 获取全部运行态快照，key 为 task_xxx。
     */
    Map<String, TaskRuntimeSnapshotDTO> getRuntimeSnapshots();

    /**
     * 获取单个任务运行态快照。
     */
    TaskRuntimeSnapshotDTO getRuntimeSnapshot(String taskId);
}
