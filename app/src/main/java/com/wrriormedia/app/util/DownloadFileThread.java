package com.wrriormedia.app.util;


import com.wrriormedia.app.R;
import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.app.model.PushVersionModel;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.listener.DownloadListener;
import com.wrriormedia.library.util.FileUtil;
import com.wrriormedia.library.util.PackageUtil;

import java.io.File;
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
            String apkFileName = WrriormediaApplication.getInstance().getBaseContext().getPackageName() + "_" + mUpdateModel.getVersion()
                    + ".apk";
            File downloadFile = new File(downloadDir, apkFileName);
            downFile(mUpdateModel.getUrl(), downloadFile);
        } catch (Exception e) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FAILED, PackageUtil.getString(R.string.no_sdcard)));
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
            HttpURLConnection urlConn;
            long fileSize = 0;
            try {
                URL url = new URL(urlStr);
                urlConn = (HttpURLConnection) url.openConnection();
                inputStream = urlConn.getInputStream();
                // 计算默认文件大小
                fileSize = urlConn.getContentLength();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FileUtil.write2SDFromInput(downloadFile, inputStream, fileSize, new DownloadListener() {
                @Override
                public void onDownloadFail() {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FAILED, null));
                }

                @Override
                public void onDownloading(int progress) {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_NORMAL, progress));
                }

                @Override
                public void onDownloadFinish(File file) {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FINISH, file));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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