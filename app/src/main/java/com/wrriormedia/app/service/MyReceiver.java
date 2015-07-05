package com.wrriormedia.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.pdw.gson.Gson;
import com.wrriormedia.app.business.manager.WifiManager;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.DeleteAdModel;
import com.wrriormedia.app.model.PushAdModel;
import com.wrriormedia.app.model.PushBrightnessModel;
import com.wrriormedia.app.model.PushLogModel;
import com.wrriormedia.app.model.PushVersionModel;
import com.wrriormedia.app.model.SysStatusModel;
import com.wrriormedia.app.model.VolumeModel;
import com.wrriormedia.app.model.WifiModel;
import com.wrriormedia.library.util.StringUtil;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * <p/>
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "JPush";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
        if (null == bundle) {
            return;
        }
        String alert = bundle.getString(JPushInterface.EXTRA_ALERT);
        String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
        if (StringUtil.isNullOrEmpty(alert)) {
            return;
        }
        if ("version".equals(alert) && !StringUtil.isNullOrEmpty(extra)) {
            Gson gson = new Gson();
            PushVersionModel versionModel = gson.fromJson(extra, PushVersionModel.class);
            Intent versionIntent = new Intent(ConstantSet.KEY_EVENT_ACTION_NEW_VERSION);
            versionIntent.putExtra(PushVersionModel.class.getName(), versionModel);
            context.sendBroadcast(versionIntent);
        } else if ("ad".equals(alert) && !StringUtil.isNullOrEmpty(extra)) {
            Gson gson = new Gson();
            PushAdModel pushAdModel = gson.fromJson(extra, PushAdModel.class);
            Intent adIntent = new Intent(ConstantSet.KEY_EVENT_ACTION_NEW_AD);
            adIntent.putExtra(PushAdModel.class.getName(), pushAdModel);
            context.sendBroadcast(adIntent);
        } else if ("text_ad".equals(alert) && !StringUtil.isNullOrEmpty(extra)) {
            Gson gson = new Gson();
            PushAdModel pushAdModel = gson.fromJson(extra, PushAdModel.class);
            Intent adIntent = new Intent(ConstantSet.KEY_EVENT_ACTION_NEW_TEXT_AD);
            adIntent.putExtra(PushAdModel.class.getName(), pushAdModel);
            context.sendBroadcast(adIntent);
        } else if ("delete_ad".equals(alert) && !StringUtil.isNullOrEmpty(extra)) {
            // 用逗号隔开广告id
            Gson gson = new Gson();
            DeleteAdModel deleteAdModel = gson.fromJson(extra, DeleteAdModel.class);
            Intent adIntent = new Intent(ConstantSet.KEY_EVENT_ACTION_DELETE_AD);
            adIntent.putExtra(DeleteAdModel.class.getName(), deleteAdModel);
            context.sendBroadcast(adIntent);
        } else if ("sys_status".equals(alert) && !StringUtil.isNullOrEmpty(extra)) {
            Gson gson = new Gson();
            SysStatusModel sysStatusModel = gson.fromJson(extra, SysStatusModel.class);
            Intent adIntent = new Intent(ConstantSet.KEY_EVENT_ACTION_SYS_STATUS);
            adIntent.putExtra(SysStatusModel.class.getName(), sysStatusModel);
            context.sendBroadcast(adIntent);
        } else if ("log_time".equals(alert) && !StringUtil.isNullOrEmpty(extra)) {
            Gson gson = new Gson();
            PushLogModel sysStatusModel = gson.fromJson(extra, PushLogModel.class);
            Intent adIntent = new Intent(ConstantSet.KEY_EVENT_ACTION_LOG_TIME);
            adIntent.putExtra(PushLogModel.class.getName(), sysStatusModel);
            context.sendBroadcast(adIntent);
        } else if ("brightness".equals(alert) && !StringUtil.isNullOrEmpty(extra)) {
            Gson gson = new Gson();
            PushBrightnessModel sysStatusModel = gson.fromJson(extra, PushBrightnessModel.class);
            Intent adIntent = new Intent(ConstantSet.KEY_EVENT_ACTION_BRITENESS);
            adIntent.putExtra(PushBrightnessModel.class.getName(), sysStatusModel);
            context.sendBroadcast(adIntent);
        } else if ("volume".equals(alert) && !StringUtil.isNullOrEmpty(extra)) {
            Gson gson = new Gson();
            VolumeModel sysStatusModel = gson.fromJson(extra, VolumeModel.class);
            Intent adIntent = new Intent(ConstantSet.KEY_EVENT_ACTION_VOLUME);
            adIntent.putExtra(VolumeModel.class.getName(), sysStatusModel);
            context.sendBroadcast(adIntent);
        } else if ("net".equals(alert) && !StringUtil.isNullOrEmpty(extra)) {
            Gson gson = new Gson();
            WifiModel wifiModel = gson.fromJson(extra, WifiModel.class);
            if (wifiModel != null) {
                new WifiManager().connectWifi(context, wifiModel);
            }
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }
}
