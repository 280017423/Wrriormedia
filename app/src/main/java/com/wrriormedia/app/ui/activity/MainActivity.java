package com.wrriormedia.app.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.pdw.gson.Gson;
import com.wrriormedia.app.R;
import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.business.dao.DBMgr;
import com.wrriormedia.app.business.manager.AdManager;
import com.wrriormedia.app.business.manager.LogManager;
import com.wrriormedia.app.business.requst.DeviceRequest;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.CmdModel;
import com.wrriormedia.app.model.DeleteAdModel;
import com.wrriormedia.app.model.DownloadModel;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.app.model.MediaImageModel;
import com.wrriormedia.app.model.MediaVideoModel;
import com.wrriormedia.app.model.PushAdModel;
import com.wrriormedia.app.model.PushBrightnessModel;
import com.wrriormedia.app.model.PushLogModel;
import com.wrriormedia.app.model.PushVersionModel;
import com.wrriormedia.app.model.SysStatusModel;
import com.wrriormedia.app.model.TextModel;
import com.wrriormedia.app.model.VolumeModel;
import com.wrriormedia.app.service.DownloadService;
import com.wrriormedia.app.ui.widget.AutoScrollTextView;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.app.util.SystemUtil;
import com.wrriormedia.app.util.TimeUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.imageloader.core.DisplayImageOptions;
import com.wrriormedia.library.imageloader.core.assist.FailReason;
import com.wrriormedia.library.imageloader.core.assist.ImageLoadingListener;
import com.wrriormedia.library.imageloader.core.display.SimpleBitmapDisplayer;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.FileUtil;
import com.wrriormedia.library.util.MessageException;
import com.wrriormedia.library.util.PackageUtil;
import com.wrriormedia.library.util.StringUtil;
import com.wrriormedia.library.util.TimerUtil;
import com.wrriormedia.library.util.UIUtil;

import java.io.File;
import java.util.ArrayList;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

public class MainActivity extends HtcBaseActivity implements SurfaceHolder.Callback {
    private int mPlayIndex;
    private int mTextIndex;
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
    private AutoScrollTextView mTvAdPos10;
    private AutoScrollTextView mTvAdPos11;

