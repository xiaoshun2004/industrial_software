package com.scut.industrial_software.model.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    // 用户ID - 改为String类型以匹配响应示例
    private String userId;

    // 用户名
    private String username;

    // 用户电话号码
    private String phone;

    // 权限级别（0-普通用户 1-管理员）
    private Integer permission;

    // 任务权限（0-个人权限 1-组织权限）
    private Integer taskPermission;
    
    // 所属组织名称
    private String organization;

    // 组织ID
    private String orgId;
}