package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("task")
public class Task {

    /**
     * 任务唯一ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 所属服务器ID
     */
    private Integer serverId;

    /**
     * 所属服务器名称
     */
    private String serverName;

    /**
     * 任务类型（模型训练/数据备份等）
     */
    private String type;

    /**
     * 优先级：1=高、2=中、3=低
     */
    private Integer priority;

    /**
     * 需求CPU核心数
     */
    private Integer cpuCoreNeed;

    /**
     * 需求内存大小
     */
    private Integer memoryNeed;

    /**
     * 进度百分比（%）
     */
    private Integer progress;

    /**
     * 任务状态（running,waiting,end）(运行中,等待中,已完成)
     */
    private String status;

    /**
     * 任务开始时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;
}
