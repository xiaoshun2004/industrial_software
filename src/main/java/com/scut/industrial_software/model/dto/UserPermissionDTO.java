package com.scut.industrial_software.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户权限修改DTO
 */
@Data
public class UserPermissionDTO {
    
    @NotNull(message = "用户ID不能为空")
    private String userId;
    
    @NotNull(message = "权限值不能为空")
    private Integer permission; // 0-普通用户 1-管理员
} 