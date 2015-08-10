package com.warriormedia.app.util;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.pdw.gson.Gson;
import com.warriormedia.app.app.WarriormediaApplication;
import com.warriormedia.app.business.dao.DBMgr;
import com.warriormedia.app.business.manager.LogManager;
import com.warriormedia.app.common.ConstantSet;
import com.warriormedia.app.model.DownloadModel;
import com.warriormedia.app.model.EventBusModel;
import com.warriormedia.app.model.MediaVideoModel;
import com.warriormedia.library.eventbus.EventBus;
import com.warriormedia.library.util.EvtLog;
import com.warriormedia.library.util.FileUtil;
import com.warriormedia.library.util.MessageException;
import com.warriormedia.library.util.NetUtil;
import com.warriormedia.library.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载工具类
 *
 * @author zou.sq
 * @since 2013-03-28 zou.sq 添加获取时间戳的方法
 */
public class DownLoadUtil {

    private static HttpUtils mHttpUtils = new HttpUtils();
    private static int count = 0;

    public static void download(final DownloadModel model) {
        count = 0;
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
            return;
        }
        final File downloadFile = new File(downloadDir, fileName);
        if (downloadFile.exists()) { // 如果存在，先删除
            downloadFile.delete();
        }
        EvtLog.d("aaa", "下载路径：" + downloadFile.getAbsolutePath());
        mHttpUtils.download(url, downloadFile.getAbsolutePath(), true, false, new RequestCallBack<File>() {

            @Override
            public void onStart() {
                MediaVideoModel downLoadVideoModel = model.getVideo();
                String url = downLoadVideoModel.getFirst();
                if (StringUtil.isNullOrEmpty(url)) {
                    url = downLoadVideoModel.getSecond();
                }
                SharedPreferenceUtil.saveValue(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, url, (int) (System.currentTimeMillis() / 1000));
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_START, null, model.getAid()));
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                long progress = current * 100 / total;
                count++;
                if (count % 5 == 0 || progress >= 100) {
                    EvtLog.d("aaa", model.getAid() + "下载开始progress:" + progress);
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_NORMAL, progress, model.getAid()));
                }
            }

            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                MediaVideoModel downLoadVideoModel = model.getVideo();
                String url = downLoadVideoModel.getFirst();
                if (StringUtil.isNullOrEmpty(url)) {
                    url = downLoadVideoModel.getSecond();
                }
                int startTime = SharedPreferenceUtil.getIntegerValueByKey(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, url);
                int offset = (int) (System.currentTimeMillis() / 1000) - startTime;
                ArrayList<String> logList = new ArrayList<>();
                logList.add("0");
                logList.add("" + offset);
                logList.add(url);
                logList.add(NetUtil.isWifi(WarriormediaApplication.getInstance().getBaseContext()) ? "wifi" : "3g");
                LogManager.saveLog(4, new Gson().toJson(logList));
                model.setIsDownloadFinish(1);
                DBMgr.saveModel(model);
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FINISH, null, model.getAid()));
                deleteOldVideoFile();
            }


            @Override
            public void onFailure(HttpException error, String msg) {
                MediaVideoModel downLoadVideoModel = model.getVideo();
                String url = downLoadVideoModel.getFirst();
                if (StringUtil.isNullOrEmpty(url)) {
                    url = downLoadVideoModel.getSecond();
                }
                int startTime = SharedPreferenceUtil.getIntegerValueByKey(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, url);
                int offset = (int) (System.currentTimeMillis() / 1000) - startTime;
                ArrayList<String> logList = new ArrayList<>();
                logList.add("1");
                logList.add("" + offset);
                logList.add(url);
                logList.add(NetUtil.isWifi(WarriormediaApplication.getInstance().getBaseContext()) ? "wifi" : "3g");
                LogManager.saveLog(4, new Gson().toJson(logList));
                EvtLog.d("aaa", "******************** 下载失败 ********************");
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FAILED, msg, model.getAid()));
                if (downloadFile.exists()) {
                    downloadFile.delete();
                }
            }
        });
    }

    private static void deleteOldVideoFile() {
        File downloadDir = null;
        try {
            downloadDir = FileUtil.getDownloadDir();
        } catch (MessageException e) {
            e.printStackTrace();
        }
        if (!downloadDir.exists()) {
            return;
        }
        File[] fileList = downloadDir.listFiles();
        List<DownloadModel> downloadModels = DBMgr.getBaseModels(DownloadModel.class);
        if (downloadModels != null && downloadModels.size() > 0) {
            if (fileList != null && fileList.length > 0) {
                for (int i = 0; i < fileList.length; i++) {
                    String name = fileList[i].getName();
                    if (name.endsWith(".apk")) {
                        // needn't delete apk
                        continue;
                    }
                    boolean existsFlag = false;
                    for (int j = 0; j < downloadModels.size(); j++) {
                        MediaVideoModel videoModel = downloadModels.get(j).getVideo();
                        if (videoModel != null && name.equals(videoModel.getFileName())) {
                            existsFlag = true;
                            break;
                        }
                    }
                    if (!existsFlag) {
                        fileList[i].delete();
                    }
                }
            }
        } else {
            if (fileList != null && fileList.length > 0) {
                for (int i = 0; i < fileList.length; i++) {
                    fileList[i].delete();
                }
            }
        }
    }

}
