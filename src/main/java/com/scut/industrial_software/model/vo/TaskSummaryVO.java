package com.scut.industrial_software.model.vo;

import lombok.Data;

@Data
public class TaskSummaryVO {

    private Long pendingCount = 0L;

    private Long runningCount = 0L;

    private Long completedCount = 0L;

    private Long failedCount = 0L;

    private Long stoppedCount = 0L;
}
