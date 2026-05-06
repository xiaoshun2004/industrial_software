package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

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
     * 任务状态（pending、running、paused、waiting、completed）
     */
    private String status;

    /**
     * 所属服务器ID
     */
    private Integer serverId;

    /**
     * 所属服务器名称
     */
    private String serverName;

    /**
     * 优先级：1=高，2=中，3=低
     */
    private Integer priority;

    /**
     * CPU 核心需求
     */
    private Integer cpuCoreNeed;

    /**
     * 内存需求(GB)
     */
    private Integer memoryNeed;

    /**
     * 执行进度(0-100)
     */
    private Integer progress;

    /**
     * 启动时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 失败原因
     */
    private String errorMsg;

    /**
     * 计算类型（GPU等）（可选）
     */
    private String computeResource;
}
