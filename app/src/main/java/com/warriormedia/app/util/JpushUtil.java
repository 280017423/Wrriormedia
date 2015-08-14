package com.warriormedia.app.util;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.warriormedia.library.app.HtcApplicationBase;
import com.warriormedia.library.util.EvtLog;
import com.warriormedia.library.util.NetUtil;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class JpushUtil {

    private static JpushUtil mJpushUtil = null;

    private static final String TAG = "JpushUtil";

    private static final int MSG_SET_ALIAS = 1001;
    private static final int MSG_SET_TAGS = 1002;

    private JpushUtil() {

    }

    public static JpushUtil getInstance() {
        if (null == mJpushUtil) {
            mJpushUtil = new JpushUtil();
        }
        return mJpushUtil;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    Log.d(TAG, "Set alias in handler."+ msg.obj);
                    JPushInterface.setAliasAndTags(HtcApplicationBase.getInstance().getBaseContext(), (String) msg.obj, null, mAliasCallback);
                    break;

                case MSG_SET_TAGS:
                    Log.d(TAG, "Set tags in handler.");
                    JPushInterface.setAliasAndTags(HtcApplicationBase.getInstance().getBaseContext(), null, (Set<String>) msg.obj, mTagsCallback);
                    break;

                default:
                    Log.i(TAG, "Unhandled msg - " + msg.what);
            }
        }
    };

    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            Log.d(TAG, "===="+code);
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    EvtLog.i(TAG, logs);
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    EvtLog.i(TAG, logs);
                    if (NetUtil.isNetworkAvailable()) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }
        }

    };

    private final TagAliasCallback mTagsCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    EvtLog.i(TAG, logs);
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    EvtLog.i(TAG, logs);
                    if (NetUtil.isNetworkAvailable()) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_TAGS, tags), 1000 * 60);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    EvtLog.e(TAG, logs);
            }
        }

    };

    public void setAlias(String alias) {
        if (TextUtils.isEmpty(alias)) {
            EvtLog.d(TAG, "别名为空");
            return;
        }
        //调用JPush API设置Alias
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, alias));
    }
}
