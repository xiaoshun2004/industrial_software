package com.scut.industrial_software.service;

import com.scut.industrial_software.model.dto.UserLoginDTO;
import com.scut.industrial_software.model.vo.LoginResponseVO;

public interface IAuthService {
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录响应
     */
    LoginResponseVO login(UserLoginDTO loginDTO);

    /**
     * 验证验证码
     * @param key 验证码key
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String key, String code);
}