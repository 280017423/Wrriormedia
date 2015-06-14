
package com.wrriormedia.library.authentication;

import android.app.Activity;
import android.content.Intent;

import com.wrriormedia.library.model.ActionModel;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录验证器基类（登录界面设置等在使用者中实现）
 *
 * @param <T> 动作执行结果
 * @author tan.xx
 * @version 2013-12-11 上午10:23:57 tan.xx
 */
public abstract class BaseLoginProcessor<T extends BaseActionResult> implements com.wrriormedia.library.authentication.IBaseLoginListener {
    // 登录动作标识传递key
    public static final String IDENTIFY = "Identify";
    // 登录类型传递
    public static final String KEY_LOGIN_TYPE = "LOGIN_TYPE";
    private static final String TAG = BaseLoginProcessor.class.getSimpleName();
    // 已登录标记
    private static final int ISLOGIN = 1;
    // 未登录标记
    private static final int NOLOGIN = 0;
    // 记录登录状态
    private static int LOGIN_STATUS_CODE;
    // 缓存记录动作 key: identify,value: ActionObject
    @SuppressWarnings("rawtypes")
    private static Map<String, ActionModel> ACTION_MAP;
    // 登录界面类
    private Class<? extends Activity> mLoginActivityClass;

    /**
     * 构造方法
     *
     * @param loginActivityClass 登录界面
     */
    protected BaseLoginProcessor(Class<? extends Activity> loginActivityClass) {
        this.mLoginActivityClass = loginActivityClass;
    }

    /**
     * 是否本地已经登录
     *
     * @return boolean
     */
    protected abstract boolean hasUserLogin();

    /**
     * 执行操作(登录成功或者登录取消后回调)
     *
     * @param action        动作
     * @param isCancelLogin 是否是取消登录
     */
    protected abstract void doAction(final ActionModel<?> action, boolean isCancelLogin);

    /**
     * 执行自动登录
     *
     * @param action 动作
     */
    public void executeAutoLoginTask(ActionModel<?> action) {
    }

    /**
     * @return 返回登陆状态码
     */
    public int getLoginStatus() {
        return LOGIN_STATUS_CODE;
    }

    /**
     * 设置登录状态
     *
     * @param isLogin 是否登录
     */
    public void setLoginStatus(boolean isLogin) {
        if (isLogin) {
            LOGIN_STATUS_CODE = ISLOGIN;
        } else {
            LOGIN_STATUS_CODE = NOLOGIN;
        }
    }

    /**
     * @return 返回本地登陆状态
     */
    public boolean isLogin() {
        return LOGIN_STATUS_CODE == ISLOGIN;
    }

