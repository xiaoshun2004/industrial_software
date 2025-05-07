package com.scut.industrial_software.model.dto;

import lombok.Data;

@Data
public class UserPageQueryDTO {
    // 当前页码，默认第1页
    private Integer pageNum = 1;
    // 每页记录数，默认10条
    private Integer pageSize = 10;
    // 搜索关键词
    private String keyword;
}