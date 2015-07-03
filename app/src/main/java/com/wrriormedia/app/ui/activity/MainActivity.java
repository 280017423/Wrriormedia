package com.wrriormedia.app.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;

import com.wrriormedia.app.R;
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
import com.wrriormedia.app.service.DelOldFilesService;
import com.wrriormedia.app.service.DownloadService;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.app.util.SystemUtil;
import com.wrriormedia.app.util.VideoUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.imageloader.core.DisplayImageOptions;
import com.wrriormedia.library.imageloader.core.display.SimpleBitmapDisplayer;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.StringUtil;
import com.wrriormedia.library.util.UIUtil;
import com.wrriormedia.library.widget.LoadingUpView;

import java.util.Calendar;
import java.util.TimeZone;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.VideoView;

public class MainActivity extends HtcBaseActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private LoadingUpView mLoadingUpView;
    private int mPlayIndex;
    private ImageView mIvAdPos0;
    private ImageView mIvAdPos1;
    private ImageView mIvAdPos2;
    private ImageView mIvAdPos3;
    private ImageView mIvAdPos4;
    private ImageView mIvAdPos5;
    private ImageView mIvAdPos6;
    private ImageView mIvAdPos7;
    private ImageView mIvAdPos8;
    private ImageView mIvAdPos9;

    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariable();
        initViews();
        new CmdTask().execute();
        checkStorage();
    }

    private void initVariable() {
        EventBus.getDefault().register(this);
        mLoadingUpView = new LoadingUpView(this, false);
    }

    private void initViews() {
        mVideoView = (VideoView) findViewById(R.id.surface_view);

        mIvAdPos0 = (ImageView) findViewById(R.id.iv_ad_pos_0);
        mIvAdPos1 = (ImageView) findViewById(R.id.iv_ad_pos_1);
        mIvAdPos2 = (ImageView) findViewById(R.id.iv_ad_pos_2);
        mIvAdPos3 = (ImageView) findViewById(R.id.iv_ad_pos_3);
        mIvAdPos4 = (ImageView) findViewById(R.id.iv_ad_pos_4);
        mIvAdPos5 = (ImageView) findViewById(R.id.iv_ad_pos_5);
        mIvAdPos6 = (ImageView) findViewById(R.id.iv_ad_pos_6);
        mIvAdPos7 = (ImageView) findViewById(R.id.iv_ad_pos_7);
        mIvAdPos8 = (ImageView) findViewById(R.id.iv_ad_pos_8);
        mIvAdPos9 = (ImageView) findViewById(R.id.iv_ad_pos_9);

        mVideoView.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
        return true;
    }

    public void onEventMainThread(EventBusModel model) {
        if (null == model || StringUtil.isNullOrEmpty(model.getEventBusAction())) {
            return;
        }
        if (ConstantSet.KEY_EVENT_ACTION_PLAY_VIDEO.equals(model.getEventBusAction())) {
            EvtLog.d("aaa", "播放视频");
            UIUtil.setViewVisible(mVideoView);
            VideoUtil.play((MediaVideoModel) model.getEventBusObject(), mVideoView);
        } else if (ConstantSet.KEY_EVENT_ACTION_PLAY_IMAGE.equals(model.getEventBusAction())) {
            showPosImg((MediaImageModel) model.getEventBusObject());
            EvtLog.d("aaa", "显示图像");
        } else if (ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT.equals(model.getEventBusAction())) {
            EvtLog.d("aaa", "播放下一个");
            UIUtil.setViewGone(mVideoView);
            mPlayIndex++;
            hideAllImg();
            AdManager.getPlayAd(mPlayIndex);
        } else if (ConstantSet.KEY_EVENT_ACTION_PLAY_NO_AD.equals(model.getEventBusAction())) {
            EvtLog.d("aaa", "当前没有广告数据");
            toast("当前没有广告数据");
        }
    }

    public void onEventMainThread(ActionResult result) {
        if (ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
            CmdModel model = (CmdModel) result.ResultObject;
            if (StringUtil.isNullOrEmpty(model.getUpdate_fields())) {
                SystemManager.setModifyTime(ServerAPIConstant.ACTION_KEY_MODIFY);// 更新本地的上次请求时间
                return; // 没有设置更新
            }
            new CountDownTimer(model.getNext_time() * 1000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    toast("下次执行cmd指令");
                    new CmdTask().execute();
                }
            }.start();
            VersionModel versionModel = model.getVersion();
            if (null != versionModel && !StringUtil.isNullOrEmpty(versionModel.getUrl())) {
                checkVersion(model.getVersion());
                return;
            }
            boolean isSuccess = SystemManager.connectWifi(this, model.getWifi());
            // TODO 设置wifi失败
//            if (!isSuccess) {
//                toast("wifi设置失败");
//                return;
//            }
            if (model.isNeedUpdate("download") && 1 == model.getDownload()) {
                EvtLog.d("aaa", "有新的下载就获取下载信息");
                new DownloadTask().execute();
            }
            if (model.isNeedUpdate("ad") && 1 == model.getAd()) {
                EvtLog.d("aaa", "有新的广告就获取广告信息");
                new AdTask().execute();
            } else {
                EvtLog.d("aaa", "没有新的广告就开始播放广告信息");
                AdManager.getPlayAd(mPlayIndex);
            }
            SystemUtil.changeBrightnessSlide(MainActivity.this, model.getBrightness() / 10f);// 改变屏幕亮度
            SystemUtil.setStreamVolume(MainActivity.this, model.getVolume());// 改变声音大小
            SystemManager.setModifyTime(ServerAPIConstant.ACTION_KEY_MODIFY);// 更新model时间
        } else {
            //TODO 记录日志
            showErrorMsg(result);
        }
    }

    private void checkVersion(VersionModel versionModel) {
        Intent intent = new Intent(MainActivity.this, UpdateStateActivity.class);
        intent.putExtra(VersionModel.class.getName(), versionModel);
        startActivity(intent);
    }


    class CmdTask extends AsyncTask<Void, Void, ActionResult> {

        @Override
        protected void onPreExecute() {
            showLoadingUpView(mLoadingUpView, "正在获取指令信息...");
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
            showLoadingUpView(mLoadingUpView, "正在获取广告信息...");
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
            showLoadingUpView(mLoadingUpView, "正在获取下载信息...");
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

    private void hideAllImg() {
        UIUtil.setViewInVisible(mIvAdPos0);
        UIUtil.setViewInVisible(mIvAdPos1);
        UIUtil.setViewInVisible(mIvAdPos2);
        UIUtil.setViewInVisible(mIvAdPos3);
        UIUtil.setViewInVisible(mIvAdPos4);
        UIUtil.setViewInVisible(mIvAdPos5);
        UIUtil.setViewInVisible(mIvAdPos6);
        UIUtil.setViewInVisible(mIvAdPos7);
        UIUtil.setViewInVisible(mIvAdPos8);
        UIUtil.setViewInVisible(mIvAdPos9);
    }

    private void showPosImg(final MediaImageModel mediaImageModel) {
        ImageView displayView = null;
        int position = mediaImageModel.getPos();
        EvtLog.d("aaa", "显示第" + position + "张");
        switch (position) {
            case 0:
                displayView = mIvAdPos0;
                break;
            case 1:
                displayView = mIvAdPos1;
                break;
            case 2:
                displayView = mIvAdPos2;
                break;
            case 3:
                displayView = mIvAdPos3;
                break;
            case 4:
                displayView = mIvAdPos4;
                break;
            case 5:
                displayView = mIvAdPos5;
                break;
            case 6:
                displayView = mIvAdPos6;
                break;
            case 7:
                displayView = mIvAdPos7;
                break;
            case 8:
                displayView = mIvAdPos8;
                break;
            case 9:
                displayView = mIvAdPos9;
                break;
            default:
                break;
        }
        UIUtil.setViewVisible(displayView);
        mImageLoader.displayImage(mediaImageModel.getFirst(), displayView, new DisplayImageOptions.Builder()
                .cacheInMemory().cacheOnDisc().displayer(new SimpleBitmapDisplayer())
                .build());
        int time = mediaImageModel.getTime();
        if (0 != time) {
            new CountDownTimer(time * 1000, time * 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                }
            }.start();
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
        super.onDestroy();
    }

    // 每周一检查卡的容量
    private void checkStorage() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        if (2 == c.get(Calendar.DAY_OF_WEEK)) {
            startService(new Intent(MainActivity.this, DelOldFilesService.class));
        }
    }

}
