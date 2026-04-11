package com.scut.industrial_software.model.dto;

import lombok.Data;

/**
 * 文件元数据更新参数DTO
 */
@Data
public class FileMetaUpdateDTO {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件描述
     */
    private String description;
}
