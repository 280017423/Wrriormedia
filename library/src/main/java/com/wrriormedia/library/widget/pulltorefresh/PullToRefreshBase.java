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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.wrriormedia.library.R;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.PackageUtil;
import com.wrriormedia.library.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 上下拉动刷新控件的基类
 *
 * @param <T> 上下拉动控件的主控件
 * @author wang.xy
 * @version 2012-08-14 王先佑 重构文本、注释等<br>
 *          2012-09-13 王先佑 重构，增加onHeaderRefresh方法<br>
 *          2012-09-18 王先佑 添加headerVisibleChange方法，当拉动控件时触发该事件；<br>
 *          2012-09-29 谭晓星 增加下拉刷新时无数据时的处理<br>
 *          2012-10-24 谭晓星 增加setHeaderVisible相关方法，代码触发下拉刷新事件。<br>
 *          优化刷新完成后rest方法,优化标记变量保存<br>
 *          2012-11-09 谭晓星
 *          优化控件，增加下拉正在刷新置顶时，限制向下滑动。头部刷新或下拉刷新时限制上拉刷新动作,正在刷新数据且暂无内容数据时限制滑动
 *          。优化setHeaderVisible方法<br>
 *          2013-01-07 谭晓星 增加setTimeTask()相关处理头部刷新最小显示时间方法，优化头部刷新的显示<br>
 *          2013-08-12 谭晓星 下拉刷新也使用setTimeTask()，优化头部刷新的显示<br>
 *          2014-02-06 谭晓星 修复setHeaderVisible方法，延时动画问题<br>
 * @since 2012-8-14
 */
public abstract class PullToRefreshBase<T extends View> extends LinearLayout {

    // ===========================================================
    // Constants
    // ===========================================================
    public static final int INITIAL_FRESH_SUM = 0;

    static final boolean DEBUG = false;

    static final String LOG_TAG = "PullToRefresh";

    static final float FRICTION = 2.0f;

    static final int PULL_TO_REFRESH = 0x0;
    static final int RELEASE_TO_REFRESH = 0x1;
    static final int REFRESHING = 0x2;
    static final int MANUAL_REFRESHING = 0x3;

    static final Mode DEFAULT_MODE = Mode.PULL_DOWN_TO_REFRESH;

    static final String STATE_STATE = "ptr_state";
    static final String STATE_MODE = "ptr_mode";
    static final String STATE_CURRENT_MODE = "ptr_current_mode";
    static final String STATE_DISABLE_SCROLLING_REFRESHING = "ptr_disable_scrolling";
    static final String STATE_SHOW_REFRESHING_VIEW = "ptr_show_refreshing_view";
    static final String STATE_HAS_MOREDATA = "mHasMoreData";
    static final String STATE_SUPER = "ptr_super";

    // 头部刷新
    private static final int HEADER_REFRESH = 3;
    private static final int HEADER_REFRESH_TIME_TASK = 4;
    // 头部刷新显示最小时间
    private static final int HEADER_REFRESH_MIN_TIME = 800;

    // ===========================================================
    // Fields
    // ===========================================================
    private final Handler mHandler = new Handler();
    protected T mRefreshableView;
    // 是否正在加载数据
    protected boolean mIsLoadingData = true;
    // 用于标记是否首次正在显示头部刷新
    protected boolean mIsFisrtHeaderShowing = true;
    protected boolean mIsBeingDragged;
    // 是否显示Footer
    protected boolean mIsFooterViewEnabled;
    // 是否第一次默认显示下拉头部动画
    protected boolean mIsShowHeaderFresh = true;
    /**
     * 下拉时是否超出头部高度，
     */
    protected boolean mHaveExceededHeaderHeight;
    protected Mode mMode = DEFAULT_MODE;
    private OnResizeListener mOnResizeListener;
    // 加载次数
    private int mFreshSum = INITIAL_FRESH_SUM;
    // 一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件
    private int mTouchSlop;
    private float mLastMotionX;
    private float mLastMotionY;
    private float mInitialMotionY;

    private int mState = PULL_TO_REFRESH;

    private Mode mCurrentMode;
    private boolean mPullToRefreshEnabled = true;

    // 是否有更多数据
    private boolean mHasMoreData = true;

    private boolean mShowViewWhileRefreshing = true;
    private boolean mDisableScrollingWhileRefreshing = true;
    private boolean mFilterTouchEvents = true;
    private HeaderLoadingLayout mHeaderLayout;
    private FooterLoadingLayout mFooterLayout;

