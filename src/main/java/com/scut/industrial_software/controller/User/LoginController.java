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
        
        try {
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
            
        } catch (RuntimeException e) {
            // 处理分布式锁获取失败等运行时异常
            String message = e.getMessage();
            if (message != null && message.contains("分布式锁")) {
                log.info("用户 {} 并发登录被拒绝: {}", dto.getUsername(), message);
                return ApiResult.failed("登录操作正在进行中，请稍后再试");
            } else {
                log.info("用户 {} 登录失败: {}", dto.getUsername(), message);
                return ApiResult.failed(message != null ? message : "登录失败");
            }
        } catch (Exception e) {
            log.error("用户 {} 登录过程中发生异常", dto.getUsername(), e);
            return ApiResult.failed("登录失败，请稍后重试");
        }
    }
}
