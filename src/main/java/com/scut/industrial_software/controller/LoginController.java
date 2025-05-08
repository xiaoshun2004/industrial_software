package com.scut.industrial_software.controller;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.LoginInfoDTO;
import com.scut.industrial_software.model.dto.UserLoginDTO;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.model.vo.LoginResponseVO;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private IModUsersService iModUsersService;

    @PostMapping("auth/jsonLogin")
    public ApiResult<LoginResponseVO> login(@RequestBody UserLoginDTO dto){
        //todo:验证码
        System.out.println("开始认证");
        // 2. 验证用户
        ModUsers user = iModUsersService.login(dto);
        if (user == null) {
            return ApiResult.failed("用户名或密码错误");
        }
        // 3. 生成 Token（需确保 JJWT 依赖已添加）
        Map<String,Object> claims = new HashMap<>();
        claims.put("id",user.getUserId());
        claims.put("name",user.getUsername());
        String token = JwtUtils.generateToken(claims);

        // 4. 构建登录响应VO
        LoginResponseVO responseVO = LoginResponseVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .token(token)
                .permission(user.getPermission())
                .phone(user.getPhone())
                .build();

        return ApiResult.success(responseVO);
    }
}