    /**
     * 跳转到登录界面（主动跳转使用）
     *
     * @param activity  Activity
     * @param loginType 登录类型
     */
    public void jumpToLoginActivity(Activity activity, Intent intent, LOGIN_TYPE loginType) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        ActionModel action = new ActionModel(activity, null, null, false);
        action.setLoginType(loginType);
        if (intent != null) {
            action.setIntent(intent);
        }
        processorToLogin(action);
    }

    /**
     * 登录验证跳转界面
     *
     * @param action ActionModel
     */
    public void startActivity(ActionModel action) {
        if (action == null) {
            return;
        }
        Activity activity = action.getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Intent intent = action.getIntent();
        if (intent == null) {
            return;
        }
        if (hasUserLogin()) {
            action.getActivity().startActivity(intent);
        } else {
            // 进入登录
            processorToLogin(action);
        }
    }

    /**
     * 添加动作数据缓存 （未登录时才需要）
     *
     * @param action ActionModel
     */
    private synchronized void addActionObject(ActionModel action) {
        if (action == null) {
            return;
        }
        if (ACTION_MAP == null) {
            ACTION_MAP = new HashMap<>();
        }
        String identify = action.getIdentify();
        if (StringUtil.isNullOrEmpty(identify)) {
            return;
        }
        if (ACTION_MAP.containsKey(identify)) {
            // 存在相同的移除，现在的机制应该不会出现相同的
            ACTION_MAP.remove(identify);
        }
        ACTION_MAP.put(identify, action);
    }

    /**
     * 从缓存中获取动作记录
     *
     * @param identify 动作标识
     * @return ActionObject
     */
    private synchronized ActionModel getActionObject(String identify) {
        EvtLog.d(TAG, "getActionObject identify:" + identify);
        if (StringUtil.isNullOrEmpty(identify) || ACTION_MAP == null) {
            return null;
        }
        return ACTION_MAP.get(identify);
    }

    /**
     * 清除动作缓存记录
     *
     * @param action ActionModel
     */
    public synchronized void removeActionObject(ActionModel action) {
        if (action == null) {
            return;
        }
        String key = action.getIdentify();
        if (StringUtil.isNullOrEmpty(key) || ACTION_MAP == null || !ACTION_MAP.containsKey(key)) {
            return;
        }
        EvtLog.d(TAG, "removeActionObject:" + key);
        ACTION_MAP.remove(key);
    }

    /**
     * 跳转到登录界面处理
     *
     * @param action ActionModel
     */
    public void processorToLogin(ActionModel action) {
        if (action == null) {
            return;
        }
        Activity activity = action.getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        // 记录动作
        addActionObject(action);
        // 暂时认为跳转到登录界面后都为未登录状态
        setLoginStatus(false);
        startToLoginActivity(activity, action.getIdentify(), action.getLoginType());

    }

    /**
     * 获取进入登录界面Intent （ActivityGroup等跳转中使用,需要传递其它参数时使用）
     *
     * @param activity  Activity
     * @param loginType 登录类型
     * @return Intent
     */
    public Intent getLoginIntent(Activity activity, LOGIN_TYPE loginType) {
        if (activity == null || activity.isFinishing()) {
            return null;
        }
        ActionModel action = new ActionModel(activity, null, null, false);
        action.setLoginType(loginType);
        Intent mIntent = new Intent(activity, mLoginActivityClass);
        if (!StringUtil.isNullOrEmpty(action.getIdentify())) {
            mIntent.putExtra(IDENTIFY, action.getIdentify());
        }
        // 记录动作
        addActionObject(action);
        // 暂时认为跳转到登录界面后都为未登录状态
        setLoginStatus(false);
        return mIntent;
    }

    /**
     * 跳转至登录界面
     *
     * @param activity       Activity
     * @param actionIdentify 动作标记
     */
    private void startToLoginActivity(Activity activity, String actionIdentify, LOGIN_TYPE loginType) {
        if (mLoginActivityClass == null) {
            return;
        }
        Intent mIntent = new Intent(activity, mLoginActivityClass);
        if (!StringUtil.isNullOrEmpty(actionIdentify)) {
            mIntent.putExtra(IDENTIFY, actionIdentify);
        }
        if (loginType != null) {
            mIntent.putExtra(KEY_LOGIN_TYPE, loginType);
        }
        activity.startActivity(mIntent);
    }

    /**
     * onLoginSuccess 登录成功
     *
     * @param loginActivity  登录上下文
     * @param actionIdentify 动作标识ID
     */
    @Override
    public void onLoginSuccess(Activity loginActivity, String actionIdentify) {
        // 设置为登录状态
        setLoginStatus(true);
        if (StringUtil.isNullOrEmpty(actionIdentify)) {
            return;
        }
        ActionModel actionObject = getActionObject(actionIdentify);
        if (actionObject == null) {
            return;
        }
        if (actionObject.getLoginType() == null) {
            return;
        }
        // 回调按分支执行
        switch (actionObject.getLoginType()) {
            // 数据请求类型
            case From_GetData_Type:
                // 继续网络请求数据
                doAction(actionObject, false);
                loginActivity.finish();
                // 清除动作记录
                removeActionObject(actionObject);
                break;

            case From_Jump_Activity_Type: // 跳转界面进入登录
            case Exit_To_Cancel_Apk:
                Intent intent = actionObject.getIntent();
                if (intent != null) {
                    // 继续跳转界面
                    loginActivity.startActivity(intent);
                    loginActivity.finish();
                }
                // 清除动作记录
                removeActionObject(actionObject);
                break;
            default:
                // 清除动作记录
                removeActionObject(actionObject);
                break;
        }
    }

    /**
     * onLoginError 登录失败
     *
     * @param loginActivity  动作上下文
     * @param actionIdentify 动作标识
     */
    @Override
    public void onLoginError(Activity loginActivity, String actionIdentify) {
        // 暂无特殊处理
    }

    /**
     * 取消登录
     *
     * @param loginActivity     登录上下文
     * @param actionIdentify    动作标识
     * @param mainActivityClass 主页
     */
    @Override
    public void onLoginCancel(Activity loginActivity, String actionIdentify, Class<? extends Activity> mainActivityClass) {
        if (StringUtil.isNullOrEmpty(actionIdentify)) {
            return;
        }
        ActionModel actionObject = getActionObject(actionIdentify);
        if (actionObject == null) {
            return;
        }
        // 清除动作记录
        removeActionObject(actionObject);
        if (actionObject.getLoginType() == null) {
            return;
        }
        // 回调按分支执行
        switch (actionObject.getLoginType()) {
            case From_GetData_Type: // 获取数据进入登录
                // 执行取消回调
                doAction(actionObject, true);
                break;
            default:
                break;
        }
        // 关闭登录界面
        loginActivity.finish();
    }

    /**
     * 登录类型，可扩展分支
     *
     * @author tan.xx
     * @version 2013-12-12 上午11:35:43 tan.xx
     */
    public enum LOGIN_TYPE {
        From_Jump_Activity_Type, // 跳转界面进入登录
        From_GetData_Type, // 获取数据进入登录
        Exit_To_Cancel_Apk// 如果返回登录，按返回键就要退出时要进入的界面
    }
}
