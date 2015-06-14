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
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.wrriormedia.library.R;
import com.wrriormedia.library.util.EvtLog;

/**
 * 上下拉动刷新控件的实现类，该类限定了子类必须是AbsListView的类型，该类同时实现了OnScrollListener接口
 *
 * @param <T> 上下拉动控件的主控件
 * @author wang.xy
 * @version 2012-08-15，王先佑，重构文本、注释等<br>
 *          2012-09-13 王先佑 当前一次没有数据，用户下拉时，隐藏mEmptyView<br>
 *          2012-09-18 王先佑 实现headerVisibleChange方法，当拉动控件时，计算是否显示emptyView；<br>
 *          2012-09-29 谭晓星 增加下拉刷新时无数据时的处理
 * @since 2012-8-14
 */
public abstract class PullToRefreshAdapterViewBase<T extends AbsListView> extends PullToRefreshBase<T> implements
        OnScrollListener {

    static final boolean DEFAULT_SHOW_INDICATOR = false;

    private int mSavedLastVisibleIndex = -1;
    private OnScrollListener mOnScrollListener;
    private OnLastItemVisibleListener mOnLastItemVisibleListener;
    private View mEmptyView;
    private FrameLayout mRefreshableViewHolder;

    private IndicatorLayout mIndicatorIvTop;
    private IndicatorLayout mIndicatorIvBottom;

    private boolean mShowIndicator;
    private View mVTimeLine;

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     */
    public PullToRefreshAdapterViewBase(Context context) {
        super(context);
        mRefreshableView.setOnScrollListener(this);
    }

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     * @param attrs   属性数组
     */
    public PullToRefreshAdapterViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRefreshableView.setOnScrollListener(this);
    }

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     * @param mode    拉动的样式
     */
    public PullToRefreshAdapterViewBase(Context context, Mode mode) {
        super(context, mode);
        mRefreshableView.setOnScrollListener(this);
    }

    /**
     * 获取长按菜单
     *
     * @return 返回长按菜单
     */
    @Override
    public abstract ContextMenuInfo getContextMenuInfo();

    /**
     * Gets whether an indicator graphic should be displayed when the View is in
     * a state where a Pull-to-Refresh can happen. An example of this state is
     * when the Adapter View is scrolled to the top and the mode is set to
     * {@link Mode#PULL_DOWN_TO_REFRESH}. The default value is
     * {@value #DEFAULT_SHOW_INDICATOR}.
     *
     * @return true if the indicators will be shown
     */
    public boolean getShowIndicator() {
        return mShowIndicator;
    }

    /**
     * Sets whether an indicator graphic should be displayed when the View is in
     * a state where a Pull-to-Refresh can happen. An example of this state is
     * when the Adapter View is scrolled to the top and the mode is set to
     * {@link Mode#PULL_DOWN_TO_REFRESH}
     *
     * @param showIndicator - true if the indicators should be shown.
     */
    public void setShowIndicator(boolean showIndicator) {
        mShowIndicator = showIndicator;

        if (getShowIndicatorInternal()) {
            // If we're set to Show Indicator, add/update them
            addIndicatorViews();
        } else {
            // If not, then remove then
            removeIndicatorViews();
        }
    }

    /**
     * @param view             当前滚动的view
     * @param firstVisibleItem 第一个可见项的下标
     * @param visibleItemCount 可见项的总数
     * @param totalItemCount   项的总数
     */
    @Override
    public final void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                               final int totalItemCount) {
        if (DEBUG) {
            Log.d(LOG_TAG, "First Visible: " + firstVisibleItem + ". Visible Count: " + visibleItemCount
                    + ". Total Items: " + totalItemCount);
        }

        // If we have a OnItemVisibleListener, do check...
        if (null != mOnLastItemVisibleListener) {
            // Detect whether the last visible item has changed
            final int lastVisibleItemIndex = firstVisibleItem + visibleItemCount;

            /**
             * Check that the last item has changed, we have any items, and that
             * the last item is visible. lastVisibleItemIndex is a zero-based
             * index, so we add one to it to check against totalItemCount.
             */
            if (visibleItemCount > 0 && (lastVisibleItemIndex + 1) == totalItemCount) {
                if (lastVisibleItemIndex != mSavedLastVisibleIndex) {
                    mSavedLastVisibleIndex = lastVisibleItemIndex;
                    mOnLastItemVisibleListener.onLastItemVisible();
                }
            }
        }

        // If we're showing the indicator, check positions...
        if (getShowIndicatorInternal()) {
            updateIndicatorViewsVisibility();
        }

        // Finally call OnScrollListener if we have one
        if (null != mOnScrollListener) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

    }

    /**
     * Callback method to be invoked while the list view or grid view is being
     * scrolled. If the view is being scrolled, this method will be called
     * before the next frame of the scroll is rendered. In particular, it will
     * be called before any calls to getView(int, View, ViewGroup).
     *
     * @param view        The view whose scroll state is being reported
     * @param scrollState The current scroll state. One of SCROLL_STATE_IDLE,
     *                    SCROLL_STATE_TOUCH_SCROLL or SCROLL_STATE_IDLE.
     */
    @Override
    public final void onScrollStateChanged(final AbsListView view, final int scrollState) {
        if (null != mOnScrollListener) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * 返回数据为空时候视图
     *
     * @return
     */
    public final View getEmptyView() {
        return mEmptyView;
    }

    /**
     * Sets the Empty View to be used by the Adapter View.
     * <p/>
     * We need it handle it ourselves so that we can Pull-to-Refresh when the
     * Empty View is shown.
     * <p/>
     * Please note, you do <strong>not</strong> usually need to call this method
     * yourself. Calling setEmptyView on the AdapterView will automatically call
     * this method and set everything up. This includes when the Android
     * Framework automatically sets the Empty View based on it's ID.
     *
     * @param newEmptyView - Empty View to be used
     */
    public final void setEmptyView(View newEmptyView) {
        // If we already have an Empty View, remove it
        if (null != mEmptyView) {
            mRefreshableViewHolder.removeView(mEmptyView);
        }

        mEmptyView = newEmptyView;
        if (null != newEmptyView) {
            // New view needs to be clickable so that Android recognizes it as a
            // target for Touch Events
            newEmptyView.setClickable(true);

            ViewParent newEmptyViewParent = newEmptyView.getParent();
            if (null != newEmptyViewParent && newEmptyViewParent instanceof ViewGroup) {
                ((ViewGroup) newEmptyViewParent).removeView(newEmptyView);
            }

            mRefreshableViewHolder.addView(newEmptyView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            if (mRefreshableView instanceof EmptyViewMethodAccessor) {
                ((EmptyViewMethodAccessor) mRefreshableView).setEmptyViewInternal(newEmptyView);
            } else {
                mRefreshableView.setEmptyView(newEmptyView);
            }
        }
    }

    /**
     * 设置最后一项显示时的监听器
     *
     * @param listener 被设置的监听器
     */
    public final void setOnLastItemVisibleListener(OnLastItemVisibleListener listener) {
        mOnLastItemVisibleListener = listener;
    }

    /**
     * 设置滚动的监听器
     *
     * @param listener 被设置的监听器
     */
    public final void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    @Override
    protected void addRefreshableView(Context context, T refreshableView) {
        mRefreshableViewHolder = new FrameLayout(context);
        mRefreshableViewHolder.addView(refreshableView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        if (isNeedTimeLine()) {
            // 加入时间线
            mVTimeLine = new View(context);
            mVTimeLine.setBackgroundResource(R.drawable.center_line);
            android.widget.FrameLayout.LayoutParams layoutParams = new android.widget.FrameLayout.LayoutParams(
                    2, android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            mVTimeLine.setFocusable(false);
            mVTimeLine.setFocusableInTouchMode(false);
            mRefreshableViewHolder.addView(mVTimeLine, layoutParams);
        }
        addView(mRefreshableViewHolder, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
    }

    protected final void setTimeLineTopPadding(int padding) {
        if (isNeedTimeLine() && mVTimeLine != null) {
            android.widget.FrameLayout.LayoutParams mLayoutParams = (android.widget.FrameLayout.LayoutParams) mVTimeLine
                    .getLayoutParams();
            mLayoutParams.topMargin = padding;
            mVTimeLine.setLayoutParams(mLayoutParams);
            mVTimeLine.invalidate();
        }
    }

    /**
     * 根据返回，确定是否需要使用时间线
     *
     * @return 是否需要绘制时间线
     */
    protected abstract boolean isNeedTimeLine();

    /**
     * Returns the number of Adapter View Footer Views. This will always return
     * 0 for non-ListView views.
     *
     * @return 0 for non-ListView views, possibly 1 for ListView
     */
    protected int getNumberInternalFooterViews() {
        return 0;
    }

    /**
     * Returns the number of Adapter View Header Views. This will always return
     * 0 for non-ListView views.
     *
     * @return 0 for non-ListView views, possibly 1 for ListView
     */
    protected int getNumberInternalHeaderViews() {
        return 0;
    }

    protected int getNumberInternalViews() {
        return getNumberInternalHeaderViews() + getNumberInternalFooterViews();
    }

    @Override
    protected void handleStyledAttributes(TypedArray a) {
        // Set Show Indicator to the XML value, or default value
        mShowIndicator = a.getBoolean(R.styleable.PullToRefresh_ptrShowIndicator, DEFAULT_SHOW_INDICATOR);
    }

    @Override
    protected boolean isReadyForPullDown() {
        return isFirstItemVisible();
    }

    @Override
    protected boolean isReadyForPullUp() {
        return isLastItemVisible();
    }

    @Override
    protected void onPullToRefresh() {
        super.onPullToRefresh();

        if (getShowIndicatorInternal()) {
            switch (getCurrentMode()) {
                case PULL_UP_TO_REFRESH:
                    mIndicatorIvBottom.pullToRefresh();
                    break;
                case PULL_DOWN_TO_REFRESH:
                    // default:
                    mIndicatorIvTop.pullToRefresh();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onReleaseToRefresh() {
        super.onReleaseToRefresh();

        if (getShowIndicatorInternal()) {
            switch (getCurrentMode()) {
                case PULL_UP_TO_REFRESH:
                    mIndicatorIvBottom.releaseToRefresh();
                    break;
                case PULL_DOWN_TO_REFRESH:
                default:
                    mIndicatorIvTop.releaseToRefresh();
                    break;
            }
        }

        // if (null != mEmptyView) {
        // if (mHaveExceededHeaderHeight) {
        // mEmptyView.setVisibility(View.INVISIBLE);
        // } else {
        // mEmptyView.setVisibility(View.VISIBLE);
        // }
        // EvtLog.d(LOG_TAG, "mIsExceedHeaderHeight=" +
        // mHaveExceededHeaderHeight);
        // }
    }

    @Override
    protected void resetHeader() {
        super.resetHeader();

        if (getShowIndicatorInternal()) {
            updateIndicatorViewsVisibility();
        }
    }

    @Override
    protected void setRefreshingInternal(boolean doScroll) {
        super.setRefreshingInternal(doScroll);

        if (getShowIndicatorInternal()) {
            updateIndicatorViewsVisibility();
        }
    }

    @Override
    protected void updateUIForMode() {
        super.updateUIForMode();

        // Check Indicator Views consistent with new Mode
        if (getShowIndicatorInternal()) {
            addIndicatorViews();
        }
    }

    private void addIndicatorViews() {
        Mode mode = getMode();

        if (mode.canPullDown() && null == mIndicatorIvTop) {
            // If the mode can pull down, and we don't have one set already
            mIndicatorIvTop = new IndicatorLayout(getContext(), Mode.PULL_DOWN_TO_REFRESH);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.rightMargin = getResources().getDimensionPixelSize(R.dimen.indicator_right_padding);
            params.gravity = Gravity.TOP | Gravity.END;
            mRefreshableViewHolder.addView(mIndicatorIvTop, params);

        } else if (!mode.canPullDown() && null != mIndicatorIvTop) {
            // If we can't pull down, but have a View then remove it
            mRefreshableViewHolder.removeView(mIndicatorIvTop);
            mIndicatorIvTop = null;
        }

        if (mode.canPullUp() && null == mIndicatorIvBottom) {
            // If the mode can pull down, and we don't have one set already
            mIndicatorIvBottom = new IndicatorLayout(getContext(), Mode.PULL_UP_TO_REFRESH);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.rightMargin = getResources().getDimensionPixelSize(R.dimen.indicator_right_padding);
            params.gravity = Gravity.BOTTOM | Gravity.END;
            mRefreshableViewHolder.addView(mIndicatorIvBottom, params);

        } else if (!mode.canPullUp() && null != mIndicatorIvBottom) {
            // If we can't pull down, but have a View then remove it
            mRefreshableViewHolder.removeView(mIndicatorIvBottom);
            mIndicatorIvBottom = null;
        }
    }

    private boolean getShowIndicatorInternal() {
        return mShowIndicator && isPullToRefreshEnabled();
    }

    private boolean isFirstItemVisible() {
        if (mRefreshableView.getCount() <= getNumberInternalViews()) {
            return true;
        } else if (mRefreshableView.getFirstVisiblePosition() == 0) {

            final View firstVisibleChild = mRefreshableView.getChildAt(0);

            if (firstVisibleChild != null) {
                return firstVisibleChild.getTop() >= mRefreshableView.getTop();
            }
        }

        return false;
    }

    private boolean isLastItemVisible() {
        final int count = mRefreshableView.getCount();
        final int lastVisiblePosition = mRefreshableView.getLastVisiblePosition();

        if (DEBUG) {
            Log.d(LOG_TAG, "isLastItemVisible. Count: " + count + " Last Visible Pos: " + lastVisiblePosition);
        }

        if (count <= getNumberInternalViews()) {
            return true;
        } else if (lastVisiblePosition == count - 1) {

            final int childIndex = lastVisiblePosition - mRefreshableView.getFirstVisiblePosition();
            final View lastVisibleChild = mRefreshableView.getChildAt(childIndex);

            if (lastVisibleChild != null) {
                return lastVisibleChild.getBottom() <= mRefreshableView.getBottom();
            }
        }

        return false;
    }

    private void removeIndicatorViews() {
        if (null != mIndicatorIvTop) {
            mRefreshableViewHolder.removeView(mIndicatorIvTop);
            mIndicatorIvTop = null;
        }

        if (null != mIndicatorIvBottom) {
            mRefreshableViewHolder.removeView(mIndicatorIvBottom);
            mIndicatorIvBottom = null;
        }
    }

    private void updateIndicatorViewsVisibility() {
        if (null != mIndicatorIvTop) {
            if (!isRefreshing() && isReadyForPullDown()) {
                if (!mIndicatorIvTop.isVisible()) {
                    mIndicatorIvTop.show();
                }
            } else {
                if (mIndicatorIvTop.isVisible()) {
                    mIndicatorIvTop.hide();
                }
            }
        }

        if (null != mIndicatorIvBottom) {
            if (!isRefreshing() && isReadyForPullUp()) {
                if (!mIndicatorIvBottom.isVisible()) {
                    mIndicatorIvBottom.show();
                }
            } else {
                if (mIndicatorIvBottom.isVisible()) {
                    mIndicatorIvBottom.hide();
                }
            }
        }
    }

    @Override
    protected void setSelectionTop() {
        if (this.getRefreshableView() != null) {
            this.getRefreshableView().setSelection(0);
        }
    }

    @Override
    protected void headerVisibleChange(int differentHeight) {
        super.headerVisibleChange(differentHeight);
        if (null != mEmptyView) {
            int itemCount = getItemCount();
            if (itemCount > 0) {
                mEmptyView.setVisibility(View.GONE);
            } else if (itemCount == 0) {

                if (differentHeight < 0) {
                    mEmptyView.setVisibility(View.GONE);
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected int getItemCount() {

        ListAdapter listAdapter = getRefreshableView().getAdapter();
        int itemCount;
        if (listAdapter == null) {
            itemCount = 0;
        } else if (listAdapter instanceof HeaderViewListAdapter) {
            HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter) listAdapter;
            itemCount = headerViewListAdapter.getCount() - headerViewListAdapter.getFootersCount()
                    - headerViewListAdapter.getHeadersCount();
        } else {
            itemCount = listAdapter.getCount();
        }
        EvtLog.d(LOG_TAG, "getIsLoadingData()=" + getIsLoadingData() + "，listAdapter == null " + (listAdapter == null)
                + "，listAdapter.getCount()=" + itemCount);
        return itemCount;
    }

    @Override
    protected void setEmptyViewState() {
        super.setEmptyViewState();
        if (null != mEmptyView) {
            int itemCount = getItemCount();
            if (itemCount > 0) {
                mEmptyView.setVisibility(View.GONE);
            } else if (itemCount == 0) {
                // 非第一次刷新
                if (getFreshSum() != PullToRefreshBase.INITIAL_FRESH_SUM && getIsLoadingData()) {
                    mEmptyView.setVisibility(View.GONE);
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }

        }
    }

    @Override
    protected void setEmptyGone() {
        if (null != mEmptyView) {
            mEmptyView.setVisibility(View.GONE);
        }
        super.setEmptyGone();
    }
}
