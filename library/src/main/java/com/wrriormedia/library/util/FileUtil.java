package com.wrriormedia.library.util;

import android.app.Activity;
import android.os.Environment;

import com.wrriormedia.library.R;
import com.wrriormedia.library.app.HtcApplicationBase;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

/**
 * 文件帮助类
 *
 * @author wang.xy
 */
public class FileUtil {
    public static final int BUFSIZE = 256;
    public static final int COUNT = 320;
    private static final String TAG = "FileUtils";
    private static final String DOWNLOAD_PATH = "wrriormedia/downloads/download";
    private static final long SIZE_KB = 1024;
    private static final long SIZE_MB = 1048576;
    private static final long SIZE_GB = 1073741824;
    private static final int SO_TIMEOUT = 600000;
    private static final int CONNECTION_TIMEOUT = 5000;

    /**
     * 获取下载文件
     *
     * @return 文件
     * @throws MessageException 异常信息
     */
    public static File getDownloadDir() throws MessageException {
        File downloadFile = null;
        if (isSDCardReady()) {
            downloadFile = new File(Environment.getExternalStorageDirectory(), AppUtil.getMetaDataByKey(HtcApplicationBase.getInstance().getBaseContext(), "download_dir"));
            if (!downloadFile.exists()) {
                downloadFile.mkdirs();
            }
        }
        if (downloadFile == null) {
            throw new MessageException(PackageUtil.getString(R.string.no_sdcard));
        }
        return downloadFile;
    }

