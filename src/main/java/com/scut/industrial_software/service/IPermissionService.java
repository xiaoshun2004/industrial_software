package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;

/**
 * 权限管理服务
 * 提供统一的权限控制和并发安全的权限操作
 */
public interface IPermissionService {
    
    /**
     * 检查用户是否有指定权限
     * @param userId 用户ID
     * @param requiredPermission 需要的权限级别
     * @return 是否有权限
     */
    boolean hasPermission(Integer userId, Integer requiredPermission);
    
    /**
     * 检查当前用户是否有管理员权限
     * @return 是否是管理员
     */
    boolean isCurrentUserAdmin();
    
    /**
     * 安全地修改用户权限（带并发控制）
     * @param targetUserId 目标用户ID
     * @param newPermission 新权限级别
     * @return 操作结果
     */
    ApiResult<Object> changeUserPermissionSafely(Integer targetUserId, Integer newPermission);
    
    /**
     * 权限修改后，使相关Token失效
     * @param userId 用户ID
     * @param reason 失效原因
     */
    void invalidateUserTokenOnPermissionChange(Integer userId, String reason);
    
    /**
     * 获取用户当前权限级别
     * @param userId 用户ID
     * @return 权限级别
     */
    Integer getUserPermission(Integer userId);
    
    /**
     * 验证权限修改的合法性
     * @param operatorUserId 操作者用户ID
     * @param targetUserId 目标用户ID
     * @param newPermission 新权限
     * @return 是否合法
     */
    boolean validatePermissionChange(Integer operatorUserId, Integer targetUserId, Integer newPermission);
} 