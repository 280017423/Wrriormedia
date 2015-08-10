package com.warriormedia.app.util;


import com.warriormedia.app.model.DownloadModel;
import com.warriormedia.library.util.FileUtil;
import com.warriormedia.library.util.StringUtil;

import java.io.File;
import java.util.List;

/**
 * 删除旧文件线程
 *
 * @author zou.sq
 */
public class DelOldFilesThread extends Thread {

    private List<DownloadModel> downloadModels;
    private long needDelSize;
    private long delSize;

    public DelOldFilesThread(List<DownloadModel> downloadModels, long needDelSize) {
        this.downloadModels = downloadModels;
        this.needDelSize = needDelSize;
    }


    @Override
    public void run() {
        super.run();
        try {
            File downloadDir = FileUtil.getDownloadDir();
            for (int i = 0; i < downloadModels.size(); i++) {
                DownloadModel model = downloadModels.get(i);
                String videoFileName = model.getVideo().getMd5();
                if (!StringUtil.isNullOrEmpty(videoFileName)) {
                    File file = new File(downloadDir + File.separator + videoFileName);
                    if (file.exists()) {
                        delSize += file.length();
                        file.delete();
                    }
                }
                String imgFileName = model.getImage().getMd5();
                if (!StringUtil.isNullOrEmpty(imgFileName)) {
                    File file = new File(downloadDir + File.separator + imgFileName);
                    if (file.exists()) {
                        delSize += file.length();
                        file.delete();
                    }
                }
                if (delSize > needDelSize) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}