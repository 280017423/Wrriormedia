package com.wrriormedia.app.business.manager;


import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.common.ServerAPIConstant;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.library.util.StringUtil;

public class SystemManager {

    public static String getModifyTime() {
        String modifyTime = SharedPreferenceUtil.getStringValueByKey(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ServerAPIConstant.ACTION_KEY_MODIFY);
        if (StringUtil.isNullOrEmpty(modifyTime)) {
            return ""+(System.currentTimeMillis() / 1000);
        }
        return modifyTime;
    }

    public static String setModifyTime(String modifyTime) {
        SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ServerAPIConstant.ACTION_KEY_MODIFY, modifyTime);
    }
}
