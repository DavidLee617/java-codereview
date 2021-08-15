package com.warehouse.utils;

import org.springframework.util.DigestUtils;

/**
 * 加密工具类
 */
public class EncryptUtil {

    public static String md5(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    public static String encrypt(String password) {
        return md5("chengfeng" + DigestUtils.md5DigestAsHex(password.getBytes()) + "chengfeng");
    }

}
