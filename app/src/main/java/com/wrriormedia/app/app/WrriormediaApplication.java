package com.wrriormedia.app.app;


import com.pdw.gson.Gson;
import com.wrriormedia.app.business.manager.LogManager;
import com.wrriormedia.app.common.ServerAPIConstant;
import com.wrriormedia.app.util.CrashHandler;
import com.wrriormedia.app.util.DBUtil;
import com.wrriormedia.library.app.HtcApplicationBase;
import com.wrriormedia.library.http.DefaultPDWHttpClient;
import com.wrriormedia.library.imageloader.cache.disc.naming.Md5FileNameGenerator;
import com.wrriormedia.library.imageloader.core.ImageLoader;
import com.wrriormedia.library.imageloader.core.ImageLoaderConfiguration;
import com.wrriormedia.library.util.NetUtil;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * 全局应用程序
 */
public class WrriormediaApplication extends HtcApplicationBase {
    public static final int THREAD_POOL_SIZE = 3;
    public static final int MEMORY_CACHE_SIZE = 1500000;

    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        initImageLoader();
        // 打开数据库
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBUtil.getDataManager().firstOpen();
            }
        }).start();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }

    /**
     * This configuration tuning is custom. You can tune every option, you may
     * tune some of them, or you can create default configuration by
     * ImageLoaderConfiguration.createDefault(this); method.
     */
    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .threadPoolSize(THREAD_POOL_SIZE).threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(MEMORY_CACHE_SIZE).denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                        // .enableLogging() // Not necessary in common
                .build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public void savaLog(String url, List<NameValuePair> postParams, String errorInfo) {
        String uploadUrl = ServerAPIConstant.getAPIUrl(ServerAPIConstant.ACTION_LOG_UPLOAD);
        if (url.equals(uploadUrl)) {
            return;
        }
        ArrayList<String> logList = new ArrayList<>();
        logList.add(url);
        logList.add(DefaultPDWHttpClient.buildContent(postParams));
        logList.add(NetUtil.isWifi(WrriormediaApplication.getInstance().getBaseContext()) ? "wifi" : "3g");
        logList.add(errorInfo);
        LogManager.saveLog(3, new Gson().toJson(logList));
    }
}
