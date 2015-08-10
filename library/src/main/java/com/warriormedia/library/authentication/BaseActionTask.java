/**
 * @Project: Framework
 * @Title: BaseActionTask.java
 * @author tan.xx
 * @date 2013-12-12 下午2:24:21
 * @Copyright: 2013 www.paidui.cn Inc. All rights reserved.
 * @version V1.0
 */
package com.warriormedia.library.authentication;

import android.os.AsyncTask;

import com.warriormedia.library.model.ActionModel;
import com.warriormedia.library.util.EvtLog;

/**
 * 网络请求处理任务执行基类
 *
 * @param <T> 动作执行结果
 * @author tan.xx
 * @version 2013-12-12 下午2:24:21 tan.xx
 */
public abstract class BaseActionTask<T extends com.warriormedia.library.authentication.BaseActionResult> {

    protected static final String TAG = BaseActionTask.class.getSimpleName();

    protected BaseActionTask() {
    }

    /**
     * 请求回调
     *
     * @param result
     * @param action
     */
    protected abstract void doResultCallBack(T result, ActionModel<?> action);

    /**
     * 自动登录回调(无自动登录，可不实现)
     *
     * @param result 结果
     * @param action 登录前动作
     */
    protected void doAutoLoginCallBack(T result, ActionModel<?> action) {
    }

    /**
     * 返回登录操作
     *
     * @param action 动作
     */
    protected abstract void doLoginCancelAction(final ActionModel<?> action);

    /**
     * 登录请求(无自动登录可以不实现)
     */
    protected T doAutoLoginReq() {
        return null;
    }

    /**
     * 执行网络请求
     *
     * @param action        动作
     * @param isCancelLogin 是否是取消登录
     */
    public void executeTask(ActionModel<?> action, boolean isCancelLogin) {
        new BaseTask(action, isCancelLogin).execute();
    }

    /**
     * 执行自动登录请求
     *
     * @param action 登录 前动作
     */
    public void executeAutoLoginTask(ActionModel<?> action) {
        new AutoLoginTask(action).execute();
    }

    /**
     * 任务执行类
     *
     * @author tan.xx
     */
    public class BaseTask extends AsyncTask<Void, Void, T> {
        // 本次动作
        private ActionModel mAction;
        // 是否是取消登录时
        private boolean mIsCancelLogin;

        /**
         * 构造方法
         *
         * @param action        ActionModel
         * @param isCancelLogin 是否是取消登录使用
         */
        public BaseTask(ActionModel action, boolean isCancelLogin) {
            this.mAction = action;
            this.mIsCancelLogin = isCancelLogin;
        }

        @Override
        protected T doInBackground(Void... params) {
            if (mAction == null) {
                return null;
            }
            if (mIsCancelLogin) {
                // 取消登录
                // 子线程回调
                if (!mAction.isOnUiThreadCallBack()) {
                    doLoginCancelAction(mAction);
                    EvtLog.d(TAG, "--------isOnBackThreadCallBack-- doLoginCancelAction");
                }
                return null;
            } else {
                // 网络请求
                com.warriormedia.library.authentication.IBaseActionListener<T> mListener = mAction.getListener();
                if (mListener == null) {
                    return null;
                }
                T result = mListener.onAsyncRun();
                // 子线程回调
                if (!mAction.isOnUiThreadCallBack()) {
                    doResultCallBack(result, mAction);
                    EvtLog.d(TAG, "--------isOnBackThreadCallBack-- doResultCallBack");
                }
                return result;
            }
        }

        @Override
        protected void onPostExecute(T result) {
            super.onPostExecute(result);
            if (mAction == null) {
                return;
            }
            if (!mAction.isOnUiThreadCallBack()) {
                return;
            }

            // UI线程
            // 取消登录
            if (mIsCancelLogin) {
                doLoginCancelAction(mAction);
                EvtLog.d(TAG, "--------isOnUiThreadCallBack-- doLoginCancelAction");
            } else {
                // UI线程回调
                doResultCallBack(result, mAction);
                EvtLog.d(TAG, "--------isOnUiThreadCallBack-- doResultCallBack");
            }
        }

    }

    /**
     * 自动登录任务类
     *
     * @author tan.xx
     */
    public class AutoLoginTask extends AsyncTask<Void, Void, T> {
        // 登录前动作
        private ActionModel<?> mAction;

        public AutoLoginTask(ActionModel<?> action) {
            this.mAction = action;
        }

        @Override
        protected T doInBackground(Void... params) {
            T result = doAutoLoginReq();
            doAutoLoginCallBack(result, mAction);
            return result;
        }

    }

}
