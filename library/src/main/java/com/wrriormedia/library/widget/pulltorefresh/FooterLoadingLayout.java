/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.wrriormedia.library.widget.pulltorefresh;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wrriormedia.library.R;
import com.wrriormedia.library.util.PackageUtil;
import com.wrriormedia.library.widget.pulltorefresh.PullToRefreshBase.Mode;

/**
 * 下方的加载布局类
 * 
 * @author wang.xy
 * @since 2012-8-14
 * @version 2012-8-14，王先佑，重构文本、注释等 <br>
 *          2012-09-29 谭晓星 增加下拉刷新时无数据时的处理
 * 
 */
public class FooterLoadingLayout extends FrameLayout {
	// 是否还有更多数据
	public boolean HasMoreData = true;
	private final TextView mFooterText;
	private final ProgressBar mProgressBarRight;

	private String mRefreshingLabel;
	private String mReleaseLabel;
	// 原始文字显示
	private String mInternalReleaseLabel;
	private String mNoMoreDataLabel;
	private boolean mIsShowFooter;

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            当前布局文件的上下文
	 */
	public FooterLoadingLayout(Context context) {
		super(context);

		ViewGroup header = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_footer, this);
		mFooterText = (TextView) header.findViewById(R.id.pull_to_refresh_text);
		mProgressBarRight = (ProgressBar) header.findViewById(R.id.pull_to_refresh_progress_right);

		mNoMoreDataLabel = PackageUtil.getString(R.string.no_more_data);
		mRefreshingLabel = PackageUtil.getString(R.string.pull_to_refresh_from_bottom_refreshing_label);
		mReleaseLabel = PackageUtil.getString(R.string.pull_to_refresh_from_bottom_release_label);
		mInternalReleaseLabel = PackageUtil.getString(R.string.pull_down_to_refresh_pull_label);

		reset();
	}

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            当前布局文件的上下文
	 * @param mode
	 *            拉动的样式
	 * @param attrs
	 *            属性数组
	 */
	public FooterLoadingLayout(Context context, final Mode mode, TypedArray attrs) {
		super(context);

		ViewGroup header = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_footer, this);
		mFooterText = (TextView) header.findViewById(R.id.pull_to_refresh_text);
		mProgressBarRight = (ProgressBar) header.findViewById(R.id.pull_to_refresh_progress_right);

		if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextColor)) {
			ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderTextColor);
			setTextColor(null != colors ? colors : ColorStateList.valueOf(Color.BLACK));
		}

		if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderBackground)) {
			Drawable background = attrs.getDrawable(R.styleable.PullToRefresh_ptrHeaderBackground);
			if (null != background) {
				setBackgroundDrawable(background);
			}
		}
		mIsShowFooter = attrs.getBoolean(R.styleable.PullToRefresh_isShowFooter, true);
		if (!mIsShowFooter) {
			this.setVisibility(View.INVISIBLE);
		}
		// 修复5.0崩溃的问题
		// attrs.recycle();
		// attrs = null;
		mNoMoreDataLabel = PackageUtil.getString(R.string.no_more_data);
		mRefreshingLabel = PackageUtil.getString(R.string.pull_to_refresh_from_bottom_refreshing_label);
		mReleaseLabel = PackageUtil.getString(R.string.pull_to_refresh_from_bottom_release_label);
		mInternalReleaseLabel = PackageUtil.getString(R.string.pull_down_to_refresh_pull_label);

		reset();
	}

	/**
	 * 将下部的各个子控件重置为初始状态
	 */
	public void reset() {
		HasMoreData = true;

		mRefreshingLabel = PackageUtil.getString(R.string.pull_to_refresh_from_bottom_refreshing_label);
		mReleaseLabel = PackageUtil.getString(R.string.pull_to_refresh_from_bottom_release_label);

		mFooterText.setText(Html.fromHtml(mInternalReleaseLabel));
		mProgressBarRight.setVisibility(View.GONE);
		if (!mIsShowFooter) {
			this.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 重置为初始状态
	 * 
	 * @param hasMoreData
	 *            是否还有更多数据
	 */
	public void reset(boolean hasMoreData) {
		HasMoreData = hasMoreData;

		if (HasMoreData) {
			mRefreshingLabel = PackageUtil.getString(R.string.pull_to_refresh_from_bottom_refreshing_label);
			mReleaseLabel = PackageUtil.getString(R.string.pull_to_refresh_from_bottom_release_label);
			mFooterText.setText(Html.fromHtml(mInternalReleaseLabel));
		} else {
			// 已无数据
			mFooterText.setText(Html.fromHtml(mRefreshingLabel));
		}

		mProgressBarRight.setVisibility(View.GONE);
		if (!mIsShowFooter) {
			this.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 释放是进行刷新动作
	 */
	public void releaseToRefresh() {
		if (!HasMoreData) {
			// 已无数据
			mFooterText.setText(Html.fromHtml(mRefreshingLabel));
		} else {
			mFooterText.setText(Html.fromHtml(mReleaseLabel));
		}
	}

	/**
	 * 设置各控件为正在刷新的状态
	 */
	public void refreshing() {
		mFooterText.setText(Html.fromHtml(mRefreshingLabel));
		if (HasMoreData) {
			mProgressBarRight.setVisibility(View.VISIBLE);
		} else {
			mProgressBarRight.setVisibility(View.GONE);
		}
	}

	/**
	 * 设置“正在刷新”时显示的文本信息；该方法在没有更多数据时无效
	 * 
	 * @param refreshingLabel
	 *            刷新的文本信息
	 */
	public void setRefreshingLabel(String refreshingLabel) {
		if (HasMoreData) {
			mRefreshingLabel = refreshingLabel;
		}
	}

	/**
	 * 设置“释放控件”时显示的文本信息；该方法在没有更多数据时无效
	 * 
	 * @param releaseLabel
	 *            刷新的文本信息
	 */
	public void setReleaseLabel(String releaseLabel) {
		if (HasMoreData) {
			mReleaseLabel = releaseLabel;
		}
	}

	private void setTextColor(ColorStateList color) {
		mFooterText.setTextColor(color);
	}

	/**
	 * 设置上拉刷新还没有完全显示时的默认文字
	 * 
	 * @param pullLabel
	 *            上拉刷新还没有完全显示时的默认文字
	 */
	public void setPullLabel(String pullLabel) {
		mInternalReleaseLabel = pullLabel;
	}

	/**
	 * 当上拉刷新还没有完全显示头部时，显示的文本信息
	 */
	public void pullToRefresh() {
		if (HasMoreData) {
			mFooterText.setText(Html.fromHtml(mInternalReleaseLabel));
		} else {
			mFooterText.setText(Html.fromHtml(mRefreshingLabel));
		}
	}

	/**
	 * 设置“没有更多数据”时，显示的文本信息
	 * 
	 * @param label
	 *            “没有更多数据”时，显示的文本信息
	 */
	public void setNoMoreDataLabel(String label) {
		HasMoreData = false;

		mNoMoreDataLabel = label;
		mRefreshingLabel = label;
		mReleaseLabel = label;
		mFooterText.setText(Html.fromHtml(mRefreshingLabel));
	}

	/**
	 * 设置“没有更多数据”时，显示默认的文本提示信息
	 */
	public void setNoMoreDataLabel() {
		HasMoreData = false;
		mRefreshingLabel = mNoMoreDataLabel;
		mReleaseLabel = mNoMoreDataLabel;
		mFooterText.setText(Html.fromHtml(mRefreshingLabel));
	}

	/**
	 * @return the mIsShowFooter
	 */
	public boolean isShowFooter() {
		return mIsShowFooter;
	}

}
