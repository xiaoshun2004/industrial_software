package com.scut.industrial_software.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
     * 文件系统中的唯一标识符
     */
    private String id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小 (单位: B)
     */
    private String fileSize;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
} 