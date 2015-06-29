package com.wrriormedia.app.business.manager;


import com.wrriormedia.app.business.dao.DBMgr;
import com.wrriormedia.app.model.DownLoadVideoModel;
import com.wrriormedia.app.model.DownloadModel;
import com.wrriormedia.app.util.DownLoadUtil;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.StringUtil;

import java.util.List;

public class DownloadManager {

    public static void downTask() {
        List<DownloadModel> downloadModels = DBMgr.getHistoryData(DownloadModel.class);
        if (null != downloadModels && !downloadModels.isEmpty()) {
            for (DownloadModel model : downloadModels) {
                if (null != model && null != model.getVideo()) {
                    DownLoadVideoModel downLoadVideoModel = model.getVideo();
                    if (StringUtil.isNullOrEmpty(downLoadVideoModel.getFileName())) {
                        continue;
                    }
                    if (1 != model.getIsDownloadFinish()) {
                        DownLoadUtil.download(model);
                    } else {
                        EvtLog.d("aaa", model.getAid() + "已经下载过");
                    }
                }
            }
        }
    }
}
