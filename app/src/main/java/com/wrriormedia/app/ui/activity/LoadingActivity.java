package com.wrriormedia.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.wrriormedia.app.R;
import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.model.StatusModel;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.PackageUtil;

/**
 * app启动界面
 */
public class LoadingActivity extends HtcBaseActivity {

    private TextView mTvImmeInfo;
    private TextView mTvSerial;
    private TextView mTvAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        initVariable();
        initViews();
    }

    private void initVariable() {
        EventBus.getDefault().register(this);
    }
    private void initViews() {
        mTvImmeInfo = (TextView)findViewById(R.id.tv_imie);
        mTvSerial = (TextView)findViewById(R.id.tv_serial);
        mTvAddress = (TextView)findViewById(R.id.tv_address);
    }

    public void onEventMainThread(ActionResult result) {
        if (ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
            StatusModel model = (StatusModel) SharedPreferenceUtil.getObject(WrriormediaApplication.getInstance().getBaseContext(), StatusModel.class.getName(), StatusModel.class);
            String ready = model.getReady();
            if ("0".equals(ready)) {
                mTvImmeInfo.setText(getString(R.string.imie_info, PackageUtil.getTerminalSign()));
                mTvSerial.setText(getString(R.string.serial_info, model.getSerial()));
                mTvAddress.setText(getString(R.string.address_info, model.getAddress()));
            } else if ("1".equals(ready)) {
                startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                finish();
            }
        } else {
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
}