    private MediaPlayer mMediaPlayer;
    private SurfaceView mPreview;
    private SurfaceHolder holder;
    private View mViewVideo;
    private Intent mIntent;
    private PushBroadCast mPushBroadCast;
    private PowerManager mPowerManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariable();
        initViews();
        mPowerManger = (PowerManager) getSystemService(Context.POWER_SERVICE);
        new CmdTask().execute();
        AdManager.getPlayAd(mPlayIndex);
        AdManager.getTextAd(mTextIndex);
        startService(mIntent);
    }

    private void initVariable() {
        Vitamio.initialize(this);
        EventBus.getDefault().register(this);
        mIntent = new Intent(MainActivity.this, DownloadService.class);
        mPushBroadCast = new PushBroadCast();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_NEW_VERSION);
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_NEW_AD);
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_NEW_TEXT_AD);
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_DELETE_AD);
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_SYS_STATUS);
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_LOG_TIME);
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_BRITENESS);
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_VOLUME);
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mPushBroadCast, filter);
    }

    private void initViews() {
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

        mTvAdPos10 = (AutoScrollTextView) findViewById(R.id.tv_10);
        mTvAdPos11 = (AutoScrollTextView) findViewById(R.id.tv_11);

        mViewVideo = findViewById(R.id.ll_video);
        mPreview = (SurfaceView) findViewById(R.id.surface);
        holder = mPreview.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void onEventMainThread(EventBusModel model) {
        if (null == model || StringUtil.isNullOrEmpty(model.getEventBusAction())) {
            return;
        }
        if (ConstantSet.KEY_EVENT_ACTION_PLAY_VIDEO.equals(model.getEventBusAction())) {
            playVideo((MediaVideoModel) model.getEventBusObject());
            mViewVideo.setVisibility(View.VISIBLE);
        } else if (ConstantSet.KEY_EVENT_ACTION_PLAY_IMAGE.equals(model.getEventBusAction())) {
            DownloadModel downloadModel = (DownloadModel) model.getEventBusObject();
            showPosImg(downloadModel);
        } else if (ConstantSet.KEY_EVENT_ACTION_PLAY_TEXT.equals(model.getEventBusAction())) {
            TextModel textModel = (TextModel) model.getEventBusObject();
            mTvAdPos10.setVisibility(View.GONE);
            mTvAdPos11.setVisibility(View.GONE);
            if (textModel.getPos() == 10) {
                mTvAdPos10.setVisibility(View.VISIBLE);
                mTvAdPos10.setText(textModel.getMsg());
                mTvAdPos10.setSpeed(AutoScrollTextView.SPEED_SLOW);
                mTvAdPos10.startScroll();
                mTvAdPos10.setOnStopListener(new AutoScrollTextView.OnStopListener() {
                    @Override
                    public void stop() {
                        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_TEXT_NEXT, null));
                    }
                });
            } else {
                mTvAdPos11.setVisibility(View.VISIBLE);
                mTvAdPos11.setText(textModel.getMsg());
                mTvAdPos11.setSpeed(AutoScrollTextView.SPEED_SLOW);
                mTvAdPos11.startScroll();
                mTvAdPos11.setOnStopListener(new AutoScrollTextView.OnStopListener() {
                    @Override
                    public void stop() {
                        EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_TEXT_NEXT, null));
                    }
                });
            }

        } else if (ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT.equals(model.getEventBusAction())) {
            mViewVideo.setVisibility(View.GONE);
            mPlayIndex++;
            hideAllImg();
            AdManager.getPlayAd(mPlayIndex);
        } else if (ConstantSet.KEY_EVENT_ACTION_PLAY_TEXT_NEXT.equals(model.getEventBusAction())) {
            mTextIndex++;
            AdManager.getTextAd(mTextIndex);
        } else if (ConstantSet.KEY_EVENT_ACTION_PLAY_NO_AD.equals(model.getEventBusAction())) {
            new CountDownTimer(10 * 1000, 10 * 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                }
            }.start();
        } else if (ConstantSet.KEY_EVENT_ACTION_PLAY_NO_TEXT_AD.equals(model.getEventBusAction())) {
            new CountDownTimer(10 * 1000, 10 * 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_TEXT_NEXT, null));
                }
            }.start();
        } else if (ConstantSet.KEY_EVENT_ACTION_LOCK_SCREEN.equals(model.getEventBusAction())) {
            mIvAdPos0.setVisibility(View.VISIBLE);
            mIvAdPos0.setImageResource(R.mipmap.stop_bg);
        } else if (ConstantSet.KEY_EVENT_ACTION_LOG_TIMER.equals(model.getEventBusAction())) {
            LogManager.timeUploadLog();
        } else if (ConstantSet.KEY_EVENT_ACTION_LOADER_IMAGE.equals(model.getEventBusAction())) {
            final DownloadModel downloadModel = (DownloadModel) model.getEventBusObject();
            if (null == downloadModel || null == downloadModel.getImage()) {
                return;
            }
            downloadModel.setIsImageFinish(1);
            DBMgr.saveModel(downloadModel);
            new UpdateTask("ad:" + downloadModel.getAid() + ":image").execute();
        }
    }

    class CmdTask extends AsyncTask<Void, Void, ActionResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ActionResult doInBackground(Void... params) {
            return DeviceRequest.cmd();
        }

        @Override
        protected void onPostExecute(ActionResult result) {
            if (ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
                CmdModel model = (CmdModel) result.ResultObject;
                AdManager.adStatus(MainActivity.this, mPowerManger);
                SystemUtil.setStreamVolume(MainActivity.this, model.getVolume());// 改变声音大小
                LogManager.timeUploadLog();
            }
        }
    }

    class DownloadTask extends AsyncTask<Void, Void, ActionResult> {

        private String mAid;
        private boolean mIsTextAd;
        private boolean mNeedFeedback;

        public DownloadTask(String aid, boolean isTextAd, boolean needFeedback) {
            this.mAid = aid;
            this.mIsTextAd = isTextAd;
            this.mNeedFeedback = needFeedback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ActionResult doInBackground(Void... params) {
            return DeviceRequest.getAdDownload(Integer.parseInt(mAid), mIsTextAd);
        }

        @Override
        protected void onPostExecute(ActionResult result) {
            if (ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
                if (mNeedFeedback) {
                    new UpdateTask(mIsTextAd ? "text_ad" : "ad").execute();
                }
            } else {
                showErrorMsg(result);
            }
        }
    }

    class UpdateTask extends AsyncTask<Void, Void, ActionResult> {

        private String mAlert;

        public UpdateTask(String alert) {
            this.mAlert = alert;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ActionResult doInBackground(Void... params) {
            return DeviceRequest.update(mAlert);
        }

        @Override
        protected void onPostExecute(ActionResult result) {
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

    private void showPosImg(final DownloadModel downloadModel) {
        MediaImageModel mediaImageModel = downloadModel.getImage();
        ImageView displayView = null;
        int position = mediaImageModel.getPos();
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
                .build(), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted() {

            }

            @Override
            public void onLoadingFailed(FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(Bitmap loadedImage) {
                MediaImageModel mediaImageModel = downloadModel.getImage();
                if (null != mediaImageModel && !StringUtil.isNullOrEmpty(mediaImageModel.getFirst()) && 1 != downloadModel.getIsImageFinish()) {
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_LOADER_IMAGE, downloadModel));
                }
            }

            @Override
            public void onLoadingCancelled() {

            }
        });
        int time = mediaImageModel.getTime();
        EvtLog.d("aaa", ">>>>  time = " + time);
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

    private void playVideo(MediaVideoModel mediaVideoModel) {
        File downloadDir = null;
        try {
            downloadDir = FileUtil.getDownloadDir();
        } catch (MessageException e) {
            e.printStackTrace();
        }
        String fileName = mediaVideoModel.getMd5();
        if (StringUtil.isNullOrEmpty(fileName)) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
            return;
        }
        File downloadFile = new File(downloadDir, fileName);
        if (!downloadFile.exists()) {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
            return;
        }
        releaseMediaPlayer();
        try {
            mMediaPlayer = new MediaPlayer(this);
            mMediaPlayer.setDataSource(downloadFile.getAbsolutePath()); //Path to video file
            mMediaPlayer.setDisplay(holder); //Set SurfaceHolder
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    releaseMediaPlayer();
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    releaseMediaPlayer();
                    EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                    return true;
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
        } catch (Exception e) {
            EvtLog.d("aaa", e.toString());
        }
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public class PushBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConstantSet.KEY_EVENT_ACTION_NEW_VERSION.equals(action)) {
                PushVersionModel versionModel = (PushVersionModel) intent.getSerializableExtra(PushVersionModel.class.getName());
                if (null != versionModel && !StringUtil.isNullOrEmpty(versionModel.getUrl()) && !StringUtil.isNullOrEmpty(versionModel.getVersion())) {
                    boolean flag = false;
                    String version = versionModel.getVersion();
                    try {
                        if (version.contains(".")) {
                            version = version.replaceAll(".", "");
                            String currVersionName = PackageUtil.getVersionName();
                            if (currVersionName.contains(".")) {
                                currVersionName = currVersionName.replaceAll(".", "");
                            }
                            if (Integer.parseInt(version) > Integer.parseInt(currVersionName)) {
                                flag = true;
                            }
                        } else {
                            if (Integer.parseInt(version) > PackageUtil.getVersionCode()) {
                                flag = true;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (flag) {
                        Intent versionIntent = new Intent(MainActivity.this, UpdateStateActivity.class);
                        versionIntent.putExtra(PushVersionModel.class.getName(), versionModel);
                        startActivity(versionIntent);
                    } else {
                        toast(getString(R.string.update_version_less_than_curr));
                    }
                }
            } else if (ConstantSet.KEY_EVENT_ACTION_LOG_TIME.equals(action)) {
                PushLogModel sysStatusModel = (PushLogModel) intent.getSerializableExtra(PushLogModel.class.getName());
                if (null != sysStatusModel) {
                    SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_LOG_TIME, sysStatusModel.getLog_time());
                    LogManager.timeUploadLog();
                    new UpdateTask("log_time").execute();
                }
            } else if (ConstantSet.KEY_EVENT_ACTION_BRITENESS.equals(action)) {
                PushBrightnessModel sysStatusModel = (PushBrightnessModel) intent.getSerializableExtra(PushBrightnessModel.class.getName());
                if (null != sysStatusModel) {
                    SystemUtil.changeBrightnessSlide(MainActivity.this, sysStatusModel.getBrightness() / 10f);
                    new UpdateTask("brightness").execute();
                }
            } else if (ConstantSet.KEY_EVENT_ACTION_VOLUME.equals(action)) {
                VolumeModel sysStatusModel = (VolumeModel) intent.getSerializableExtra(VolumeModel.class.getName());
                if (null != sysStatusModel) {
                    SystemUtil.setStreamVolume(MainActivity.this, sysStatusModel.getVolume());
                    new UpdateTask("volume").execute();
                }
            } else if (ConstantSet.KEY_EVENT_ACTION_NEW_AD.equals(action)) {
                PushAdModel versionModel = (PushAdModel) intent.getSerializableExtra(PushAdModel.class.getName());
                if (null != versionModel && !StringUtil.isNullOrEmpty(versionModel.getAid())) {
                    new DownloadTask(versionModel.getAid(), false, false).execute();
                }
            } else if (ConstantSet.KEY_EVENT_ACTION_NEW_TEXT_AD.equals(action)) {
                PushAdModel versionModel = (PushAdModel) intent.getSerializableExtra(PushAdModel.class.getName());
                if (null != versionModel && !StringUtil.isNullOrEmpty(versionModel.getAid())) {
                    new DownloadTask(versionModel.getAid(), true, false).execute();
                }
            } else if (ConstantSet.KEY_EVENT_ACTION_SYS_STATUS.equals(action)) {
                SysStatusModel versionModel = (SysStatusModel) intent.getSerializableExtra(SysStatusModel.class.getName());
                if (null != versionModel) {
                    CmdModel model = (CmdModel) SharedPreferenceUtil.getObject(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, CmdModel.class);
                    model.setSys_status(versionModel.getSys_status());
                    SharedPreferenceUtil.saveObject(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, model);
                    AdManager.adStatus(MainActivity.this, mPowerManger);
                    new UpdateTask("sys_status").execute();
                }
            } else if (ConstantSet.KEY_EVENT_ACTION_DELETE_AD.equals(action)) {
                DeleteAdModel deleteAdModel = (DeleteAdModel) intent.getSerializableExtra(DeleteAdModel.class.getName());
                if (null == deleteAdModel) {
                    return;
                }
                String aid = deleteAdModel.getAid();
                if (!StringUtil.isNullOrEmpty(aid)) {
                    String[] aids = aid.split(",");
                    for (String deleteAid : aids) {
                        AdManager.deleteAd(deleteAid);
                        ArrayList<String> logList = new ArrayList<>();
                        logList.add(deleteAid);
                        logList.add(SystemUtil.getLeftSpace());
                        LogManager.saveLog(5, new Gson().toJson(logList));
                    }
                    new UpdateTask("delete_ad").execute();
                }
            } else if (Intent.ACTION_TIME_TICK.equals(action)) {
                CmdModel model = (CmdModel) SharedPreferenceUtil.getObject(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, CmdModel.class);
                if (null == model || 0 != model.getSys_status()) {
                    return;
                }
                AdManager.setLockScreen(MainActivity.this, !TimeUtil.isBetweenTime(), mPowerManger, false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        releaseMediaPlayer();
        TimerUtil.stopTimer(LogManager.class.getName());
        if (null != mPushBroadCast) {
            unregisterReceiver(mPushBroadCast);
        }
        if (null != mIntent) {
            stopService(mIntent);
        }
        TimerUtil.stopTimer("DownloadService");
        super.onDestroy();
    }

}