    /**
     * 在SD卡上面创建文件
     *
     * @param filePath 文件路径
     * @return 文件
     * @throws IOException 异常
     */
    public static File createSDFile(String filePath) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        return file;
    }

    /**
     * 在SD卡上面创建目录
     *
     * @param dirName 目录名称
     * @return 文件
     */
    public static File createSDDir(String dirName) {
        File dir = new File(dirName);
        dir.mkdir();
        return dir;
    }

    /**
     * 判断指定的文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 准备文件夹，文件夹若不存在，则创建
     *
     * @param filePath 文件路径
     */
    public static void prepareFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 删除指定的文件或目录
     *
     * @param filePath 文件路径
     */
    public static void delete(String filePath) {
        if (filePath == null) {
            return;
        }
        try {
            File file = new File(filePath);
            delete(file);
        } catch (Exception e) {
            com.wrriormedia.library.util.EvtLog.e(TAG, e);
        }
    }

    /**
     * 删除指定的文件或目录
     *
     * @param file 文件
     */
    public static void delete(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            deleteDirRecursive(file);
        } else {
            file.delete();
        }
    }

    /**
     * 递归删除目录
     *
     * @param dir 文件路径
     */
    public static void deleteDirRecursive(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            } else {
                deleteDirRecursive(f);
            }
        }
        dir.delete();
    }

    /**
     * 递归取得文件夹大小
     *
     * @param filedir 文件
     * @return 大小
     */
    public static long getFileSize(File filedir) {
        long size = 0;
        if (null == filedir) {
            return size;
        }
        File[] files = filedir.listFiles();
        if (null == files || files.length == 0) {
            return size;
        }

        try {
            for (File f : files) {
                if (f.isDirectory()) {
                    size += getFileSize(f);
                } else {
                    FileInputStream fis = new FileInputStream(f);
                    size += fis.available();
                    fis.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;

    }

    /**
     * 转换文件大小
     *
     * @param fileS 大小
     * @return 转换后的文件大小
     */
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.0");
        String fileSizeString = "";
        if (fileS == 0) {
            fileSizeString = "0" + "KB";
        } else if (fileS < SIZE_KB) {
            fileSizeString = df.format((double) fileS) + "KB";
        } else if (fileS < SIZE_MB) {
            fileSizeString = df.format((double) fileS / SIZE_KB) + "KB";
        } else if (fileS < SIZE_GB) {
            fileSizeString = df.format((double) fileS / SIZE_MB) + "M";
        } else {
            fileSizeString = df.format((double) fileS / SIZE_GB) + "G";
        }
        return fileSizeString;
    }

    /**
     * 将文件写入SD卡
     *
     * @param path     路径
     * @param fileName 文件名称
     * @param input    输入流
     * @return 文件
     */
    public static File writeToSDCard(String path, String fileName, InputStream input) {
        File file = null;
        OutputStream output = null;
        try {
            createSDDir(path);
            file = createSDFile(path + fileName);
            output = new FileOutputStream(file);

            byte[] buffer = new byte[BUFSIZE];
            int readedLength = -1;
            while ((readedLength = input.read(buffer)) != -1) {
                output.write(buffer, 0, readedLength);
            }
            output.flush();

        } catch (Exception e) {
            com.wrriormedia.library.util.EvtLog.e(TAG, e);
        } finally {
            try {
                if (null != output) {
                    output.close();
                }
            } catch (IOException e) {
                com.wrriormedia.library.util.EvtLog.e(TAG, e);
            }
        }

        return file;
    }

    /**
     * 判断SD卡是否已经准备好
     *
     * @return 是否有SDCARD
     */
    public static boolean isSDCardReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 使用Http下载文件，并保存在手机目录中
     *
     * @param urlStr                url地址
     * @param path                  路径
     * @param fileName              文件名称
     * @param onDownloadingListener 下载监听器
     * @return -1:文件下载出错 0:文件下载成功
     * @throws MessageException
     */
    public static boolean downFile(String urlStr, String path, String fileName, boolean isUpgradeMust,
                                   OnDownloadingListener onDownloadingListener) {
        InputStream inputStream = null;
        try {
            if (!path.endsWith("/")) {
                path += "/";
            }
            String filePath = path + fileName + System.currentTimeMillis();
            com.wrriormedia.library.util.EvtLog.d("test", "当前路径为:   " + filePath);
            if (isFileExist(filePath)) {
                delete(filePath);
            }
            HttpClient client = new DefaultHttpClient();
            // 设置网络连接超时和读数据超时
            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT)
                    .setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);
            HttpGet httpget = new HttpGet(urlStr);
            HttpResponse response = client.execute(httpget);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                com.wrriormedia.library.util.EvtLog.e(TAG, "http status code is: " + statusCode);
                return false;
            }
            InputStream fileStream = response.getEntity().getContent();
            // 检查下载文件夹，若没有，则创建
            prepareFile(path);
            FileOutputStream output = new FileOutputStream(filePath);
            byte[] buffer = new byte[BUFSIZE];
            int len = 0;
            int count = 0;
            int progress = 0;
            while ((len = fileStream.read(buffer)) > 0) {
                count += len;
                progress += len;
                com.wrriormedia.library.util.EvtLog.d(TAG, "read " + len + " bytes, total read: " + count + " bytes");
                output.write(buffer, 0, len);
                if (onDownloadingListener != null && count >= BUFSIZE * COUNT) {
                    com.wrriormedia.library.util.EvtLog.d(TAG, "onDownloadingListener.onDownloading()");
                    onDownloadingListener.onDownloading(progress);
                    count = 0;
                }
            }
            if (onDownloadingListener != null && count >= 0) {
                com.wrriormedia.library.util.EvtLog.d(TAG, "onDownloadingListener else)");
                onDownloadingListener.onDownloading(progress);
                count = 0;
            }
            fileStream.close();
            output.close();
            if (onDownloadingListener != null) {
                onDownloadingListener.onDownloadComplete(filePath);
            }
        } catch (Exception e) {
            com.wrriormedia.library.util.EvtLog.d(TAG, "downFile Exception");
            com.wrriormedia.library.util.EvtLog.e(TAG, e);
            if (onDownloadingListener != null) {
                onDownloadingListener.onError(isUpgradeMust);
            }
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                com.wrriormedia.library.util.EvtLog.e(TAG, e);
                EvtLog.d(TAG, "downFile Exception in finally");
            }
        }
        return true;
    }

    /**
     * 获取文件的全路径（或安装目录下）
     *
     * @param director 目录
     * @param filename 文件名
     * @return 文件全路径
     */
    public static String getFullPath(String director, String filename) {
        String dir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStorageDirectory().toString();
            dir = dir + File.separator;
        } else {
            dir = HtcApplicationBase.getInstance().getApplicationInfo().dataDir;
        }

        if (director != null && director.trim().length() > 0) {
            dir = dir + File.separator + director;
            File file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }

        if (filename != null && filename.trim().length() > 0) {
            return dir + File.separator + filename;
        }
        return dir;
    }

    /**
     * 获取安装目录下面指定目录
     *
     * @param director 安装目录下面指定目录
     * @return 安装目录下面指定目录
     */
    public static String getFullPath(String director) {
        String dir = HtcApplicationBase.getInstance().getApplicationInfo().dataDir;

        if (director != null && director.trim().length() > 0) {
            dir = dir + File.separator + director;
            File file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return dir;
    }

    /**
     * 得到sdcard路径
     *
     * @return 得到sdcard路径
     */
    public static String getExtPath() {
        String path = "";
        if (isSDCardReady()) {
            path = Environment.getExternalStorageDirectory().getPath();
        }
        return path;
    }

    /**
     * @param activity 上下文对象
     * @return 得到应用程序路径目录
     */
    public static String getPackagePath(Activity activity) {
        return activity.getFilesDir().toString();
    }

    /**
     * 取得文件大小
     *
     * @param f 文件
     * @return long 大小
     */
    public long getFileSizes(File f) {
        long s = 0;
        try {
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                s = fis.available();
                if (null != fis) {
                    fis.close();
                }
            } else {
                f.createNewFile();
            }
        } catch (Exception e) {
            com.wrriormedia.library.util.EvtLog.w(TAG, e);
        }
        return s;
    }

    /**
     * @author Q.d
     */
    public interface OnDownloadingListener {
        /**
         * 下载
         *
         * @param progressInByte 已下载的字节长度
         */
        void onDownloading(int progressInByte);

        /**
         * 下载完成后的回调方法
         *
         * @param filePath 文件路径
         */
        void onDownloadComplete(String filePath);

        /**
         * 下周失败的回调方法
         *
         * @param isUpgradeMust 是否必须升级
         */
        void onError(boolean isUpgradeMust);
    }

}
