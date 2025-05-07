package com.scut.industrial_software.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    // 获取当前时间的 DATETIME 格式
    public static String getCurrentDatetime() {
        LocalDateTime now = LocalDateTime.now();  // 获取当前时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  // 定义格式
        return now.format(formatter);  // 格式化为字符串
    }
}
