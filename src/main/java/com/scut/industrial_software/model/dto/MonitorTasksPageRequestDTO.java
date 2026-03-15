package com.scut.industrial_software.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
public class MonitorTasksPageRequestDTO extends PageRequestDTO{
    /**
     * 任务状态过滤条件（可选）
     */
    @Pattern(regexp = "^(pending|running|completed|failed|未启动|仿真中|已结束)?$", message = "status 值非法")
    private String status;
    /**
     * 服务器ID过滤条件（可选）
     */
    private Integer serverId;
}
