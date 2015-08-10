package com.warriormedia.app.util;


import com.warriormedia.app.R;
import com.warriormedia.app.app.WarriormediaApplication;
import com.warriormedia.app.common.ConstantSet;
import com.warriormedia.app.model.EventBusModel;
import com.warriormedia.app.model.PushVersionModel;
import com.warriormedia.library.eventbus.EventBus;
import com.warriormedia.library.listener.DownloadListener;
import com.warriormedia.library.util.EvtLog;
import com.warriormedia.library.util.FileUtil;
import com.warriormedia.library.util.PackageUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载升级文件线程
 *
 * @author zou.sq
 */
public class DownloadFileThread extends Thread {

    private PushVersionModel mUpdateModel;

    public DownloadFileThread(PushVersionModel mUpdateModel) {
        this.mUpdateModel = mUpdateModel;
    }


    @Override
    public void run() {
        super.run();
        if (mUpdateModel == null) {
            return;
        }
        try {
            File downloadDir = FileUtil.getDownloadDir();
            File apkList[] = downloadDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith(".apk")) {
                        return true;
                    }
                    return false;
                }
            });
            if (apkList != null && apkList.length > 0) {
                for (int i = 0; i < apkList.length; i++) {
                    apkList[i].delete();
                    EvtLog.d("aaa", ">>>> delete old apk before download new apk.");
                }
            }
            String apkFileName = WarriormediaApplication.getInstance().getBaseContext().getPackageName() + "_" + mUpdateModel.getVersion()
                    + ".apk";
            File downloadFile = new File(downloadDir, apkFileName);
            downFile(mUpdateModel.getUrl(), downloadFile);
        } catch (Exception e) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_APP_STATUS_FAILED, PackageUtil.getString(R.string.no_sdcard)));
        }
    }

    /**
     * @param urlStr       下载apk地址
     * @param downloadFile 本地保存文件
     * @return boolean false 文件下载出错 true 文件下载成功
     */
    public boolean downFile(final String urlStr, File downloadFile) {
        InputStream inputStream = null;
        try {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PAUSE_PLAY, null));
            HttpURLConnection urlConn;
            URL url = new URL(urlStr);
            urlConn = (HttpURLConnection) url.openConnection();
            inputStream = urlConn.getInputStream();
            // 计算默认文件大小
            long fileSize = urlConn.getContentLength();
            FileUtil.write2SDFromInput(downloadFile, inputStream, fileSize, new DownloadListener() {
                @Override
                public void onDownloadFail() {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_APP_STATUS_FAILED, null));
                }

                @Override
                public void onDownloading(int progress) {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_APP_STATUS_NORMAL, progress));
                }

                @Override
                public void onDownloadFinish(File file) {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_APP_STATUS_FINISH, file));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_APP_STATUS_FAILED, null));
            return false;
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}