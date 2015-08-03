package com.wrriormedia.app.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.widget.Toast;

import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.business.requst.DeviceRequest;
import com.wrriormedia.app.model.StatusModel;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.library.eventbus.EventBus;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.StringUtil;

public class NetWorkChangeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        //String action = intent.getAction();
        //if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
                for (int i = 0; i < networkInfo.length; i++) {
                    State state = networkInfo[i].getState();
                    if (State.CONNECTED == state) {
                        Toast.makeText(context, "网络打开", Toast.LENGTH_LONG).show();
                        StatusModel model = (StatusModel) SharedPreferenceUtil.getObject(WrriormediaApplication.getInstance().getBaseContext(), StatusModel.class.getName(), StatusModel.class);
                        if (null == model || StringUtil.isNullOrEmpty(model.getSerial())) {
                            EvtLog.d("aaa", "NetWorkChangeBroadcastReceiver, serial is null, start VerifyTask");
                            new VerifyTask().execute();
                        } /*else {
                            EvtLog.d("aaa", "NetWorkChangeBroadcastReceiver, model is not null, return jump to MainActivity");
                            ActionResult result = new ActionResult();
                            result.ResultCode = ActionResult.RESULT_CODE_SUCCESS;
                            EventBus.getDefault().post(result);
                        }*/
                        return;
                    }
                }
            }
        /*} else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            EvtLog.d("aaa", "BOOT_COMPLETED, action = " + action);
            ComponentName cn = new ComponentName("com.wrriormedia.app", "com.wrriormedia.app.ui.activity.LoadingActivity");
            Intent i = new Intent();
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setComponent(cn);
            context.startActivity(i);
        }*/
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