package com.warriormedia.app.business.requst;

import com.warriormedia.app.app.WarriormediaApplication;
import com.warriormedia.app.business.dao.DBMgr;
import com.warriormedia.app.business.manager.SystemManager;
import com.warriormedia.app.common.ConstantSet;
import com.warriormedia.app.common.ServerAPIConstant;
import com.warriormedia.app.model.CmdModel;
import com.warriormedia.app.model.DownloadModel;
import com.warriormedia.app.model.DownloadTextModel;
import com.warriormedia.app.model.StatusModel;
import com.warriormedia.app.util.ActionResult;
import com.warriormedia.app.util.SharedPreferenceUtil;
import com.warriormedia.library.app.JsonResult;
import com.warriormedia.library.http.HttpClientUtil;
import com.warriormedia.library.util.EvtLog;
import com.warriormedia.library.util.NetUtil;
import com.warriormedia.library.util.PackageUtil;
import com.warriormedia.library.util.StringUtil;

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
    public static String aidFlag = "";
    public static int adType = -1;

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
                        SharedPreferenceUtil.saveObject(WarriormediaApplication.getInstance().getBaseContext(), StatusModel.class.getName(), model);
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

    public static ActionResult uploadLog(String logInfo) {
        EvtLog.d("aaa", "开始上传日志");
        ActionResult result = new ActionResult();
        String url = ServerAPIConstant.getAPIUrl(ServerAPIConstant.ACTION_LOG_UPLOAD);
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_ID, PackageUtil.getTerminalSign()));
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_LOG, logInfo));
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
        postParams.add(new BasicNameValuePair(ServerAPIConstant.ACTION_KEY_NET, NetUtil.isWifi(WarriormediaApplication.getInstance().getBaseContext()) ? "wifi" : "3g"));
        try {
            JsonResult jsonResult = HttpClientUtil.get(url, null, postParams);
            if (jsonResult != null) {
                if (jsonResult.isOK()) {
                    CmdModel model = jsonResult.getData(CmdModel.class); // 保存到本地
                    SharedPreferenceUtil.saveValue(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, ConstantSet.KEY_LOG_TIME, model.getLog_time());
                    SharedPreferenceUtil.saveObject(WarriormediaApplication.getInstance().getBaseContext(), ConstantSet.KEY_GLOBAL_CONFIG_FILENAME, model);
                    SystemManager.setModifyTime(ServerAPIConstant.ACTION_KEY_MODIFY);// 更新model时间
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
            adType = -1;
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
                                aidFlag = "服务器新增的: " + aid;
                                DBMgr.saveModel(textModel);
                            } else {
                                aidFlag = "服务器修改的: " + aid;
                                DBMgr.saveModel(textModel, DownloadTextModel.WHERE_CASE, "" + aid);
                            }
                        }
                        result.ResultObject = textModel;
                    } else {
                        DownloadModel downloadModel = jsonResult.getData(DownloadModel.class);
                        if (null != downloadModel) {
                            downloadModel.setAid(aid);
                            if (2 == downloadModel.getType() && null != downloadModel.getImage()
                                    && !StringUtil.isNullOrEmpty(downloadModel.getImage().getMd5())) {
                                downloadModel.setIsImageFinish(1);
                                adType = 2;
                            }
                            DownloadModel localModel = DBMgr.getBaseModel(DownloadModel.class, DownloadModel.WHERE_CASE_SUB + " = " + aid);
                            if (null == localModel) {
                                EvtLog.d("aaa", "服务器新增的" + aid);
                                aidFlag = "服务器新增的: " + aid;
                                DBMgr.saveModel(downloadModel);
                            } else {
                                EvtLog.d("aaa", "这个是服务器修改本地数据库的" + aid);
                                aidFlag = "服务器修改的: " + aid;
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

    public static ActionResult wifiCheck() {
        ActionResult result = new ActionResult();
        String url = "http://api.warriormedia.cn" + ServerAPIConstant.ACTION_WIFI_CHECK;
        try {
            JsonResult jsonResult = HttpClientUtil.get(url, null, null);
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

}
