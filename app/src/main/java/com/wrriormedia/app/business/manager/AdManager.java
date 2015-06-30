package com.wrriormedia.app.business.manager;


import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.business.dao.DBMgr;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.AdContentModel;
import com.wrriormedia.app.model.CmdModel;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.app.model.MediaImageModel;
import com.wrriormedia.app.model.MediaModel;
import com.wrriormedia.app.model.MediaVideoModel;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.EvtLog;

import java.util.List;

public class AdManager {

    public static void getPlayAd(int index) {
        EvtLog.d("aaa", "获取要播放的视屏");
        AdContentModel model = DBMgr.getBaseModel(AdContentModel.class, "");
        if (null == model) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
            return;
        }
        List<MediaModel> mediaModels = model.getMedia();
        if (null == mediaModels || mediaModels.isEmpty()) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
            return;
        }
        int size = mediaModels.size();
        for (int i = 0; i < size; i++) {
            MediaModel mediaModel = mediaModels.get(index % size);
            MediaVideoModel mediaVideoModel = mediaModel.getVideo();
            MediaImageModel mediaImageModel = mediaModel.getImage();
            if (null != mediaVideoModel) {
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_VIDEO, mediaVideoModel));
                return;
            }
            if (null != mediaImageModel) {
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_IMAGE, mediaImageModel));
                return;
            }
        }
        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
    }

    /**
     * 获取当前视屏播放状态
     *
     * @return 0 正常播放广告; 1 暂停播放，展示默认图; 2 系统关闭屏幕，停止播放
     */
    public static int getAdStatus() {
        CmdModel model = (CmdModel) SharedPreferenceUtil.getObject(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, CmdModel.class);
        if (null == model) {
            return 1;
        }
        return model.getSys_status();
    }
}