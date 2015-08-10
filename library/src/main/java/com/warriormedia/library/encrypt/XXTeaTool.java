package com.warriormedia.library.encrypt;

import android.util.Base64;

import com.warriormedia.library.util.StringUtil;

import java.io.UnsupportedEncodingException;

/**
 * xxt加密帮助类
 *
 * @author Zou.sq
 * @version 1.0
 */
public class XXTeaTool {
    /**
     * XT加密字符串算法
     *
     * @param data 待加密的数据
     * @param key  指定的key
     * @return String 加密后的数据
     */
    public static String encrypt(String data, String key) {
        String result = "";
        if (!StringUtil.isNullOrEmpty(data) && !StringUtil.isNullOrEmpty(key)) {
            try {
                result = new String(
                        Base64.encode(encrypt(data.getBytes(), key.getBytes("utf-8")), Base64.DEFAULT), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * XXT加密字节数组算法
     *
     * @param data 待加密的字节数组数据
     * @param key  指定的key
     * @return String 加密后的数据
     */
    public static byte[] encrypt(byte[] data, byte[] key) {
        if (data.length == 0) {
            return data;
        }
        return toByteArray(encrypt(toIntArray(data, true), toIntArray(key, false)), false);
    }

    /**
     * XXT解密字节数组算法
     *
     * @param data 待解密的字节数组数据
     * @param key  指定的key
     * @return byte[] 解密之后的数据
     */
    public static byte[] decrypt(byte[] data, byte[] key) {
        if (data.length == 0) {
            return data;
        }
        return toByteArray(decrypt(toIntArray(data, false), toIntArray(key, false)), true);
    }

    /**
     * XXT加密字节数组算法
     *
     * @param v 待加密的字节数组数据
     * @param k 指定的key
     * @return String 加密后的数据
     */
    public static int[] encrypt(int[] v, int[] k) {
        int n = v.length - 1;

        if (n < 1) {
            return v;
        }
        if (k.length < 4) {
            int[] key = new int[4];

            System.arraycopy(k, 0, key, 0, k.length);
            k = key;
        }
        int z = v[n], y, delta = 0x9E3779B9, sum = 0, e;
        int p, q = 6 + 52 / (n + 1);

        while (q-- > 0) {
            sum = sum + delta;
            e = sum >>> 2 & 3;
            for (p = 0; p < n; p++) {
                y = v[p + 1];
                z = v[p] += (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
            }
            y = v[0];
            z = v[n] += (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
        }
        return v;
    }

    /**
     * XXT解密字节数组算法
     *
     * @param v 待解密的字节数组数据
     * @param k 指定的key
     * @return byte[] 解密之后的数据
     */
    public static int[] decrypt(int[] v, int[] k) {
        int n = v.length - 1;

        if (n < 1) {
            return v;
        }
        if (k.length < 4) {
            int[] key = new int[4];

            System.arraycopy(k, 0, key, 0, k.length);
            k = key;
        }
        int z, y = v[0], delta = 0x9E3779B9, sum, e;
        int p, q = 6 + 52 / (n + 1);

        sum = q * delta;
        while (sum != 0) {
            e = sum >>> 2 & 3;
            for (p = n; p > 0; p--) {
                z = v[p - 1];
                y = v[p] -= (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
            }
            z = v[n];
            y = v[0] -= (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
            sum = sum - delta;
        }
        return v;
    }

    /**
     * Convert byte array to int array.
     *
     * @param data          字节数组
     * @param includeLength 是否包含长度
     * @return int[] 处理过后的数据
     */
    private static int[] toIntArray(byte[] data, boolean includeLength) {
        int n = (((data.length & 3) == 0) ? (data.length >>> 2) : ((data.length >>> 2) + 1));
        int[] result;

        if (includeLength) {
            result = new int[n + 1];
            result[n] = data.length;
        } else {
            result = new int[n];
        }
        n = data.length;
        for (int i = 0; i < n; i++) {
            result[i >>> 2] |= (0x000000ff & data[i]) << ((i & 3) << 3);
        }
        return result;
    }

    /**
     * Convert int array to byte array.
     *
     * @param data          int array
     * @param includeLength length
     * @return byte array.
     */
    private static byte[] toByteArray(int[] data, boolean includeLength) {
        int n = data.length << 2;
        if (includeLength) {
            int m = data[data.length - 1];

            if (m > n) {
                return null;
            } else {
                n = m;
            }
        }
        byte[] result = new byte[n];

        for (int i = 0; i < n; i++) {
            result[i] = (byte) ((data[i >>> 2] >>> ((i & 3) << 3)) & 0xff);
        }
        return result;
    }
}
