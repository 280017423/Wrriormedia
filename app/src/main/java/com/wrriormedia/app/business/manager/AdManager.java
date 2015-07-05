package com.wrriormedia.app.business.manager;


import android.app.Activity;

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
        if (SharedPreferenceUtil.getBooleanValueByKey(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_LOCK_SCREEN)) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                long current = System.currentTimeMillis() / 1000;
                String whereCase = DownloadModel.START + "<" + current + " AND " + DownloadModel.END + ">" + current;
                List<DownloadModel> downloadModels = DBMgr.getBaseModels(DownloadModel.class, whereCase);
                if (null == downloadModels || downloadModels.isEmpty()) {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NO_AD, null));
                    return;
                }
                DownloadModel mediaModel = downloadModels.get(index % downloadModels.size());
                MediaVideoModel mediaVideoModel = mediaModel.getVideo();
                MediaImageModel mediaImageModel = mediaModel.getImage();
                if (null != mediaVideoModel && !StringUtil.isNullOrEmpty(mediaVideoModel.getMd5())) {
                    if (0 == mediaModel.getIsDownloadFinish()) {
                        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                        return;
                    } else {
                        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_VIDEO, mediaVideoModel));
                    }
                }
                if (null != mediaImageModel && !StringUtil.isNullOrEmpty(mediaImageModel.getMd5())) {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_IMAGE, mediaImageModel));
                }
            }
        }).start();
    }

    public static void getTextAd(int index) {
        if (SharedPreferenceUtil.getBooleanValueByKey(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_LOCK_SCREEN)) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_TEXT_NEXT, null));
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
     * @return 0 正常播放广告; 1 暂停播放，展示默认图;2，亮度调为0
     */
    public static void adStatus(Activity context) {
        CmdModel model = (CmdModel) SharedPreferenceUtil.getObject(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, CmdModel.class);
        if (null == model) {
            setLockScreen(false);
            return;
        }
        switch (model.getSys_status()) {
            case 0:
                SystemUtil.changeBrightnessSlide(context, model.getBrightness() / 10f);// 改变屏幕亮度
                setLockScreen(false);
                break;
            case 1:
                SystemUtil.changeBrightnessSlide(context, model.getBrightness() / 10f);// 改变屏幕亮度
                setLockScreen(true);
                break;
            case 2:
                setLockScreen(true);
                SystemUtil.changeBrightnessSlide(context, 0.01f);
                break;
        }
    }

    public static void setLockScreen(boolean lockScreen) {
        if (lockScreen) {
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_AD_ACTIVITY, false);
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_TEXT_AD_ACTIVITY, false);
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_LOCK_SCREEN, true);
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_LOCK_SCREEN, null));
        } else {
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_AD_ACTIVITY, true);
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_TEXT_AD_ACTIVITY, true);
            if (SharedPreferenceUtil.getBooleanValueByKey(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_LOCK_SCREEN)) {
                SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_IS_LOCK_SCREEN, false);
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_TEXT_NEXT, null));
            }
        }
    }
}