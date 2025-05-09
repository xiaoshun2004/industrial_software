package com.scut.industrial_software.controller;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.ChangePasswordDTO;
import com.scut.industrial_software.model.dto.UserPageQueryDTO;
import com.scut.industrial_software.model.dto.UserRegisterDTO;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.model.vo.UserInfoVO;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.utils.UserHolder;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhou
 * @since 2025-03-29
 */
@Slf4j
@Controller
@RequestMapping("/modUsers")
public class ModUsersController {
    @Autowired
    private IModUsersService iModUsersService;

    /**
     * 返回所有用户信息（测试）
     * @return
     */
    @ResponseBody
    @GetMapping
    public List<ModUsers> getAllUsers() {
        return iModUsersService.getAllUsers();
    }

    /**
     * 用户注册
     * @param userRegisterDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/register")
    public ApiResult<Object> registerUser(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册，用户名: {}", userRegisterDTO.getUsername());

        ModUsers user = iModUsersService.executeRegister(userRegisterDTO);
        if (ObjectUtils.isEmpty(user)) {
            return ApiResult.failed("账号注册失败");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        return ApiResult.success(map);
    }

    /**
     * 分页查询用户列表
     */
    @ResponseBody
    @PostMapping("/page")
    public ApiResult<PageVO<UserInfoVO>> pageUsersPost(@RequestBody UserPageQueryDTO queryDTO) {
        log.info("用户信息分页查询...");
        PageVO<UserInfoVO> pageResult = iModUsersService.pageUsers(queryDTO);
        return ApiResult.success(pageResult);
    }

    /**
     * 查询当前用户信息 回显
     * @return
     */
    @ResponseBody
    @GetMapping("/info")
    public ApiResult<UserInfoVO> getCurrentUserInfo() {
        log.info("获取当前登录用户信息");
        UserInfoVO userInfo = iModUsersService.getCurrentUserInfo();
        if (userInfo == null) {
            return ApiResult.failed("获取用户信息失败");
        }
        return ApiResult.success(userInfo);
    }

    /**
     * 修改用户密码
     * @param changePasswordDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/changePassword")
    public ApiResult<Object> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        log.info("用户 {} 正在修改密码", UserHolder.getUser().getId()); // 假设使用 UserHolder 获取当前用户 ID
        Long userId = UserHolder.getUser().getId();  // 获取当前用户 ID

        return iModUsersService.changePassword(userId, changePasswordDTO);
    }
}