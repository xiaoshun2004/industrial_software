package com.scut.industrial_software.model.dto;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUpdateUserInfoDTO {
    // 用户名
    private String username;

    // 电话号码
    private String phone;
}