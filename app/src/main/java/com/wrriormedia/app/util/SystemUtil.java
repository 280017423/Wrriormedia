package com.wrriormedia.app.util;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.WindowManager;

import com.wrriormedia.library.util.EvtLog;

/**
 * 系统工具类
 *
 * @author zou.sq
 */
public class SystemUtil {

    /**
     * 改变亮度
     *
     * @param activity 当前界面
     * @param percent  0-1之间取值
     */
    public static void changeBrightnessSlide(Activity activity, float percent) {
        EvtLog.d("aaa", "设置亮度：" + percent);
        WindowManager.LayoutParams lpa = activity.getWindow().getAttributes();
        lpa.screenBrightness = percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        activity.getWindow().setAttributes(lpa);
    }

    public static void setStreamVolume(Activity activity, int current) {
        EvtLog.d("aaa", "设置声音：" + current);
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current * max / 10, AudioManager.ADJUST_SAME);
    }

}
