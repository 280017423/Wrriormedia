/**
 * ****************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * *****************************************************************************
 */
package com.wrriormedia.library.widget.pulltorefresh;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wrriormedia.library.R;
import com.wrriormedia.library.widget.pulltorefresh.PullToRefreshBase.Mode;

/**
 * 上下拉动控件右边的指示器
 *
 * @author wang.xy
 * @since 2012-8-14
 * @version 2012-8-14，王先佑，重构文本、注释等
 *
 */
public class IndicatorLayout extends FrameLayout implements AnimationListener {

    private static final int DEGREE_180 = -180;
    private static final int DEFAULT_ROTATION_ANIMATION_DURATION = 150;
    private final Animation mRotateAnimation;
    private final Animation mResetRotateAnimation;

    private Animation mInAnim;
    private Animation mOutAnim;
    private ImageView mArrowImageView;

    /**
     * 构造函数
     *
     * @param context
     *            当前布局文件的上下文
     */
    public IndicatorLayout(Context context) {
        super(context);

        mArrowImageView = new ImageView(context);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);

        int indicatorInternalPadding = getResources().getDimensionPixelSize(R.dimen.indicator_internal_padding);
        lp.rightMargin = indicatorInternalPadding;
        lp.topMargin = indicatorInternalPadding;
        lp.bottomMargin = indicatorInternalPadding;
        lp.leftMargin = indicatorInternalPadding;
        addView(mArrowImageView, lp);

        int inAnimResId;
        int outAnimResId;
        inAnimResId = R.anim.slide_in_from_top;
        outAnimResId = R.anim.slide_out_to_top;
        setBackgroundResource(R.drawable.indicator_bg_top);
        mArrowImageView.setImageResource(R.drawable.arrow_down);

        mInAnim = AnimationUtils.loadAnimation(context, inAnimResId);
        mInAnim.setAnimationListener(this);

        mOutAnim = AnimationUtils.loadAnimation(context, outAnimResId);
        mOutAnim.setAnimationListener(this);

        final Interpolator interpolator = new LinearInterpolator();
        mRotateAnimation = new RotateAnimation(
                0, DEGREE_180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setInterpolator(interpolator);
        mRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
        mRotateAnimation.setFillAfter(true);

        mResetRotateAnimation = new RotateAnimation(
                DEGREE_180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mResetRotateAnimation.setInterpolator(interpolator);
        mResetRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
        mResetRotateAnimation.setFillAfter(true);

    }

    /**
     * 构造函数
     *
     * @param context
     *            当前布局文件的上下文
     * @param mode
     *            拉动的样式
     */
    public IndicatorLayout(Context context, Mode mode) {
        super(context);

        mArrowImageView = new ImageView(context);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);

        int indicatorInternalPadding = getResources().getDimensionPixelSize(R.dimen.indicator_internal_padding);
        lp.rightMargin = indicatorInternalPadding;
        lp.topMargin = indicatorInternalPadding;
        lp.bottomMargin = indicatorInternalPadding;
        lp.leftMargin = indicatorInternalPadding;
        addView(mArrowImageView, lp);

        int inAnimResId;
        int outAnimResId;
        switch (mode) {
            case PULL_UP_TO_REFRESH:
                inAnimResId = R.anim.slide_in_from_bottom;
                outAnimResId = R.anim.slide_out_to_bottom;
                setBackgroundResource(R.drawable.indicator_bg_bottom);
                mArrowImageView.setImageResource(R.drawable.arrow_up);
                break;
            default:
            case PULL_DOWN_TO_REFRESH:
                inAnimResId = R.anim.slide_in_from_top;
                outAnimResId = R.anim.slide_out_to_top;
                setBackgroundResource(R.drawable.indicator_bg_top);
                mArrowImageView.setImageResource(R.drawable.arrow_down);
                break;
        }

        mInAnim = AnimationUtils.loadAnimation(context, inAnimResId);
        mInAnim.setAnimationListener(this);

        mOutAnim = AnimationUtils.loadAnimation(context, outAnimResId);
        mOutAnim.setAnimationListener(this);

        final Interpolator interpolator = new LinearInterpolator();
        mRotateAnimation = new RotateAnimation(
                0, DEGREE_180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setInterpolator(interpolator);
        mRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
        mRotateAnimation.setFillAfter(true);

        mResetRotateAnimation = new RotateAnimation(
                DEGREE_180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mResetRotateAnimation.setInterpolator(interpolator);
        mResetRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
        mResetRotateAnimation.setFillAfter(true);

    }

    /**
     * 指示器是否可见
     *
     * @return 返回指示器是否可见
     */
    public final boolean isVisible() {
        Animation currentAnim = getAnimation();
        if (null != currentAnim) {
            return mInAnim == currentAnim;
        }

        return getVisibility() == View.VISIBLE;
    }

    /**
     * 执行消失的动画
     */
    public void hide() {
        startAnimation(mOutAnim);
    }

    /**
     * 执行显示的动画
     */
    public void show() {
        startAnimation(mInAnim);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (animation == mOutAnim) {
            mArrowImageView.clearAnimation();
            setVisibility(View.GONE);
        } else if (animation == mInAnim) {
            setVisibility(View.VISIBLE);
        }

        clearAnimation();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // NO-OP
    }

    @Override
    public void onAnimationStart(Animation animation) {
        setVisibility(View.VISIBLE);
    }

    /**
     * 执行释放时的动画效果
     */
    public void releaseToRefresh() {
        mArrowImageView.startAnimation(mRotateAnimation);
    }

    /**
     * 执行下拉但头部还没有完全显示的动画效果
     */
    public void pullToRefresh() {
        mArrowImageView.startAnimation(mResetRotateAnimation);
    }

}
