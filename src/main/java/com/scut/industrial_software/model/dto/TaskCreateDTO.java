package com.scut.industrial_software.model.dto;

import lombok.Data;

/**
 * 任务创建请求参数
 */
@Data
public class TaskCreateDTO {
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 仿真阶段
     */
    private String simulationStage;
    
    /**
     * 任务类型
     */
    private String type;
    
    /**
     * 创建者
     */
    private String creator;
} 