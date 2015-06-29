package com.wrriormedia.library.widget.pulltorefresh;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wrriormedia.library.R;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.StringUtil;
import com.wrriormedia.library.widget.pulltorefresh.PullToRefreshBase.Mode;

/**
 * 上方的布局类
 *
 * @author wang.xy
 * @version 2012-8-14，王先佑，重构文本、注释等
 * @since 2012-8-14
 */
public class HeaderLoadingLayout extends FrameLayout {

    private static final int AnimationsToDegrees = -180;
    private static final long AnimationsDurationMillis = 200;

    private final ImageView mHeaderImage;
    private final ImageView mIvHeaderDivider;
    // private final Matrix mHeaderImageMatrix;

    private final TextView mHeaderText;
    private final TextView mSubHeaderText;
    private final ProgressBar mProgressBarLeft;
    // private final ProgressBar mProgressBarRight;

    private String mPullLabel;
    private String mRefreshingLabel;
    private String mReleaseLabel;

    // private final Animation mRotateAnimation;

    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;
    private float mScaleValue;
    private int mHeaderDividerColor;
    private boolean mIsShowHeader;

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     */
    public HeaderLoadingLayout(Context context) {
        super(context);

        ViewGroup headerViewGroup = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header,
                this);
        mHeaderText = (TextView) headerViewGroup.findViewById(R.id.pull_to_refresh_text);
        mProgressBarLeft = (ProgressBar) headerViewGroup.findViewById(R.id.pull_to_refresh_progress_left);
        mSubHeaderText = (TextView) headerViewGroup.findViewById(R.id.pull_to_refresh_sub_text);
        mHeaderImage = (ImageView) headerViewGroup.findViewById(R.id.pull_to_refresh_image);
        mIvHeaderDivider = (ImageView) headerViewGroup.findViewById(R.id.iv_headerDivider);

        setupAnimations();

        mRefreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
        mReleaseLabel = context.getString(R.string.pull_to_refresh_release_label);
        mPullLabel = context.getString(R.string.pull_to_refresh_pull_label);
        reset();
    }

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     * @param mode    拉动的样式
     * @param attrs   属性数组
     */
    public HeaderLoadingLayout(Context context, final Mode mode, TypedArray attrs) {
        super(context);

        ViewGroup headerViewGroup = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header,
                this);
        mHeaderText = (TextView) headerViewGroup.findViewById(R.id.pull_to_refresh_text);
        mProgressBarLeft = (ProgressBar) headerViewGroup.findViewById(R.id.pull_to_refresh_progress_left);
        mSubHeaderText = (TextView) headerViewGroup.findViewById(R.id.pull_to_refresh_sub_text);
        mHeaderImage = (ImageView) headerViewGroup.findViewById(R.id.pull_to_refresh_image);
        mIvHeaderDivider = (ImageView) headerViewGroup.findViewById(R.id.iv_headerDivider);

        setupAnimations();

        // final Interpolator interpolator = new LinearInterpolator();
        // mRotateAnimation = new RotateAnimation(
        // 0, DEGREE_CIRCLE, Animation.RELATIVE_TO_SELF, 0.5f,
        // Animation.RELATIVE_TO_SELF, 0.5f);
        // mRotateAnimation.setInterpolator(interpolator);
        // mRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
        // mRotateAnimation.setRepeatCount(Animation.INFINITE);
        // mRotateAnimation.setRepeatMode(Animation.RESTART);

        switch (mode) {
            case PULL_UP_TO_REFRESH:
                // 什么都不做
                break;
            case PULL_DOWN_TO_REFRESH:
            default:
                mRefreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
                mReleaseLabel = context.getString(R.string.pull_to_refresh_release_label);
                mPullLabel = context.getString(R.string.pull_to_refresh_pull_label);
                break;
        }

        initAttributies(context, attrs);

        reset();
    }

    private void initAttributies(Context context, TypedArray attrs) {
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextColor)) {
            ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderTextColor);
            setTextColor(null != colors ? colors : ColorStateList.valueOf(Color.BLACK));
        }
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderSubTextColor)) {
            ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderSubTextColor);
            setSubTextColor(null != colors ? colors : ColorStateList.valueOf(Color.BLACK));
        }
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderBackground)) {
            Drawable background = attrs.getDrawable(R.styleable.PullToRefresh_ptrHeaderBackground);
            if (null != background) {
                setBackgroundDrawable(background);
            }
        }

        // Try and get defined drawable from Attrs
        Drawable imageDrawable = null;
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawable)) {
            imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawable);
        }

        // If we don't have a user defined drawable, load the default
        if (null == imageDrawable) {
            imageDrawable = context.getResources().getDrawable(R.drawable.arrow_down);
        }
        mHeaderDividerColor = attrs.getColor(R.styleable.PullToRefresh_headerDivider, 0);
        if (mHeaderDividerColor != 0) {
            mIvHeaderDivider.setVisibility(View.VISIBLE);
            mIvHeaderDivider.setBackgroundColor(mHeaderDividerColor);
            float dividerHeight = attrs.getDimension(R.styleable.PullToRefresh_headerDividerHeight, 1);
            RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) mIvHeaderDivider
                    .getLayoutParams();
            try {
                lp.height = Integer.parseInt(StringUtil.getDecimalFormat(0, String.valueOf(Math.rint(dividerHeight))));
                mIvHeaderDivider.setLayoutParams(lp);
            } catch (NumberFormatException e) {
                EvtLog.w("HeaderLoadingLayout", "分割线高度数据转换错误:" + e);
            }
        }
        mIsShowHeader = attrs.getBoolean(R.styleable.PullToRefresh_isShowHeader, true);
        setHeaderVisible();
        // 修复5.0崩溃的问题
        // attrs.recycle();
        // attrs = null;
        // Set Drawable, and save width/height
        setLoadingDrawable(imageDrawable);
    }

    /**
     * setHeaderVisible 设置显示
     */
    private void setHeaderVisible() {
        if (!mIsShowHeader) {
            // this.setVisibility(View.INVISIBLE);
            mHeaderText.setVisibility(View.INVISIBLE);
            mProgressBarLeft.setVisibility(View.INVISIBLE);
            mSubHeaderText.setVisibility(View.INVISIBLE);
            mHeaderImage.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 重置头部刷新控件的各个状态和值
     */
    public void reset() {
        mHeaderText.setText(Html.fromHtml(mPullLabel));
        mProgressBarLeft.setVisibility(View.GONE);
        mHeaderImage.setVisibility(View.VISIBLE);
        mHeaderImage.clearAnimation();

        resetImageRotation();
        setHeaderVisible();
        if (TextUtils.isEmpty(mSubHeaderText.getText())) {
            mSubHeaderText.setVisibility(View.GONE);
        } else {
            mSubHeaderText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * setHeaderBarGone 设置头部图片状态 此方法用在reset() 之前 优化显示
     */
    public void setHeaderBarGone() {
        mHeaderImage.setVisibility(View.GONE);
        mProgressBarLeft.setVisibility(View.GONE);
    }

    /**
     * 即将释放控件时，设置头部第一行的文本内容
     */
    public void releaseToRefresh() {
        mHeaderText.setText(Html.fromHtml(mReleaseLabel));
    }

    /**
     * 设置下拉刷新还没有完全显示时的默认文字
     *
     * @param pullLabel 下拉刷新还没有完全显示时的默认文字
     */
    public void setPullLabel(String pullLabel) {
        mPullLabel = pullLabel;
    }

    /**
     * 设置头部控件为“正在刷新”状态
     */
    public void refreshing() {
        if (!mIsShowHeader) {
            return;
        }
        mHeaderText.setText(Html.fromHtml(mRefreshingLabel));
        mHeaderImage.clearAnimation();
        mProgressBarLeft.setVisibility(View.VISIBLE);
        mHeaderImage.setVisibility(View.GONE);

    }

    /**
     * 正在刷新时，设置头部第一行的文本内容
     *
     * @param refreshingLabel 设置头部第一行的文本内容
     */
    public void setRefreshingLabel(String refreshingLabel) {
        mRefreshingLabel = refreshingLabel;
    }

    /**
     * 设置即将释放时，显示的文本信息
     *
     * @param releaseLabel 即将释放时的文本信息
     */
    public void setReleaseLabel(String releaseLabel) {
        mReleaseLabel = releaseLabel;
    }

    /**
     * 当下拉刷新还没有完全显示头部时，显示的文本信息
     */
    public void pullToRefresh() {
        mHeaderText.setText(Html.fromHtml(mPullLabel));
    }

    /**
     * 设置头部两行文本的颜色
     *
     * @param color 颜色值
     */
    public void setTextColor(ColorStateList color) {
        mHeaderText.setTextColor(color);
        mSubHeaderText.setTextColor(color);
    }

    /**
     * 设置头部两行文本的颜色
     *
     * @param color 颜色值
     */
    public void setTextColor(int color) {
        setTextColor(ColorStateList.valueOf(color));
    }

    /**
     * 设置头部第二行文本的颜色
     *
     * @param color 颜色值
     */
    public void setSubTextColor(ColorStateList color) {
        mSubHeaderText.setTextColor(color);
    }

    /**
     * 设置头部第二行文本的颜色
     *
     * @param color 颜色值
     */
    public void setSubTextColor(int color) {
        setSubTextColor(ColorStateList.valueOf(color));
    }

    /**
     * 设置加载默认的图标
     *
     * @param imageDrawable 加载默认的图标
     */
    public void setLoadingDrawable(Drawable imageDrawable) {
        // Set Drawable, and save width/height
        mHeaderImage.setImageDrawable(imageDrawable);
    }

    /**
     * 设置第二行文本的文字
     *
     * @param label 设置的文本内容
     */
    public void setSubHeaderText(CharSequence label) {
        if (!mIsShowHeader) {
            return;
        }
        if (TextUtils.isEmpty(label)) {
            mSubHeaderText.setVisibility(View.GONE);
        } else {
            mSubHeaderText.setText(label);
            mSubHeaderText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 下拉时，设置左边箭头的动画效果
     *
     * @param scaleOfHeight 滚动的高度标识
     */
    public void onPullY(float scaleOfHeight) {
        if (!mIsShowHeader) {
            return;
        }
        if (mScaleValue < 1 && scaleOfHeight >= 1) {
            mHeaderImage.startAnimation(mFlipAnimation);
        } else if (scaleOfHeight < 1 && mScaleValue >= 1) {
            mHeaderImage.startAnimation(mReverseFlipAnimation);
        }
        mScaleValue = scaleOfHeight;
        // mHeaderImageMatrix.setRotate(scaleOfHeight * 90, mRotationPivotX,
        // mRotationPivotY);
        // mHeaderImage.setImageMatrix(mHeaderImageMatrix);
    }

    private void resetImageRotation() {
        mScaleValue = 0;
        mHeaderImage.clearAnimation();
        // mHeaderImageMatrix.reset();
        // mHeaderImage.setImageMatrix(mHeaderImageMatrix);
    }

    private void setupAnimations() {
        // Load all of the animations we need in code rather than through XML
        mFlipAnimation = new RotateAnimation(
                0, AnimationsToDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(AnimationsDurationMillis);
        mFlipAnimation.setFillAfter(true);
        mReverseFlipAnimation = new RotateAnimation(
                AnimationsToDegrees, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(AnimationsDurationMillis);
        mReverseFlipAnimation.setFillAfter(true);
    }
}
