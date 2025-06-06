package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 任务实体类
 * </p>
 *
 * @author zhou
 * @since 2025-03-29
 */
@Getter
@Setter
@TableName("mod_tasks")
public class ModTasks implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "task_id", type = IdType.AUTO)
    private Integer taskId;

    @JsonProperty("task_name")
    private String taskName;

    /**
     * 创建者用户ID（存储到数据库，但不返回给前端）
     */
    @JsonIgnore
    @TableField("creator")
    private Integer creatorId;
    
    /**
     * 创建者用户名（不存储到数据库，用于返回给前端）
     */
    @TableField(exist = false)
    private String creator;

    @JsonProperty("creation_time")
    private LocalDateTime creationTime;

    private Integer projectId;
    
    /**
     * 仿真阶段（前处理、后处理、求解器）
     */
    private String simulationStage;
    
    /**
     * 任务类型（多体、结构、冲击、通用后处理）
     */
    private String type;
    
    /**
     * 任务状态（未启动、仿真中、暂停中）
     */
    private String status;
}