    private int mHeaderHeight;
    private int mFooterHeight;
    private OnRefreshListener mOnRefreshListener;
    private OnRefreshListener2 mOnRefreshListener2;
    private OnScrollYChangeListener mOnScrollYChangeListener;
    private SmoothScrollRunnable mCurrentSmoothScrollRunnable;
    private RefreshTimeTask mRefreshTimeTask;
    // 头部是否显示 用于头部刷新计时器的判断标记
    private boolean mIsHeaderVisible;
    // 用于标记是重置显示
    private boolean mIsReset = true;
    // 是否头部在刷新
    private boolean mIsHeaderFreshing;
    // 是否到达头部刷新最小时间
    private boolean mIsHeaderMinTimeup = true;
    // 是否显示头部view
    private boolean mIsShowHeader;
    private Handler mHeaderRefreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HEADER_REFRESH:
                    // 下拉刷新接口
                    mOnRefreshListener2.onPullDownToRefresh();
                    break;
                case HEADER_REFRESH_TIME_TASK:
                    EvtLog.d(LOG_TAG, "头部刷新最小时间已到.. mIsHeaderVisible:"
                            + mIsHeaderVisible);
                    mIsHeaderMinTimeup = true;
                    // 数据已经请求过来
                    if (!mIsHeaderVisible) {
                        onRefreshComplete();
                    }
                    break;
                default:
                    break;
            }
        }

    };

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     */
    public PullToRefreshBase(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     * @param attrs   属性数组
     */
    public PullToRefreshBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     * @param mode    拉动的样式
     */
    public PullToRefreshBase(Context context, Mode mode) {
        super(context);
        mMode = mode;
        init(context, null);
    }

    // 在 setAdapter前使用
    public void setIsShowHeaderFresh(boolean isShowHeaderFresh) {
        this.mIsShowHeaderFresh = isShowHeaderFresh;
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(LinearLayout.VERTICAL);

        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();

        // Styleables from XML
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PullToRefresh);
        handleStyledAttributes(a);

        if (a.hasValue(R.styleable.PullToRefresh_ptrMode)) {
            mMode = Mode.mapIntToMode(a.getInteger(
                    R.styleable.PullToRefresh_ptrMode, 0));
        }

        // Refreshable View
        // By passing the attrs, we can add ListView/GridView params via XML
        mRefreshableView = createRefreshableView(context, attrs);
        addRefreshableView(context, mRefreshableView);

        // We need to create now layouts now
        mHeaderLayout = new HeaderLoadingLayout(context,
                Mode.PULL_DOWN_TO_REFRESH, a);
        mFooterLayout = new FooterLoadingLayout(context,
                Mode.PULL_UP_TO_REFRESH, a);

        mIsFooterViewEnabled = mFooterLayout.isShowFooter();

        // Add Header/Footer Views
        updateUIForMode();

        // Styleables from XML
        if (a.hasValue(R.styleable.PullToRefresh_ptrHeaderBackground)) {
            Drawable background = a
                    .getDrawable(R.styleable.PullToRefresh_ptrHeaderBackground);
            if (null != background) {
                setBackgroundDrawable(background);
            }
        }
        if (a.hasValue(R.styleable.PullToRefresh_ptrAdapterViewBackground)) {
            Drawable background = a
                    .getDrawable(R.styleable.PullToRefresh_ptrAdapterViewBackground);
            if (null != background) {
                mRefreshableView.setBackgroundDrawable(background);
            }
        }
        mIsShowHeader = a.getBoolean(R.styleable.PullToRefresh_isShowHeader,
                true);
        // headerDividerColor =
        // a.getColor(R.styleable.PullToRefresh_headerDivider, 0);
        // mHeaderLayout.setHeaderDivider(headerDividerColor);
        a.recycle();
        setLastedHeaderLabel();
    }

    /**
     * Get the mode that this view is currently in. This is only really useful
     * when using <code>Mode.BOTH</code>.
     *
     * @return Mode that the view is currently in
     */
    public final Mode getCurrentMode() {
        return mCurrentMode;
    }

    public void setOnResizeListener(OnResizeListener onResizeListener) {
        mOnResizeListener = onResizeListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mOnResizeListener != null) {
            mOnResizeListener.onResizeListener(w, h, oldw, oldw);
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Returns whether the Touch Events are filtered or not. If true is
     * returned, then the View will only use touch events where the difference
     * in the Y-axis is greater than the difference in the X-axis. This means
     * that the View will not interfere when it is used in a horizontal
     * scrolling View (such as a ViewPager).
     *
     * @return boolean - true if the View is filtering Touch Events
     */
    public final boolean getFilterTouchEvents() {
        return mFilterTouchEvents;
    }

    /**
     * Set the Touch Events to be filtered or not. If set to true, then the View
     * will only use touch events where the difference in the Y-axis is greater
     * than the difference in the X-axis. This means that the View will not
     * interfere when it is used in a horizontal scrolling View (such as a
     * ViewPager), but will restrict which types of finger scrolls will trigger
     * the View.
     *
     * @param filterEvents - true if you want to filter Touch Events. Default is true.
     */
    public final void setFilterTouchEvents(boolean filterEvents) {
        mFilterTouchEvents = filterEvents;
    }

    /**
     * Get the mode that this view has been set to. If this returns
     * <code>Mode.BOTH</code>, you can use <code>getCurrentMode()</code> to
     * check which mode the view is currently in
     *
     * @return Mode that the view has been set to
     */
    public final Mode getMode() {
        return mMode;
    }

    /**
     * Set the mode of Pull-to-Refresh that this view will use.
     *
     * @param mode - Mode to set the View to
     */
    public final void setMode(Mode mode) {
        if (mode != mMode) {
            if (DEBUG) {
                Log.d(LOG_TAG, "Setting mode to: " + mode);
            }
            mMode = mode;
            updateUIForMode();
        }
    }

    /**
     * Get the Wrapped Refreshable View. Anything returned here has already been
     * added to the content view.
     *
     * @return The View which is currently wrapped
     */
    public final T getRefreshableView() {
        return mRefreshableView;
    }

    /**
     * Get whether the 'Refreshing' View should be automatically shown when
     * refreshing. Returns true by default.
     *
     * @return - true if the Refreshing View will be show
     */
    public final boolean getShowViewWhileRefreshing() {
        return mShowViewWhileRefreshing;
    }

    /**
     * A mutator to enable/disable whether the 'Refreshing' View should be
     * automatically shown when refreshing.
     *
     * @param showView 是否显示刷新的控件
     */
    public final void setShowViewWhileRefreshing(boolean showView) {
        mShowViewWhileRefreshing = showView;
    }

    /**
     * @return true if the current mode is Mode.PULL_DOWN_TO_REFRESH
     * @deprecated Use the value from <code>getCurrentMode()</code> instead
     */
    public final boolean hasPullFromTop() {
        return mCurrentMode == Mode.PULL_DOWN_TO_REFRESH;
    }

    /**
     * Returns whether the widget has disabled scrolling on the Refreshable View
     * while refreshing.
     *
     * @return true if the widget has disabled scrolling while refreshing
     */
    public final boolean isDisableScrollingWhileRefreshing() {
        return mDisableScrollingWhileRefreshing;
    }

    /**
     * By default the Widget disabled scrolling on the Refreshable View while
     * refreshing. This method can change this behaviour.
     *
     * @param disableScrollingWhileRefreshing - true if you want to disable scrolling while refreshing
     */
    public final void setDisableScrollingWhileRefreshing(
            boolean disableScrollingWhileRefreshing) {
        mDisableScrollingWhileRefreshing = disableScrollingWhileRefreshing;
    }

    /**
     * Whether Pull-to-Refresh is enabled
     *
     * @return enabled
     */
    public final boolean isPullToRefreshEnabled() {
        return mPullToRefreshEnabled;
    }

    /**
     * A mutator to enable/disable Pull-to-Refresh for the current View
     *
     * @param enable Whether Pull-To-Refresh should be used
     */
    public final void setPullToRefreshEnabled(boolean enable) {
        mPullToRefreshEnabled = enable;
    }

    /**
     * Returns whether the Widget is currently in the Refreshing mState
     *
     * @return true if the Widget is currently refreshing
     */
    public final boolean isRefreshing() {
        return mState == REFRESHING || mState == MANUAL_REFRESHING;
    }

    /**
     * Sets the Widget to be in the refresh mState. The UI will be updated to
     * show the 'Refreshing' view.
     *
     * @param doScroll - true if you want to force a scroll to the Refreshing view.
     */
    public final void setRefreshing(boolean doScroll) {
        if (!isRefreshing()) {
            setRefreshingInternal(doScroll);
            mState = MANUAL_REFRESHING;
        }
    }

    /**
     * Set the Last Updated Text. This displayed under the main label when
     * Pulling
     *
     * @param label - Label to set
     */
    public void setHeaderLabel(CharSequence label) {
        if (null != mHeaderLayout) {
            mHeaderLayout.setSubHeaderText(label);
        }

        // Refresh Height as it may have changed
        refreshLoadingViewsHeight();
    }

    /**
     * 设置底部文本
     *
     * @param label 设置的文本
     */
    public void setFooterLabel(CharSequence label) {
        // Refresh Height as it may have changed
        refreshLoadingViewsHeight();
    }

    /**
     * Set the drawable used in the loading layout. This is the same as calling
     * <code>setLoadingDrawable(drawable, Mode.BOTH)</code>
     *
     * @param drawable - Drawable to display
     */
    public void setLoadingDrawable(Drawable drawable) {
        setLoadingDrawable(drawable, Mode.BOTH);
    }

    /**
     * Set the drawable used in the loading layout.
     *
     * @param drawable - Drawable to display
     * @param mode     - Controls which Header/Footer Views will be updated.
     *                 <code>Mode.BOTH</code> will update all available, other values
     *                 will update the relevant View.
     */
    public void setLoadingDrawable(Drawable drawable, Mode mode) {
        if (null != mHeaderLayout && mode.canPullDown()) {
            mHeaderLayout.setLoadingDrawable(drawable);
        }
        // if (null != mFooterLayout && mode.canPullUp()) {
        // // mFooterLayout.setLoadingDrawable(drawable);
        // }

        // The Loading Height may have changed, so refresh
        refreshLoadingViewsHeight();
    }

    @Override
    public void setLongClickable(boolean longClickable) {
        getRefreshableView().setLongClickable(longClickable);
    }

    /**
     * Set OnRefreshListener for the Widget
     *
     * @param listener - Listener to be used when the Widget is set to Refresh
     */
    public final void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    /**
     * 设置OnScrollYChangeListener
     */
    public final void setOnScrollYChangeListener(
            OnScrollYChangeListener listener) {
        mOnScrollYChangeListener = listener;
    }

    /**
     * Set OnRefreshListener for the Widget
     *
     * @param listener - Listener to be used when the Widget is set to Refresh
     */
    public final void setOnRefreshListener(OnRefreshListener2 listener) {
        mOnRefreshListener2 = listener;
    }

    /**
     * Set Text to show when the Widget is being Pulled
     * <code>setPullLabel(releaseLabel, Mode.BOTH)</code>
     *
     * @param pullLabel String to display
     */
    public void setPullLabel(String pullLabel) {
        setPullLabel(pullLabel, Mode.BOTH);
    }

    /**
     * Set Text to show when the Widget is being Pulled
     *
     * @param pullLabel - String to display
     * @param mode      - Controls which Header/Footer Views will be updated.
     *                  <code>Mode.BOTH</code> will update all available, other values
     *                  will update the relevant View.
     */
    public void setPullLabel(String pullLabel, Mode mode) {
        if (null != mHeaderLayout && mode.canPullDown()) {
            mHeaderLayout.setPullLabel(pullLabel);
        }
        if (null != mFooterLayout && mode.canPullUp()) {
            mFooterLayout.setPullLabel(pullLabel);
        }
    }

    /**
     * Sets the Widget to be in the refresh mState ture. The UI will be updated
     * to show the 'Refreshing' view.
     */
    public final void setRefreshing() {
        setRefreshing(true);
    }

    /**
     * Set Text to show when the Widget is refreshing
     * <code>setRefreshingLabel(releaseLabel, Mode.BOTH)</code>
     *
     * @param refreshingLabel - String to display
     */
    public void setRefreshingLabel(String refreshingLabel) {
        setRefreshingLabel(refreshingLabel, Mode.BOTH);
    }

    /**
     * Set Text to show when the Widget is refreshing
     *
     * @param refreshingLabel - String to display
     * @param mode            - Controls which Header/Footer Views will be updated.
     *                        <code>Mode.BOTH</code> will update all available, other values
     *                        will update the relevant View.
     */
    public void setRefreshingLabel(String refreshingLabel, Mode mode) {
        if (null != mHeaderLayout && mode.canPullDown()) {
            mHeaderLayout.setRefreshingLabel(refreshingLabel);
        }
        if (null != mFooterLayout && mode.canPullUp()) {
            mFooterLayout.setRefreshingLabel(refreshingLabel);
        }
    }

    /**
     * Set Text to show when the Widget is being pulled, and will refresh when
     * released. This is the same as calling
     * <code>setReleaseLabel(releaseLabel, Mode.BOTH)</code>
     *
     * @param releaseLabel - String to display
     */
    public void setReleaseLabel(String releaseLabel) {
        setReleaseLabel(releaseLabel, Mode.BOTH);
    }

    /**
     * Set Text to show when the Widget is being pulled, and will refresh when
     * released
     *
     * @param releaseLabel - String to display
     * @param mode         - Controls which Header/Footer Views will be updated.
     *                     <code>Mode.BOTH</code> will update all available, other values
     *                     will update the relevant View.
     */
    public void setReleaseLabel(String releaseLabel, Mode mode) {
        if (null != mHeaderLayout && mode.canPullDown()) {
            mHeaderLayout.setReleaseLabel(releaseLabel);
        }
        if (null != mFooterLayout && mode.canPullUp()) {
            mFooterLayout.setReleaseLabel(releaseLabel);
        }
    }

    /**
     * 设置没有找到更多数据时的提示语
     *
     * @param label 提示语
     */
    public void setNoMoreDataText(String label) {
        if (StringUtil.isNullOrEmpty(label)) {
            mFooterLayout.setNoMoreDataLabel(label);
        }
    }

    /**
     * 设置没有找到更多数据时的提示语
     */
    public void setNoMoreData() {
        EvtLog.d(LOG_TAG, "setNoMoreData");
        mFooterLayout.setNoMoreDataLabel();
    }

    protected final FooterLoadingLayout getFooterLayout() {
        return mFooterLayout;
    }

    protected final int getHeaderHeight() {
        return mHeaderHeight;
    }

    protected final int getFooterHeight() {
        return mFooterHeight;
    }

    protected final HeaderLoadingLayout getHeaderLayout() {
        return mHeaderLayout;
    }

    protected final int getState() {
        return mState;
    }

    /**
     * Allows Derivative classes to handle the XML Attrs without creating a
     * TypedArray themsevles
     *
     * @param a - TypedArray of PullToRefresh Attributes
     */
    protected void handleStyledAttributes(TypedArray a) {
    }

    /**
     * Implemented by derived class to return whether the View is in a mState
     * where the user can Pull to Refresh by scrolling down.
     *
     * @return true if the View is currently the correct mState (for example,
     * top of a ListView)
     */
    protected abstract boolean isReadyForPullDown();

    /**
     * Implemented by derived class to return whether the View is in a mState
     * where the user can Pull to Refresh by scrolling up.
     *
     * @return true if the View is currently in the correct mState (for example,
     * bottom of a ListView)
     */
    protected abstract boolean isReadyForPullUp();

    @Override
    public final boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mPullToRefreshEnabled) {
            return false;
        }

        // if (isRefreshing() && mDisableScrollingWhileRefreshing) {
        // return true;
        // }

        final int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false;
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (isReadyForPull()) {
                    markPullStateOnMove(event);
                }
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                if (isReadyForPull()) {
                    float eventY = event.getY();
                    mLastMotionY = eventY;
                    mInitialMotionY = eventY;
                    mLastMotionX = event.getX();
                    mIsBeingDragged = false;
                }
                break;
            }
            default:
                break;
        }

        return mIsBeingDragged;
    }

    /**
     * 记录位置，更改当前状态标记
     */
    private void markPullStateOnMove(MotionEvent event) {
        final float y = event.getY();
        final float dy = y - mLastMotionY;
        final float yDiff = Math.abs(dy);
        final float xDiff = Math.abs(event.getX() - mLastMotionX);

        if (yDiff > mTouchSlop && (!mFilterTouchEvents || yDiff > xDiff)) {
            if (mMode.canPullDown() && dy >= 1f && isReadyForPullDown()) {
                mLastMotionY = y;
                mIsBeingDragged = true;
                if (mMode == Mode.BOTH) {
                    mCurrentMode = Mode.PULL_DOWN_TO_REFRESH;
                }
            } else if (mMode.canPullUp() && dy <= -1f && isReadyForPullUp()) {
                mLastMotionY = y;
                mIsBeingDragged = true;
                // 头部非刷新状态时
                if (mMode == Mode.BOTH && !mIsHeaderFreshing) {
                    mCurrentMode = Mode.PULL_UP_TO_REFRESH;
                }
            }
        }
    }

    @Override
    public final boolean onTouchEvent(MotionEvent event) {
        if (mIsFisrtHeaderShowing || (isRefreshing() && getItemCount() <= 0)
                || !mPullToRefreshEnabled) {
            return false;
        }
        // if (isRefreshing() && mDisableScrollingWhileRefreshing) {
        // return true;
        // }
        // 边缘
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && event.getEdgeFlags() != 0) {
            return false;
        }
        boolean result = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                if (mIsBeingDragged) {
                    mLastMotionY = event.getY();
                    pullEvent();
                    result = true;
                }
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                if (isReadyForPull()) {
                    mLastMotionY = event.getY();
                    mInitialMotionY = event.getY();
                    result = true;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mIsBeingDragged) {
                    result = exuctePullEventOnUp();
                }
                break;
            }
            default:
                break;
        }

        return result;
    }

    private boolean exuctePullEventOnUp() {
        mIsBeingDragged = false;
        if (mState == RELEASE_TO_REFRESH) {
            if (null != mOnRefreshListener) {
                setRefreshingInternal(true);
                mOnRefreshListener.onRefresh();
                return true;
            } else if (null != mOnRefreshListener2) {
                setRefreshingInternal(true);
                if (mCurrentMode == Mode.PULL_DOWN_TO_REFRESH) {
                    // 下拉刷新
                    setLastedHeaderLabel();
                    mIsHeaderFreshing = true;
                    mOnRefreshListener2.onPullDownToRefresh();
                    // 启动延时显示计时
                    setTimeTask();
                } else if (mCurrentMode == Mode.PULL_UP_TO_REFRESH
                        && !mIsHeaderFreshing) {
                    // 还原，设置padding
                    setLastedHeaderLabel();
                    setIsLoadingData(true);
                    // 头部非刷新时
                    // 上拉刷新
                    // 有更多数据时
                    if (mFooterLayout.HasMoreData) {
                        // 加载更多
                        mOnRefreshListener2.onPullUpToRefresh();
                    } else {
                        this.onRefreshComplete(false);
                    }
                }
                return true;
            }

            return true;
        }

        smoothScrollTo(0);

        headerVisibleChange(1);
        return true;
    }

    private void setLastedHeaderLabel() {
        setHeaderLabel(buildRefreshingSubheaderText());
    }

    /**
     * 设置刷新时头部二级文本信息<br>
     * "上次更新:MM-dd HH:mm"
     *
     * @return 刷新时头部二级文本信息
     */
    private String buildRefreshingSubheaderText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm",
                Locale.getDefault());
        String dateString = dateFormat.format(new Date());
        return PackageUtil
                .getString(R.string.pull_to_refresh_header_subtext_label)
                + dateString;
    }

    /**
     *
     */
    public void onHeaderRefresh() {
        mHeaderLayout.pullToRefresh();
        if (mOnRefreshListener2 != null) {
            mIsHeaderFreshing = true;
            mOnRefreshListener2.onPullDownToRefresh();
        }
    }

    /**
     * Called when the UI needs to be updated to the 'Pull to Refresh' state
     */
    protected void onPullToRefresh() {
        switch (mCurrentMode) {
            case PULL_UP_TO_REFRESH:
                mFooterLayout.pullToRefresh();
                break;
            case PULL_DOWN_TO_REFRESH:
                mHeaderLayout.pullToRefresh();
                break;
            default:
                break;
        }
    }

    /**
     * Called when the UI needs to be updated to the 'Release to Refresh' state
     */
    protected void onReleaseToRefresh() {
        switch (mCurrentMode) {
            case PULL_UP_TO_REFRESH:
                mFooterLayout.releaseToRefresh();
                break;
            case PULL_DOWN_TO_REFRESH:
                mHeaderLayout.releaseToRefresh();
                // mFooterLayout.refreshing();
                break;
            default:
                break;
        }
    }

    /**
     * Mark the current Refresh as complete. Will Reset the UI and hide the
     * Refreshing View
     */
    public final void onRefreshComplete() {
        mIsHeaderVisible = false;
        // 已经达到最小显示头部时间
        if (mIsHeaderMinTimeup) {
            if (mState != PULL_TO_REFRESH) {
                resetHeader();
            }
            mIsHeaderFreshing = false;
            mIsFisrtHeaderShowing = false;
            resetFooter();
            setEmptyViewState();
            mIsLoadingData = false;
            cancelTimeTask();
            mRefreshableView.setVisibility(View.VISIBLE);

        }
    }

    /**
     * Mark the current Refresh as complete. Will Reset the UI and hide the
     * Refreshing View；<br>
     * 该方法必须在setMode之前调用
     *
     * @param hasMoreData 是否还有更多数据
     */
    public final void onRefreshComplete(boolean hasMoreData) {
        mHasMoreData = hasMoreData;
        onRefreshComplete();
    }

    protected boolean getIsLoadingData() {
        return mIsLoadingData;
    }

    /**
     * 设置是否正在加载数据 主动上拉刷新时调用次方法标记
     *
     * @param isLoadingData 是否正在加载数据
     */
    protected void setIsLoadingData(boolean isLoadingData) {
        mIsLoadingData = isLoadingData;
        mFreshSum++;
    }

    protected int getFreshSum() {
        return mFreshSum;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            mMode = Mode.mapIntToMode(bundle.getInt(STATE_MODE, 0));
            mCurrentMode = Mode.mapIntToMode(bundle.getInt(STATE_CURRENT_MODE,
                    0));

            mDisableScrollingWhileRefreshing = bundle.getBoolean(
                    STATE_DISABLE_SCROLLING_REFRESHING, true);
            mShowViewWhileRefreshing = bundle.getBoolean(
                    STATE_SHOW_REFRESHING_VIEW, true);
            mHasMoreData = bundle.getBoolean(STATE_HAS_MOREDATA, true);

            // Let super Restore Itself
            super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER));

            final int viewState = bundle.getInt(STATE_STATE, PULL_TO_REFRESH);
            if (viewState == REFRESHING) {
                setRefreshingInternal(true);
                mState = viewState;
            }
            return;
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putInt(STATE_STATE, mState);
        bundle.putInt(STATE_MODE, mMode.getIntValue());
        bundle.putInt(STATE_CURRENT_MODE, mCurrentMode.getIntValue());
        bundle.putBoolean(STATE_DISABLE_SCROLLING_REFRESHING,
                mDisableScrollingWhileRefreshing);
        bundle.putBoolean(STATE_SHOW_REFRESHING_VIEW, mShowViewWhileRefreshing);
        bundle.putBoolean(STATE_HAS_MOREDATA, mHasMoreData);
        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
        return bundle;
    }

    protected void addRefreshableView(Context context, T refreshableView) {
        addView(refreshableView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
    }

    /**
     * This is implemented by derived classes to return the created View. If you
     * need to use a custom View (such as a custom ListView), override this
     * method and return an instance of your custom class.
     * <p/>
     * Be sure to set the ID of the view in this method, especially if you're
     * using a ListActivity or ListFragment.
     *
     * @param context Context to create view with
     * @param attrs   AttributeSet from wrapped class. Means that anything you
     *                include in the XML layout declaration will be routed to the
     *                created View
     * @return New instance of the Refreshable View
     */
    protected abstract T createRefreshableView(Context context,
                                               AttributeSet attrs);

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * resetHeader 重置Header显示
     *
     * @param
     * @return void
     * @throws
     */
    protected void resetHeader() {
        mState = PULL_TO_REFRESH;
        mIsBeingDragged = false;
        mIsReset = true;
        smoothScrollTo(0);
        if (mMode.canPullDown()) {
            mHeaderLayout.setHeaderBarGone();
        }
        // if (mMode.canPullUp()) {
        // mFooterLayout.reset(mHasMoreData);
        // }

    }

    /**
     * resetFooter 重置Footer显示
     *
     * @param
     * @return void
     * @throws
     */
    protected void resetFooter() {
        if (mMode.canPullUp()) {
            mFooterLayout.reset(mHasMoreData);
        }
    }

    protected void resetHeader(boolean hasMoreData) {
        mHasMoreData = hasMoreData;
        resetHeader();
    }

    protected final void setHeaderScroll(int y) {
        if (mOnScrollYChangeListener != null) {
            mOnScrollYChangeListener.onScrol(y);
        }
        scrollTo(0, y);
    }

    /**
     * setScrollToHeader 设置显示顶部下拉刷新状态
     */
    protected void setHeaderVisible() {
        mHeaderLayout.refreshing();
        if (mMode.canPullDown()) {
            // 获取到数据时显示动画延时处理
            setTimeTask();
            if (getItemCount() > 0 && mRefreshableView instanceof ListView) {
                // 直接显示头部 无下拉动画
                setHeaderScroll(-mHeaderHeight);
            } else {
                mRefreshableView.setVisibility(View.INVISIBLE);
                smoothScrollTo(-mHeaderHeight);
            }
        }
    }

    /**
     * 启动头部刷新计时器
     */
    protected void setTimeTask() {
        cancelTimeTask();
        mIsHeaderVisible = true;
        Timer mTimer = new Timer();
        mRefreshTimeTask = new RefreshTimeTask();
        EvtLog.d(LOG_TAG, "启动头部刷新计时器...");
        mIsHeaderMinTimeup = false;
        mTimer.schedule(mRefreshTimeTask, HEADER_REFRESH_MIN_TIME);
    }

    /**
     * 退出计时器
     */
    private void cancelTimeTask() {
        if (mRefreshTimeTask != null) {
            EvtLog.d(LOG_TAG, "关闭头部超时计时器...");
            mRefreshTimeTask.cancel();
            mRefreshTimeTask = null;
        }
    }

    /**
     * setHeaderVisible 设置显示顶部下拉刷新状态
     *
     * @param isToRefresh 是否刷新数据
     */
    public final void setHeaderVisible(boolean isToRefresh) {
        mState = REFRESHING;
        mIsHeaderFreshing = true;
        mIsBeingDragged = true;
        mCurrentMode = Mode.PULL_DOWN_TO_REFRESH;
        setEmptyGone();
        setSelectionTop();
        setHeaderVisible();
        mState = MANUAL_REFRESHING;
        if (isToRefresh && mMode.canPullDown() && mOnRefreshListener2 != null) {
            mHeaderRefreshHandler.sendEmptyMessage(HEADER_REFRESH);
        }
    }

    protected void setRefreshingInternal(boolean doScroll) {
        mState = REFRESHING;
        if (mMode.canPullDown()) {
            mHeaderLayout.refreshing();
        }
        if (mMode.canPullUp()) {
            mFooterLayout.refreshing();
        }

        if (doScroll) {
            if (mShowViewWhileRefreshing) {
                smoothScrollTo(mCurrentMode == Mode.PULL_DOWN_TO_REFRESH ? -mHeaderHeight
                        : mFooterHeight);
            } else {
                smoothScrollTo(0);
            }
        }
    }

    protected final void smoothScrollTo(int y) {
        if (null != mCurrentSmoothScrollRunnable) {
            mCurrentSmoothScrollRunnable.stop();
        }

        if (mOnScrollYChangeListener != null) {
            mOnScrollYChangeListener.onScrol(y);
        }
        if (getScrollY() != y) {
            mCurrentSmoothScrollRunnable = new SmoothScrollRunnable(mHandler,
                    getScrollY(), y);
            mHandler.post(mCurrentSmoothScrollRunnable);
        }
    }

    /**
     * Updates the View State when the mode has been set. This does not do any
     * checking that the mode is different to current state so always updates.
     */
    protected void updateUIForMode() {
        // Remove Header, and then add Header Loading View again if needed
        if (this == mHeaderLayout.getParent()) {
            removeView(mHeaderLayout);
        }
        if (mMode.canPullDown()) {
            addView(mHeaderLayout, 0, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        // Remove Footer, and then add Footer Loading View again if needed
        if (this == mFooterLayout.getParent()) {
            removeView(mFooterLayout);
        }
        if (mMode.canPullUp()) {
            addView(mFooterLayout, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

        }

        // Hide Loading Views
        refreshLoadingViewsHeight();

        // If we're not using Mode.BOTH, set mCurrentMode to mMode, otherwise
        // set it to pull down
        mCurrentMode = (mMode != Mode.BOTH) ? mMode : Mode.PULL_DOWN_TO_REFRESH;
    }

    /**
     * Re-measure the Loading Views height, and adjust internal padding as
     * necessary
     */
    protected void refreshLoadingViewsHeight() {
        if (mMode.canPullDown()) {
            measureView(mHeaderLayout);
            mHeaderHeight = mHeaderLayout.getMeasuredHeight();
        } else if (mMode.canPullUp()) {
            measureView(mFooterLayout);
            mFooterHeight = mFooterLayout.getMeasuredHeight();
        }

        // Hide Loading Views
        switch (mMode) {
            case BOTH:
                setPadding(0, -mHeaderHeight, 0, -mHeaderHeight);
                break;
            case PULL_UP_TO_REFRESH:
                setPadding(0, 0, 0, -mFooterHeight);
                break;
            case PULL_DOWN_TO_REFRESH:
            default:
                setPadding(0, -mHeaderHeight, 0, 0);
                break;
        }
    }

    private boolean isReadyForPull() {
        switch (mMode) {
            case PULL_DOWN_TO_REFRESH:
                return isReadyForPullDown();
            case PULL_UP_TO_REFRESH:
                return isReadyForPullUp();
            case BOTH:
                return isReadyForPullUp() || isReadyForPullDown();
            default:
                break;
        }
        return false;
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * Actions a Pull Event
     *
     * @return true if the Event has been handled, false if there has been no
     * change
     */
    private boolean pullEvent() {
        final int newHeight;
        final int oldHeight = getScrollY();
        // 头部刷新时 禁止向下滑动
        if (mIsHeaderFreshing && mIsShowHeader
                && (mLastMotionY > mInitialMotionY)) {
            return false;
        }
        // 更新header、footer高度 除FRICTION系数，使滑动时更缓和
        switch (mCurrentMode) {
            case PULL_UP_TO_REFRESH:
                newHeight = Math.round(Math.max(mInitialMotionY - mLastMotionY, 0)
                        / FRICTION);
                break;
            case PULL_DOWN_TO_REFRESH:
            default:
                newHeight = Math.round(Math.min(mInitialMotionY - mLastMotionY, 0)
                        / FRICTION);
                break;
        }

        // 更新滚动位置
        setHeaderScroll(newHeight);

        if (newHeight != 0) {
            float scale = Math.abs(newHeight) / (float) mHeaderHeight;
            switch (mCurrentMode) {
                case PULL_UP_TO_REFRESH:
                    // mFooterLayout.onPullY(scale);
                    // mFooterLayout.hindFooterImage();
                    break;
                case PULL_DOWN_TO_REFRESH:
                    if (!mIsLoadingData) {
                        // 更新头部动画
                        mHeaderLayout.onPullY(scale);
                    }
                    break;
                default:
                    break;
            }

            headerVisibleChange(mHeaderHeight - Math.abs(newHeight));

            if (mState == PULL_TO_REFRESH
                    && mHeaderHeight < Math.abs(newHeight)) {
                mState = RELEASE_TO_REFRESH;
                onReleaseToRefresh();
                return true;
            } else if (mState == RELEASE_TO_REFRESH
                    && mHeaderHeight >= Math.abs(newHeight)) {
                mState = PULL_TO_REFRESH;
                onPullToRefresh();
                return true;
            }
        }

        return oldHeight != newHeight;
    }

    /**
     * 该方法用于控件下拉时，header完全显示与部分显示触发的事件
     *
     * @param differentHeight header高度与下拉高度的差值
     */
    protected void headerVisibleChange(int differentHeight) {
        // 基类中什么都不做，
    }

    /**
     * 获取Item个数
     */
    protected int getItemCount() {
        return 0;
    }

    /**
     * 设置选中第一项
     */
    protected void setSelectionTop() {
    }

    /**
     * emptyViewState 控制emptyView 显示与隐藏
     */
    protected void setEmptyViewState() {
        // 基类中什么都不做，
    }

    /**
     * emptyViewState 控制emptyView 隐藏
     */
    protected void setEmptyGone() {
        // 基类中什么都不做，
    }

    /**
     * 上下拉动控件的模式，分为下拉、上拉和上下拉3种
     *
     * @author wang.xy
     */
    public enum Mode {
        /**
         * Only allow the user to Pull Down from the top to refresh, this is the
         * default.
         */
        PULL_DOWN_TO_REFRESH(0x1),

        /**
         * Only allow the user to Pull Up from the bottom to refresh.
         */
        PULL_UP_TO_REFRESH(0x2),

        /**
         * Allow the user to both Pull Down from the top, and Pull Up from the
         * bottom to refresh.
         */
        BOTH(0x3);

        private static final int THREE = 0x3;
        private int mIntValue;

        // The modeInt values need to match those from attrs.xml
        Mode(int modeInt) {
            mIntValue = modeInt;
        }

        /**
         * Maps an int to a specific mode. This is needed when saving state, or
         * inflating the view from XML where the mode is given through a attr
         * int.
         *
         * @param modeInt - int to map a Mode to
         * @return Mode that modeInt maps to, or PULL_DOWN_TO_REFRESH by
         * default.
         */
        public static Mode mapIntToMode(int modeInt) {
            switch (modeInt) {
                case THREE:
                    return BOTH;
                case 0x2:
                    return PULL_UP_TO_REFRESH;
                case 0x1:
                default:
                    return PULL_DOWN_TO_REFRESH;
            }
        }

        /**
         * @return true if this mode permits Pulling Down from the top
         */
        boolean canPullDown() {
            return this == PULL_DOWN_TO_REFRESH || this == BOTH;
        }

        /**
         * @return true if this mode permits Pulling Up from the bottom
         */
        boolean canPullUp() {
            return this == PULL_UP_TO_REFRESH || this == BOTH;
        }

        int getIntValue() {
            return mIntValue;
        }

    }

    /**
     * Simple Listener that allows you to be notified when the user has scrolled
     * to the end of the AdapterView. See (
     * {@link PullToRefreshAdapterViewBase#setOnLastItemVisibleListener}.
     *
     * @author Chris Banes
     */
    public interface OnLastItemVisibleListener {

        /**
         * Called when the user has scrolled to the end of the list
         */
        void onLastItemVisible();

    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public interface OnRefreshListener {

        /**
         * onRefresh will be called for both Pull Down from top, and Pull Up
         * from Bottom
         */
        void onRefresh();

    }

    /**
     * Y 坐标变换监听，用于露出header或者 footer监听
     *
     * @author tan.xx
     */
    public interface OnScrollYChangeListener {

        /**
         * 滑动 纵向
         */
        void onScrol(int y);

    }

    /**
     * An advanced version of the Listener to listen for callbacks to Refresh.
     * This listener is different as it allows you to differentiate between Pull
     * Ups, and Pull Downs.
     *
     * @author Chris Banes
     */
    public interface OnRefreshListener2 {

        /**
         * onPullDownToRefresh will be called only when the user has Pulled Down
         * from the top, and released.
         */
        void onPullDownToRefresh();

        /**
         * onPullUpToRefresh will be called only when the user has Pulled Up
         * from the bottom, and released.
         */
        void onPullUpToRefresh();
    }

    public interface OnResizeListener {
        void onResizeListener(int w, int h, int oldw, int oldh);
    }

    /**
     * 头部刷新计时器
     *
     * @author tan.xx
     */
    class RefreshTimeTask extends TimerTask {

        /**
         * run
         *
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
            Message msg = mHeaderRefreshHandler
                    .obtainMessage(HEADER_REFRESH_TIME_TASK);
            mHeaderRefreshHandler.sendMessage(msg);
        }
    }

    final class SmoothScrollRunnable implements Runnable {

        private static final int A_THOUSAND = 1000;
        private static final int ANIMATION_FPS = 16;

        private static final int ANIMATION_DURATION_MS = 190;

        private final Interpolator mInterpolator;
        private final int mScrollToY;
        private final int mScrollFromY;
        private final Handler mHandler;

        private boolean mContinueRunning = true;
        private long mStartTime = -1;
        private int mCurrentY = -1;

        public SmoothScrollRunnable(Handler handler, int fromY, int toY) {
            mHandler = handler;
            mScrollFromY = fromY;
            mScrollToY = toY;
            mInterpolator = new AccelerateDecelerateInterpolator();
        }

        @Override
        public void run() {

            /**
             * Only set mStartTime if this is the first time we're starting,
             * else actually calculate the Y delta
             */
            if (mStartTime == -1) {
                mStartTime = System.currentTimeMillis();
            } else {

                /**
                 * We do do all calculations in long to reduce software float
                 * calculations. We use 1000 as it gives us good accuracy and
                 * small rounding errors
                 */
                long normalizedTime = (A_THOUSAND * (System.currentTimeMillis() - mStartTime))
                        / ANIMATION_DURATION_MS;
                normalizedTime = Math.max(Math.min(normalizedTime, A_THOUSAND),
                        0);

                final int deltaY = Math.round((mScrollFromY - mScrollToY)
                        * mInterpolator
                        .getInterpolation(normalizedTime / 1000f));
                mCurrentY = mScrollFromY - deltaY;
                setHeaderScroll(mCurrentY);
                if (mCurrentY == 0) {
                    // 重置Header 显示
                    if (mIsReset && mMode.canPullDown()) {
                        mHeaderLayout.reset();
                        mIsReset = false;
                    }
                }
            }
            // If we're not at the target Y, keep going...
            if (mContinueRunning && mScrollToY != mCurrentY) {
                mHandler.postDelayed(this, ANIMATION_FPS);
            }
        }

        public void stop() {
            mContinueRunning = false;
            mHandler.removeCallbacks(this);
        }
    }

}
