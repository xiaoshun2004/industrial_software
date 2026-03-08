package com.scut.industrial_software.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MonitorServersPageRequestDTO extends PageRequestDTO{
    /**
     * 服务器状态过滤条件（可选）
     */
    private String status;
    /**
     * 服务器类型过滤条件（可选）
     */
    private String type;
}
