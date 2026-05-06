package com.scut.industrial_software.model.dto;

import lombok.Data;

/**
 * 客户端本地任务状态上报参数。
 */
@Data
public class ClientTaskStatusUpdateDTO {

    /**
     * 目标状态：running、completed、failed、stopped。
     */
    private String status;

    /**
     * 执行进度，0-100。
     */
    private Integer progress;

    /**
     * 失败或停止原因。
     */
    private String errorMsg;
}
