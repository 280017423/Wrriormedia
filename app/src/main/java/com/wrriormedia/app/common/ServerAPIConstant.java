package com.wrriormedia.app.common;

import com.wrriormedia.library.app.HtcApplicationBase;
import com.wrriormedia.library.util.AppUtil;

public class ServerAPIConstant {
    public static final String API_ROOT_URL = "api_root_url";

    // Action字段
    public static final String ACTION_READY = "/ready";

    // KEY字段
    public static final String ACTION_KEY_ID = "id";
    public static final String ACTION_KEY_SIM = "sim";

    /**
     * 获取后端的 api URL地址
     *
     * @param actions 方法的子路径
     * @return 返回后端的 api URL地址
     */
    public static String getAPIUrl(String actions) {
        return getApiRootUrl() + actions;
    }

    /**
     * 获取接口地址
     *
     * @return String API的基本地址
     */
    private static String getApiRootUrl() {
        return AppUtil.getMetaDataByKey(HtcApplicationBase.getInstance().getBaseContext(), API_ROOT_URL);
    }
}
