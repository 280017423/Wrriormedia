package com.wrriormedia.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;

import com.wrriormedia.app.business.dao.DBMgr;
import com.wrriormedia.app.model.DownloadModel;
import com.wrriormedia.app.util.DelOldFilesThread;

import java.io.File;
import java.util.List;

public class DelOldFilesService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long freeStorage = getFreeStorage();
        long totalStorage = getTotalStorage();
        if (((double) freeStorage) / totalStorage <= 0.2) {
            long needDelSize = (long) (totalStorage - freeStorage - totalStorage * 0.6);
            List<DownloadModel> downloadModels = DBMgr.getHistoryData(DownloadModel.class);
            if (downloadModels != null && downloadModels.size() > 0) {
                new DelOldFilesThread(downloadModels, needDelSize).start();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public long getFreeStorage() {
        StatFs stat = new StatFs(getStorageFile().getPath());
        long availBlocks = stat.getAvailableBlocks();
        long blockSize = stat.getBlockSize();
        long freeMemory = availBlocks * blockSize;
        return freeMemory;
    }

    public long getTotalStorage() {
        StatFs stat = new StatFs(getStorageFile().getPath());
        long totalBlocks = stat.getBlockCount();
        long blockSize = stat.getBlockSize();
        long totalMemory = totalBlocks * blockSize;
        return totalMemory;
    }

    private File getStorageFile() {
        return Environment.getExternalStorageDirectory();
    }

}
