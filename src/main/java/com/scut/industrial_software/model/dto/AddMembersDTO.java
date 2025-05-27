package com.scut.industrial_software.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 添加成员到组织DTO
 */
@Data
public class AddMembersDTO {
    
    /**
     * 要添加的用户ID数组
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<String> userIds;
} 