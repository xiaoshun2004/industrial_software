package com.scut.industrial_software;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.scut.industrial_software.common.api.ApiResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.core.type.TypeReference;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class VerifyCodeControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    public void setup() {
        // 构建 MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        // stub Redis ops
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testGetVerifyCode_andSaveImage() throws Exception {
        String key = "testKey";

        // 调用接口
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/auth/verifyCode")
                                .param("key", key)
                )
                .andExpect(status().isOk())
                .andReturn();

        // 解析返回结果
        String json = mvcResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        ApiResult<String> result = mapper.readValue(json, new TypeReference<ApiResult<String>>() {});
        String base64Image = result.getData();
        assertNotNull(base64Image, "应返回 Base64 编码的图片");

        // 解码并写入文件
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        Path output = Path.of("target/verifyCode.jpg");
        Files.createDirectories(output.getParent());
        Files.write(output, imageBytes);
        System.out.println("验证码图片已保存至: " + output.toAbsolutePath());

        // 验证图片有效
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        assertNotNull(img);
        assertTrue(img.getWidth() > 0 && img.getHeight() > 0, "生成的图片应有有效尺寸");
    }

    @TestConfiguration
    static class KaptchaConfig {
        @Bean
        public DefaultKaptcha captchaProducer() {
            // 简单配置一个 Kaptcha 实例
            DefaultKaptcha producer = new DefaultKaptcha();
            Properties props = new Properties();
            props.setProperty("kaptcha.border", "no");
            props.setProperty("kaptcha.textproducer.font.color", "black");
            props.setProperty("kaptcha.image.width", "200");
            props.setProperty("kaptcha.image.height", "50");
            props.setProperty("kaptcha.textproducer.font.size", "40");
            props.setProperty("kaptcha.textproducer.char.length", "5");
            producer.setConfig(new com.google.code.kaptcha.util.Config(props));
            return producer;
        }
    }
}
