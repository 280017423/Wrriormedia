package com.warriormedia.app.business.manager;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.warriormedia.app.business.requst.DeviceRequest;
import com.warriormedia.app.model.WifiModel;
import com.warriormedia.app.util.ActionResult;
import com.warriormedia.app.util.WifiAdmin;
import com.warriormedia.library.util.StringUtil;

public class WifiManager {
    private static final int CHECK_WIFI_TIME = 10000;
    private static final int MSG_CHECK_WIFI = 1;
    private int requestTimes;
    private WifiAdmin mWifiAdmin;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            requestTimes++;
            if (requestTimes <= 3) {
                startWifiCheckTask();
                mHandler.sendEmptyMessageDelayed(MSG_CHECK_WIFI, CHECK_WIFI_TIME);
            }
        }
    };

    public void connectWifi(Context context, WifiModel wifiModel) {
        requestTimes = 0;
        mWifiAdmin = new WifiAdmin(context);
        openMobileData();
        if (!StringUtil.isNullOrEmpty(wifiModel.getSsid())) {
            mWifiAdmin.openWifi();
            mWifiAdmin.addNetwork(wifiModel.getSsid(), wifiModel.getPassword(), wifiModel.getType());
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_WIFI, CHECK_WIFI_TIME);
        } else {
            open3G();
        }
    }

    private void open3G() {
        mWifiAdmin.closeWifi();
        openMobileData();
    }

    private void openMobileData() {
        if (!mWifiAdmin.getMobileDataState(null)) {
            mWifiAdmin.setMobileData(true);
        }
    }

    private void startWifiCheckTask() {
        new WifiCheckTask().execute();
    }

    class WifiCheckTask extends AsyncTask<Void, Void, ActionResult> {

        public WifiCheckTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ActionResult doInBackground(Void... params) {
            return DeviceRequest.wifiCheck();
        }

        @Override
        protected void onPostExecute(ActionResult result) {
            if (!ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
                // WIFI 不可用，重新打开3G
                if (requestTimes >= 3) {
                    mHandler.removeMessages(MSG_CHECK_WIFI);
                    open3G();
                }
            } else {
                mHandler.removeMessages(MSG_CHECK_WIFI);
            }
        }
    }
}