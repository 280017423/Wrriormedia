package com.wrriormedia.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.app.model.PushVersionModel;
import com.wrriormedia.app.util.DownloadFileThread;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.StringUtil;

public class AppUpdateService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        initVariables();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PushVersionModel model = (PushVersionModel) intent.getSerializableExtra(PushVersionModel.class.getName());
        if (null != model && !StringUtil.isNullOrEmpty(model.getUrl())) {
            new DownloadFileThread(model).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initVariables() {
        EventBus.getDefault().register(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onEventMainThread(EventBusModel model) {
        if (null == model || StringUtil.isNullOrEmpty(model.getEventBusAction())) {
            return;
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
