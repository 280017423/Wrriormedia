package com.wrriormedia.library.imageloader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides operations with files
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public final class FileUtils {

    private static final int BUFFER_SIZE = 8 * 1024; // 8 KB

    private FileUtils() {
    }

    /**
     * 复制流
     *
     * @param is 输入流
     * @param os 输出流
     * @throws IOException 异常
     */
    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        while (true) {
            int count = is.read(bytes, 0, BUFFER_SIZE);
            if (count == -1) {
                break;
            }
            os.write(bytes, 0, count);
        }
    }
}
