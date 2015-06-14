/**
 * @Project: Framework
 * @Title: ActionObject.java
 * @author tan.xx
 * @date 2013-12-17 下午4:18:52
 * @Copyright: 2013 www.paidui.cn Inc. All rights reserved.
 * @version V1.0
 */
package com.wrriormedia.library.model;

import android.app.Activity;
import android.content.Intent;

import com.wrriormedia.library.authentication.BaseLoginProcessor.LOGIN_TYPE;
import com.wrriormedia.library.authentication.IBaseActionListener;

import java.util.UUID;

/**
 * 记录动作信息类
 * 
 * @author tan.xx
 * @param <T>
 *            动作回调
 */
@SuppressWarnings({ "rawtypes" })
public class ActionModel<T extends IBaseActionListener> {
	// 唯一标识
	private String mIdentify;
	// 接口回调
	private T mListener;
	// 跳转目的
	private Intent mIntent;
	// 当前Activity
	private Activity mActivity;
	// 登录类型
	private LOGIN_TYPE mLoginType;
	// 是否在UI线程中回调
	private boolean mIsOnUiThreadCallBack;
	// 是否是自动登陆后
	private boolean mIsAfterAutoLogin;

	/**
	 * 构造方法
	 * 
	 * @param context
	 *            上下文
	 * @param listener
	 *            回调
	 * @param intent
	 *            目的界面
	 * @param isOnUiThreadCallBack
	 *            是否在UI线程回调
	 */
	public ActionModel(Activity context, T listener, Intent intent, boolean isOnUiThreadCallBack) {
		this.mActivity = context;
		this.mListener = listener;
		this.mIntent = intent;
		this.mIdentify = UUID.randomUUID().toString() + System.currentTimeMillis();
		this.mIsOnUiThreadCallBack = isOnUiThreadCallBack;
	}

	/**
	 * @return the identify
	 */
	public String getIdentify() {
		return mIdentify;
	}

	/**
	 * @param identify
	 *            the identify to set
	 */
	public void setIdentify(String identify) {
		mIdentify = identify;
	}

	/**
	 * @return the mListener
	 */
	public T getListener() {
		return mListener;
	}

	/**
	 * @param listener
	 *            the mListener to set
	 */
	public void setListener(T listener) {
		this.mListener = listener;
	}

	/**
	 * @return the intent
	 */
	public Intent getIntent() {
		return mIntent;
	}

	/**
	 * @param intent
	 *            the intent to set
	 */
	public void setIntent(Intent intent) {
		mIntent = intent;
	}

	/**
	 * @return the currentContext
	 */
	public Activity getActivity() {
		return mActivity;
	}

	/**
	 * @param activity
	 *            the mActivity to set
	 */
	public void setActivity(Activity activity) {
		mActivity = activity;
	}

	/**
	 * @return the mLoginType
	 */
	public LOGIN_TYPE getLoginType() {
		return mLoginType;
	}

	/**
	 * @param loginType
	 *            the mLoginType to set
	 */
	public void setLoginType(LOGIN_TYPE loginType) {
		this.mLoginType = loginType;
	}

	/**
	 * @return the mIsOnUiThreadCallBack
	 */
	public boolean isOnUiThreadCallBack() {
		return mIsOnUiThreadCallBack;
	}

	/**
	 * @param isOnUiThreadCallBack
	 *            the mIsOnUiThreadCallBack to set
	 */
	public void setIsOnUiThreadCallBack(boolean isOnUiThreadCallBack) {
		this.mIsOnUiThreadCallBack = isOnUiThreadCallBack;
	}

	public boolean isAfterAutoLogin() {
		return mIsAfterAutoLogin;
	}

	public void setIsAfterAutoLogin(boolean isAfterAutoLogin) {
		this.mIsAfterAutoLogin = isAfterAutoLogin;
	}
}
