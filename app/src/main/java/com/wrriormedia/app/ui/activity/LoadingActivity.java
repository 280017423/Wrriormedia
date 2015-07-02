package com.wrriormedia.app.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.wrriormedia.app.R;
import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.business.requst.DeviceRequest;
import com.wrriormedia.app.model.StatusModel;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.NetUtil;
import com.wrriormedia.library.util.StringUtil;
import com.wrriormedia.library.widget.LoadingUpView;

import io.vov.vitamio.LibsChecker;

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
        initVariable();
        initViews();
        getReadCmd();
    }

    private void initVariable() {
        if (!LibsChecker.checkVitamioLibs(this)) {
            // TODO 初始化Vitamio库
            return;
        }
        EventBus.getDefault().register(this);
        mLoadingUpView = new LoadingUpView(this, false);
    }

    private void initViews() {
        mTvImmeInfo = (TextView) findViewById(R.id.tv_imie);
        mTvSerial = (TextView) findViewById(R.id.tv_serial);
        mTvAddress = (TextView) findViewById(R.id.tv_address);
    }

    private void getReadCmd() {
        if (NetUtil.isNetworkAvailable()) {
            // 如果网络可以，直接请求ready接口，如果还没有准备好就等待网络开启广播
            StatusModel model = (StatusModel) SharedPreferenceUtil.getObject(WrriormediaApplication.getInstance().getBaseContext(), StatusModel.class.getName(), StatusModel.class);
            if (null == model || StringUtil.isNullOrEmpty(model.getSerial())) {
                new VerifyTask().execute();
            } else {
                ActionResult result = new ActionResult();
                result.ResultCode = ActionResult.RESULT_CODE_SUCCESS;
                EventBus.getDefault().post(result);
            }
        } else {
            showLoadingUpView(mLoadingUpView, "等待网络初始化...");
        }
    }

    public void onEventMainThread(ActionResult result) {
        dismissLoadingUpView(mLoadingUpView);
        if (ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
            StatusModel model = (StatusModel) SharedPreferenceUtil.getObject(WrriormediaApplication.getInstance().getBaseContext(), StatusModel.class.getName(), StatusModel.class);
            String ready = model.getReady();

            // TODO 调试
            startActivity(new Intent(LoadingActivity.this, MainActivity.class));
            finish();

//            if ("0".equals(ready)) {
//                mTvImmeInfo.setText(getString(R.string.imie_info, PackageUtil.getTerminalSign()));
//                mTvSerial.setText(getString(R.string.serial_info, model.getSerial()));
//                mTvAddress.setText(getString(R.string.address_info, model.getAddress()));
//            } else if ("1".equals(ready)) {
//                startActivity(new Intent(LoadingActivity.this, MainActivity.class));
//                finish();
//            }
        } else {
            //TODO 记录日志
            showErrorMsg(result);
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
