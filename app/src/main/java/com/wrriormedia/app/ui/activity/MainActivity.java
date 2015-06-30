package com.wrriormedia.app.ui.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;

import com.wrriormedia.app.R;
import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.business.manager.AdManager;
import com.wrriormedia.app.business.manager.SystemManager;
import com.wrriormedia.app.business.requst.DeviceRequest;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.common.ServerAPIConstant;
import com.wrriormedia.app.model.CmdModel;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.app.model.MediaImageModel;
import com.wrriormedia.app.model.MediaVideoModel;
import com.wrriormedia.app.model.VersionModel;
import com.wrriormedia.app.service.DownloadService;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.app.util.SystemUtil;
import com.wrriormedia.app.util.VideoUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.imageloader.core.DisplayImageOptions;
import com.wrriormedia.library.imageloader.core.display.SimpleBitmapDisplayer;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.StringUtil;
import com.wrriormedia.library.widget.LoadingUpView;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.VideoView;

public class MainActivity extends HtcBaseActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener {
    private static final String ACTION_CMD_NEXT_TIME = "com.wrriormedia.action.cmd_next_time";
    private LoadingUpView mLoadingUpView;
    private AlarmManager mAlarmManager;
    private PendingIntent mPI;
    private CmdNextReceiver mCmdNextReceiver;
    private VideoView mVideoView;
    private int mPlayIndex;
    private ImageView mIvAd;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariable();
        initViews();
        registerCmdNextReceiver();
        initAlarm();
        new CmdTask().execute();
    }

    private void initVariable() {
        EventBus.getDefault().register(this);
        mLoadingUpView = new LoadingUpView(this, false);
    }

    private void initViews() {
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        mIvAd = (ImageView) findViewById(R.id.iv_ad);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnInfoListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int arg1, int i1) {
        switch (arg1) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (VideoUtil.isPlaying(mVideoView)) {
                    VideoUtil.stopPlayer(mVideoView);
                }
                showLoadingUpView(mLoadingUpView);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                VideoUtil.startPlayer(mVideoView);
                dismissLoadingUpView(mLoadingUpView);
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                break;
        }
        return true;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
        return true;
    }

    private void registerCmdNextReceiver() {
        mCmdNextReceiver = new CmdNextReceiver();
        registerReceiver(mCmdNextReceiver, new IntentFilter(ACTION_CMD_NEXT_TIME));
    }

    private void initAlarm() {
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_CMD_NEXT_TIME);
        mPI = PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    public void onEventMainThread(EventBusModel model) {
        if (null == model || StringUtil.isNullOrEmpty(model.getEventBusAction())) {
            return;
        }
        if (ConstantSet.KEY_EVENT_ACTION_PLAY_VIDEO.equals(model.getEventBusAction())) {
            EvtLog.d("aaa", "播放视频");
            VideoUtil.play((MediaVideoModel) model.getEventBusObject(), mVideoView);
        } else if (ConstantSet.KEY_EVENT_ACTION_PLAY_IMAGE.equals(model.getEventBusAction())) {
            MediaImageModel mediaImageModel = (MediaImageModel) model.getEventBusObject();
            String url = "http://warriormedia-warriormedia.stor.sinaapp.com/85d25e99d4d8bd3237aa47efea228f62.jpg";
            mImageLoader.displayImage(url, mIvAd, new DisplayImageOptions.Builder()
                    .cacheInMemory().cacheOnDisc().displayer(new SimpleBitmapDisplayer())
                    .build());
            new CountDownTimer(mediaImageModel.getTime() * 1000, mediaImageModel.getTime() * 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                }
            }.start();
            EvtLog.d("aaa", "显示图像");
        } else if (ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT.equals(model.getEventBusAction())) {
            EvtLog.d("aaa", "播放下一个");
            mPlayIndex++;
            AdManager.getPlayAd(mPlayIndex);
        }
    }

    public void onEventMainThread(ActionResult result) {
        if (ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
            CmdModel model = (CmdModel) result.ResultObject;
            //当无更新时，不必判断其他节点，记录下次请求时间：next_time，本地计时（到了这个时间再次发起请求），本次请求处理结束
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ServerAPIConstant.ACTION_KEY_NEXT_TIME, model.getNext_time());
            //TODO 定时闹钟，下次请求
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, model.getNext_time(), mPI);
            if (0 == model.getUpdate()) {
                return; // 没有设置更新
            }
            SystemManager.connectWifi(this, model.getWifi());
            int needDownload = model.getDownload();
            if (1 == needDownload) {
                EvtLog.d("aaa", "有新的下载");
                new DownloadTask().execute();
            }
            int needAd = model.getAd();
            if (1 == needAd) {
                EvtLog.d("aaa", "有新的广告");
                new AdTask().execute();
            }
            SystemUtil.changeBrightnessSlide(MainActivity.this, model.getBrightness() / 10f);// 改变屏幕亮度
            SystemUtil.setStreamVolume(MainActivity.this, model.getVolume());// 改变声音大小
            checkVersion(model.getVersion());
        } else {
            //TODO 记录日志
            showErrorMsg(result);
        }
    }

    private void checkVersion(VersionModel model) {
        // TODO 更新版本逻辑
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mCmdNextReceiver);
        EventBus.getDefault().unregister(this);
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
        super.onDestroy();
    }

    class CmdTask extends AsyncTask<Void, Void, ActionResult> {

        @Override
        protected void onPreExecute() {
            showLoadingUpView(mLoadingUpView);
            super.onPreExecute();
        }

        @Override
        protected ActionResult doInBackground(Void... params) {
            return DeviceRequest.cmd();
        }

        @Override
        protected void onPostExecute(ActionResult result) {
            dismissLoadingUpView(mLoadingUpView);
            EventBus.getDefault().post(result);
        }
    }

    class AdTask extends AsyncTask<Void, Void, ActionResult> {

        @Override
        protected void onPreExecute() {
            showLoadingUpView(mLoadingUpView);
            super.onPreExecute();
        }

        @Override
        protected ActionResult doInBackground(Void... params) {
            return DeviceRequest.ad();
        }

        @Override
        protected void onPostExecute(ActionResult result) {
            dismissLoadingUpView(mLoadingUpView);
            AdManager.getPlayAd(mPlayIndex);
        }
    }

    class DownloadTask extends AsyncTask<Void, Void, ActionResult> {

        @Override
        protected void onPreExecute() {
            showLoadingUpView(mLoadingUpView);
            super.onPreExecute();
        }

        @Override
        protected ActionResult doInBackground(Void... params) {
            return DeviceRequest.getAdDownload();
        }

        @Override
        protected void onPostExecute(ActionResult result) {
            dismissLoadingUpView(mLoadingUpView);
            startService(new Intent(MainActivity.this, DownloadService.class));// 开启下载服务
        }
    }

    class CmdNextReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_CMD_NEXT_TIME)) {
                EvtLog.d("aaa", "下次时间到");
//               new CmdTask().execute();
            }
        }
    }

}
