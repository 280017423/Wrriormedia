package com.wrriormedia.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.wrriormedia.app.R;
import com.wrriormedia.app.business.dao.DBMgr;
import com.wrriormedia.app.business.manager.DownloadManager;
import com.wrriormedia.app.business.requst.DeviceRequest;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.DownloadModel;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.StringUtil;

public class DownloadService extends Service {

    public static final String TAG = "DownloadService";

    private static final int PROGRESS_MAX = 100;
    private Notification mNotification;
    private RemoteViews mRemoteViews;
    private NotificationManager mNotificationManager;
    private static final int WHAT_RESTART_DOWNLOAD = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case WHAT_RESTART_DOWNLOAD:
                EvtLog.d("aaa", "10秒倒计时开始下载");
                DownloadManager.downTask();
                break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        initVariables();
    }

    private void initVariables() {
        EventBus.getDefault().register(this);
        setUpNotification();
        timeDownload();
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

    public void onEventMainThread(final EventBusModel model) {
        if (null == model || StringUtil.isNullOrEmpty(model.getEventBusAction())) {
            return;
        }
        if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FINISH)) {
            mNotificationManager.cancel(model.getEventId());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DeviceRequest.update("ad:" + model.getEventId());
                }
            }).start();
            timeDownload();
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_NEXT)) {
            EvtLog.d("aaa", ">>>> DownloadService, 重启定时器.............");
            timeDownload();
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_START)) {
            mNotificationManager.notify(model.getEventId(), mNotification);
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FAILED)) {
            timeDownload();
            mNotificationManager.cancel(model.getEventId());
            if (null != model.getEventBusObject()) {
                Toast.makeText(this, (String) model.getEventBusObject(), Toast.LENGTH_LONG).show();
            }
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_NORMAL)) {
            long progress = (long) model.getEventBusObject();
            if (progress > PROGRESS_MAX) {
                progress = PROGRESS_MAX;
            }
            mRemoteViews.setProgressBar(R.id.progress_horizontal, PROGRESS_MAX, (int) progress, false);
            mRemoteViews.setImageViewResource(R.id.notification_image, R.mipmap.ic_launcher);
            mRemoteViews.setTextViewText(R.id.tip, "下载进度:" + progress + "%");
            mNotification.contentView = mRemoteViews;
            mNotificationManager.notify(model.getEventId(), mNotification);
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_IMAGE)) {
            final DownloadModel downloadModel = (DownloadModel) model.getEventBusObject();
            if (null == downloadModel || null == downloadModel.getImage()) {
                return;
            }
            downloadModel.setIsImageFinish(1);
            DBMgr.saveModel(downloadModel);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DeviceRequest.update("ad:" + downloadModel.getAid() + ":image");
                }
            }).start();
            timeDownload();
        }
    }

    private void timeDownload() {
        /*TimerUtil.startTimer(TAG, 10, 1 * 1000, new TimerUtil.TimerActionListener() {
            @Override
            public void doAction() {
                if (TimerUtil.getTimerTime(TAG) <= 0) {
                    EvtLog.d("aaa", "10秒倒计时开始下载");
                    DownloadManager.downTask();
                    TimerUtil.stopTimer(TAG);
                }
            }
        });*/

        if (mHandler.hasMessages(WHAT_RESTART_DOWNLOAD)) {
            mHandler.removeMessages(WHAT_RESTART_DOWNLOAD);
        }
        mHandler.sendEmptyMessageDelayed(WHAT_RESTART_DOWNLOAD, 10 * 1000);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
