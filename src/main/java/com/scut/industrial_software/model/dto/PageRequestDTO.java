package com.scut.industrial_software.model.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;

/**
 * 分页请求参数
 */
@Data
public class PageRequestDTO {
    
    /**
     * 当前页码
     */
    @Min(value = 1, message = "pageNum 必须大于 0")
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    @Min(value = 1, message = "pageSize 必须大于 0")
    private Integer pageSize = 10;
    
    /**
     * 搜索关键字
     */
    private String keyword;
} 