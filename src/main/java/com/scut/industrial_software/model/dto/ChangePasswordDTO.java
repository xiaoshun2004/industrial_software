package com.scut.industrial_software.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDTO {
    @NotEmpty(message = "旧密码不能为空")
    private String oldPassword;

    @NotEmpty(message = "新密码不能为空")
    private String newPassword;
}