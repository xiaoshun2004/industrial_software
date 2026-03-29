package com.scut.industrial_software.model.vo;

import com.scut.industrial_software.model.entity.ModTasks;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端任务监控列表项。
 */
@Data
public class MonitorTaskItemVO {

    private String taskId;

    private String taskName;

    private Integer serverId;

    private String serverName;

    private String type;

    private Integer priority;

    private Integer cpuCoreNeed;

    private Integer memoryNeed;

    private String status;

    private Integer progress;

    private LocalDateTime startTime;

    private String errorMsg;

    public static MonitorTaskItemVO from(ModTasks task) {
        MonitorTaskItemVO vo = new MonitorTaskItemVO();
        vo.setTaskId("task_" + task.getTaskId());
        vo.setTaskName(task.getTaskName());
        vo.setServerId(task.getServerId());
        vo.setServerName(task.getServerName());
        vo.setType(task.getType());
        vo.setPriority(task.getPriority());
        vo.setCpuCoreNeed(task.getCpuCoreNeed());
        vo.setMemoryNeed(task.getMemoryNeed());
        vo.setStatus(task.getStatus());
        vo.setProgress(task.getProgress());
        vo.setStartTime(task.getStartTime());
        vo.setErrorMsg(task.getErrorMsg());
        return vo;
    }
}

