package com.wrriormedia.app.util;

import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.app.model.MediaVideoModel;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.FileUtil;
import com.wrriormedia.library.util.MessageException;
import com.wrriormedia.library.util.StringUtil;

import java.io.File;

import io.vov.vitamio.widget.VideoView;

/**
 * 视屏播放工具类
 *
 * @author zou.sq
 * @since 2013-03-28 zou.sq 添加获取时间戳的方法
 */
public class VideoUtil {

    private static final String TAG = "VideoUtil";

    public static void play(MediaVideoModel model, VideoView videoView) {
        if (videoView == null) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
            return;
        }
        EvtLog.d(TAG, "即将播放视屏" + model.toString());
        File downloadDir = null;
        try {
            downloadDir = FileUtil.getDownloadDir();
        } catch (MessageException e) {
            e.printStackTrace();
        }
        String fileName = model.getMd5();
        if (StringUtil.isNullOrEmpty(fileName)) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
            return;
        }
        File downloadFile = new File(downloadDir, fileName);
        if (!downloadFile.exists()) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
            return;
        }
        videoView.setVideoPath(downloadFile.getAbsolutePath());
        videoView.requestFocus();
    }

    public static void stopPlayer(VideoView videoView) {
        if (videoView != null)
            videoView.pause();
    }

    public static void startPlayer(VideoView videoView) {
        if (videoView != null)
            videoView.start();
    }

    public static boolean isPlaying(VideoView videoView) {
        return videoView != null && videoView.isPlaying();
    }

}
