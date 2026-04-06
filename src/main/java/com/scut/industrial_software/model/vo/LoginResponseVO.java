package com.scut.industrial_software.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录成功返回的视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseVO {
    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 认证token
     */
    private String token;

    /**
     * 用户权限
     */
    private Integer permission;

    /**
     * 组内权限：0-普通成员，1-组管理员
     */
    private Integer taskPermission;

    /**
     * 用户电话号码
     */
    private String phone;
}
