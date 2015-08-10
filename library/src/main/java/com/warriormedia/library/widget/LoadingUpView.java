package com.warriormedia.library.widget;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.warriormedia.library.R;
import com.warriormedia.library.util.EvtLog;
import com.warriormedia.library.util.StringUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 加载等待框 此等待框基于单个Activity内部显示
 *
 * @version 2012-11-1 下午1:31:10 xu.xb
 */
public class LoadingUpView {
    private static final int Default_DialogTimeOut = 300;
    private static final int START_ANIM = 3;
    private static final int ERROR = 4;
    private static final int DISMISS = 5;
    private static final int SHOW_DIALOG = 6;
    private static final int Handler_TimeTask_Timeout = 7;
    private int mDialogTimeOut = Default_DialogTimeOut;
    private LinearLayout mLoadingView;
    private Animation mAnim;
    private ImageView mIvLoading;
    private TextView mTvMsg;
    private Activity mCurrentActivity;
    private Boolean mIsShowing = false;
    private View mParentView;
    private ProgressDialog mProgressDialog;
    private boolean mIsFrameLayoutParentView;

    private int mScreenWidth;
    private int mScreenHeight;
    // 是否为限制用户操作view，为透明，显示后一定时间自动隐藏
    private boolean mIsLimitView;

    private DialogTimeTask mDialogTimeTask;

    // 是否阻塞用户操作
    private boolean mIsBlock;
    // 是否按返回键可以消失，使用阻塞时设置此参数
    private boolean mCancelable;
    // 按返回键关闭Activity
    private boolean mCancelCloseActivity;
    private int mResId;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_ANIM:
                    if (mIvLoading != null) {
                        mIvLoading.startAnimation(mAnim);
                    }
                    break;
                case ERROR:
                    break;
                case DISMISS:
                    if (mIsBlock) {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                    } else {
                        removeLoadingView();
                        mLoadingView = null;
                    }
                    break;
                case SHOW_DIALOG:
                    showProgressDialog(mCurrentActivity, String.valueOf(msg.obj));
                    break;
                case Handler_TimeTask_Timeout:
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    cancelTimeTask();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 构造方法
     *
     * @param act     Activity
     * @param isBlock 是否阻塞用户操作
     */
    public LoadingUpView(Activity act, boolean isBlock) {
        this.mIsBlock = isBlock;
        init(act);
    }

    /**
     * 构造方法
     *
     * @param act Activity
     */
    public LoadingUpView(Activity act) {
        init(act);
    }

    /**
     * setmIsLimitView 是否是显示限制用户点击view
     *
     * @param mIsLimitView
     * @return void
     */
    public void setmIsLimitView(boolean mIsLimitView) {
        this.mIsLimitView = mIsLimitView;
        this.mIsBlock = mIsLimitView;
    }

    public void setmIsLimitAndShowTime(boolean mIsLimitView, int dialogTimeOut) {
        this.mIsLimitView = mIsLimitView;
        this.mIsBlock = mIsLimitView;
        this.mDialogTimeOut = dialogTimeOut;
    }

    /**
     * 初始化
     *
     * @param act Activity
     */
    private void init(Activity act) {
        this.mCurrentActivity = act;
        if (act.getWindow() == null) {
            if (EvtLog.IS_DEBUG_LOGGABLE) {
                mHandler.sendEmptyMessage(ERROR);
            }
            return;
        }
        mParentView = act.getWindow().getDecorView().getRootView();
        mIsFrameLayoutParentView = mParentView instanceof FrameLayout;
    }

    private void initView() {
        mLoadingView = (LinearLayout) View.inflate(mCurrentActivity, R.layout.popup, null);
        mTvMsg = (TextView) mLoadingView.findViewById(R.id.tv_popup);
        mIvLoading = (ImageView) mLoadingView.findViewById(R.id.img_popup);
        if (0 != mResId) {
            mIvLoading.setImageResource(mResId);
        }
        mAnim = AnimationUtils.loadAnimation(mCurrentActivity, R.anim.popup_loading);
    }

    public void setLoadView(int resId) {
        mResId = resId;
    }

    /**
     * 显示
     */
    public void showPopup() {
        // showPopup(PackageUtil.getString(R.string.downloading_data));
        showPopup("");
    }

    /**
     * show EvtLoadingUpView
     *
     * @param msg 内容
     */
    public void showPopup(final String msg) {
        // 在show之前判断是否已经关闭
        if (mCurrentActivity != null && mCurrentActivity.isFinishing()) {
            return;
        }
        // 阻塞
        if (mIsBlock) {
            if (mIsLimitView) {
                setTimeTask();
            }
            changeShowStatus(true);
            Message mMsg = new Message();
            mMsg.what = SHOW_DIALOG;
            mMsg.obj = msg;
            mHandler.sendMessage(mMsg);
        } else {
            if (mParentView == null) {
                return;
            }
            if (mLoadingView == null) {
                initView();
            }
            removeLoadingView();
            addLoadingView(msg);
        }

    }

    /**
     * dismiss EvtLoadingUpView
     */
    public void dismiss() {
        if (mIsShowing) {
            changeShowStatus(false);
            mHandler.sendEmptyMessage(DISMISS);
        }
    }

    /**
     * 设置是否阻塞用户操作，在使用showPopup方法前调用
     *
     * @param isBlock 是否阻塞用户操作
     */
    public void setIsBlock(boolean isBlock) {
        this.mIsBlock = isBlock;
    }

