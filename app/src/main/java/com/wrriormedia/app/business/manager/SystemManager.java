package com.wrriormedia.app.business.manager;


import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.common.ServerAPIConstant;
import com.wrriormedia.app.util.SharedPreferenceUtil;

public class SystemManager {

    public static int getModifyTime() {
        int modifyTime = SharedPreferenceUtil.getIntegerValueByKey(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ServerAPIConstant.ACTION_KEY_MODIFY);
        if (-1 == modifyTime) {
            return (int)System.currentTimeMillis() / 1000;
        }
        return modifyTime;
    }

    public static void setModifyTime() {
        SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ServerAPIConstant.ACTION_KEY_MODIFY, (int)System.currentTimeMillis() / 1000);
    }
}
