package com.wrriormedia.app.business.manager;


import com.wrriormedia.app.business.dao.DBMgr;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.DownloadModel;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.app.model.MediaVideoModel;
import com.wrriormedia.app.util.DownLoadUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.StringUtil;

import java.util.List;

public class DownloadManager {

    public static void downTask() {
        List<DownloadModel> downloadModels = DBMgr.getBaseModels(DownloadModel.class, DownloadModel.IS_DOWNLOAD_FINISH + " = 0");
        if (null != downloadModels && !downloadModels.isEmpty()) {
            int count = 0;
            for (DownloadModel model : downloadModels) {
                count++;
                if (null != model && null != model.getVideo()) {
                    MediaVideoModel downLoadVideoModel = model.getVideo();
                    //MediaImageModel downLoadImageModel = model.getImage();
                    if (null == downLoadVideoModel || StringUtil.isNullOrEmpty(downLoadVideoModel.getFileName())) {
                        /*if (downLoadImageModel != null && !StringUtil.isNullOrEmpty(downLoadImageModel.getMd5()) && 1 != model.getIsImageFinish()) {
                            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_IMAGE, model));
                        }*/
                        if (count == downloadModels.size()) {
                            EvtLog.d("aaa", "没有需要下载的视频，图片或者文本是最后一条广告，重新开启定时器.....................");
                            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_NEXT, null));
                        }
                        continue;
                    }
                    if (1 != model.getIsDownloadFinish()) {
                        DownLoadUtil.download(model);
                        //TODO 一次下载一个，下载完成或者失败就重新检查
                        break;
                    } else {
                        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_NEXT, null));
                    }
                } else {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_NEXT, null));
                }
            }
        } else {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_NEXT, null));
        }
    }
}
