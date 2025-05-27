package com.scut.industrial_software.controller.User;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.UserLoginDTO;
import com.scut.industrial_software.model.vo.LoginResponseVO;
import com.scut.industrial_software.service.IAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LoginController {

    @Autowired
    private IAuthService authService;

    @PostMapping("auth/jsonLogin")
    public ApiResult<LoginResponseVO> login(@RequestBody UserLoginDTO dto) {
        log.info("用户登录，name：{}", dto.getUsername());
        
        // // 1. 验证验证码
        // if (!authService.verifyCode(dto.getKey(), dto.getVerificationCode())) {
        //     return ApiResult.failed("验证码错误或已过期");
        // }

        // 2. 执行登录
        LoginResponseVO responseVO = authService.login(dto);
        if (responseVO == null) {
            return ApiResult.failed("用户名或密码错误");
        }
        
        return ApiResult.success(responseVO);
    }
}
