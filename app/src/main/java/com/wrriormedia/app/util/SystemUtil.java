package com.wrriormedia.app.util;

import android.app.Activity;
import android.view.WindowManager;

/**
 * 系统工具类
 *
 * @author zou.sq
 */
public class SystemUtil {

    /**
     * 改变亮度
     *@param activity 当前界面
     * @param percent 0-1之间取值
     */
    public static void changeBrightnessSlide(Activity activity, float percent) {

        WindowManager.LayoutParams lpa = activity.getWindow().getAttributes();
        lpa.screenBrightness = percent;
        if (lpa.screenBrightness > 1.0f){
            lpa.screenBrightness = 1.0f;
        }else if (lpa.screenBrightness < 0.01f){
            lpa.screenBrightness = 0.01f;
        }
        activity.getWindow().setAttributes(lpa);
    }

}
