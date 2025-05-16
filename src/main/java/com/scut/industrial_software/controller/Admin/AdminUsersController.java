package com.scut.industrial_software.controller.Admin;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.AdminUpdateUserInfoDTO;
import com.scut.industrial_software.model.dto.UserPageQueryDTO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.model.vo.UserInfoVO;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.service.impl.TokenBlacklistService;
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

    @Autowired
    private TokenBlacklistService tokenBlacklistService;


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

    /**
     * 管理员重置用户密码为123456
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/{userId}/resetPassword")
    public ApiResult<Object> resetUserPassword(@PathVariable Integer userId) {
        log.info("管理员正在重置用户 {} 的密码", userId);
        return modUsersService.resetPasswordByAdmin(userId);
    }

    /**
     * 分页查询用户列表
     */
    @ResponseBody
    @PostMapping("/page")
    public ApiResult<PageVO<UserInfoVO>> pageUsersPost(@RequestBody UserPageQueryDTO queryDTO) {
        log.info("用户信息分页查询...");
        PageVO<UserInfoVO> pageResult = modUsersService.pageUsers(queryDTO);
        return ApiResult.success(pageResult);
    }

    /**
     * 管理员删除用户
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    public ApiResult<Object> deleteUser(@PathVariable Integer userId) {
        log.info("管理员正在删除用户 {} 的信息", userId);
        return modUsersService.deleteUserByAdmin(userId);
    }

    /**
     * 修改用户权限
     * @return
     */
    @ResponseBody
    @PostMapping("/{userId}/changePermission")
    public ApiResult<Object> changePermission(@PathVariable Integer userId,
                                              @RequestParam Integer permission){


        log.info("管理员正在修改用户 {} 的权限为 {}", userId, permission);
        // 校验只能是 0 或 1
        if (permission == null || (permission != 0 && permission != 1)) {
            return ApiResult.failed("permission 只能是 0 或 1");
        }
        return modUsersService.changePermissionByAdmin(userId, permission);
    }

}