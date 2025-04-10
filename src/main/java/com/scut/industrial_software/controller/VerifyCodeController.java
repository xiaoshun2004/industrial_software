package com.scut.industrial_software.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.scut.industrial_software.common.api.ApiResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@RestController
public class VerifyCodeController {

    @Autowired
    private DefaultKaptcha captchaProducer;

    @GetMapping("/auth/verifyCode")
    public ApiResult<String> getVerifyCode(HttpSession session) throws Exception {
        // 生成验证码文本
        String verifyCode = captchaProducer.createText();
        session.setAttribute("verifyCode", verifyCode);

        // 生成验证码图片
        BufferedImage challenge = captchaProducer.createImage(verifyCode);
        System.out.println(verifyCode );
        // 转为Base64编码
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(challenge, "jpg", byteArrayOutputStream);
        String encodedCaptcha = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        System.out.println(encodedCaptcha );
        // 使用 ApiResult 封装返回结果
        return ApiResult.success(encodedCaptcha);  // 返回Base64编码的验证码图片
    }


}
