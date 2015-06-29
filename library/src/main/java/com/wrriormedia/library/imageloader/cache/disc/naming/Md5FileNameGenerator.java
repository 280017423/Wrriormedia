package com.wrriormedia.library.imageloader.cache.disc.naming;

import android.util.Log;

import com.wrriormedia.library.imageloader.core.ImageLoader;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Names image file as MD5 hash of image URI
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class Md5FileNameGenerator implements FileNameGenerator {

    private static final String HASH_ALGORITHM = "MD5";
    private static final int DIGITS = 10;
    private static final int LETTERS = 26;
    private static final int RADIX = DIGITS + LETTERS;

    @Override
    public String generate(String imageUri) {
        byte[] md5 = getMD5(imageUri.getBytes());
        BigInteger bi = new BigInteger(md5).abs();
        return bi.toString(RADIX);
    }

    private byte[] getMD5(byte[] data) {
        byte[] hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(data);
            hash = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            Log.e(ImageLoader.TAG, e.getMessage(), e);
        }
        return hash;
    }
}
