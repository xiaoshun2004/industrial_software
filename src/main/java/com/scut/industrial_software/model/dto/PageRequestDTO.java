package com.scut.industrial_software.model.dto;

import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageRequestDTO {
    
    /**
     * 当前页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
    
    /**
     * 搜索关键字
     */
    private String keyword;
} 