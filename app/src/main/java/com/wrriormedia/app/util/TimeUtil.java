package com.wrriormedia.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类
 *
 * @author zou.sq
 * @since 2013-03-28 zou.sq 添加获取时间戳的方法
 */
public class TimeUtil {

    public static void setSystemTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        String dateTime = sdf.format(new Date(time));
        String[] timeList = dateTime.split("-");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, Integer.parseInt(timeList[0]));
        c.set(Calendar.MONTH, Integer.parseInt(timeList[1]));
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeList[2]));
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeList[3]));
        c.set(Calendar.MINUTE, Integer.parseInt(timeList[4]));
        c.set(Calendar.SECOND, Integer.parseInt(timeList[5]));
        long when = c.getTimeInMillis();
        if (when / 1000 < Integer.MAX_VALUE) {
            // 等作为系统应用正式测试时再打开
            // ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

}
