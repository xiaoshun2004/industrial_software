package com.scut.industrial_software.model.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    // 用户ID
    private Integer userId;
    // 用户名
    private String username;
    // 权限级别
    private Integer permission;
}