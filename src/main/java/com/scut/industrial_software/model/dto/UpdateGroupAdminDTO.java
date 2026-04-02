package com.scut.industrial_software.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 组管理员状态修改DTO
 */
@Data
public class UpdateGroupAdminDTO {

    /**
     * 是否设置为组管理员
     */
    @NotNull(message = "组管理员状态不能为空")
    private Boolean isGroupAdmin;
}
