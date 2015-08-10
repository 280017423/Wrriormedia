package com.warriormedia.app.business.manager;


import android.content.Context;

import com.warriormedia.app.app.WarriormediaApplication;
import com.warriormedia.app.common.ConstantSet;
import com.warriormedia.app.model.WifiModel;
import com.warriormedia.app.util.SharedPreferenceUtil;
import com.warriormedia.app.util.WifiAdmin;
import com.warriormedia.library.util.StringUtil;

public class SystemManager {

    public static long getModifyTime(String modifyTimeKey) {
        int modifyTime = SharedPreferenceUtil.getIntegerValueByKey(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, modifyTimeKey);
        if (-1 == modifyTime) {
            return System.currentTimeMillis() / 1000;
        }
        return modifyTime;
    }

    public static void setModifyTime(String modifyTimeKey) {
        SharedPreferenceUtil.saveValue(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, modifyTimeKey, (int) (System.currentTimeMillis() / 1000));
    }

    public static boolean connectWifi(Context context, WifiModel wifiModel) {
        WifiAdmin mWifiAdmin = new WifiAdmin(context);
        if (null == wifiModel) {
            mWifiAdmin.closeWifi();
            return true;
        }
        if (!StringUtil.isNullOrEmpty(wifiModel.getSsid())) {
            mWifiAdmin.openWifi();
            return mWifiAdmin.addNetwork(wifiModel.getSsid(), wifiModel.getPassword(), wifiModel.getType());
        } else {
            mWifiAdmin.closeWifi();
            return true;
        }
    }
}