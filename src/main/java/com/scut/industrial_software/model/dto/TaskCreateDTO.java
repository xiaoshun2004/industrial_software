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

    /**
     * 任务优先级：1=高，2=中，3=低；不传时默认 2
     */
    private Integer priority;

    /**
     * 计算资源（可选）
     */
    private String computeResource;
} 
