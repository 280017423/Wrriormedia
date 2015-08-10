package com.warriormedia.library.util;

import android.app.Activity;
import android.os.Environment;

import com.warriormedia.library.R;
import com.warriormedia.library.app.HtcApplicationBase;
import com.warriormedia.library.listener.DownloadListener;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
            com.warriormedia.library.util.EvtLog.e(TAG, e);
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

    public static void write2SDFromInput(File downloadFile, InputStream inputStream, long size, DownloadListener listener) {
        OutputStream output = null;
        if (size <= 0) {
            if (null != listener) {
                listener.onDownloadFail();
            }
            return;
        }
        long onePercentage = size / 100;
        int progress = 0;
        try {
            output = new FileOutputStream(downloadFile);
            byte[] buffer = new byte[4 * 1024];
            int length;
            long readCount = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                Thread.sleep(100);
                output.write(buffer, 0, length);
                readCount += length;
                long maxProgress = readCount / onePercentage;
                if (maxProgress > progress && maxProgress % 5 == 0) {
                    progress = (int) maxProgress;
                    if (null != listener) {
                        listener.onDownloading(progress);
                    }
                }
            }
            output.flush();
            if (null != listener) {
                listener.onDownloadFinish(downloadFile);
            }
        } catch (Exception e) {
            if (null != listener) {
                listener.onDownloadFail();
            }
            e.printStackTrace();
        } finally {
            try {
                if (null != output) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            com.warriormedia.library.util.EvtLog.d("test", "当前路径为:   " + filePath);
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
                com.warriormedia.library.util.EvtLog.e(TAG, "http status code is: " + statusCode);
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
                com.warriormedia.library.util.EvtLog.d(TAG, "read " + len + " bytes, total read: " + count + " bytes");
                output.write(buffer, 0, len);
                if (onDownloadingListener != null && count >= BUFSIZE * COUNT) {
                    com.warriormedia.library.util.EvtLog.d(TAG, "onDownloadingListener.onDownloading()");
                    onDownloadingListener.onDownloading(progress);
                    count = 0;
                }
            }
            if (onDownloadingListener != null && count >= 0) {
                com.warriormedia.library.util.EvtLog.d(TAG, "onDownloadingListener else)");
                onDownloadingListener.onDownloading(progress);
                count = 0;
            }
            fileStream.close();
            output.close();
            if (onDownloadingListener != null) {
                onDownloadingListener.onDownloadComplete(filePath);
            }
        } catch (Exception e) {
            com.warriormedia.library.util.EvtLog.d(TAG, "downFile Exception");
            com.warriormedia.library.util.EvtLog.e(TAG, e);
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
                com.warriormedia.library.util.EvtLog.e(TAG, e);
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
            com.warriormedia.library.util.EvtLog.w(TAG, e);
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

    public static String installApkFile(File file) {
        /*Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);*/

        String[] args = { "pm", "install", "-r", file.getAbsolutePath() };
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write("/n".getBytes());
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (Exception e) {
            e.printStackTrace();
            result = "";
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    /**
     * 静默安装
     *
     * @param file 待安装的apk文件
     * @return 是否安装成功
     */
    public static boolean silentInstall(File file) {
        boolean result = false;
        Process process;
        OutputStream out;
        try {
            process = Runtime.getRuntime().exec("su");
            out = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(out);
            dataOutputStream.writeBytes("chmod 777 " + file.getPath() + "\n");
            dataOutputStream.writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r " +
                    file.getPath());
            // 提交命令
            dataOutputStream.flush();
            // 关闭流操作
            dataOutputStream.close();
            out.close();
            int value = process.waitFor();
            // 代表成功
            return value == 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
