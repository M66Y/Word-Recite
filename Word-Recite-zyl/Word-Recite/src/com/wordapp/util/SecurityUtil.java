package com.wordapp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 安全工具类
 * 提供密码加密、验证等安全功能
 */
public class SecurityUtil {

    /**
     * 使用SHA-256对密码进行哈希加密
     *
     * @param password 原始密码
     * @return 加密后的哈希字符串（十六进制）
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    /**
     * 验证密码是否与哈希值匹配
     *
     * @param password     原始密码
     * @param passwordHash 存储的哈希值
     * @return 匹配返回true
     */
    public static boolean verifyPassword(String password, String passwordHash) {
        return hashPassword(password).equals(passwordHash);
    }

    /**
     * 生成随机盐值（备用）
     *
     * @return Base64编码的盐值字符串
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}