package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("server")
@Accessors(chain = true)
public class Server implements Serializable {

    /**
     * 用于版本兼容，确保序列化和反序列化过程中类的版本一致
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 服务器唯一标识符ID
     */
    @Id
    private Long id;

    /**
     * 服务器名称
     */
    private String name;

    /**
     * 服务器IP地址
     */
    private String ip;

    /**
     * 服务器类型
     */
    private String type;

    /**
     * 服务器状态
     */
    private String status;

    /**
     * 服务器CPU核心数
     */
    private Integer cpuCores;

    /**
     * 服务器内存大小（GB）
     */
    private Integer memory;

    /**
     * 服务器最后在线时间
     */
    private LocalDateTime lastOnline;

    /**
     * 服务器CPU使用率（百分比）
     */
    @TableField(exist = false)
    private Integer cpuUsage;

    /**
     * 服务器内存使用率（百分比）
     */
    @TableField(exist = false)
    private Integer memoryUsage;

}
