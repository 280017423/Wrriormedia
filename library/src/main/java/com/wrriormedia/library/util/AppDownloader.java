package com.wrriormedia.library.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.wrriormedia.library.R;
import com.wrriormedia.library.app.HtcApplicationBase;
import com.wrriormedia.library.model.AppInfo;

import java.io.File;

/**
 * 更多应用下载类
 *
 * @author zeng.hh
 * @version 1.1.0 <br>
 *          2012-7-25 cui.yp 修改通知栏的intent，点击不响应，修改的地方： download<br>
 *          2012-08-15 cui.yp 增加ProgressDialog，当必须升级的时候，需要这个来阻止用户操作<br>
 */
public class AppDownloader {
    private static final int waitInterval = 1000;

    private static final String TAG = "AppDownloader";

    private static final int NOTIFICATION_ID = 1;
    private static final int MSG_DOWNLOAD_APP = 1;
    private static final int PROGRESS_MAX = 100;
    private static boolean ISDOWNLOADING;

    private static double PROGRESS;
    private DownloadProgressListener mDownloadProgressListener;
    private Thread mTmpDownloadThread;
    private Context mContext;
    private String mFilePath;
    // 发通知需要的变量
    private NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;
    private Intent mIntent;
    private PendingIntent mPendingIntent;
    private Notification mNotification = new Notification();
    // private Handler mHandleBar;
    private Handler mHandleBar = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            // * 设置RemoteView组件中进度条的进度值。
            if (PROGRESS > PROGRESS_MAX) {
                PROGRESS = PROGRESS_MAX;
            }
            mRemoteViews.setProgressBar(R.id.progressBar, PROGRESS_MAX, (int) PROGRESS, false);
            mRemoteViews.setTextViewText(R.id.tip, "下载:" + (int) PROGRESS + "%");
            // * 给Notification设置布局。
            mNotification.contentView = mRemoteViews;
            // * 给Notification设置Intent，单击Notification会发出这个Intent。
            mNotification.contentIntent = mPendingIntent;
            // * 发送Notification提醒。
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);

            super.handleMessage(message);
        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_DOWNLOAD_APP:
                    boolean downloadState = (Boolean) msg.obj;
                    if (downloadState) {
                        mRemoteViews.setProgressBar(R.id.progressBar, PROGRESS_MAX, PROGRESS_MAX, false);
                        /**
                         * 设置RemoteView组件中进度条的进度值。
                         */
                        mRemoteViews.setTextViewText(R.id.tip, "Download:" + PROGRESS_MAX + "%");
                        mNotificationManager.cancel(NOTIFICATION_ID);
                        Toast.makeText(mContext, R.string.more_check_version_being_installed, Toast.LENGTH_SHORT)
                                .show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    sleep(AppDownloader.waitInterval);
                                } catch (InterruptedException e) {
                                    com.wrriormedia.library.util.EvtLog.e(TAG, e);
                                }
                                install();
                                ISDOWNLOADING = false;
                            }
                        }.start();
                    } else {
                        Toast.makeText(mContext, R.string.more_check_version_download_install_app_error,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 应用下载
     *
     * @param context                  当前上下文
     * @param downloadProgressListener 下载的进度条通知
     */
    public AppDownloader(Context context, DownloadProgressListener downloadProgressListener) {
        this.mContext = context;
        mDownloadProgressListener = downloadProgressListener;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 应用下载
     *
     * @param appInfo       app信息
     * @param size          app大小
     * @param isUpgradeMust 是否强制升级
     */
    public void download(final AppInfo appInfo, final double size, final boolean isUpgradeMust) {
        // 检查磁盘
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(mContext, R.string.more_check_version_no_sdcard, Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查网络
        if (!com.wrriormedia.library.util.NetUtil.isNetworkAvailable()) {
            Toast.makeText(mContext, R.string.network_is_not_available, Toast.LENGTH_LONG).show();
            return;
        }

        /**
         * 利用notification.xml文件创建RemoteView对象。
         */
        mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification);

        /**
         * 单击Notification时发出的Intent消息。
         * 第二个NotifactionTestActivity为点击进度条时显示的activity
         */
        mIntent = new Intent();
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mPendingIntent = PendingIntent.getActivity(mContext, 0, mIntent, 0);

        // * 设置Notification的Icon图标。
        mNotification.icon = R.drawable.ic_launcher;
        if (appInfo.Bitmap == null) {
            mRemoteViews.setImageViewResource(R.id.notification_image, R.drawable.ic_launcher);
        } else {
            mRemoteViews.setImageViewBitmap(R.id.notification_image, appInfo.Bitmap);
        }
        if (isDownloading()) {
            Toast.makeText(HtcApplicationBase.getInstance().getBaseContext(), R.string.more_apps_is_downloading, Toast.LENGTH_SHORT).show();
        } else {
            mTmpDownloadThread = new Thread() {
                @Override
                public void run() {
                    ISDOWNLOADING = true;
                    File cacheDir;
                    try {
                        cacheDir = com.wrriormedia.library.util.FileUtil.getDownloadDir();
                        boolean result = com.wrriormedia.library.util.FileUtil.downFile(appInfo.AppUrl, cacheDir.getParent(), cacheDir.getName(),
                                isUpgradeMust, new AppDownloadingListener(size));
                        Message message = new Message();
                        message.what = MSG_DOWNLOAD_APP;
                        message.obj = result;
                        mHandler.sendMessage(message);
                    } catch (com.wrriormedia.library.util.MessageException e) {
                        String message = mContext.getResources().getString(
                                R.string.more_check_version_download_file_error)
                                + e.getMessage();
                        Looper.prepare();
                        Toast.makeText(HtcApplicationBase.getInstance().getBaseContext(), message, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        com.wrriormedia.library.util.EvtLog.e(TAG, e);
                    } finally {
                        ISDOWNLOADING = false;
                    }
                }
            };
            mTmpDownloadThread.setPriority(Thread.MIN_PRIORITY);
            mTmpDownloadThread.start();
        }
    }

    /**
     * 下载方法
     *
     * @param appUrl        应用链接地址
     * @param size          应用大小
     * @param isUpgradeMust 是否必须升级
     */
    public void download(final String appUrl, final double size, final boolean isUpgradeMust) {
        AppInfo appInfo = new AppInfo();
        appInfo.AppUrl = appUrl;
        appInfo.Bitmap = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.ic_launcher)).getBitmap();
        download(appInfo, size, isUpgradeMust);
    }

    /**
     * 现在是否正在下载
     *
     * @return 是否成功
     */
    public boolean isDownloading() {
        return ISDOWNLOADING;
    }

    /**
     * 开始执行安装程序
     */
    public void install() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            com.wrriormedia.library.util.EvtLog.d(TAG, "apk path: " + mFilePath);
            File file = new File(mFilePath);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            mContext.startActivity(intent);
        } catch (Exception e) {
            com.wrriormedia.library.util.EvtLog.e(TAG, e);
        }
    }

    private final class AppDownloadingListener implements com.wrriormedia.library.util.FileUtil.OnDownloadingListener {
        private final double mSize;

        private AppDownloadingListener(double size) {
            this.mSize = size;
        }

        @Override
        public void onDownloading(int progressInByte) {
            double totalSize = mSize;
            PROGRESS = ((double) progressInByte / totalSize) * PROGRESS_MAX;
            com.wrriormedia.library.util.EvtLog.d(TAG, progressInByte + "/" + ((int) totalSize) + " " + PROGRESS);
            Message message = new Message();
            mHandleBar.sendMessage(message);
            if (mDownloadProgressListener != null) {
                mDownloadProgressListener.onDownloading((int) PROGRESS, PROGRESS_MAX);
            }
        }

        @Override
        public void onDownloadComplete(String filePath) {
            mFilePath = filePath;
            com.wrriormedia.library.util.EvtLog.d(TAG, "当前路径为:  " + filePath);
            if (mDownloadProgressListener != null) {
                mDownloadProgressListener.onDownloadComplete();
            }
        }

        @Override
        public void onError(boolean isUpgradeMust) {
            com.wrriormedia.library.util.EvtLog.d(TAG, "onError, " + mDownloadProgressListener);
            if (mDownloadProgressListener != null) {
                mDownloadProgressListener.onError(isUpgradeMust);
            }
        }
    }

    /**
     * 下载时显示进度的接口
     *
     * @author colinwang
     */
    public interface DownloadProgressListener {
        /**
         * 正在下载的方法
         *
         * @param progress 当前下载值
         * @param max      最大值
         */
        void onDownloading(int progress, int max);

        /**
         * 下载完成后的回调方法
         */
        void onDownloadComplete();

        /**
         * 出错后的回调方法
         *
         * @param isUpgradeMust 是否必须升级
         */
        void onError(boolean isUpgradeMust);
    }
}
