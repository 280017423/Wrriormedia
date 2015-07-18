package com.wrriormedia.app.business.manager;


import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.PowerManager;

import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.business.dao.DBMgr;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.CmdModel;
import com.wrriormedia.app.model.DownloadModel;
import com.wrriormedia.app.model.DownloadTextModel;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.app.model.MediaImageModel;
import com.wrriormedia.app.model.MediaVideoModel;
import com.wrriormedia.app.model.TextModel;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.app.util.SystemUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.FileUtil;
import com.wrriormedia.library.util.MessageException;
import com.wrriormedia.library.util.StringUtil;

import java.io.File;
import java.util.List;

public class AdManager {
    /**
     * 获取要播放的广告
     *
     * @param index 播放索引
     */
    public static void getPlayAd(final int index) {
        EvtLog.d("aaa", "循环播放...");
        if (SharedPreferenceUtil.getBooleanValueByKey(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_LOCK_SCREEN)) {
            new CountDownTimer(10 * 1000, 10 * 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                }
            }.start();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                long current = System.currentTimeMillis() / 1000;
                String where = DownloadModel.IS_DOWNLOAD_FINISH + " = 1 OR " + DownloadModel.IS_IMAGE_FINISH + " = 1 ";
                List<DownloadModel> downloadModels = DBMgr.getBaseModels(DownloadModel.class, where);
                if (null == downloadModels || downloadModels.isEmpty()) {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NO_AD, null));
                    return;
                }
                DownloadModel mediaModel = downloadModels.get(index % downloadModels.size());
                MediaVideoModel mediaVideoModel = mediaModel.getVideo();
                MediaImageModel mediaImageModel = mediaModel.getImage();
                if ((null == mediaVideoModel || StringUtil.isNullOrEmpty(mediaVideoModel.getMd5())) && (null == mediaImageModel || StringUtil.isNullOrEmpty(mediaImageModel.getMd5()))) {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                    return;
                }
                if (null != mediaVideoModel && !StringUtil.isNullOrEmpty(mediaVideoModel.getMd5())) {
                    if (0 == mediaModel.getIsDownloadFinish() || current < mediaModel.getStart() || current > mediaModel.getEnd()) {
                        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                        return;
                    } else {
                        if (null != mediaImageModel && !StringUtil.isNullOrEmpty(mediaImageModel.getMd5())) {
                            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_IMAGE, mediaModel));
                        }
                        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_VIDEO, mediaVideoModel));
                    }
                } else {
                    if (null != mediaImageModel && !StringUtil.isNullOrEmpty(mediaImageModel.getMd5()) && current > mediaModel.getStart() && current < mediaModel.getEnd()) {
                        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_IMAGE, mediaModel));
                    }
                }
            }
        }).start();
    }

    public static void getTextAd(int index) {
        if (SharedPreferenceUtil.getBooleanValueByKey(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_LOCK_SCREEN)) {
            new CountDownTimer(10 * 1000, 10 * 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_TEXT_NEXT, null));
                }
            }.start();
            return;
        }
        long current = System.currentTimeMillis() / 1000;
        String whereCase = DownloadModel.START + "<" + current + " AND " + DownloadModel.END + ">" + current;
        List<DownloadTextModel> downloadModels = DBMgr.getBaseModels(DownloadTextModel.class, whereCase);
        if (null == downloadModels || downloadModels.isEmpty()) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NO_TEXT_AD, null));
            return;
        }
        DownloadTextModel mediaModel = downloadModels.get(index % downloadModels.size());
        TextModel textModel = mediaModel.getText();
        if (null != textModel && !StringUtil.isNullOrEmpty(textModel.getMsg())) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_TEXT, textModel));
        } else {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_TEXT_NEXT, textModel));
        }

    }

    public static void deleteAd(String aid) {
        DownloadModel model = DBMgr.getBaseModel(DownloadModel.class, DownloadModel.WHERE_CASE_SUB + " = " + aid);
        if (null == model) {
            return;
        }
        File downloadDir = null;
        try {
            downloadDir = FileUtil.getDownloadDir();
        } catch (MessageException e) {
            e.printStackTrace();
        }
        MediaVideoModel downLoadVideoModel = model.getVideo();
        if (null != downLoadVideoModel) {
            String fileName = downLoadVideoModel.getFileName();
            File downloadFile = new File(downloadDir, fileName);
            if (downloadFile.exists()) { // 如果存在，先删除
                downloadFile.delete();
            }
        }
        DBMgr.delete(DownloadModel.class, DownloadModel.WHERE_CASE_SUB + " = " + aid);
    }

    /**
     * 获取当前视屏播放状态
     *
     * @return 0 正常播放广告; 1 暂停播放，展示默认图;2，休眠
     */
    public static void adStatus(Activity context, PowerManager pm) {
        CmdModel model = (CmdModel) SharedPreferenceUtil.getObject(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, CmdModel.class);
        if (null == model) {
            setLockScreen(context, false, pm, false);
            return;
        }
        switch (model.getSys_status()) {
            case 0:
                SystemUtil.changeBrightnessSlide(context, model.getBrightness() / 10f);// 改变屏幕亮度
                setLockScreen(context, false, pm, false);
                break;
            case 1:
                SystemUtil.changeBrightnessSlide(context, model.getBrightness() / 10f);// 改变屏幕亮度
                setLockScreen(context, false, pm, true);
                break;
            case 2:
                setLockScreen(context, true, pm, false);
                //SystemUtil.changeBrightnessSlide(context, 0.01f);
                break;
        }
    }

    public static void setLockScreen(Activity context, boolean lockScreen, PowerManager pm, boolean showDefaultPic) {
        if (lockScreen) {
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_AD_ACTIVITY, false);
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_TEXT_AD_ACTIVITY, false);
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_LOCK_SCREEN, true);
            //pm.goToSleep(SystemClock.uptimeMillis());
            Intent intent = new Intent("com.wrriormedia.app.sleep");
            context.sendBroadcast(intent);
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PAUSE_PLAY, null));
        } else {
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_AD_ACTIVITY, true);
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_TEXT_AD_ACTIVITY, true);
            if (SharedPreferenceUtil.getBooleanValueByKey(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_LOCK_SCREEN)) {
                SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_LOCK_SCREEN, false);
                //pm.wakeUp(SystemClock.uptimeMillis());
                Intent intent = new Intent("com.wrriormedia.app.wakeup");
                context.sendBroadcast(intent);
                //EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                //EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_TEXT_NEXT, null));
            }
            if (showDefaultPic) {
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_LOCK_SCREEN, null));
            } else {
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_RESTART_PLAY, null));
            }
        }
    }
}