    /**
     * 是否按返回键可以消失，使用阻塞时设置此参数
     *
     * @param mCancelable 是否按返回键可以消失
     */
    public void setCancelable(boolean mCancelable) {
        this.mCancelable = mCancelable;
    }

    /**
     * 是否按返回键关闭Activity(show之前调用)
     *
     * @param cancelCloseActivity 是否关闭Activity
     */
    public void setCancelCloseActivity(boolean cancelCloseActivity) {
        this.mCancelCloseActivity = cancelCloseActivity;
    }

    private void removeLoadingView() {
        if (mIsFrameLayoutParentView) {
            ((FrameLayout) mParentView).removeView(mLoadingView);
        }
    }

    private void addLoadingView(String msg) {
        if (mIsFrameLayoutParentView) {
            if (mScreenWidth == 0) {
                getDisplayConfig();
            }
            ((FrameLayout) mParentView)
                    .addView(mLoadingView, new FrameLayout.LayoutParams(mScreenWidth, mScreenHeight));
            if (!mIsLimitView) {
                mIvLoading.setVisibility(View.VISIBLE);
                if (StringUtil.isNullOrEmpty(msg)) {
                    mTvMsg.setVisibility(View.GONE);
                } else {
                    mTvMsg.setVisibility(View.VISIBLE);
                    mTvMsg.setText(msg);
                }
            } else {
                mIvLoading.setVisibility(View.INVISIBLE);
                mTvMsg.setVisibility(View.INVISIBLE);
            }

            changeShowStatus(true);
            mHandler.sendEmptyMessage(START_ANIM);
        }
    }

    /**
     * 保持loadingview状态 在Activity onResume() 方法中调用
     */
    public void onResume() {
        if (mIsShowing && mLoadingView != null) {
            mHandler.sendEmptyMessage(START_ANIM);
        }
    }

    /**
     * 返回EvtLoadingUpView是否显示
     *
     * @return EvtLoadingUpView是否显示
     */
    public boolean isShowing() {
        return mIsShowing;
    }

    private void changeShowStatus(boolean bShowing) {
        this.mIsShowing = bShowing;
    }

    /**
     * showProgressDialog 创建显示
     *
     * @param act Activity
     * @param msg 消息
     */
    public void showProgressDialog(final Activity act, String msg) {
        Activity activity = act;
        if (act.isFinishing()) {
            return;
        }
        if (act.getParent() != null) {
            activity = act.getParent();
        }
        if (activity.getParent() != null) {
            activity = activity.getParent();
        }
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // 如果退出要关闭Activity，就不能设置是否取消，一定可以取消
            if (mCancelCloseActivity) {
                mProgressDialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        if (!act.isFinishing()) {
                            act.finish();
                        }
                    }
                });
            }
            // 在show之前判断是否已经关闭
            if (activity.isFinishing()) {
                return;
            }
            mProgressDialog.show();
            View view = View.inflate(activity, R.layout.popup, null);
            view.setBackgroundColor(Color.TRANSPARENT);
            ImageView mRetote = (ImageView) view.findViewById(R.id.img_popup);
            TextView mLoadMsg = (TextView) view.findViewById(R.id.tv_popup);

            if (0 != mResId) {
                mRetote.setImageResource(mResId);
            }
            if (!mIsLimitView) {
                mRetote.setVisibility(View.VISIBLE);
                if (mAnim == null) {
                    mAnim = AnimationUtils.loadAnimation(mCurrentActivity, R.anim.popup_loading);
                }
                mAnim.setInterpolator(new LinearInterpolator());
                mRetote.startAnimation(mAnim);
                if (StringUtil.isNullOrEmpty(msg)) {
                    mLoadMsg.setVisibility(View.GONE);
                } else {
                    mLoadMsg.setVisibility(View.VISIBLE);
                    mLoadMsg.setText(msg);
                }
            } else {
                mLoadMsg.setVisibility(View.INVISIBLE);
                mRetote.setVisibility(View.INVISIBLE);
            }
            Window window = mProgressDialog.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.alpha = 1f;
            lp.dimAmount = 0.000001f; // 修改在2.1和2.2的模拟器上黑屏的问题
            window.setAttributes(lp);
            window.setContentView(view);

            mProgressDialog.setIndeterminate(true);
            // 如果退出要关闭Activity，就不能设置是否取消，一定可以取消
            if (!mCancelCloseActivity) {
                mProgressDialog.setCancelable(mCancelable);
            } else {
                mProgressDialog.setCancelable(true);
            }
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
    }

    /**
     * 超时计时器
     */
    private void setTimeTask() {
        cancelTimeTask();
        Timer mTimer = new Timer();
        mDialogTimeTask = new DialogTimeTask();
        mTimer.schedule(mDialogTimeTask, mDialogTimeOut);
    }

    private void cancelTimeTask() {
        if (mDialogTimeTask != null) {
            mDialogTimeTask.cancel();
            mDialogTimeTask = null;
        }
    }

    /*
     * 获取分辨率
     */
    private void getDisplayConfig() {
        DisplayMetrics dm = new DisplayMetrics();
        mCurrentActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        // 宽度
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        Rect frame = new Rect();
        mCurrentActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
    }

    class DialogTimeTask extends TimerTask {
        @Override
        public void run() {
            Message msg = mHandler.obtainMessage(Handler_TimeTask_Timeout);
            mHandler.sendMessage(msg);
        }
    }
}
