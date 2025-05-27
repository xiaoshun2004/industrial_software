package com.scut.industrial_software.model.dto;

import lombok.Data;

/**
 * 成员分页查询DTO
 */
@Data
public class MemberPageQueryDTO {
    
    /**
     * 当前页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
} 