/**
 * @Project: PMH_Main
 * @Title: ActivityManager.java
 * @Package com.pdw.pmh.library
 * @Description: activity的管理
 * @author huang.b
 * @date 2013-10-8 下午1:44:14
 * @Copyright: 2013 www.paidui.cn Inc. All rights reserved.
 * @version V1.0
 */
package com.wrriormedia.library.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * PMH的Activity管理类
 *
 * @author huang.b
 */
public class QJActivityManager {
    private static String TAG = "PDWActivityManager";
    private static QJActivityManager INSTANCE;

    private Stack<Activity> mActivityStack = new Stack<>();

    private QJActivityManager() {
    }

    /**
     * 单例
     *
     * @return ActivityManager
     * @Method: getActivityManager
     */
    public static QJActivityManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new QJActivityManager();
        }
        return INSTANCE;
    }

    /**
     * @param activity 添加一个Activity
     */
    public void pushActivity(Activity activity) {
        mActivityStack.add(activity);
    }

    /**
     * 得到当前Activity
     *
     * @param @return
     * @return Activity
     */
    public Activity currentActivity() {
        return mActivityStack.lastElement();
    }

    /**
     * 删除Activity
     *
     * @param activity activity
     * @Method: popActivity
     */
    public void popActivity(Activity activity) {
        mActivityStack.remove(activity);
    }

    /**
     * 清除所有Activity 清楚所有Activity除了当前Activity以外
     *
     * @param cls 除了当前Activity以外
     */
    public void popAllExceptOne(Class<?> cls) {
        Activity saveAct = null;
        int size = mActivityStack.size();
        for (int i = 0; i < size; i++) {
            Activity act = mActivityStack.get(i);
            if (act.getClass().equals(cls)) {
                saveAct = act;
                continue;
            }
            act.finish();
        }
        mActivityStack.clear();
        if (saveAct != null) {
            mActivityStack.push(saveAct);
        }
    }

    /**
     * 清除所有Activity 清楚所有Activity除了指定Activitys以外
     *
     * @param mClasses 除了当前Activity以外
     */
    public void popAllExceptOthers(Class<? extends Activity>[] mClasses) {
        int size = mActivityStack.size();
        List<Activity> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Activity act = mActivityStack.get(i);
            boolean isFinish = true;
            for (int j = 0; j < mClasses.length; j++) {
                if (act.getClass().getSimpleName().equals(mClasses[j].getSimpleName())) {
                    list.add(act);
                    isFinish = false;
                    break;
                }
            }
            if (isFinish) {
                act.finish();
            }

        }
        mActivityStack.clear();
        for (Activity activity : list) {
            mActivityStack.push(activity);
        }
    }

    /**
     * 输出所有正在运行的Activity ；
     */
    public void showAll() {
        for (Iterator<Activity> iterator = mActivityStack.iterator(); iterator.hasNext(); ) {
            Activity act = iterator.next();
            EvtLog.d(TAG, act.getClass().getSimpleName());
        }
    }
}
