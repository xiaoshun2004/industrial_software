package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件元数据表
 * </p>
 *
 * @since 2023-10-28
 */
@Data
@Accessors(chain = true)
@TableName("file_meta")
public class FileMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件唯一ID，用于API调用
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件系统中的唯一标识符 (UUID)，用于防止文件名冲突
     */
    private String fileUuid;

    /**
     * 用户上传时原始文件名
     */
    private String fileName;

    /**
     * 文件在服务器上的物理存储路径
     */
    private String filePath;

    /**
     * 文件大小 (单位: B)
     */
    private Long fileSize;

    /**
     * 文件的MIME类型 (例如: application/pdf)
     */
    private String fileType;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 创建者用户ID
     */
    private Long creatorId;

    /**
     * 创建者用户名 (冗余字段，方便查询)
     */
    private String creatorName;

    /**
     * 存储位置 (LOCAL_DISK, ALI_OSS等)
     */
    private String storageLocation;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 文件隶属的数据库表
     */
    private String dbType;
} 