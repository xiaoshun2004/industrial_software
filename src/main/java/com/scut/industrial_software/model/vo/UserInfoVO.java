package com.scut.industrial_software.model.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    // 用户ID
    private Integer userId;

    // 用户名
    private String username;

    // 权限级别（0-普通用户 1-管理员）
    private Integer permission;

    // 用户电话号码
    private String phone;
    
    // 所属组织名称
    private String organization;
}