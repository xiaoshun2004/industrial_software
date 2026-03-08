package com.scut.industrial_software.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MonitorTasksPageRequestDTO extends PageRequestDTO{
    /**
     * 任务状态过滤条件（可选）
     */
    private String status;
    /**
     * 服务器ID过滤条件（可选）
     */
    private Integer serverId;
}
