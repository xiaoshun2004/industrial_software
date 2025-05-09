package com.scut.industrial_software.controller.Admin;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.AdminUpdateUserInfoDTO;
import com.scut.industrial_software.service.IModUsersService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/admin/users")
public class AdminUsersController {

    @Autowired
    private IModUsersService modUsersService;

    /**
     * 管理员修改用户信息
     * @param userId
     * @param updateDTO
     * @return
     */
    @PostMapping("/{userId}/info")
    public ApiResult<Object> updateUserInfo(
            @PathVariable Integer userId,
            @Valid @RequestBody AdminUpdateUserInfoDTO updateDTO) {

        log.info("管理员正在修改用户 {} 的基本信息", userId);

        // 临时手动校验
        String phone = updateDTO.getPhone();
        if (phone != null && !phone.isEmpty() && !phone.matches("^1\\d{10}$")) {
            return ApiResult.failed("电话号码必须以1开头且为11位数字");
        }
        return modUsersService.updateUserInfoByAdmin(userId, updateDTO);
    }

}