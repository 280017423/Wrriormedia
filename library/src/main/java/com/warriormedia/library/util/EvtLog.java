package com.warriormedia.library.util;

import android.util.Log;

import com.warriormedia.library.http.NetworkException;

import java.net.BindException;
import java.net.ConnectException;
import java.net.HttpRetryException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;

/**
 * 日志打印工具类
 *
 * @author zou.sq
 */
public class EvtLog {
    public static boolean IS_DEBUG_LOGGABLE = true;
    public static boolean IS_ERROR_LOGGABLE = true;

    static {
        IS_DEBUG_LOGGABLE = com.warriormedia.library.util.PackageUtil.getConfigBoolean("debug_log_enable");
        IS_ERROR_LOGGABLE = com.warriormedia.library.util.PackageUtil.getConfigBoolean("error_log_enable");
    }

    /**
     * 输出debug信息
     *
     * @param tag 标签
     * @param msg 信息
     */
    public static void d(String tag, String msg) {
        if (IS_DEBUG_LOGGABLE) {
            Log.d(tag, msg);
        }
    }

    /**
     * @param tag 标签
     * @param msg 信息
     */
    public static void i(String tag, String msg) {
        if (IS_DEBUG_LOGGABLE) {
            Log.i(tag, msg);
        }
    }

    /**
     * @param tag 标签
     * @param msg 信息
     */
    public static void w(String tag, String msg) {
        if (IS_DEBUG_LOGGABLE) {
            Log.w(tag, msg);
        }
    }

    /**
     * 输出错误信息
     *
     * @param tag       标签
     * @param exception 输出异常信息到控制台
     */
    public static void w(String tag, Throwable exception) {
        if (IS_DEBUG_LOGGABLE) {
            Log.w(tag, exception);
        }
    }

    /**
     * 输出error信息并在程序中toast显示，该错误不会记录在日志文件中
     *
     * @param tag 标签
     * @param msg 信息
     */
    public static void e(String tag, String msg) {
        if (IS_ERROR_LOGGABLE) {
            Log.e(tag, msg);
        }
    }

    /**
     * 输出错误信息
     *
     * @param tag       标签
     * @param exception 异常对象：如果是MessageException，则会抛出提示信息；
     *                  如果是NetworkException，只会在控制台输出；
     *                  如果是其他的Exception，则会记录日志信息，并输出通用的提示信息
     */
    public static void e(String tag, Throwable exception) {
        String message;
        if (IS_ERROR_LOGGABLE) {
            if (exception instanceof com.warriormedia.library.util.MessageException) {
                message = exception.getMessage();
                Log.e(tag, message);
            } else if (exception instanceof NetworkException) {
                message = exception.getMessage();
                Log.w(tag, message);
            } else if (isNetworkException(exception)) {
                if (exception.getMessage() != null) {
                    Log.d(tag, exception.getMessage());
                }
            } else {
                Log.e(tag, "", exception);
            }
        }
    }

    // 根据sdk文档列出的java.net包中会抛出的所有异常，来进行对比
    private static boolean isNetworkException(Throwable exception) {
        return null != exception && (exception instanceof BindException
                || exception instanceof ConnectException
                || exception instanceof HttpRetryException
                || exception instanceof MalformedURLException
                || exception instanceof NoRouteToHostException
                || exception instanceof PortUnreachableException
                || exception instanceof ProtocolException
                || exception instanceof SocketException
                || exception instanceof SocketTimeoutException
                || exception instanceof UnknownHostException
                || exception instanceof UnknownServiceException
                || exception instanceof URISyntaxException);
    }
}
