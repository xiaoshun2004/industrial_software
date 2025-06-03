package com.scut.industrial_software.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户组织修改DTO
 */
@Data
public class UserOrganizationDTO {
    
    @NotNull(message = "用户ID不能为空")
    private String userId;
    
    private String orgId; // 目标组织ID（可为空字符串表示移出组织）
} 