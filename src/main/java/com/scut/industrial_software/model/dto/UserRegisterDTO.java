package com.scut.industrial_software.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDTO {
    @NotEmpty(message = "用户名不能为空")
    private String username;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 6, message = "密码长度至少为6位")
    private String password;

    @NotNull(message = "权限不能为空")
    private Integer permission;

    @Pattern(regexp = "^\\d{11}$", message = "手机号必须为11位数字")
    private String phone;
}