package com.wrriormedia.library.listener;

import java.io.File;

/**
 * @author zou.sq
 *         下载监听器
 */
public interface DownloadListener {

    /**
     * 开始下载监听
     */
    void onDownloadFail();

    /**
     * 下载中监听
     *
     * @param progress 下载进度
     */
    void onDownloading(int progress);

    /**
     * 结束下载监听
     */
    void onDownloadFinish(File file);
}
