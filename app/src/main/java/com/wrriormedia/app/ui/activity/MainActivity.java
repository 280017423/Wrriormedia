package com.wrriormedia.app.ui.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.wrriormedia.app.R;
import com.wrriormedia.app.business.requst.DeviceRequest;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.library.eventbus.EventBus;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getCmd();
    }

    private void getCmd(){
        new CmdTask().execute();
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
            if (null == result) {
                return;
            }
            EventBus.getDefault().post(result);
            if (!ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {
                //TODO 记录日志
            }
        }
    }
}
