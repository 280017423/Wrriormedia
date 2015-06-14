package com.wrriormedia.library.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * @author alina
 * 
 */
public class AppUtil {
	private static final String TAG = "AppUtil";

	/**
	 * @param context
	 *            上下文
	 * @param key
	 *            meta data key
	 * @return meta data value
	 */
	public static String getMetaDataByKey(Context context, String key) {
		String result = null;
		try {
			PackageManager packageManager = context.getPackageManager();
			ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);
			if (applicationInfo != null) {
				String appSign = applicationInfo.metaData.getString(key);
				if (appSign != null) {
					result = appSign;
				}
			}
		} catch (Exception ex) {
			com.wrriormedia.library.util.EvtLog.e(TAG, "读app key 失败.");
		}
		return result;
	}

	/**
	 * @param ctx
	 *            上下文
	 * @param intent
	 *            intent对象
	 * @return 是否安装app
	 */
	public static boolean isInstalledApp(Context ctx, Intent intent) {
		boolean flag = false;
		List<ResolveInfo> rList = ctx.getPackageManager().queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
		int telAppSize = rList.size();
		if (telAppSize > 0) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 重启应用(不会关闭进程)
	 * 
	 * @param activity
	 *            当前界面
	 */
	public static void restartApp(Activity activity) {
		if (activity == null) {
			return;
		}
		Intent i = activity.getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(i);
		activity.finish();
	}
}
