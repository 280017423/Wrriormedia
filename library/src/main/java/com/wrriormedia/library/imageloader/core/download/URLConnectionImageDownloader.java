package com.wrriormedia.library.imageloader.core.download;

import com.wrriormedia.library.imageloader.core.assist.FlushedInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

/**
 * Default implementation of ImageDownloader. Uses {@link URLConnection} for
 * image stream retrieving.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class URLConnectionImageDownloader extends ImageDownloader {

    /**
     * {@value}
     */
    public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
    /**
     * {@value}
     */
    public static final int DEFAULT_HTTP_READ_TIMEOUT = 20 * 1000; // milliseconds

    private int connectTimeout;
    private int readTimeout;

    public URLConnectionImageDownloader() {
        this(DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);
    }

    public URLConnectionImageDownloader(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public InputStream getStreamFromNetwork(URI imageUri) throws IOException {
        URLConnection conn = imageUri.toURL().openConnection();
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        return new FlushedInputStream(new BufferedInputStream(conn.getInputStream()));
    }
}