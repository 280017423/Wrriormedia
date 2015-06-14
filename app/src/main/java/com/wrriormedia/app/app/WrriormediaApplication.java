package com.wrriormedia.app.app;


import com.wrriormedia.app.util.DBUtil;
import com.wrriormedia.library.app.HtcApplicationBase;
import com.wrriormedia.library.imageloader.cache.disc.naming.Md5FileNameGenerator;
import com.wrriormedia.library.imageloader.core.ImageLoader;
import com.wrriormedia.library.imageloader.core.ImageLoaderConfiguration;

/**
 * 全局应用程序
 */
public class WrriormediaApplication extends HtcApplicationBase {
    public static final int THREAD_POOL_SIZE = 3;
    public static final int MEMORY_CACHE_SIZE = 1500000;

    @Override
    public void onCreate() {
        super.onCreate();
        initImageLoader();
        // 打开数据库
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBUtil.getDataManager().firstOpen();
            }
        }).start();
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

}
