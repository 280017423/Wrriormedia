package com.warriormedia.library.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 软键盘的工具类
 */
public class ImeUtil {
    private static final String TAG = "ImeUtil";

    /**
     * 隐藏软键盘1
     *
     * @param act Activity
     */
    public static void hideSoftInput(Activity act) {
        try {
            if (act == null) {
                return;
            }
            final View v = act.getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                // method 1
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                // method 2
                // imm.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(),
                // InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            EvtLog.w(TAG, e);
        }
    }

    /**
     * 隐藏软键盘(实在关闭不了的时候使用)
     *
     * @param act      Activity
     * @param editText EditText
     */
    public static void hideSoftInput(Activity act, EditText editText) {
        try {
            if (act == null || editText == null) {
                return;
            }
            if (editText.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        } catch (Exception e) {
            EvtLog.w(TAG, e);
        }
    }

    /**
     * 显示软键盘1
     *
     * @param act Activity
     */
    public static void showShoftInput(Activity act) {
        try {
            if (act == null) {
                return;
            }
            InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(null, 0);
        } catch (Exception e) {
            EvtLog.w(TAG, e);
        }
    }

    /**
     * 切换软键盘（开- 关，关 - 开）
     *
     * @param context 上下文对象
     */
    public static void showSoftInput(Context context) {
        try {
            InputMethodManager m = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            EvtLog.w(TAG, e);
        }
    }

    /**
     * 软键盘是否显示(小白:测试好像没用，不建议使用。)
     *
     * @param context 上下文对象
     * @return boolean true表示软键盘在显示否则返回false
     */
    public static boolean isSoftInputShow(Context context) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            return imm.isActive();
        } catch (Exception e) {
            EvtLog.w(TAG, e);
        }
        return false;
    }

    /**
     * 显示软键盘2
     *
     * @param Activity 参数
     */
    public static void showInputKeyboard(Activity context) {
        context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    /**
     * 隐藏软键盘2
     *
     * @param Activity 参数
     */
    public static void hideInputKeyboard(Activity context) {
        context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}