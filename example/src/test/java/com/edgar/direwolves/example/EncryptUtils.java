package com.edgar.direwolves.example;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtils {
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String HMACSHA256 = "HMACSHA256";
    public static final String HMACSHA512 = "HMACSHA512";
    public static final String HMACMD5 = "HMACMD5";
    public static final String MD5 = "MD5";

    public EncryptUtils() {
    }

    public static String encryptHmacSha256(String data, String secret) throws IOException {
        return byte2hex(encryptHMAC(data, secret, "HMACSHA256"));
    }

    public static String encryptHmacSha512(String data, String secret) throws IOException {
        return byte2hex(encryptHMAC(data, secret, "HMACSHA512"));
    }

    public static String encryptHmacMd5(String data, String secret) throws IOException {
        return byte2hex(encryptHMAC(data, secret, "HMACMD5"));
    }

    public static byte[] encryptHMAC(String data, String secret, String algorithm) throws IOException {
        Object bytes = null;

        try {
            SecretKeySpec gse = new SecretKeySpec(secret.getBytes("UTF-8"), algorithm);
            Mac mac = Mac.getInstance(gse.getAlgorithm());
            mac.init(gse);
            byte[] bytes1 = mac.doFinal(data.getBytes("UTF-8"));
            return bytes1;
        } catch (GeneralSecurityException var6) {
            throw new IOException(var6.toString());
        }
    }

    public static String encryptMD5(String data) throws IOException {
        MessageDigest md5 = null;

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException var3) {
            return null;
        }

        md5.update(data.getBytes("UTF-8"));
        return byte2hex(md5.digest());
    }

    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();

        for (int i = 0; i < bytes.length; ++i) {
            String hex = Integer.toHexString(bytes[i] & 255);
            if (hex.length() == 1) {
                sign.append("0");
            }

            sign.append(hex.toUpperCase());
        }

        return sign.toString();
    }
}