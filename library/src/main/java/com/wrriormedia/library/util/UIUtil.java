package com.wrriormedia.library.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * UI的帮助类
 *
 * @author wang.xy<br>
 * @version 2013-08-02 xu.xb 加入移动EditText光标的方法<br>
 */
public class UIUtil {
    private static final String TAG = "UIUtil";
    private static final Object mSync = new Object();
    private static final int DEFAULT_COOLING_TIME = 3000;
    private static final List<String> ACTION_LIST = new ArrayList<>();

    /**
     * 设置listview高度，以适应内容
     *
     * @param listView 指定的listview
     */
    public static void setListViewHeightMatchContent(ListView listView) {
        try {
            // 获取ListView对应的Adapter
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) {
                return;
            }

            int totalHeight = 0;
            int length = listAdapter.getCount();
            for (int i = 0; i < length; i++) { // listAdapter.getCount()返回数据项的数目
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0); // 计算子项View 的宽高
                totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
                EvtLog.d("debug", "setListViewHeightMatchContent, " + i + ", " + listItem.getMeasuredHeight() + ", "
                        + listItem.getBackground().getIntrinsicHeight());
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            // listView.getDividerHeight()获取子项间分隔符占用的高度
            // params.height最后得到整个ListView完整显示需要的高度
            listView.setLayoutParams(params);

            EvtLog.d("debug", "setListViewHeightMatchContent, h: " + totalHeight);
        } catch (Exception e) {
            EvtLog.w(TAG, e);
        }
    }

    /**
     * 设置view的高度
     *
     * @param view   指定的view
     * @param height 指定的高度，以像素为单位
     */
    public static void setViewHeight(View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }

    /**
     * 设置view的宽度
     *
     * @param view   指定的view
     * @param width 指定的宽度，以像素为单位
     */
    public static void setViewWidth(View view, int width) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        view.setLayoutParams(params);
    }

    /**
     * 设置不同颜色的文字
     *
     * @param startPos 需要文字颜色不同的开始位置
     * @param endPos   需要文字颜色不同的结束位置
     * @param text     文字内容
     * @param color    需要转化成的颜色
     * @param tv       需要操作的textview
     */
    public static void setColorfulText(int startPos, int endPos, String text, int color, TextView tv) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        builder.setSpan(new ForegroundColorSpan(color), startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(builder);
    }

    /**
     * 设置删除线
     *
     * @param startPos 需要删除线的开始位置
     * @param endPos   需要删除线的结束位置
     * @param text     文字内容
     * @param tv       需要操作的textview
     */
    public static void setDeleteLineText(int startPos, int endPos, String text, TextView tv) {
        SpannableStringBuilder style = new SpannableStringBuilder(text);
        style.setSpan(new StrikethroughSpan(), startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(style);
    }

    /**
     * @param tv 需要操作的textview
     * @Description 设置下划线
     */
    public static void setUnderLine(TextView tv) {
        tv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
        tv.getPaint().setAntiAlias(true);
    }

    /**
     * dip转换为px
     *
     * @param context  上下文对象
     * @param dipValue dip值
     * @return px值
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px转换为dip
     *
     * @param context 上下文对象
     * @param pxValue px值
     * @return dip值
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 限制执行频率的方法。如按钮需要在指定的3000ms时间后才能再次执行，使用方式如：<br>
     *
     * @param id             方法的标识，可以使用按钮控件的id或者其他唯一标识方法的字符串
     * @param actionListener 方法的回调函数
     */
    public static void limitReClick(final String id, ActionListener actionListener) {
        if (StringUtil.isNullOrEmpty(id) || actionListener == null) {
            throw new NullPointerException();
        }

        synchronized (mSync) {
            if (ACTION_LIST.contains(id)) {
                return;
            } else {
                ACTION_LIST.add(id);

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        removeAction(id);
                    }
                }, DEFAULT_COOLING_TIME);
            }
        }
        actionListener.doAction();
    }

    /**
     * 限制执行频率的方法。如按钮需要在指定的时间后才能再次执行，使用方式如：<br>
     *
     * @param id             方法的标识，可以使用按钮控件的id或者其他唯一标识方法的字符串
     * @param delay          延迟时间，以毫秒为单位
     * @param actionListener 方法的回调函数
     */
    public static void limitReClick(final String id, int delay, ActionListener actionListener) {
        synchronized (mSync) {
            if (ACTION_LIST.contains(id)) {
                return;
            } else {
                ACTION_LIST.add(id);

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        removeAction(id);
                    }
                }, delay);
            }
        }
        actionListener.doAction();
    }

    public static ImageView getImageViewFromBitmap(Context ctx, Bitmap bitmap) {
        ImageView imageView = new ImageView(ctx);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(bitmap);
        return imageView;
    }

    /**
     * 把View绘制到Bitmap上
     *
     * @param view   需要绘制的View
     * @param width  该View的宽度
     * @param height 该View的高度
     * @return 返回Bitmap对象
     */
    public static Bitmap getBitmapFromView(View view, int width, int height, Config bitmapConfig) {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);
        view.layout(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        Canvas canvas = new Canvas(bitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        view.draw(canvas);
        return bitmap;
    }

    public static void removeAction(String id) {
        synchronized (mSync) {
            ACTION_LIST.remove(id);
        }
    }

    /**
     * 移动光标到最后
     *
     * @param editText 输入框
     */
    public static void moveCursolToEnd(EditText editText) {
        if (editText == null) {
            return;
        }
        Editable text = editText.getText();
        if (text != null) {
            Selection.setSelection(text, text.length());
        }
    }

    /**
     * 移动光标到指定位置
     *
     * @param editText 输入框
     * @param index    位置
     */
    public static void moveCursolToIndex(EditText editText, int index) {
        if (editText == null) {
            return;
        }
        Editable text = editText.getText();
        if (text != null) {
            Selection.setSelection(text, text.length());
        }
    }

    /**
     * 设置View显示,判断了是否已显示
     */
    public static void setViewVisible(View view) {
        if (view != null && view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置ViewGone,判断了是否已显示
     */
    public static void setViewGone(View view) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * 设置粗体
     *
     * @param textView
     */
    public static void setBoldText(TextView textView) {
        if (textView == null) {
            return;
        }
        textView.getPaint().setFakeBoldText(true);
    }

    public static int getScreenWidth(Activity activity) {
        if (activity != null) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            return metrics.widthPixels;
        }

        return 480;
    }

    public static int getScreenHeight(Activity activity) {
        if (activity != null) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            return metrics.heightPixels;
        }
        return 800;
    }

    /**
     * @author zou.sq
     */
    public interface ActionListener {
        /**
         * 限制点击冻结接触方法
         */
        void doAction();
    }
}
