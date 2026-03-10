package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("server")
public class Server {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 服务器名称
     */
    private String name;

    /**
     * 服务器IP
     */
    private String ip;

    /**
     * 服务器规格
     */
    private String specification;

    /**
     * 服务器状态 （running/idle/offline/maintenance)(运行中/空闲/离线/维护中)
     */
    private String status;

    /**
     * CPU核心数
     */
    private Integer cpuCores;

    /**
     * 内存大小（GB）
     */
    private Integer memory;

    /**
     * CPU使用率（百分比）
     */
    private String cpuUsage;

    /**
     * 内存使用率（百分比）
     */
    private String memoryUsage;

}
