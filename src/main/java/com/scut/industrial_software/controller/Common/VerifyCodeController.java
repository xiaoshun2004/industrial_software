package com.scut.industrial_software.controller.Common;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.scut.industrial_software.common.api.ApiResult;
import jakarta.servlet.http.HttpSession;
import com.scut.industrial_software.model.constant.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
@RestController
public class VerifyCodeController {

    @Autowired
    private DefaultKaptcha captchaProducer;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成验证码并返回给前端，同时将验证码和传入的 key 存储到 Redis
     * @param key 前端传递的 key
     * @return 验证码图片的 Base64 编码
     */
    @GetMapping("/auth/verifyCode")
    public ApiResult<String> getVerifyCode(@RequestParam String key) throws Exception {
        // 生成验证码文本
        String verifyCode = captchaProducer.createText();

        // 保存验证码到 Redis，设置过期时间为5分钟
        redisTemplate.opsForValue().set(RedisConstants.VERIFY_CODE_PREFIX + key, verifyCode, RedisConstants.VERIFY_CODE_TTL, TimeUnit.MINUTES);

        // 生成验证码图片
        BufferedImage challenge = captchaProducer.createImage(verifyCode);

        // 转为Base64编码
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(challenge, "jpg", byteArrayOutputStream);
        String encodedCaptcha = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

        // 返回Base64编码的验证码图片
        return ApiResult.success(encodedCaptcha);  // 返回Base64编码的验证码图片
    }
}



