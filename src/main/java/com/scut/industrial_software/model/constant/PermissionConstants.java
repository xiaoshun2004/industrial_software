package com.scut.industrial_software.model.constant;

/**
 * 权限相关常量
 */
public class PermissionConstants {
    
    // 权限级别
    public static final Integer PERMISSION_USER = 0;       // 普通用户
    public static final Integer PERMISSION_ADMIN = 1;      // 管理员
    
    // Redis键前缀
    public static final String PERMISSION_LOCK_PREFIX = "permission:change:";
    
    // 权限操作相关消息
    public static final String MSG_PERMISSION_CHANGED = "权限修改成功，用户需要重新登录";
    public static final String MSG_NO_PERMISSION = "没有权限执行此操作";
    public static final String MSG_INVALID_PERMISSION_VALUE = "权限值只能是 0（普通用户）或 1（管理员）";
    public static final String MSG_CANNOT_MODIFY_SELF = "不能修改自己的权限";
    public static final String MSG_USER_NOT_FOUND = "目标用户不存在";
    public static final String MSG_PERMISSION_UNCHANGED = "用户权限已经是目标权限级别";
    public static final String MSG_OPERATION_FAILED = "权限修改失败，请稍后重试";
    
    private PermissionConstants() {
        // 防止实例化
    }
} 