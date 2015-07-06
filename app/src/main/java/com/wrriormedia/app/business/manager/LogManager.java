package com.wrriormedia.app.business.manager;


import com.pdw.gson.Gson;
import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.business.dao.DBMgr;
import com.wrriormedia.app.business.requst.DeviceRequest;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.app.model.LogModel;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.TimerUtil;

import java.util.List;

public class LogManager {

    public static void saveLog(int type, String logInfo) {
        LogModel model = new LogModel();
        model.setLogTime((int) (System.currentTimeMillis() / 1000));
        model.setLogType(type);
        model.setLog(logInfo);
        DBMgr.saveModel(model);
    }

    public static void timeUploadLog() {
        int logTime = SharedPreferenceUtil.getIntegerValueByKey(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_LOG_TIME);
        if (0 != logTime) {
            upload(logTime);
        }
    }

    private static void upload(final int logTime) {
        TimerUtil.stopTimer(LogManager.class.getName());
        TimerUtil.startTimer(LogManager.class.getName(), logTime, 1000, new TimerUtil.TimerActionListener() {
            @Override
            public void doAction() {
                if (TimerUtil.getTimerTime(LogManager.class.getName()) > 0) {
                    EvtLog.d("aaa", "" + TimerUtil.getTimerTime(LogManager.class.getName()));
                    return;
                }
                TimerUtil.stopTimer(LogManager.class.getName());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<LogModel> logModels = DBMgr.getBaseModels(LogModel.class, "", 50 + "");
                        String logInfo = "";
                        if (null != logModels && !logModels.isEmpty()) {
                            Gson gson = new Gson();
                            logInfo = gson.toJson(logModels);
                        }
                        ActionResult result = DeviceRequest.uploadLog(logInfo);
                        if (ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
                            if (null != logModels && !logModels.isEmpty()) {
                                for (int i = 0; i < logModels.size(); i++) {
                                    EvtLog.d("aaa", "" + logModels.get(i).getID());
                                    DBMgr.delete(LogModel.class, logModels.get(i).getID());
                                }
                            }
                        }
                        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_LOG_TIMER, null));
                    }
                }).start();
            }
        });
    }
}