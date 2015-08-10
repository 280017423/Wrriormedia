package com.warriormedia.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.warriormedia.app.model.EventBusModel;
import com.warriormedia.app.model.PushVersionModel;
import com.warriormedia.app.util.DownloadFileThread;
import com.warriormedia.library.eventbus.EventBus;
import com.warriormedia.library.util.StringUtil;

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
