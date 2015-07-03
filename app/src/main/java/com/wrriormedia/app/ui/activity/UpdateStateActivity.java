package com.wrriormedia.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wrriormedia.app.R;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.model.EventBusModel;
import com.wrriormedia.app.model.VersionModel;
import com.wrriormedia.app.service.AppUpdateService;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.FileUtil;
import com.wrriormedia.library.util.StringUtil;

import java.io.File;

/**
 * @author zou.sq 强制升级界面
 */
public class UpdateStateActivity extends HtcBaseFragmentActivity {
    private TextView mTxtProgress;
    private TextView mTxtState;
    private ProgressBar mProgressBar;
    private VersionModel mVersionModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_download);
        initVariables();
        initViews();
        startDownloadService();
    }

    private void initVariables() {
        mVersionModel = (VersionModel) getIntent().getSerializableExtra(VersionModel.class.getName());
        if (null == mVersionModel || StringUtil.isNullOrEmpty(mVersionModel.getUrl())) {
            finish();
        }
        EventBus.getDefault().register(this);
        getWindow().getDecorView().setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private void initViews() {
        mTxtProgress = (TextView) findViewById(R.id.tv_common_progress);
        mTxtState = (TextView) findViewById(R.id.tv_common_title);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_common_progress);
    }

    private void startDownloadService() {
        Intent intent = new Intent(this, AppUpdateService.class);
        intent.putExtra(VersionModel.class.getName(), mVersionModel);
        startService(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    public void onEventMainThread(EventBusModel model) {
        if (null == model || StringUtil.isNullOrEmpty(model.getEventBusAction())) {
            return;
        }
        if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FINISH)) {
            mTxtState.setText(getString(R.string.download_success));
            File file = (File) model.getEventBusObject();
            toast("需要静默安装");
            FileUtil.silentInstall(file); // TODO 需要静默安装成功之后才更新本地时间
            finish();
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_FAILED)) {
            toast(getString(R.string.download_failed));
            finish();
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_STATUS_NORMAL)) {
            int progress = (int) model.getEventBusObject();
            if (progress > 100) {
                progress = 100;
            }
            mProgressBar.setProgress(progress);
            mTxtProgress.setText(progress + "%");
            mTxtState.setText(R.string.download_app_info);
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}