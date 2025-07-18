package com.scut.industrial_software.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 文件元数据视图对象
 */
@Data
@Accessors(chain = true)
public class FileMetaVO {
    /**
     * 文件唯一ID
     */
    private Long id;

    /**
     * 文件系统中的唯一标识符
     */
    private String fileUuid;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小 (单位: B)
     */
    private Long fileSize;

    /**
     * 文件的MIME类型
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
     * 创建者用户名
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
} 