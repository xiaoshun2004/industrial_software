package com.scut.industrial_software.utils;

public class TransFileSizeUtil {
    /**
     * 将文件大小转换为人类可读的格式
     *
     * @param size 文件大小（单位：字节）
     * @return 转换后的文件大小字符串
     */
    public static String transFileSize(Long size) {

        // 如果入参为空或小于0，返回"0 B"
        if (size == null || size < 0) {
            return "0 B";
        }

        // 根据文件大小进行转换
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }

    }

    public static void main(String[] args) {
        System.out.println(transFileSize(1023L));
        System.out.println(transFileSize(1024L));
        System.out.println(transFileSize(1024 * 1024L));
        System.out.println(transFileSize(1024 * 1024 * 1023L));
    }
}
