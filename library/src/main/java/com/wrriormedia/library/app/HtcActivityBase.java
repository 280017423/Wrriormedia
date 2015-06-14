package com.wrriormedia.library.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.PackageUtil;
import com.wrriormedia.library.util.StringUtil;

/**
 * 所有应用Activity的基类
 *
 * @author zou.sq
 * @version <br>
 *          2013-08-27, huang.b 修改toast方法，内部文字为居中显示；<Br>
 */
public abstract class HtcActivityBase extends Activity {
    private static final String TAG = "HtcActivityBase";
    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EvtLog.d(TAG, "onCreate start... ");
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        EvtLog.e(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        EvtLog.e(TAG, "onRestoreInstanceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 默认的toast方法，该方法封装下面的两点特性：<br>
     * 1、只有当前activity所属应用处于顶层时，才会弹出toast；<br>
     * 2、默认弹出时间为 Toast.LENGTH_SHORT;
     *
     * @param msg 弹出的信息内容
     */
    public void toast(final String msg) {
        if (PackageUtil.isTopApplication(this)) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!StringUtil.isNullOrEmpty(msg)) {
                        Toast toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        // 用来防止某些系统自定义了消息框
                        if (tv != null) {
                            tv.setGravity(Gravity.CENTER);
                        }
                        toast.show();
                    }
                }
            });
        }
    }
}
