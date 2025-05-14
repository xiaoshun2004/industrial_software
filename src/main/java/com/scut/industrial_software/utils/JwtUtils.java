package com.scut.industrial_software.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.Map;

import static com.scut.industrial_software.model.constant.JwtConstants.EXPIRATION_TIME;
import static com.scut.industrial_software.model.constant.JwtConstants.SECRET_KEY;

public class JwtUtils {
    /*// 生成安全的密钥（推荐）
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 12 * 60 * 60 * 1000; // 12小时*/


    /*// 使用固定的密钥字符串
    private static final String SECRET_KEY_STRING = "your-fixed-secret-key-asdfghjkl0202020202";  // 固定的密钥
    private static final long EXPIRATION_TIME = 30 * 60 * 1000; // 30分钟

    // 将固定的密钥字符串转换为 SecretKey
    private static final SecretKey SECRET_KEY = new SecretKeySpec(SECRET_KEY_STRING.getBytes(), SignatureAlgorithm.HS256.getJcaName());*/

    /**
     * 生成 JWT 令牌
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY) // 使用安全密钥
                .compact();
    }

    /**
     * 解析 JWT 令牌
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}