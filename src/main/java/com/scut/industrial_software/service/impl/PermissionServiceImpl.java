package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.constant.PermissionConstants;
import com.scut.industrial_software.model.constant.RedisConstants;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.service.IPermissionService;
import com.scut.industrial_software.utils.DistributedLockUtil;
import com.scut.industrial_software.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 权限管理服务实现
 * 提供并发安全的权限控制功能
 */
@Slf4j
@Service
public class PermissionServiceImpl implements IPermissionService {
    
    @Autowired
    private IModUsersService modUsersService;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private DistributedLockUtil distributedLockUtil;
    
    // 使用权限常量
    private static final Integer PERMISSION_USER = PermissionConstants.PERMISSION_USER;
    private static final Integer PERMISSION_ADMIN = PermissionConstants.PERMISSION_ADMIN;
    
    @Override
    public boolean hasPermission(Integer userId, Integer requiredPermission) {
        if (userId == null || requiredPermission == null) {
            return false;
        }
        
        try {
            Integer userPermission = getUserPermission(userId);//根据用户id获取用户权限
            return userPermission != null && userPermission >= requiredPermission;
        } catch (Exception e) {
            log.error("检查用户权限时发生异常，用户ID: {}", userId, e);
            return false; // 出现异常时保守地拒绝权限
        }
    }
    
    /**
     * 检查操作者是否为管理员
     */
    public boolean isCurrentUserAdmin() {
        try {
            Integer currentUserId = UserHolder.getUser().getId();
            return hasPermission(currentUserId, PERMISSION_ADMIN);
        } catch (Exception e) {
            log.error("检查当前用户管理员权限时发生异常", e);
            return false;
        }
    }
    
    @Override
    public ApiResult<Object> changeUserPermissionSafely(Integer targetUserId, Integer newPermission) {
        // 参数验证
        if (targetUserId == null || newPermission == null) {
            return ApiResult.failed("参数不能为空");
        }
        // 检查权限值是否合法
        if (!newPermission.equals(PERMISSION_USER) && !newPermission.equals(PERMISSION_ADMIN)) {
            return ApiResult.failed(PermissionConstants.MSG_INVALID_PERMISSION_VALUE);
        }
        
        // 获取操作者信息
        Integer operatorUserId = UserHolder.getUser().getId().intValue();//获取当前用户id
        
        // 验证操作权限
        if (!validatePermissionChange(operatorUserId, targetUserId, newPermission)) {
            return ApiResult.failed(PermissionConstants.MSG_NO_PERMISSION);
        }
        
        // 使用分布式锁确保并发安全
        String lockKey = PermissionConstants.PERMISSION_LOCK_PREFIX + targetUserId;
        
        return distributedLockUtil.executeWithLock(
                lockKey,
                10L,
                TimeUnit.SECONDS,
                () -> executePermissionChange(targetUserId, newPermission)
        );
    }
    
    /**
     * 执行权限修改的核心逻辑
     */
    private ApiResult<Object> executePermissionChange(Integer targetUserId, Integer newPermission) {
        try {
            // 1. 检查目标用户是否存在
            ModUsers targetUser = modUsersService.getById(targetUserId);
            if (targetUser == null) {
                return ApiResult.failed("目标用户不存在");
            }
            
            // 2. 检查权限是否已经是目标值
            if (targetUser.getPermission().equals(newPermission)) {
                return ApiResult.success("用户权限已经是目标权限级别");
            }
            
            // 3. 记录旧权限
            Integer oldPermission = targetUser.getPermission();
            
            // 4. 更新权限
            targetUser.setPermission(newPermission);
            boolean updateSuccess = modUsersService.updateById(targetUser);
            
            if (!updateSuccess) {
                return ApiResult.failed("权限修改失败");
            }
            
            // 5. 权限修改成功后，使该用户的Token失效
            String reason = String.format("permission_changed_from_%d_to_%d", oldPermission, newPermission);
            invalidateUserTokenOnPermissionChange(targetUserId, reason);
            
            log.info("用户 {} 的权限已从 {} 修改为 {}", targetUserId, oldPermission, newPermission);
            
            return ApiResult.success("权限修改成功，用户需要重新登录");
            
        } catch (Exception e) {
            log.error("执行权限修改时发生异常，目标用户: {}", targetUserId, e);
            return ApiResult.failed("权限修改失败，请稍后重试");
        }
    }
    
    @Override
    public void invalidateUserTokenOnPermissionChange(Integer userId, String reason) {
        try {
            ModUsers user = modUsersService.getById(userId);
            if (user == null) {
                log.warn("尝试使Token失效时，用户不存在: {}", userId);
                return;
            }
            
            String tokenKey = RedisConstants.USER_TOKEN_KEY_PREFIX + user.getUsername() + ":token";
            String token = redisTemplate.opsForValue().get(tokenKey);
            
            if (token != null && !token.isEmpty()) {
                // 将token加入黑名单
                tokenBlacklistService.kickOutDevice(token, reason);
                
                // 删除Redis中的token记录
                redisTemplate.delete(tokenKey);
                
                log.info("用户 {} 的Token已因权限变更而失效", user.getUsername());
            }
            
        } catch (Exception e) {
            log.error("使用户Token失效时发生异常，用户ID: {}", userId, e);
        }
    }
    
    @Override
    public Integer getUserPermission(Integer userId) {
        try {
            ModUsers user = modUsersService.getById(userId);
            return user != null ? user.getPermission() : null;
        } catch (Exception e) {
            log.error("获取用户权限时发生异常，用户ID: {}", userId, e);
            return null;
        }
    }
    
    @Override
    public boolean validatePermissionChange(Integer operatorUserId, Integer targetUserId, Integer newPermission) {
        try {
            // 1. 检查操作者是否是管理员
            if (!hasPermission(operatorUserId, PERMISSION_ADMIN)) {
                log.warn("非管理员用户 {} 尝试修改权限", operatorUserId);
                return false;
            }
            
            // 2. 管理员不能修改自己的权限（防止权限锁定）
            if (operatorUserId.equals(targetUserId)) {
                log.warn("管理员 {} 尝试修改自己的权限", operatorUserId);
                return false;
            }
            
            // 3. 检查是否试图创建超级管理员（如果有更复杂的权限级别）
            // 这里可以根据业务需求添加更多验证逻辑
            
            return true;
            
        } catch (Exception e) {
            log.error("验证权限修改合法性时发生异常", e);
            return false;
        }
    }
} 