package com.scut.industrial_software.model.dto;

import lombok.Data;

/**
 * 文件查询参数DTO
 */
@Data
public class FileQueryDTO {
    /**
     * 当前页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
    
    /**
     * 搜索关键词（文件名或描述）
     */
    private String keyword;
} 