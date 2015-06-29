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
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

/**
 * 上下拉动刷新ExpandableListView控件
 *
 *
 * @author wang.xy
 * @since 2012-8-14
 * @version 2012-8-14，王先佑，重构文本、注释等
 *
 */
public class PullToRefreshExpandableListView extends PullToRefreshAdapterViewBase<ExpandableListView> {
    /**
     * 构造函数
     *
     * @param context
     *            当前布局文件的上下文
     */
    public PullToRefreshExpandableListView(Context context) {
        super(context);
    }

    /**
     * 构造函数
     *
     * @param context
     *            当前布局文件的上下文
     * @param attrs
     *            属性数组
     */
    public PullToRefreshExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 构造函数
     *
     * @param context
     *            当前布局文件的上下文
     * @param mode
     *            拉动的样式
     */
    public PullToRefreshExpandableListView(Context context, Mode mode) {
        super(context, mode);
    }

    @Override
    public ContextMenuInfo getContextMenuInfo() {
        return ((InternalExpandableListView) getRefreshableView()).getContextMenuInfo();
    }

    @Override
    protected final ExpandableListView createRefreshableView(Context context, AttributeSet attrs) {
        ExpandableListView lv = new InternalExpandableListView(context, attrs);

        // Set it to this so it can be used in ListActivity/ListFragment
        lv.setId(android.R.id.list);
        return lv;
    }

    @Override
    protected boolean isNeedTimeLine() {
        return false;
    }

    class InternalExpandableListView extends ExpandableListView implements EmptyViewMethodAccessor {

        public InternalExpandableListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public ContextMenuInfo getContextMenuInfo() {
            return super.getContextMenuInfo();
        }

        @Override
        public void setEmptyView(View emptyView) {
            PullToRefreshExpandableListView.this.setEmptyView(emptyView);
        }

        @Override
        public void setEmptyViewInternal(View emptyView) {
            super.setEmptyView(emptyView);
        }

        @Override
        public void setAdapter(ExpandableListAdapter adapter) {
            if (mIsShowHeaderFresh) {
                setHeaderVisible(false);
            }
            super.setAdapter(adapter);
        }
    }
}
