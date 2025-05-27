package com.scut.industrial_software.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增组织DTO
 */
@Data
public class CreateOrganizationDTO {
    
    /**
     * 组织名称（2-20字符）
     */
    @NotEmpty(message = "组织名称不能为空")
    @Size(min = 2, max = 20, message = "组织名称长度必须在2-20字符之间")
    private String orgName;
} 