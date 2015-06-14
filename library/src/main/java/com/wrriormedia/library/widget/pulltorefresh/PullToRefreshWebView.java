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
import android.util.AttributeSet;
import android.util.FloatMath;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.wrriormedia.library.R;


/**
 * 上下拉动刷新WebView控件
 *
 * @author wang.xy
 * @version 2012-8-14，王先佑，重构文本、注释等
 * @since 2012-8-14
 */
public class PullToRefreshWebView extends PullToRefreshBase<WebView> {

    private static final int FULL_PROCESS = 100;

    private final OnRefreshListener mDefaultOnRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            mRefreshableView.reload();
        }

    };

    private final WebChromeClient mDefaultWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == FULL_PROCESS) {
                onRefreshComplete();
            }
        }

    };

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     */
    public PullToRefreshWebView(Context context) {
        super(context);

        /**
         * Added so that by default, Pull-to-Refresh refreshes the page
         */
        setOnRefreshListener(mDefaultOnRefreshListener);
        mRefreshableView.setWebChromeClient(mDefaultWebChromeClient);
    }

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     * @param attrs   属性数组
     */
    public PullToRefreshWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        /**
         * Added so that by default, Pull-to-Refresh refreshes the page
         */
        setOnRefreshListener(mDefaultOnRefreshListener);
        mRefreshableView.setWebChromeClient(mDefaultWebChromeClient);
    }

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     * @param mode    拉动的样式
     */
    public PullToRefreshWebView(Context context, Mode mode) {
        super(context, mode);

        /**
         * Added so that by default, Pull-to-Refresh refreshes the page
         */
        setOnRefreshListener(mDefaultOnRefreshListener);
        mRefreshableView.setWebChromeClient(mDefaultWebChromeClient);
    }

    @Override
    protected WebView createRefreshableView(Context context, AttributeSet attrs) {
        WebView webView = new WebView(context, attrs);

        webView.setId(R.id.webview);
        return webView;
    }

    @Override
    protected boolean isReadyForPullDown() {
        return mRefreshableView.getScrollY() == 0;
    }

    @Override
    protected boolean isReadyForPullUp() {
        float exactContentHeight = FloatMath.floor(mRefreshableView.getContentHeight() * mRefreshableView.getScale());
        return mRefreshableView.getScrollY() >= (exactContentHeight - mRefreshableView.getHeight());
    }
}
