package com.github.djaquels.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class StringUtils {

    public static String getMD5(String source) {
        try {
            byte[] bytesOfMessage = source.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytesOfMessage);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String hashValue = sb.toString();
            return hashValue;

        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error getting md5");
            return "";
        }

    }
}
