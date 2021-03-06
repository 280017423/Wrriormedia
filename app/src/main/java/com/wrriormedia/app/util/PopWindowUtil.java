package com.wrriormedia.app.util;

import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.wrriormedia.app.listener.OnPopDismissListener;

/**
 * PoPWindow 弹出窗口控件
 *
 * @author zou.sq
 */
public class PopWindowUtil {
    private View mMenuView;
    private PopupWindow mPopupWindow;
    private OnPopDismissListener mListener;

    /**
     * @param menuView 需要显示的view
     * @param listener PopWindow消失监听
     */
    public PopWindowUtil(View menuView, OnPopDismissListener listener) {
        this.mMenuView = menuView;
        mListener = listener;
        initView();
    }

    private void initView() {
        mPopupWindow = new PopupWindow(mMenuView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mPopupWindow.setContentView(mMenuView);
        mMenuView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dismiss();
            }
        });
        mPopupWindow.setFocusable(true);
        // 点击popupwindow窗口之外的区域popupwindow消失
        ColorDrawable dw = new ColorDrawable(0x00);
        mPopupWindow.setBackgroundDrawable(dw);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                if (null != mListener) {
                    mListener.onDismiss();
                }
            }
        });
    }

    /**
     * 消失Pop
     */
    public void dismiss() {
        if (null == mPopupWindow) {
            return;
        }
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    /**
     * 显示Pop
     *
     * @param view PopWindow相对位置的视图
     */
    public void showAsDropDown(View view) {
        if (null == mPopupWindow) {
            return;
        }
        if (!mPopupWindow.isShowing()) {
            mPopupWindow.showAsDropDown(view);
        }
    }

    /**
     * 显示Pop
     */
    public void show() {
        show(Gravity.CENTER);
    }

    /**
     * 显示Pop
     */
    public void show(int gravity) {
        if (null == mPopupWindow) {
            return;
        }
        if (!mPopupWindow.isShowing()) {
            mPopupWindow.showAtLocation(mMenuView, gravity, 0, 0);
        }
    }

    /**
     * 显示Pop
     */
    public void showAndDismiss() {
        show();
        new CountDownTimer(3000, 3000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                dismiss();
            }
        }.start();
    }

}