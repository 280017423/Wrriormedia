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
import android.util.AttributeSet;

/**
 * 上下拉动刷新ListView控件
 *
 *
 * @author xu.xb
 * @since 2012-8-14
 * @version 2014-3-20 xu.xb 创建中间添加时间线的下拉菜单
 *
 */
public class PullToRefreshWithTimeLineListView extends PullToRefreshListView {
    public PullToRefreshWithTimeLineListView(Context context) {
        super(context);
    }

    public PullToRefreshWithTimeLineListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean isNeedTimeLine() {
        return true;
    }
}
