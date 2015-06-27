package com.wrriormedia.app.ui.activity;

import android.os.AsyncTask;
import android.os.Bundle;

import com.wrriormedia.app.R;
import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.business.requst.DeviceRequest;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.common.ServerAPIConstant;
import com.wrriormedia.app.model.CmdModel;
import com.wrriormedia.app.model.VersionModel;
import com.wrriormedia.app.model.WifiModel;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.app.util.SystemUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.widget.LoadingUpView;


public class MainActivity extends HtcBaseActivity {

    private LoadingUpView mLoadingUpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariable();
        getCmd();
    }

    private void initVariable() {
        EventBus.getDefault().register(this);
        mLoadingUpView = new LoadingUpView(this, false);
    }

    private void getCmd() {
        new CmdTask().execute();
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

    public void onEventMainThread(ActionResult result) {
        if (ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
            CmdModel model = (CmdModel) result.ResultObject;
            //TODO 需要更新系统时间 3.相差超过10秒，需要校准本地时间；
            if (0 == model.getUpdate()) {
                EvtLog.d("aaa", "不需要更新");
                //当无更新时，不必判断其他节点，记录下次请求时间：next_time，本地计时（到了这个时间再次发起请求），本次请求处理结束
                SharedPreferenceUtil.saveValue(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ServerAPIConstant.ACTION_KEY_NEXT_TIME, model.getNext_time());
                //TODO 定时闹钟，下次请求
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

            SystemUtil.changeBrightnessSlide(MainActivity.this, model.getBrightness() / 10f);// 改变屏幕亮度
            SystemUtil.setStreamVolume(MainActivity.this, model.getVolume());// 改变声音大小
            checkVersion(model.getVersion());
            new AdTask().execute();
        } else {
            //TODO 记录日志
            showErrorMsg(result);
        }
    }

    private void checkVersion(VersionModel model) {
        // TODO 更新版本逻辑
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
            // TODO 重点是要解析这个接口
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
