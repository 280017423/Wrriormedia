package com.wrriormedia.library.widget.pulltorefresh;

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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.wrriormedia.library.R;

/**
 * 上下拉动刷新ListView控件
 *
 * @author wang.xy
 * @version 2012-8-14，王先佑，重构文本、注释等<br>
 *          2012-09-29 谭晓星 增加下拉刷新时无数据时的处理<br>
 *          2012-10/31 徐晓柏 增加下拉刷新内容列表事件分发处理<br>
 *          2012-11-09 谭晓星 重载setHeaderVisible方法，优化频繁使用此方法时显示异常问题<br>
 *          2014-02-06 谭晓星 修复setHeaderVisible方法，延时动画问题<br>
 * @since 2012-8-14
 */
public class PullToRefreshListView extends PullToRefreshAdapterViewBase<ListView> {

    // listview headerview是否有显示过
    private boolean mListViewHasShow;
    private HeaderLoadingLayout mHeaderLoadingView;
    private FooterLoadingLayout mFooterLoadingView;

    private FrameLayout mLvFooterLoadingFrame;
    // 是否是点击
    private boolean mIsOnclick;

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     */
    public PullToRefreshListView(Context context) {
        super(context);
        setDisableScrollingWhileRefreshing(false);
    }

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     * @param attrs   属性数组
     */
    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDisableScrollingWhileRefreshing(false);
    }

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     * @param mode    拉动的样式
     */
    public PullToRefreshListView(Context context, Mode mode) {
        super(context, mode);
        setDisableScrollingWhileRefreshing(false);
    }

    @Override
    public ContextMenuInfo getContextMenuInfo() {
        return ((InternalListView) getRefreshableView()).getContextMenuInfo();
    }

    @Override
    public void setHeaderLabel(CharSequence label) {
        super.setHeaderLabel(label);

        if (null != mHeaderLoadingView && getMode().canPullDown()) {
            mHeaderLoadingView.setSubHeaderText(label);
        }

        refreshLoadingViewsHeight();
    }

    public boolean isOnclick() {
        return mIsOnclick;
    }

    /**
     * 设置下拉时的文本信息
     *
     * @param pullLabel 下拉时的文本信息
     * @param mode      上下拉动的模式
     */
    @Override
    public void setPullLabel(String pullLabel, Mode mode) {
        super.setPullLabel(pullLabel, mode);

        if (null != mHeaderLoadingView && mode.canPullDown()) {
            mHeaderLoadingView.setPullLabel(pullLabel);
        }
        // if (null != mFooterLoadingView && mode.canPullUp()) {
        // // mFooterLoadingView.setPullLabel(pullLabel);
        // }
    }

    /**
     * 设置正在刷新的文本信息
     *
     * @param refreshingLabel 正在刷新的文本信息
     * @param mode            上下拉动的模式
     */
    @Override
    public void setRefreshingLabel(String refreshingLabel, Mode mode) {
        super.setRefreshingLabel(refreshingLabel, mode);

        if (null != mHeaderLoadingView && mode.canPullDown()) {
            mHeaderLoadingView.setRefreshingLabel(refreshingLabel);
        }
        if (null != mFooterLoadingView && mode.canPullUp()) {
            mFooterLoadingView.setRefreshingLabel(refreshingLabel);
        }
    }

    /**
     * 设置手势释放时的文本信息
     *
     * @param releaseLabel 手势释放时的文本信息
     * @param mode         上下拉动的模式
     */
    @Override
    public void setReleaseLabel(String releaseLabel, Mode mode) {
        super.setReleaseLabel(releaseLabel, mode);

        if (null != mHeaderLoadingView && mode.canPullDown()) {
            mHeaderLoadingView.setReleaseLabel(releaseLabel);
        }
        if (null != mFooterLoadingView && mode.canPullUp()) {
            mFooterLoadingView.setReleaseLabel(releaseLabel);
        }
    }

    @Override
    protected final ListView createRefreshableView(Context context, AttributeSet attrs) {
        ListView lv = new InternalListView(context, attrs);
        // Get Styles from attrs
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefresh);

        // Create Loading Views ready for use later
        FrameLayout frame = new FrameLayout(context);
        mHeaderLoadingView = new HeaderLoadingLayout(context, Mode.PULL_DOWN_TO_REFRESH, a);
        frame.addView(mHeaderLoadingView, ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        mHeaderLoadingView.setVisibility(View.GONE);
        lv.addHeaderView(frame, null, false);

        mLvFooterLoadingFrame = new FrameLayout(context);
        mFooterLoadingView = new FooterLoadingLayout(context, Mode.PULL_UP_TO_REFRESH, a);
        mLvFooterLoadingFrame.addView(mFooterLoadingView, ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        mFooterLoadingView.setVisibility(View.GONE);

        a.recycle();

        // Set it to this so it can be used in ListActivity/ListFragment
        lv.setId(android.R.id.list);
        return lv;
    }

    @Override
    protected int getNumberInternalFooterViews() {
        return null != mFooterLoadingView ? 1 : 0;
    }

    @Override
    protected int getNumberInternalHeaderViews() {
        return null != mHeaderLoadingView ? 1 : 0;
    }

    @Override
    protected void resetHeader() {

        // If we're not showing the Refreshing view, or the list is empty, then
        // the header/footer views won't show so we use the
        // normal method
        ListAdapter adapter = mRefreshableView.getAdapter();
        if (!mListViewHasShow) {
            // headerView未显示过
            if (!getShowViewWhileRefreshing() || null == adapter || adapter.isEmpty()) {
                super.resetHeader();
                return;
            }
        }

        HeaderLoadingLayout originalLoadingLayout = null;
        HeaderLoadingLayout listViewLoadingLayout = null;
        FooterLoadingLayout originalFooterLoadingLayout = null;
        FooterLoadingLayout listViewFooterLoadingLayout = null;

        int scrollToHeight = getHeaderHeight();
        int selection;
        boolean scroll;

        switch (getCurrentMode()) {
            case PULL_UP_TO_REFRESH:
                originalFooterLoadingLayout = getFooterLayout();
                listViewFooterLoadingLayout = mFooterLoadingView;
                selection = mRefreshableView.getCount() - 1;
                scroll = mRefreshableView.getLastVisiblePosition() == selection;
                break;
            case PULL_DOWN_TO_REFRESH:
            default:
                originalLoadingLayout = getHeaderLayout();
                listViewLoadingLayout = mHeaderLoadingView;
                scrollToHeight *= -1;
                selection = 0;
                scroll = mRefreshableView.getFirstVisiblePosition() == selection;
                break;
        }

        // Set our Original View to Visible
        if (originalLoadingLayout != null) {
            originalLoadingLayout.setVisibility(View.VISIBLE);
        } else {
            originalFooterLoadingLayout.setVisibility(View.VISIBLE);
        }

        /**
         * Scroll so the View is at the same Y as the ListView header/footer,
         * but only scroll if we've pulled to refresh and it's positioned
         * correctly
         */
        if (scroll && getState() != MANUAL_REFRESHING) {
            mRefreshableView.setSelection(selection);
            setHeaderScroll(scrollToHeight);
        }

        // Hide the ListView Header/Footer
        if (listViewLoadingLayout != null) {
            listViewLoadingLayout.setVisibility(View.GONE);
            setTimeLineTopPadding(0);
        } else {
            listViewFooterLoadingLayout.setVisibility(View.GONE);
        }

        super.resetHeader();
    }

    @Override
    protected void setRefreshingInternal(boolean doScroll) {
        // If we're not showing the Refreshing view, or the list is empty, then
        // the header/footer views won't show so we use the
        // normal method
        ListAdapter adapter = mRefreshableView.getAdapter();
        if (!getShowViewWhileRefreshing() || null == adapter || adapter.isEmpty()) {
            super.setRefreshingInternal(doScroll);
            return;
        }
        super.setRefreshingInternal(false);
        HeaderLoadingLayout originalLoadingLayout = null;
        HeaderLoadingLayout listViewLoadingLayout = null;
        FooterLoadingLayout originalFooterLoadingLayout = null;
        FooterLoadingLayout listViewFooterLoadingLayout = null;
        final int selection;
        final int scrollToY;
        switch (getCurrentMode()) {
            case PULL_UP_TO_REFRESH:
                originalFooterLoadingLayout = getFooterLayout();
                listViewFooterLoadingLayout = mFooterLoadingView;
                selection = mRefreshableView.getCount() - 1;
                scrollToY = getScrollY() - getFooterHeight();
                break;
            case PULL_DOWN_TO_REFRESH:
            default:
                originalLoadingLayout = getHeaderLayout();
                listViewLoadingLayout = mHeaderLoadingView;
                selection = 0;
                scrollToY = getScrollY() + getHeaderHeight();
                break;
        }
        if (doScroll) {
            // We scroll slightly so that the ListView's header/footer is at the
            // same Y position as our normal header/footer
            setHeaderScroll(scrollToY);
        }
        // 还有更多数据
        if (originalFooterLoadingLayout != null && originalFooterLoadingLayout.HasMoreData) {
            // Set our Original View to Visible
            if (originalLoadingLayout != null) {
                originalLoadingLayout.setVisibility(View.INVISIBLE);
            } else {
                originalFooterLoadingLayout.setVisibility(View.INVISIBLE);
            }
            // Hide the ListView Header/Footer
            if (listViewLoadingLayout != null) {
                listViewLoadingLayout.setVisibility(View.VISIBLE);
                listViewLoadingLayout.measure(0, 0);
                setTimeLineTopPadding(listViewLoadingLayout.getMeasuredHeight());
                mListViewHasShow = true;
                listViewLoadingLayout.refreshing();
            } else {
                listViewFooterLoadingLayout.setVisibility(View.VISIBLE);
                listViewFooterLoadingLayout.refreshing();
            }
        } else {
            // 无更多数据 Set our Original View to Visible
            if (originalLoadingLayout != null) {
                originalLoadingLayout.setVisibility(View.INVISIBLE);
            } else {
                // 显示父类的FooterLoadingLayout
                originalFooterLoadingLayout.setVisibility(View.VISIBLE);
            }
            // Hide the ListView Header/Footer
            if (listViewLoadingLayout != null) {
                mListViewHasShow = true;
                listViewLoadingLayout.setVisibility(View.VISIBLE);
                listViewLoadingLayout.measure(0, 0);
                setTimeLineTopPadding(listViewLoadingLayout.getMeasuredHeight());
                listViewLoadingLayout.refreshing();
            } else {
                // 隐藏 listViewFooter 用父类的FooterLoadingLayout
                listViewFooterLoadingLayout.setVisibility(View.GONE);
                originalFooterLoadingLayout.refreshing();
            }
        }
        if (doScroll) {
            // Make sure the ListView is scrolled to show the loading
            // header/footer
            mRefreshableView.setSelection(selection);
            // Smooth scroll as normal
            smoothScrollTo(0);
        }
    }

    @Override
    protected void setHeaderVisible() {

        if (mIsFisrtHeaderShowing || getItemCount() <= 0) {
            super.setHeaderVisible();
        } else {
            // 获取到数据时显示动画延时处理
            setTimeTask();
            // 列表有数据时
            // 解决重复调用显示头部刷新，头部布局不随listview滚动问题
            HeaderLoadingLayout originalLoadingLayout;
            HeaderLoadingLayout listViewLoadingLayout;
            originalLoadingLayout = getHeaderLayout();
            listViewLoadingLayout = mHeaderLoadingView;
            // Scroll 到顶部 显示为listview的headerview

            setHeaderScroll(0);
            // Set our Original View to Visible
            if (originalLoadingLayout != null) {
                originalLoadingLayout.setVisibility(View.INVISIBLE);
            }
            // 显示lisetview headerview
            if (listViewLoadingLayout != null) {
                mListViewHasShow = true;
                listViewLoadingLayout.setVisibility(View.VISIBLE);
                listViewLoadingLayout.refreshing();
            }
        }
    }

    @Override
    protected boolean isNeedTimeLine() {
        return false;
    }

    class InternalListView extends ListView implements EmptyViewMethodAccessor {

        private boolean mAddedLvFooter;
        private int mDefaultXDistance;
        private int mDefaultYDistance;
        /**
         * 最后一次触发的坐标
         */
        private float mStartX = 0;
        private float mStartY = 0;
        /**
         * 用来判定Y轴的滑动
         */
        private int touchSlop = 0;
        private float mRate = 1;

        public InternalListView(Context context, AttributeSet attrs) {
            super(context, attrs);

            final ViewConfiguration configuration = ViewConfiguration.get(getContext());
            touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
            mDefaultXDistance = ViewConfiguration.getMinimumFlingVelocity();

            // ViewConfiguration viewConfiguration =
            // ViewConfiguration.get(context);
            // mDefaultXDistance = viewConfiguration.getMinimumFlingVelocity();
            // mDefaultYDistance = mDefaultXDistance / 2;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = (int) ev.getX();
                    mStartY = (int) ev.getY();
                    // Log.d("xx", "===============down===");
                    break;
                case MotionEvent.ACTION_UP:
                    if (!isMovingInX(ev.getX())) {
                        mIsOnclick = true;
                        // return false;
                    }
                    mIsOnclick = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int stopX = (int) ev.getX();
                    int stopY = (int) ev.getY();
                    // Log.d("xx", "===============isMovingInX==="
                    // + isMovingInX(stopX) + "   " + lastMotionX +"   " +
                    // mDefaultXDistance / 5 );
                    // 若根据自定义规则判断当前是水平滑动，则将事件交给下层控件处理
                    if (!isMovingInY(stopX, stopY)) {
                        return false;
                    }

                default:
                    break;
            }
            return super.onInterceptTouchEvent(ev);
        }

        /**
         * 检测是否是纵滑动
         *
         * @param x Position X
         * @param y Position Y
         */
        private boolean isMovingInY(float x, float y) {
            final int xDiff = (int) Math.abs(x - mStartX);
            final int yDiff = (int) Math.abs(y - mStartY);
            final int touchSlop = this.touchSlop;
            return xDiff == 0 || (yDiff / xDiff >= 1) && (yDiff > touchSlop);
        }

        /**
         * isMovingInX是否是水平移动，区分点击事件
         *
         * @param x 水平移动的间距
         * @return boolean 是否是水平移动
         */
        private boolean isMovingInX(float x) {
            final int xDiff = (int) Math.abs(x - mStartX);
            return xDiff >= mDefaultXDistance / 2;
        }

        private boolean isMovingHorizontal(int startX, int startY, int stopX, int stopY) {
            int xDiff = stopX - startX;
            int yDiff = Math.abs(stopY - startY);
            boolean conditionOne = xDiff >= mDefaultXDistance && yDiff < mDefaultYDistance;
            boolean conditionTwo = yDiff >= mDefaultYDistance && xDiff < -mDefaultXDistance
                    && Math.abs(xDiff) / yDiff > mRate;
            return conditionOne || conditionTwo;
        }

        @Override
        public void draw(Canvas canvas) {
            /**
             * This is a bit hacky, but ListView has got a bug in it when using
             * Header/Footer Views and the list is empty. This masks the issue
             * so that it doesn't cause an FC. See Issue #66.
             */
            try {
                super.draw(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public ContextMenuInfo getContextMenuInfo() {
            return super.getContextMenuInfo();
        }

        @Override
        public void setAdapter(ListAdapter adapter) {
            // Add the Footer View at the last possible moment
            if (!mAddedLvFooter && mIsFooterViewEnabled) {
                addFooterView(mLvFooterLoadingFrame, null, false);
                mAddedLvFooter = true;
            }
            if (mIsShowHeaderFresh) {
                setHeaderVisible(false);
            }
            super.setAdapter(adapter);
        }

        @Override
        public View getEmptyView() {
            return PullToRefreshListView.this.getEmptyView();
        }

        @Override
        public void setEmptyView(View emptyView) {
            PullToRefreshListView.this.setEmptyView(emptyView);
        }

        @Override
        public void setEmptyViewInternal(View emptyView) {
            super.setEmptyView(emptyView);
        }
    }

}
