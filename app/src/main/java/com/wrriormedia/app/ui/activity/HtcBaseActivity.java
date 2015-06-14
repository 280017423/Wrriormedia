package com.wrriormedia.app.ui.activity;

import android.os.Bundle;

import com.wrriormedia.app.R;
import com.wrriormedia.app.ui.widget.dialog.ISimpleDialogListener;
import com.wrriormedia.app.util.ActionResult;
import com.wrriormedia.library.authentication.BaseActionResult;
import com.wrriormedia.library.imageloader.core.ImageLoader;
import com.wrriormedia.library.util.QJActivityManager;
import com.wrriormedia.library.widget.LoadingUpView;

class HtcBaseActivity extends HtcBaseFragmentActivity implements ISimpleDialogListener {
    protected ImageLoader mImageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QJActivityManager.getInstance().pushActivity(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QJActivityManager.getInstance().popActivity(this);
    }

    /**
     * toast显示错误消息
     *
     * @param result ActionResult
     */
    protected void showErrorMsg(BaseActionResult result) {
        showErrorToast(result, getResources().getString(R.string.network_is_not_available), false);
    }

    /**
     * toast显示错误消息
     *
     * @param result           ActionResult
     * @param noMsgReturnToast 若result.obj中没有信息，是否显示默认信息
     */
    protected void showErrorMsg(ActionResult result, boolean noMsgReturnToast) {
        showErrorToast(result, getResources().getString(R.string.network_is_not_available), noMsgReturnToast);
    }

    /**
     * toast显示错误消息
     *
     * @param result      ActionResult
     * @param netErrorMsg 网络异常时显示消息
     */
    protected void showErrorMsg(ActionResult result, String netErrorMsg) {
        showErrorToast(result, netErrorMsg, false);
    }

    /**
     * 显示错误消息
     *
     * @param result           ActionResult
     * @param netErrorMsg      网络异常时显示消息
     * @param noMsgReturnToast 是否需要无返回值的提示
     */
    private void showErrorToast(BaseActionResult result, String netErrorMsg, boolean noMsgReturnToast) {
        if (result == null) {
            return;
        }
        if (ActionResult.RESULT_CODE_NET_ERROR.equals(result.ResultCode)) {
            toast(netErrorMsg);
        } else if (result.ResultObject != null) {
            // 增加RESULT_CODE_ERROR值时也弹出网络异常
            if (ActionResult.RESULT_CODE_NET_ERROR.equals(result.ResultObject.toString())) {
                toast(netErrorMsg);
            } else {
                toast(result.ResultObject.toString());
            }
        }
    }

    public boolean showLoadingUpView(LoadingUpView loadingUpView) {
        return showLoadingUpView(loadingUpView, "");
    }

    public boolean showLoadingUpView(LoadingUpView loadingUpView, String info) {
        if (loadingUpView != null && !loadingUpView.isShowing()) {
            if (null == info) {
                info = "";
            }
            loadingUpView.showPopup(info);
            return true;
        }
        return false;
    }

    public boolean dismissLoadingUpView(LoadingUpView loadingUpView) {
        if (loadingUpView != null && loadingUpView.isShowing()) {
            loadingUpView.dismiss();
            return true;
        }
        return false;
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {

    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {

    }

    @Override
    public void onCancelled(int requestCode) {

    }
}
