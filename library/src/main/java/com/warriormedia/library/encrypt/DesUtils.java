package com.warriormedia.library.encrypt;

import org.apache.commons.codec.binary.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Des加密工具类
 *
 * @author zou.sq
 */
public class DesUtils {
    public static final String ALGORITHM_DES = "DES/ECB/PKCS7Padding";

    /**
     * 加密字符串
     *
     * @param key  加密key
     * @param data 加密字符串
     * @return String 加密后的字符串
     * @throws Exception 加密异常
     */
    public static String encode(String key, String data) throws Exception {
        return encode(data.getBytes("UTF-8"), key);
    }

    /**
     * 加密字节数组
     *
     * @param key  加密key
     * @param data 加密字节数组
     * @return String 加密后的字符串
     * @throws Exception 加密异常
     */
    public static String encode(byte[] data, String key) throws Exception {
        try {
            DESKeySpec dks = new DESKeySpec(key.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] bytes = cipher.doFinal(data);
            return new String(Base64.encodeBase64(bytes));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * 加密字节数组
     *
     * @param key  加密key的字节数组
     * @param data 加密字节数组
     * @return String 加密后的字符串
     * @throws Exception 加密异常
     */
    public static String encode(byte[] data, byte[] key) throws Exception {
        try {
            DESKeySpec dks = new DESKeySpec(key);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] bytes = cipher.doFinal(data);
            return new String(Base64.encodeBase64(bytes));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * 解密字节数组
     *
     * @param key  加密key
     * @param data 解密字节数组
     * @return String 解密后的字节数组
     * @throws Exception 解密异常
     */
    private static byte[] decode(byte[] data, String key) throws Exception {
        try {
            DESKeySpec dks = new DESKeySpec(key.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * 解密字符串
     *
     * @param key  解密key
     * @param data 已经加密的字符串
     * @return String 解密后的字符串
     */
    public static String decode(String key, String data) {
        byte[] datas;
        String value;
        try {
            if (System.getProperty("os.name") != null
                    && ("sunos".equalsIgnoreCase(System.getProperty("os.name")) || "linux".equalsIgnoreCase(System
                    .getProperty("os.name")))) {
                datas = decode(Base64.decodeBase64(data.getBytes()), key);
            } else {
                datas = decode(Base64.decodeBase64(data.getBytes()), key);
            }
            value = new String(datas);
        } catch (Exception e) {
            value = "";
        }
        return value;
    }
}
