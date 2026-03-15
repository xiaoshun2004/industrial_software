package com.scut.industrial_software.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 运行态任务快照，用于实时监控覆盖数据库快照。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRuntimeSnapshotDTO {

    private String taskId;

    private String status;

    private Integer progress;

    private Integer serverId;

    private String serverName;

    private LocalDateTime startTime;

    private String errorMsg;
}

