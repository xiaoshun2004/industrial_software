package com.scut.industrial_software.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 加密密码
    public static String encodePassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    // 校验密码
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    public static void main(String[] args) {
        String password = "123456";
        String hashedPassword = encodePassword(password);
        System.out.println("加密后的密码: " + hashedPassword);

        // 验证密码
        System.out.println("密码匹配: " + matches("123456", hashedPassword));
    }
}