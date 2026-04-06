package com.scut.industrial_software.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 组内权限修改DTO
 */
@Data
public class UpdateGroupAdminDTO {

    /**
     * 组内权限：0-普通成员，1-组管理员
     */
    @NotNull(message = "组内权限不能为空")
    @Min(value = 0, message = "组内权限只能为0或1")
    @Max(value = 1, message = "组内权限只能为0或1")
    private Integer taskPermission;
}
