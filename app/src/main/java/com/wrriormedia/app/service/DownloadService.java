package com.wrriormedia.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.wrriormedia.app.R;
import com.wrriormedia.app.business.manager.DownloadManager;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.StringUtil;

public class DownloadService extends Service {

    private static final int PROGRESS_MAX = 100;
    private Notification mNotification;
    private RemoteViews mRemoteViews;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        initVariables();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DownloadManager.downTask();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initVariables() {
        EventBus.getDefault().register(this);
        setUpNotification();
    }

    private void setUpNotification() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent mIntent = new Intent();
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker("下载");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(false);
        builder.setOngoing(true);//设置为不可清除模式
        builder.setContentIntent(mPendingIntent);

        mNotification = builder.build();

        mRemoteViews = new RemoteViews(this.getPackageName(), R.layout.notification);
        mRemoteViews.setProgressBar(R.id.progress_horizontal, PROGRESS_MAX, 0, false);
        mRemoteViews.setImageViewResource(R.id.notification_image, R.mipmap.ic_launcher);
        mRemoteViews.setTextViewText(R.id.tip, "下载进度:0%");

        mNotification.contentView = mRemoteViews;
        mNotification.contentIntent = mPendingIntent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onEventMainThread(EventBusModel model) {
        if (null == model || StringUtil.isNullOrEmpty(model.getEventBusAction())) {
            return;
        }
        if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FINISH)) {
            mNotificationManager.cancel(model.getEventId());
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_START)) {
            mNotificationManager.notify(model.getEventId(), mNotification);
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FAILED)) {
            mNotificationManager.cancel(model.getEventId());
            if (null != model.getEventBusObject()) {
                Toast.makeText(this, (String) model.getEventBusObject(), Toast.LENGTH_LONG).show();
            }
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_NORMAL)) {
            long progress = (long) model.getEventBusObject();
            if (progress > PROGRESS_MAX) {
                progress = PROGRESS_MAX;
            }
            mRemoteViews.setProgressBar(R.id.progress_horizontal, PROGRESS_MAX, (int)progress, false);
            mRemoteViews.setImageViewResource(R.id.notification_image, R.mipmap.ic_launcher);
            mRemoteViews.setTextViewText(R.id.tip, "下载进度:" + progress + "%");
            mNotification.contentView = mRemoteViews;
            mNotificationManager.notify(model.getEventId(), mNotification);
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}