package com.wrriormedia.app.business.requst;

import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.common.ServerAPIConstant;
import com.wrriormedia.app.model.StatusModel;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.library.app.JsonResult;
import com.wrriormedia.library.http.HttpClientUtil;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.PackageUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备请求类
 *
 * @author zou.sq
 */
public class DeviceRequest {

    private static final String TAG = "DeviceRequest";

    public static ActionResult verifyDevice() {
        ActionResult result = new ActionResult();
        String url = ServerAPIConstant.getAPIUrl(ServerAPIConstant.ACTION_READY);
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_ID, PackageUtil.getTerminalSign()));
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_SIM, PackageUtil.getLine1Number()));
        try {
            JsonResult jsonResult = HttpClientUtil.post(url, null, postParams);
            if (jsonResult != null) {
                if (jsonResult.isOK()) {
                    StatusModel model = jsonResult.getData(StatusModel.class);
                    if (null != model) {
                        SharedPreferenceUtil.saveObject(WrriormediaApplication.getInstance().getBaseContext(), StatusModel.class.getName(), model);
                    }
                    result.ResultObject = model;
                } else {
                    result.ResultObject = jsonResult.Msg;
                }
                result.ResultCode = jsonResult.State;
            } else {
                result.ResultCode = ActionResult.RESULT_CODE_NET_ERROR;
            }
        } catch (Exception e) {
            result.ResultCode = ActionResult.RESULT_CODE_NET_ERROR;
            EvtLog.w(TAG, e);
        }
        return result;
    }
}
