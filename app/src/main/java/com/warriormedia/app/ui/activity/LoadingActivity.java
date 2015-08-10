package com.warriormedia.app.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.warriormedia.app.R;
import com.warriormedia.app.app.WarriormediaApplication;
import com.warriormedia.app.business.manager.LogManager;
import com.warriormedia.app.business.requst.DeviceRequest;
import com.warriormedia.app.model.StatusModel;
import com.warriormedia.app.util.ActionResult;
import com.warriormedia.app.util.SharedPreferenceUtil;
import com.warriormedia.library.eventbus.EventBus;
import com.warriormedia.library.util.EvtLog;
import com.warriormedia.library.util.NetUtil;
import com.warriormedia.library.util.PackageUtil;
import com.warriormedia.library.util.StringUtil;
import com.warriormedia.library.widget.LoadingUpView;

/**
 * app启动界面
 */
public class LoadingActivity extends HtcBaseActivity {

    private TextView mTvImmeInfo;
    private TextView mTvSerial;
    private TextView mTvAddress;
    private LoadingUpView mLoadingUpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        wakeupMachine();
        initVariable();
        initViews();
        getReadCmd();
        LogManager.saveLog(2, "" + (int) (System.currentTimeMillis() / 1000));
    }

    private void wakeupMachine() {
        Intent intent = new Intent("com.wrriormedia.app.wakeup");
        sendBroadcast(intent);
    }

    private void initVariable() {
        /*if (!LibsChecker.checkVitamioLibs(this)) {
            // 初始化Vitamio库
            EvtLog.d("aaa", "init check vitamio lib fail.......");
            Toast.makeText(this, "init check vitamio lib fail", Toast.LENGTH_SHORT).show();
            return;
        }*/
        EventBus.getDefault().register(this);
        mLoadingUpView = new LoadingUpView(this, false);
    }

    private void initViews() {
        mTvImmeInfo = (TextView) findViewById(R.id.tv_imie);
        mTvSerial = (TextView) findViewById(R.id.tv_serial);
        mTvAddress = (TextView) findViewById(R.id.tv_address);
    }

    private void getReadCmd() {
        // 如果网络可以，直接请求ready接口，如果还没有准备好就等待网络开启广播
        StatusModel model = (StatusModel) SharedPreferenceUtil.getObject(WarriormediaApplication.getInstance().getBaseContext(), StatusModel.class.getName(), StatusModel.class);
        if (null != model && !StringUtil.isNullOrEmpty(model.getSerial())) {
            startActivity(new Intent(LoadingActivity.this, MainActivity.class));
            finish();
        } else {
            if (NetUtil.isNetworkAvailable()) {
                new VerifyTask().execute();
            } else {
                showLoadingUpView(mLoadingUpView, "等待网络初始化...");
            }
        }
    }

    public void onEventMainThread(ActionResult result) {
        dismissLoadingUpView(mLoadingUpView);
        if (null != result) {
            EvtLog.d("aaa", ">>>> ready, ResultCode = " + result.ResultCode + ", msg = " + result.ResultObject);
        }
        if (ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
            if (null != result.ResultObject) {
                StatusModel model = (StatusModel) result.ResultObject;
                String ready = model.getReady();
                if ("0".equals(ready)) {
                    mTvImmeInfo.setText(PackageUtil.getTerminalSign());
                    mTvSerial.setText(model.getSerial());
                    mTvAddress.setText(model.getAddress());
                } else {
                    startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                    finish();
                }
            }/* else {
                startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                finish();
            }*/
        } else {
            //TODO 记录日志
            showErrorMsg(result);
            // 现在由于机器没有IMEI，所以收到 id empty 的时候先跳到MainActivity界面
            startActivity(new Intent(LoadingActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    class VerifyTask extends AsyncTask<Void, Void, ActionResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ActionResult doInBackground(Void... params) {
            return DeviceRequest.ready();
        }

        @Override
        protected void onPostExecute(ActionResult result) {
            if (null == result) {
                return;
            }
            EventBus.getDefault().post(result);
        }
    }
}
