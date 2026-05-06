package com.scut.industrial_software.model.dto;

import lombok.Data;

/**
 * 远程服务器启动任务请求参数。
 */
@Data
public class RemoteTaskStartDTO {

    /**
     * 远程调度优先级：1=高，2=中，3=低。
     */
    private Integer priority;
}
