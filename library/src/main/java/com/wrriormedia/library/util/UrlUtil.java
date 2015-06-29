package com.wrriormedia.library.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.apache.http.NameValuePair;

import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

/**
 * 处理Url的工具类
 *
 * @author zou.sq
 */
public class UrlUtil {
    private static final String TAG = "UrlUtil";

    /**
     * 拼接Url和请求参数
     *
     * @param url       url地址
     * @param getParams <NameValuePair> getParams 请求参数
     * @return url地址
     */
    public static String buildUrl(String url, List<NameValuePair> getParams) {
        String returnUrl = url;
        if (getParams != null && !getParams.isEmpty()) {
            if (returnUrl.endsWith("/")) {
                returnUrl = returnUrl.substring(0, returnUrl.length() - 1);
            }
            if (!returnUrl.contains("?")) {
                returnUrl = returnUrl + "?";
            }
            returnUrl = returnUrl + buildContent(getParams);
            return returnUrl;
        }
        return returnUrl;
    }

    /**
     * 组装参数
     *
     * @param getParams <NameValuePair> getParams 参数集合
     * @return String 组装后的参数
     */
    public static String buildContent(List<NameValuePair> getParams) {
        String content = "";
        String tempParamters = "";
        for (int i = 0; i < getParams.size(); i++) {
            NameValuePair nameValuePair = getParams.get(i);
            if (nameValuePair != null) {
                String key = com.wrriormedia.library.util.StringUtil.isNullOrEmpty(nameValuePair.getName()) ? "" : nameValuePair.getName();
                String value = com.wrriormedia.library.util.StringUtil.isNullOrEmpty(nameValuePair.getValue()) ? "" : nameValuePair.getValue();
                tempParamters = tempParamters + "&" + key + "=" + URLEncoder.encode(value);
            }
        }
        if (tempParamters.length() > 1) {
            content = tempParamters.substring(1);
        } else {
            content = tempParamters;
        }
        return content;
    }

    public static String getSignString(List<NameValuePair> getParams) {
        if (getParams == null) {
            return "";
        }
        String content;
        String tempParams = "";
        for (int i = 0; i < getParams.size(); i++) {
            NameValuePair nameValuePair = getParams.get(i);
            if (nameValuePair != null) {
                String key = com.wrriormedia.library.util.StringUtil.isNullOrEmpty(nameValuePair.getName()) ? "" : nameValuePair.getName();
                String value = com.wrriormedia.library.util.StringUtil.isNullOrEmpty(nameValuePair.getValue()) ? "" : nameValuePair.getValue();
                tempParams = tempParams + "&" + key + "=" + value;
            }
        }
        if (tempParams.length() > 1) {
            content = tempParams.substring(1);
        } else {
            content = tempParams;
        }
        com.wrriormedia.library.util.EvtLog.d(TAG, "content:  " + content);
        if (com.wrriormedia.library.util.StringUtil.isNullOrEmpty(content)) {
            return "";
        }
        String sign = com.wrriormedia.library.util.StringUtil.getMd5Hash(content, null);
        if (com.wrriormedia.library.util.StringUtil.isNullOrEmpty(sign)) {
            return "";
        }
        return sign.toUpperCase(Locale.getDefault());
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        boolean result = false;
        if (context != null && intent != null) {
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
            if (resolveInfos != null && resolveInfos.size() > 0) {
                result = true;
            }
        }
        return result;
    }
}
