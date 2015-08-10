package com.warriormedia.app.util;

import com.warriormedia.app.app.WarriormediaApplication;
import com.warriormedia.app.common.ConstantSet;
import com.warriormedia.app.model.CmdModel;
import com.warriormedia.library.util.EvtLog;
import com.warriormedia.library.util.StringUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 *
 * @author zou.sq
 * @since 2013-03-28 zou.sq 添加获取时间戳的方法
 */
public class TimeUtil {

    public static boolean isBetweenTime() {
        CmdModel model = (CmdModel) SharedPreferenceUtil.getObject(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, CmdModel.class);
        if (null == model || StringUtil.isNullOrEmpty(model.getEnd_time()) || StringUtil.isNullOrEmpty(model.getStart_time())) {
            return true;
        }
        EvtLog.d("aaa", model.toString());
        String startTime = model.getStart_time();
        String endTime = model.getEnd_time();
        if (!startTime.contains(":") || !endTime.contains(":")){
            return true;
        }
        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int startMinute = Integer.parseInt(startTime.split(":")[1]);
        int endHour = Integer.parseInt(endTime.split(":")[0]);
        int endMinute = Integer.parseInt(endTime.split(":")[1]);

        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(new Date());

        Calendar min = Calendar.getInstance();
        min.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
        min.set(Calendar.MONTH, currentDate.get(Calendar.MONTH));
        min.set(Calendar.HOUR_OF_DAY, startHour);
        min.set(Calendar.MINUTE, startMinute);
        min.set(Calendar.SECOND, 0);
        min.set(Calendar.MILLISECOND, 0);

        Calendar max = Calendar.getInstance();
        max.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
        max.set(Calendar.MONTH, currentDate.get(Calendar.MONTH));
        max.set(Calendar.HOUR_OF_DAY, endHour);
        max.set(Calendar.MINUTE, endMinute);
        max.set(Calendar.SECOND, 0);
        max.set(Calendar.MILLISECOND, 0);

        if (startHour * 60 + startMinute > endHour * 60 + endMinute) {
            max.set(Calendar.HOUR_OF_DAY, 24 + endHour);
        } else {
            max.set(Calendar.HOUR_OF_DAY, endHour);
        }
        return currentDate.getTimeInMillis() >= min.getTimeInMillis()
                && currentDate.getTimeInMillis() <= max.getTimeInMillis();
    }

}
