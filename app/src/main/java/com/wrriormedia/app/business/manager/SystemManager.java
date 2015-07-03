package com.wrriormedia.app.business.manager;


import android.content.Context;

import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.WifiModel;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.app.util.WifiAdmin;
import com.wrriormedia.library.util.StringUtil;

public class SystemManager {

    public static long getModifyTime(String modifyTimeKey) {
        int modifyTime = SharedPreferenceUtil.getIntegerValueByKey(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, modifyTimeKey);
        if (-1 == modifyTime) {
            return System.currentTimeMillis() / 1000;
        }
        return modifyTime;
    }

    public static void setModifyTime(String modifyTimeKey) {
        SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, modifyTimeKey, (int) (System.currentTimeMillis() / 1000));
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