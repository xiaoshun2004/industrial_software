package com.scut.industrial_software.model.dto;

import lombok.Data;

/**
 * 组织分页查询DTO
 */
@Data
public class OrganizationPageQueryDTO {
    
    /**
     * 当前页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
    
    /**
     * 搜索关键词（组织名称）
     */
    private String keyword;
} 