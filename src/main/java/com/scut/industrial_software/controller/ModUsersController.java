package com.scut.industrial_software.controller;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.UserRegisterDTO;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.service.IModUsersService;
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

}