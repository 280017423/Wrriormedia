package com.warriormedia.library.http;

import com.warriormedia.library.R;

/**
 * 网络异常
 *
 * @author wang.xy
 */
public class NetworkException extends Exception {

    private static final long serialVersionUID = 4521612743569217432L;
    private static String MESSAGE = com.warriormedia.library.util.PackageUtil.getString(R.string.network_is_not_available);
    private int exceptionCode;

    /**
     * 构造函数
     */
    public NetworkException() {
        super(MESSAGE);
    }

    /**
     * 构造函数
     *
     * @param message 网络异常的内容
     */
    public NetworkException(String message) {
        super(message);
    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(int mExceptionCode) {
        this.exceptionCode = mExceptionCode;
    }
}
