package com.wrriormedia.app.business.manager;


import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.util.SharedPreferenceUtil;

public class SystemManager {

    public static long getModifyTime(String modifyTimeKey) {
        int modifyTime = SharedPreferenceUtil.getIntegerValueByKey(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, modifyTimeKey);
        if (-1 == modifyTime) {
            return System.currentTimeMillis() / 1000;
        }
        return modifyTime;
    }

    public static void setModifyTime(String modifyTimeKey) {
        SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, modifyTimeKey, (int) System.currentTimeMillis() / 1000);
    }
}
