package com.warriormedia.library.app;

import android.app.Application;

import org.apache.http.NameValuePair;

import java.util.List;

/**
 * 全局应用程序
 *
 * @author zou.sq
 * @version <br>
 */
public abstract class HtcApplicationBase extends Application {

    private static HtcApplicationBase instance;

    public static HtcApplicationBase getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public abstract void savaLog(String url, List<NameValuePair> postParams, String errorInfo);
}
