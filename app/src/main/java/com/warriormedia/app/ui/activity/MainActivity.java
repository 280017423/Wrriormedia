package com.warriormedia.app.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pdw.gson.Gson;
import com.warriormedia.app.R;
import com.warriormedia.app.app.WarriormediaApplication;
import com.warriormedia.app.business.dao.DBMgr;
import com.warriormedia.app.business.manager.AdManager;
import com.warriormedia.app.business.manager.LogManager;
import com.warriormedia.app.business.requst.DeviceRequest;
import com.warriormedia.app.common.ConstantSet;
import com.warriormedia.app.model.CmdModel;
import com.warriormedia.app.model.DeleteAdModel;
import com.warriormedia.app.model.DownloadModel;
import com.warriormedia.app.model.EventBusModel;
import com.warriormedia.app.model.MediaImageModel;
import com.warriormedia.app.model.MediaVideoModel;
import com.warriormedia.app.model.PushAdModel;
import com.warriormedia.app.model.PushBrightnessModel;
import com.warriormedia.app.model.PushLogModel;
import com.warriormedia.app.model.PushVersionModel;
import com.warriormedia.app.model.SysStatusModel;
import com.warriormedia.app.model.TextModel;
import com.warriormedia.app.model.VolumeModel;
import com.warriormedia.app.service.DownloadService;
import com.warriormedia.app.ui.widget.AutoScrollTextView;
import com.warriormedia.app.ui.widget.MyVideoView;
import com.warriormedia.app.util.ActionResult;
import com.warriormedia.app.util.SharedPreferenceUtil;
import com.warriormedia.app.util.SystemUtil;
import com.warriormedia.app.util.TimeUtil;
import com.warriormedia.library.eventbus.EventBus;
import com.warriormedia.library.imageloader.core.DisplayImageOptions;
import com.warriormedia.library.imageloader.core.assist.FailReason;
import com.warriormedia.library.imageloader.core.assist.ImageLoadingListener;
import com.warriormedia.library.imageloader.core.display.SimpleBitmapDisplayer;
import com.warriormedia.library.util.EvtLog;
import com.warriormedia.library.util.FileUtil;
import com.warriormedia.library.util.MessageException;
import com.warriormedia.library.util.PackageUtil;
import com.warriormedia.library.util.StringUtil;
import com.warriormedia.library.util.TimerUtil;
import com.warriormedia.library.util.UIUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends HtcBaseActivity {
    private static final long START_PLAY_DELAY = 5 * 1000;
    private static final long HIDE_AID_VIEW_DELAY = 2 * 1000;
    private static final int WHAT_START_PLAY = 1;
    private static final int WHAT_HIDE_AID_VIEW = 2;
    private static final int WHAT_HIDE_DOWNLOAD_VIEW = 3;
    private static final int WHAT_REBOOT = 4;
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
    private ImageView mIvDefaultPic;
    private AutoScrollTextView mTvAdPos10;
    private AutoScrollTextView mTvAdPos11;
    private TextView tv_aid, tv_download;

    private MyVideoView mViewVideo;
    private Intent mIntent;
    private PushBroadCast mPushBroadCast;
    private PowerManager mPowerManger;
    private MediaVideoModel currVideoModel;
    private boolean videoPauseFlag = false;
    private CountDownTimer mPicCountTimer;
    private int picShowTime;
    private boolean pausePlayingFlag;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_START_PLAY:
                    AdManager.getPlayAd(mPlayIndex);
                    AdManager.getTextAd(mTextIndex);
                    break;
                case WHAT_HIDE_AID_VIEW:
                    tv_aid.setVisibility(View.GONE);
                    break;
                case WHAT_HIDE_DOWNLOAD_VIEW:
                    tv_download.setVisibility(View.GONE);
                    break;
                case WHAT_REBOOT:
                    Intent iReboot = new Intent(ConstantSet.ACTION_REBOOT);
                    sendBroadcast(iReboot);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariable();
        initViews();
        initVideoView();
        mPowerManger = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (SharedPreferenceUtil.getBooleanValueByKey(WarriormediaApplication.getInstance().getBaseContext(),
                ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_GLOBAL_DOWNLOAD_APP)) {
            SharedPreferenceUtil.saveValue(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME,
                    ConstantSet.KEY_GLOBAL_DOWNLOAD_APP, false);
            EvtLog.d("aaa", "MainActivity onCreate, set KEY_GLOBAL_DOWNLOAD_APP false");
        }
        new CmdTask().execute();
        mHandler.sendEmptyMessageDelayed(WHAT_START_PLAY, START_PLAY_DELAY);
        startService(mIntent);
    }

    private void initVariable() {
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
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_SHOW_START);
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_SHOW_FINISH);
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_SHOW_FAILED);
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_SHOW_NORMAL);
        filter.addAction(ConstantSet.KEY_EVENT_ACTION_RESTART_PLAY_DOWNLOAD_APP_FAILED);
        registerReceiver(mPushBroadCast, filter);
    }

    private void initVideoView() {
        mViewVideo.setOnErrorListener(new android.media.MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(android.media.MediaPlayer mp, int what, int extra) {
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
                return false;
            }
        });

        mViewVideo.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(android.media.MediaPlayer mp) {
                EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
            }
        });

        mViewVideo.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(android.media.MediaPlayer mp) {
                mViewVideo.start();
            }
        });
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
        mIvDefaultPic = (ImageView) findViewById(R.id.iv_default_pic);

        mTvAdPos10 = (AutoScrollTextView) findViewById(R.id.tv_10);
        mTvAdPos11 = (AutoScrollTextView) findViewById(R.id.tv_11);
        tv_aid = (TextView) findViewById(R.id.tv_aid);
        tv_download = (TextView) findViewById(R.id.tv_download);

        mViewVideo = (MyVideoView) findViewById(R.id.video_view);
    }

    public void onEventMainThread(EventBusModel model) {
        if (null == model || StringUtil.isNullOrEmpty(model.getEventBusAction())) {
            return;
        }
        if (ConstantSet.KEY_EVENT_ACTION_PLAY_VIDEO.equals(model.getEventBusAction())) {
            currVideoModel = (MediaVideoModel) model.getEventBusObject();
            mViewVideo.setVisibility(View.VISIBLE);
            playVideo(currVideoModel);
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
            mViewVideo.stopPlayback();
            mViewVideo.setVisibility(View.GONE);
            currVideoModel = null;
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
            pausePlay();
            mIvDefaultPic.setVisibility(View.VISIBLE);
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
        } else if (ConstantSet.KEY_EVENT_ACTION_PAUSE_PLAY.equals(model.getEventBusAction())) {
            pausePlay();
        } else if (ConstantSet.KEY_EVENT_ACTION_RESTART_PLAY.equals(model.getEventBusAction())) {
            restartPlay();
        }
    }

    private void pausePlay() {
        if (mViewVideo.isPlaying()) {
            mViewVideo.pause();
            pausePlayingFlag = true;
        } else if (picShowTime > 0) {
            if (mPicCountTimer != null) {
                mPicCountTimer.cancel();
                mPicCountTimer = null;
            }
        }
    }

    private void restartPlay() {
        boolean isDownloadAppFlag = SharedPreferenceUtil.getBooleanValueByKey(WarriormediaApplication.getInstance().getBaseContext(),
                ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_GLOBAL_DOWNLOAD_APP);
        EvtLog.d("aaa", "****** restart play, isDownloadAppFlag = " + isDownloadAppFlag);
        if (mIvDefaultPic.getVisibility() == View.VISIBLE) {
            mIvDefaultPic.setVisibility(View.GONE);
        }
        if (isDownloadAppFlag) {
            return;
        }
        try {
            if (pausePlayingFlag) {
                pausePlayingFlag = false;
                mViewVideo.start();
            } else if (picShowTime > 0 && null == mPicCountTimer) {
                mPicCountTimer = new PicCountTimer(picShowTime * 1000, picShowTime * 1000);
                mPicCountTimer.start();
            } else {
                // EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
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
                if (mNeedFeedback || (2 == DeviceRequest.adType)) {
                    new UpdateTask(mIsTextAd ? "text_ad" : "ad").execute();
                }
                //if (!mIsTextAd) {
                    if (mHandler.hasMessages(WHAT_HIDE_AID_VIEW)) {
                        mHandler.removeMessages(WHAT_HIDE_AID_VIEW);
                    }
                    tv_aid.setText(DeviceRequest.aidFlag);
                    tv_aid.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(WHAT_HIDE_AID_VIEW, HIDE_AID_VIEW_DELAY);
                //}
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
        UIUtil.setViewVisible(displayView);
        picShowTime = mediaImageModel.getTime();
        EvtLog.d("aaa", ">>>>  picShowTime = " + picShowTime);
        if (0 != picShowTime) {
            mPicCountTimer = new PicCountTimer(picShowTime * 1000, picShowTime * 1000);
            mPicCountTimer.start();
        }
    }

    class PicCountTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public PicCountTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            EventBus.getDefault().post(new EventBusModel(ConstantSet.KEY_EVENT_ACTION_PLAY_NEXT, null));
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
        mViewVideo.setVideoPath(downloadFile.getAbsolutePath());
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
                            version = version.replaceAll("\\.", "");
                            String currVersionName = PackageUtil.getVersionName();
                            if (currVersionName.contains(".")) {
                                currVersionName = currVersionName.replaceAll("\\.", "");
                            }
                            if (Integer.parseInt(version) >= Integer.parseInt(currVersionName)) {
                                flag = true;
                            }
                        } else {
                            if (Integer.parseInt(version) >= PackageUtil.getVersionCode()) {
                                flag = true;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (flag) {
                        pausePlay();
                        SharedPreferenceUtil.saveValue(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME,
                                ConstantSet.KEY_GLOBAL_DOWNLOAD_APP, true);
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
                    SharedPreferenceUtil.saveValue(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_LOG_TIME, sysStatusModel.getLog_time());
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
                    new DownloadTask(versionModel.getAid(), true, true).execute();
                }
            } else if (ConstantSet.KEY_EVENT_ACTION_SYS_STATUS.equals(action)) {
                SysStatusModel versionModel = (SysStatusModel) intent.getSerializableExtra(SysStatusModel.class.getName());
                if (null != versionModel) {
                    CmdModel model = (CmdModel) SharedPreferenceUtil.getObject(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, CmdModel.class);
                    model.setSys_status(versionModel.getSys_status());
                    SharedPreferenceUtil.saveObject(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, model);
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
                CmdModel model = (CmdModel) SharedPreferenceUtil.getObject(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, CmdModel.class);
                if (null == model || 0 != model.getSys_status()) {
                    return;
                }
                AdManager.setLockScreen(MainActivity.this, !TimeUtil.isBetweenTime(), mPowerManger, false);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String dateStr = sdf.format(new Date());
                String[] list = dateStr.split("-");
                if (list != null && list.length == 6) {
                    EvtLog.d("aaa", ">>>> currentTime = " + list[3] + list[4] + list[5]);
                    if ("040000".equals(list[3] + list[4] + list[5])) {
                        mHandler.sendEmptyMessage(WHAT_REBOOT);
                    }
                }
            } else if (ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_SHOW_START.equals(action)) {
                tv_download.setText(intent.getIntExtra("aid", 0) + " start");
                tv_download.setVisibility(View.VISIBLE);
            } else if (ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_SHOW_FINISH.equals(action)) {
                tv_download.setText("finish");
                mHandler.sendEmptyMessageDelayed(WHAT_HIDE_DOWNLOAD_VIEW, HIDE_AID_VIEW_DELAY);
            } else if (ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_SHOW_FAILED.equals(action)) {
                tv_download.setText("failed");
                mHandler.sendEmptyMessageDelayed(WHAT_HIDE_DOWNLOAD_VIEW, HIDE_AID_VIEW_DELAY);
            } else if (ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_SHOW_NORMAL.equals(action)) {
                tv_download.setText(intent.getIntExtra("aid", 0) + ": " + intent.getIntExtra("progress", 0) + "%");
            } else if (ConstantSet.KEY_EVENT_ACTION_RESTART_PLAY_DOWNLOAD_APP_FAILED.equals(action)) {
                SharedPreferenceUtil.saveValue(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME,
                        ConstantSet.KEY_GLOBAL_DOWNLOAD_APP, false);
                EvtLog.d("aaa", "download app failed, set KEY_GLOBAL_DOWNLOAD_APP false");
                restartPlay();
            } else if (ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_APP_SUCCESS.equals(action)) {
                SharedPreferenceUtil.saveValue(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME,
                        ConstantSet.KEY_GLOBAL_DOWNLOAD_APP, false);
                EvtLog.d("aaa", "download app success, set KEY_GLOBAL_DOWNLOAD_APP false");
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mViewVideo.stopPlayback();
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
