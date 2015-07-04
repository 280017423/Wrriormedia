package com.wrriormedia.app.util;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.wrriormedia.app.business.dao.DBMgr;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.DownloadModel;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.app.model.MediaVideoModel;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.FileUtil;
import com.wrriormedia.library.util.MessageException;
import com.wrriormedia.library.util.StringUtil;

import java.io.File;

/**
 * 下载工具类
 *
 * @author zou.sq
 * @since 2013-03-28 zou.sq 添加获取时间戳的方法
 */
public class DownLoadUtil {

    private static HttpUtils mHttpUtils = new HttpUtils();

    public static void download(final DownloadModel model) {
        File downloadDir = null;
        try {
            downloadDir = FileUtil.getDownloadDir();
        } catch (MessageException e) {
            e.printStackTrace();
        }
        MediaVideoModel downLoadVideoModel = model.getVideo();
        String url = downLoadVideoModel.getFirst();
        String fileName = downLoadVideoModel.getFileName();
        if (StringUtil.isNullOrEmpty(url)) {
            url = downLoadVideoModel.getSecond();
        }
        if (StringUtil.isNullOrEmpty(url)) {
            EvtLog.d("aaa", model.getAid() + "的地址为空");
            return;
        }
        File downloadFile = new File(downloadDir, fileName);
        if (downloadFile.exists()) { // 如果存在，先删除
            downloadFile.delete();
        }
        EvtLog.d("aaa", "下载路径：" + downloadFile.getAbsolutePath());
        mHttpUtils.download(url, downloadFile.getAbsolutePath(), true, false, new RequestCallBack<File>() {

            @Override
            public void onStart() {
                EvtLog.d("aaa", model.getAid() + "下载开始");
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_START, null, model.getAid()));
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                long progress = current * 100 / total;
                EvtLog.d("aaa", model.getAid() + "下载开始progress:" + progress);
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_NORMAL, progress, model.getAid()));
            }

            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                EvtLog.d("aaa", model.getAid() + "下载成功");
                // 下载成功就更新本地数据库
                model.setIsDownloadFinish(1);
                DBMgr.saveModel(model);
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FINISH, null, model.getAid()));
            }


            @Override
            public void onFailure(HttpException error, String msg) {
                EvtLog.d("aaa", model.getAid() + "下载失败" + msg);
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FAILED, msg, model.getAid()));
            }
        });
    }

}
