package com.wrriormedia.library.app;

import android.app.Application;

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
}
