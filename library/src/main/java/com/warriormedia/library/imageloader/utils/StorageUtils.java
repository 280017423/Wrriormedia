package com.warriormedia.library.imageloader.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.warriormedia.library.imageloader.core.ImageLoader;
import com.warriormedia.library.util.EvtLog;
import com.warriormedia.library.util.PackageUtil;

import java.io.File;
import java.io.IOException;

/**
 * Provides application storage paths
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public final class StorageUtils {

    private static final String INDIVIDUAL_DIR_NAME = "uil-images";

    private StorageUtils() {
    }

    /**
     * 返回应用缓存目录. 如果有SD卡的话就是"/Android/data/[app_package_name]/cache" 否则
     * "/data/data/[app_package_name]/cache"
     *
     * @param context 上下文对象
     * @return 缓存目录
     */
    public static File getCacheDirectory(Context context) {
        File appCacheDir = null;
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            // /data/data/yourPackageName/cache
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    /**
     * 返回应用缓存目录. 如果有SD卡的话就是"/Android/data/[app_package_name]/cache/uil-images"
     * 否则 "/data/data/[app_package_name]/cache/uil-images"
     *
     * @param context 上下文对象
     * @return 缓存图片目录
     */
    public static File getIndividualCacheDirectory(Context context) {
        File cacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(cacheDir, INDIVIDUAL_DIR_NAME);
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = cacheDir;
            }
        }
        return individualCacheDir;
    }

    /**
     * 返回自定义的应用缓存目录.如果有SD卡，将会被创建指定路径的缓存目录否则将返回设备文件系统的缓存目录
     *
     * @param context 上下文对象(e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return 缓存目录
     */
    public static File getOwnCacheDirectory(Context context) {
        File appCacheDir = null;
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), PackageUtil.getConfigString("image_dir"));
        }
        if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
            appCacheDir = context.getCacheDir();
        }
        EvtLog.d(ImageLoader.TAG, "缓存目录是: " + appCacheDir.getAbsolutePath());
        return appCacheDir;
    }

    /**
     * @param context 上下文对象
     * @return 缓存目录
     * @Name getExternalCacheDir
     * @Description 如果有SD卡的话就是"/Android/data/[app_package_name]/cache" 否则返回null
     */
    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            try {
                new File(dataDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                Log.w(ImageLoader.TAG, "不能在外部应用缓存目录创建 \".nomedia\" 文件", e);
            }
            if (!appCacheDir.mkdirs()) {
                Log.w(ImageLoader.TAG, "不能在外部创建缓存目录");
                return null;
            }
        }
        return appCacheDir;
    }
}
