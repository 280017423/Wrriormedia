/**
 * @Project: PMH_Main
 * @Title: RoundCornerImageView.java
 * @Package com.pdw.pmh.widget
 * @author huang.b
 * @date 2013-12-25 上午10:28:39
 * @Copyright: 2013 www.paidui.cn Inc. All rights reserved.
 * @version V1.0
 */
package com.wrriormedia.library.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.wrriormedia.library.R;

/**
 * 添加圆角控件间RoundCornerImageView； roundWidth,roundHeight分别代表圆角的角度
 *
 * @author huang.b
 * @version 2013-12-25 上午10:28:39 PDW huang.b
 */
public class RoundCornerImageView extends ImageView {

    boolean mLeftTopRound;
    boolean mRightTopRound;
    boolean mLeftBottomRound;
    boolean mRightBottomRound;
    private int mRoundWidth = 15;
    private int mRoundHeight = 15;
    private Paint mPaint;
    private Paint mPaint2;

    public RoundCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public RoundCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundCornerImageView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundCornerImageView);
            mRoundWidth = a.getDimensionPixelSize(R.styleable.RoundCornerImageView_roundWidth, mRoundWidth);
            mRoundHeight = a.getDimensionPixelSize(R.styleable.RoundCornerImageView_roundHeight, mRoundHeight);
            mLeftTopRound = a.getBoolean(R.styleable.RoundCornerImageView_roundLeftTop, true);
            mRightTopRound = a.getBoolean(R.styleable.RoundCornerImageView_roundRightTop, true);
            mLeftBottomRound = a.getBoolean(R.styleable.RoundCornerImageView_roundLeftBottom, true);
            mRightBottomRound = a.getBoolean(R.styleable.RoundCornerImageView_roundRightBottom, true);
            a.recycle();
        } else {
            float density = context.getResources().getDisplayMetrics().density;
            mRoundWidth = (int) (mRoundWidth * density);
            mRoundHeight = (int) (mRoundHeight * density);
        }

        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        mPaint2 = new Paint();
        mPaint2.setXfermode(null);
    }

    @Override
    public void draw(Canvas canvas) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        Canvas canvas2 = new Canvas(bitmap);
        super.draw(canvas2);
        if (mLeftTopRound) {
            drawLiftUp(canvas2);
        }
        if (mRightTopRound) {
            drawRightUp(canvas2);
        }
        if (mLeftBottomRound) {
            drawLiftDown(canvas2);
        }
        if (mRightBottomRound) {
            drawRightDown(canvas2);
        }
        canvas.drawBitmap(bitmap, 0, 0, mPaint2);
        // 如果回收在htc one s手机上面，跟ListView使用的时候，会有图片显示不全，而且同一屏下图片重复问题；
        // bitmap.recycle();
    }

    private void drawLiftUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, mRoundHeight);
        path.lineTo(0, 0);
        path.lineTo(mRoundWidth, 0);
        path.arcTo(new RectF(0, 0, mRoundWidth * 2, mRoundHeight * 2), -90, -90);
        path.close();
        canvas.drawPath(path, mPaint);
    }

    private void drawLiftDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, getHeight() - mRoundHeight);
        path.lineTo(0, getHeight());
        path.lineTo(mRoundWidth, getHeight());
        path.arcTo(new RectF(0, getHeight() - mRoundHeight * 2, mRoundWidth * 2, getHeight()), 90, 90);
        path.close();
        canvas.drawPath(path, mPaint);
    }

    private void drawRightDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(getWidth() - mRoundWidth, getHeight());
        path.lineTo(getWidth(), getHeight());
        path.lineTo(getWidth(), getHeight() - mRoundHeight);
        path.arcTo(new RectF(getWidth() - mRoundWidth * 2, getHeight() - mRoundHeight * 2, getWidth(), getHeight()), 0,
                90);
        path.close();
        canvas.drawPath(path, mPaint);
    }

    private void drawRightUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(getWidth(), mRoundHeight);
        path.lineTo(getWidth(), 0);
        path.lineTo(getWidth() - mRoundWidth, 0);
        path.arcTo(new RectF(getWidth() - mRoundWidth * 2, 0, getWidth(), mRoundHeight * 2), -90, 90);
        path.close();
        canvas.drawPath(path, mPaint);
    }

    public void setRoundWidth(int roundWidth) {
        this.mRoundWidth = roundWidth;
    }

    public void setRoundHeight(int roundHeight) {
        this.mRoundHeight = roundHeight;
    }
}
