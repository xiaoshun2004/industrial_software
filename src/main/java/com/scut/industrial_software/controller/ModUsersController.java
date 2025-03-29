package com.scut.industrial_software.controller;

import com.scut.industrial_software.entity.ModUsers;
import com.scut.industrial_software.service.IModUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhou
 * @since 2025-03-29
 */
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
}