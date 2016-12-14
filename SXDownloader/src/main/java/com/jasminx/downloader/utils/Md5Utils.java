package com.jasminx.downloader.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加解密
 * Created by sunxiao5 on 2016/12/6.
 */

public class Md5Utils {

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String md5(String text) {
        MessageDigest msgDigest = null;

        try {
            msgDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "System doesn't support MD5 algorithm.");
        }

        try {
            msgDigest.update(text.getBytes("utf-8"));

        } catch (UnsupportedEncodingException e) {

            throw new IllegalStateException(
                    "System doesn't support your  EncodingException.");

        }

        byte[] bytes = msgDigest.digest();

        String md5Str = new String(encodeHex(bytes));

        return md5Str;
    }

    /**
     * 获取文件Md5验证码
     *
     * @param fileName
     * @return
     */
    public static String md5sum(String fileName) {
        try {
            InputStream fis = new FileInputStream(fileName);
            return md5Stream(fis);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取文件Md5验证码
     *
     * @param fis fis
     * @return
     */
    public static String md5Stream(InputStream fis) {
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return new String(encodeHex(md5.digest()));
        } catch (Exception e) {
            return null;
        }
    }

    public static char[] encodeHex(byte[] data) {

        int l = data.length;

        char[] out = new char[l << 1];

        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }

        return out;
    }
}
