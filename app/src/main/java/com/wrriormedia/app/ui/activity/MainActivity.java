package com.wrriormedia.app.ui.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;

import com.wrriormedia.app.R;
import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.business.requst.DeviceRequest;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.common.ServerAPIConstant;
import com.wrriormedia.app.model.AdContentModel;
import com.wrriormedia.app.model.CmdModel;
import com.wrriormedia.app.model.VersionModel;
import com.wrriormedia.app.model.WifiModel;
import com.wrriormedia.app.service.DownloadService;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.app.util.SystemUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.widget.LoadingUpView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends HtcBaseActivity {
    private static final String ACTION_CMD_NEXT_TIME = "com.wrriormedia.action.cmd_next_time";
    private LoadingUpView mLoadingUpView;
    private AlarmManager mAlarmManager;
    private PendingIntent mPI;
    private CmdNextReceiver mCmdNextReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariable();
        registerCmdNextReceiver();
        initAlarm();
        getCmd();
    }

    private void initVariable() {
        EventBus.getDefault().register(this);
        mLoadingUpView = new LoadingUpView(this, false);
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

    private void getCmd() {
        new CmdTask().execute();
    }

    public void onEventMainThread(ActionResult result) {
        if (ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
            CmdModel model = (CmdModel) result.ResultObject;
            //TODO 需要更新系统时间 3.相差超过10秒，需要校准本地时间；
            setSystemTime(model.getSys_time());
            //当无更新时，不必判断其他节点，记录下次请求时间：next_time，本地计时（到了这个时间再次发起请求），本次请求处理结束
            SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ServerAPIConstant.ACTION_KEY_NEXT_TIME, model.getNext_time());
            //TODO 定时闹钟，下次请求
            mAlarmManager.set(mAlarmManager.ELAPSED_REALTIME_WAKEUP, model.getNext_time() - System.currentTimeMillis(), mPI);
            if (0 == model.getUpdate()) {
                EvtLog.d("aaa", "不需要更新指令");
                new AdTask().execute();
                return;
            }
            int sysStatus = model.getSys_status();
            switch (sysStatus) {
                case 0:
                    // TODO 正常播放广告
                    EvtLog.d("aaa", "正常播放广告");
                    break;
                case 1:
                    //TODO 暂停播放，展示默认图
                    EvtLog.d("aaa", "暂停播放，展示默认图");
                    break;
                case 2:
                    //TODO 系统关闭屏幕，停止播放
                    EvtLog.d("aaa", "系统关闭屏幕，停止播放");
                    break;
                default:
                    break;
            }
            WifiModel wifiModel = model.getWifi();
            if (null != wifiModel) {
                // TODO 设置wifi
            }
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

    private void setSystemTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String dateTime = sdf.format(new Date(time));
        String[] timeList = dateTime.split("-");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, Integer.parseInt(timeList[0]));
        c.set(Calendar.MONTH, Integer.parseInt(timeList[1]));
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeList[2]));
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeList[3]));
        c.set(Calendar.MINUTE, Integer.parseInt(timeList[4]));
        c.set(Calendar.SECOND, Integer.parseInt(timeList[5]));
        long when = c.getTimeInMillis();
        if (when / 1000 < Integer.MAX_VALUE) {
            // 等作为系统应用正式测试时再打开
           // ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    private void checkVersion(VersionModel model) {
        // TODO 更新版本逻辑
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mCmdNextReceiver);
        EventBus.getDefault().unregister(this);
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
            if (null == result) {
                return;
            }
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
            if (null == result) {
                return;
            }
            AdContentModel adModel = (AdContentModel) result.ResultObject;

            // TODO 重点是要解析这个接口
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
                getCmd();
            }
        }
    }

}
