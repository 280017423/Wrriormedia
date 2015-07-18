package com.wrriormedia.app.ui.activity;

import android.content.Intent;
import android.net.Uri;
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
import com.wrriormedia.app.model.PushVersionModel;
import com.wrriormedia.app.service.AppUpdateService;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.StringUtil;

import java.io.File;

/**
 * @author zou.sq 强制升级界面
 */
public class UpdateStateActivity extends HtcBaseFragmentActivity {
    private TextView mTxtProgress;
    private TextView mTxtState;
    private ProgressBar mProgressBar;
    private PushVersionModel mVersionModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_download);
        initVariables();
        initViews();
        startDownloadService();
    }

    private void initVariables() {
        mVersionModel = (PushVersionModel) getIntent().getSerializableExtra(PushVersionModel.class.getName());
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
        intent.putExtra(PushVersionModel.class.getName(), mVersionModel);
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
        if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_APP_STATUS_FINISH)) {
            mTxtState.setText(getString(R.string.download_success));
            File file = (File) model.getEventBusObject();
            Intent installHideIntent = new Intent("android.intent.action.VIEW.HIDE");
            installHideIntent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()),
                    "application/vnd.android.package-archive");
            startActivity(installHideIntent);

            //boolean success = FileUtil.silentInstall(file);
            /*String success = FileUtil.installApkFile(file);
            toast(">>> success = " + success);
            if (StringUtil.isNullOrEmpty(success)) {
                toast(getString(R.string.install_apk_fail));
            } else {
                toast(getString(R.string.install_apk_success));
                // TODO 执行回调接口
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                String packageName = WrriormediaApplication.getInstance().getBaseContext().getPackageName();
                EvtLog.d("aa", ">>>> packageName = " + packageName);
                //toast(">>>> 旧应用 启动新应用   packageName = " + packageName);
                ComponentName cn = new ComponentName(packageName, packageName + ".ui.activity.LoadingActivity");
                intent.setComponent(cn);
                startActivity(intent);
            }*/
            finish();
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_APP_STATUS_FAILED)) {
            toast((String) model.getEventBusObject());
            toast(getString(R.string.download_failed));
            finish();
        } else if (model.getEventBusAction().equals(ConstantSet.KEY_EVENT_ACTION_DOWNLOAD_APP_STATUS_NORMAL)) {
            int progress = (Integer) model.getEventBusObject();
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
