package com.wrriormedia.app.business.requst;

import com.wrriormedia.app.app.WrriormediaApplication;
import com.wrriormedia.app.business.dao.DBMgr;
import com.wrriormedia.app.business.manager.SystemManager;
import com.wrriormedia.app.common.ConstantSet;
import com.wrriormedia.app.common.ServerAPIConstant;
import com.wrriormedia.app.model.CmdModel;
import com.wrriormedia.app.model.DownloadModel;
import com.wrriormedia.app.model.DownloadTextModel;
import com.wrriormedia.app.model.StatusModel;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.app.util.SharedPreferenceUtil;
import com.wrriormedia.library.app.JsonResult;
import com.wrriormedia.library.http.HttpClientUtil;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.NetUtil;
import com.wrriormedia.library.util.PackageUtil;
import com.wrriormedia.library.util.StringUtil;

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

    public static ActionResult ready() {
        ActionResult result = new ActionResult();
        String url = ServerAPIConstant.getAPIUrl(ServerAPIConstant.ACTION_READY);
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_ID, PackageUtil.getTerminalSign()));
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_EQ_VERSION, "WM001"));
        String phoneNum = PackageUtil.getLine1Number();
        if (StringUtil.isNullOrEmpty(phoneNum)) {
            phoneNum = "0";
        }
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_SIM, phoneNum));
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_OPERATOR, PackageUtil.getProvidersName()));
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
                result.ResultCode = jsonResult.Code;
            } else {
                result.ResultCode = ActionResult.RESULT_CODE_NET_ERROR;
            }
        } catch (Exception e) {
            result.ResultCode = ActionResult.RESULT_CODE_NET_ERROR;
            EvtLog.w(TAG, e);
        }
        return result;
    }

    public static ActionResult update(String alert) {
        ActionResult result = new ActionResult();
        String url = ServerAPIConstant.getAPIUrl(ServerAPIConstant.ACTION_CMD_UPDATE);
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_ID, PackageUtil.getTerminalSign()));
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_KEY, alert));
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_STATUS, "1"));
        try {
            JsonResult jsonResult = HttpClientUtil.post(url, null, postParams);
            if (jsonResult != null) {
                result.ResultObject = jsonResult.Msg;
                result.ResultCode = jsonResult.Code;
            } else {
                result.ResultCode = ActionResult.RESULT_CODE_NET_ERROR;
            }
        } catch (Exception e) {
            result.ResultCode = ActionResult.RESULT_CODE_NET_ERROR;
            EvtLog.w(TAG, e);
        }
        return result;
    }

    public static ActionResult cmd() {
        ActionResult result = new ActionResult();
        String url = ServerAPIConstant.getAPIUrl(ServerAPIConstant.ACTION_CMD_GET);
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_ID, PackageUtil.getTerminalSign()));
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_VERSION, PackageUtil.getVersionName()));
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_MODIFY, SystemManager.getModifyTime(url) + ""));
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_NET, NetUtil.isWifi(WrriormediaApplication.getInstance().getBaseContext()) ? "wifi" : "3g"));
        try {
            JsonResult jsonResult = HttpClientUtil.get(url, null, postParams);
            if (jsonResult != null) {
                if (jsonResult.isOK()) {
                    CmdModel model = jsonResult.getData(CmdModel.class); // 保存到本地
                    SharedPreferenceUtil.saveObject(WrriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, model);
                    result.ResultObject = model;
                } else {
                    result.ResultObject = jsonResult.Msg;
                }
                result.ResultCode = jsonResult.Code;
            } else {
                result.ResultCode = ActionResult.RESULT_CODE_NET_ERROR;
            }
        } catch (Exception e) {
            result.ResultCode = ActionResult.RESULT_CODE_NET_ERROR;
            EvtLog.w(TAG, e);
        }
        return result;
    }

    public static ActionResult getAdDownload(final int aid, final boolean isTextAd) {
        ActionResult result = new ActionResult();
        String url = ServerAPIConstant.getAPIUrl(ServerAPIConstant.ACTION_AD_GET);
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_ID, PackageUtil.getTerminalSign()));
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_AID, aid + ""));
        try {
            JsonResult jsonResult = HttpClientUtil.get(url, null, postParams);
            if (jsonResult != null) {
                if (jsonResult.isOK()) {
                    SystemManager.setModifyTime(url);// 更新本地的上次请求时间
                    if (isTextAd) {
                        DownloadTextModel textModel = jsonResult.getData(DownloadTextModel.class);
                        if (null != textModel) {
                            textModel.setAid(aid);
                            DownloadTextModel localTextModel = DBMgr.getBaseModel(DownloadTextModel.class, DownloadTextModel.WHERE_CASE_SUB + " = " + aid);
                            if (null == localTextModel) {
                                EvtLog.d("aaa", "服务器新增的" + aid);
                                DBMgr.saveModel(textModel);
                            } else {
                                EvtLog.d("aaa", "这个是服务器修改本地数据库的" + aid);
                                // TODO 先删除本地已经下载的数据
                                DBMgr.saveModel(textModel, DownloadTextModel.WHERE_CASE, "" + aid);
                            }
                        }
                        result.ResultObject = textModel;
                    } else {
                        DownloadModel downloadModel = jsonResult.getData(DownloadModel.class);
                        if (null != downloadModel) {
                            downloadModel.setAid(aid);
                            DownloadModel localModel = DBMgr.getBaseModel(DownloadModel.class, DownloadModel.WHERE_CASE_SUB + " = " + aid);
                            if (null == localModel) {
                                EvtLog.d("aaa", "服务器新增的" + aid);
                                DBMgr.saveModel(downloadModel);
                            } else {
                                EvtLog.d("aaa", "这个是服务器修改本地数据库的" + aid);
                                // TODO 先删除本地已经下载的数据
                                downloadModel.setIsDownloadFinish(1); // TODO 正式环境去掉
                                DBMgr.saveModel(downloadModel, DownloadModel.WHERE_CASE, "" + aid);
                            }
                        }
                        result.ResultObject = downloadModel;
                    }
                } else {
                    result.ResultObject = jsonResult.Msg;
                }
                result.ResultCode = jsonResult.Code;
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
