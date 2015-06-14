package com.wrriormedia.library.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.wrriormedia.library.R;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.ImageUtil;

/**
 * 类似于iPhone的开关控件
 *
 * @author wang.xy
 * @version 2012-8-14，王先佑，重构文本、注释等
 * @since 2012-8-14
 */
public class SlipButton extends View implements OnTouchListener {
    private static final String TAG = "SlipButton";

    // 记录当前按钮是否打开,true为打开,false为关闭
    private boolean mNowChoose;
    private boolean mIsChecked;
    // 记录用户是否在滑动的变量
    private boolean mOnSlip;
    // 按下时的x,当前的x
    private float mDownX;
    private float mNowX;
    private Rect mBtnOn;
    // 打开和关闭状态下,游标的Rect .
    private Rect mBtnOff;
    private boolean mIsChgLsnOn;
    private OnChangedListener mChgLsn;
    private Bitmap mBitmapOn;
    private Bitmap mBitmapOff;
    private Bitmap mSlipButtonThumb;

    private int mBitmapOnTop;
    private int mBitmapOffTop;
    private int mSlipButtonTop;
    private Paint mPaint;

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     */
    public SlipButton(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * 构造函数
     *
     * @param context 当前布局文件的上下文
     * @param attrs   属性数组
     */
    public SlipButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * 构造函数
     *
     * @param context  当前布局文件的上下文
     * @param attrs    属性数组
     * @param defStyle The default style to apply to this view. If 0, no style will
     *                 be applied (beyond what is included in the theme). This may
     *                 either be an attribute resource, whose value will be retrieved
     *                 from the current theme, or an explicit style resource.
     */
    public SlipButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    // 初始化
    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint();

        if (null != attrs) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlipButton);
            Drawable slipButtonOn = typedArray.getDrawable(R.styleable.SlipButton_slip_button_on_bg);
            Drawable slipButtonOff = typedArray.getDrawable(R.styleable.SlipButton_slip_button_off_bg);
            Drawable slipButtonThumb = typedArray.getDrawable(R.styleable.SlipButton_slip_button_thumb);
            if (null != slipButtonOn) {
                mBitmapOn = ImageUtil.drawable2Bitmap(slipButtonOn);
            }
            if (null != slipButtonOff) {
                mBitmapOff = ImageUtil.drawable2Bitmap(slipButtonOff);
            }
            if (null != slipButtonThumb) {
                mSlipButtonThumb = ImageUtil.drawable2Bitmap(slipButtonThumb);
            }
            typedArray.recycle();
        }

        mBtnOn = new Rect(0, 0, mSlipButtonThumb.getWidth(), mSlipButtonThumb.getHeight());
        mBtnOff = new Rect(
                mBitmapOff.getWidth() - mSlipButtonThumb.getWidth(), 0, mBitmapOff.getWidth(),
                mSlipButtonThumb.getHeight());
        // 设置监听器,也可以直接复写OnTouchEvent
        setOnTouchListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = mBitmapOff.getWidth();
        int height = mBitmapOff.getHeight();
        if (MeasureSpec.EXACTLY == widthMode) {
            width = widthSize;
        }
        if (MeasureSpec.EXACTLY == heightMode) {
            height = heightSize;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initBitmapTop();
    }

    /*
     * 绘图函数(non-Javadoc)
     *
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Matrix matrix = new Matrix();

        float x;
        // 滑动到前半段与后半段的背景不同,在此做判断
        if (mNowX < (mBitmapOn.getWidth() / 2)) {
            x = mNowX - mSlipButtonThumb.getWidth() / 2;
            // 画出关闭时的背景
            canvas.drawBitmap(mBitmapOff, 0, mBitmapOffTop, mPaint);
        } else {
            x = mBitmapOn.getWidth() - mSlipButtonThumb.getWidth() / 2;
            // 画出打开时的背景
            canvas.drawBitmap(mBitmapOn, 0, mBitmapOnTop, mPaint);
        }
        // 是否是在滑动状态,
        if (mOnSlip) {
            // 是否划出指定范围,不能让游标跑到外头,必须做这个判断
            if (mNowX >= mBitmapOn.getWidth()) {
                // 减去游标1/2的长度...
                x = mBitmapOn.getWidth() - mSlipButtonThumb.getWidth() / 2;
            } else if (mNowX < 0) {
                x = 0;
            } else {
                x = mNowX - mSlipButtonThumb.getWidth() / 2;
            }
            // 非滑动状态
        } else {
            // 根据现在的开关状态设置画游标的位置
            if (mNowChoose) {
                x = mBtnOff.left;
                // 初始状态为true时应该画出打开状态图片
                canvas.drawBitmap(mBitmapOn, 0, mBitmapOnTop, mPaint);
            } else {
                x = mBtnOn.left;
            }
        }

        if (mIsChecked) {
            canvas.drawBitmap(mBitmapOn, 0, mBitmapOnTop, mPaint);
            x = mBtnOff.left;
            mIsChecked = !mIsChecked;
        }

        // 对游标位置进行异常判断...
        if (x < 0) {
            x = 0;
        } else if (x > mBitmapOn.getWidth() - mSlipButtonThumb.getWidth()) {
            x = mBitmapOn.getWidth() - mSlipButtonThumb.getWidth();
        }
        // 画出游标.
        canvas.drawBitmap(mSlipButtonThumb, x, mSlipButtonTop, mPaint);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 根据动作来执行代码
        switch (event.getAction()) {
            // 滑动
            case MotionEvent.ACTION_MOVE:
                mNowX = event.getX();
                EvtLog.d(TAG, "ACTION_MOVE mNowX:" + mNowX);
                break;
            // 按下
            case MotionEvent.ACTION_DOWN:
                mOnSlip = true;
                mDownX = event.getX();
                mNowX = mDownX;
                break;
            // 移到控件外部
            case MotionEvent.ACTION_CANCEL:
                mOnSlip = false;
                boolean choose = mNowChoose;
                if (mNowX >= (mBitmapOn.getWidth() / 2)) {
                    mNowX = mBitmapOn.getWidth() - mSlipButtonThumb.getWidth() / 2;
                    mNowChoose = true;
                } else {
                    mNowX = mNowX - mSlipButtonThumb.getWidth() / 2;
                    mNowChoose = false;
                }
                // 如果设置了监听器,就调用其方法..
                if (mIsChgLsnOn && (choose != mNowChoose)) {
                    mChgLsn.onChanged(this, mNowChoose);
                }
                break;
            // 松开
            case MotionEvent.ACTION_UP:
                mOnSlip = false;
                boolean lastChoose = mNowChoose;
                if (event.getX() >= (mBitmapOn.getWidth() / 2)) {
                    mNowX = mBitmapOn.getWidth() - mSlipButtonThumb.getWidth() / 2;
                    mNowChoose = true;
                } else {
                    mNowX = mNowX - mSlipButtonThumb.getWidth() / 2;
                    mNowChoose = false;
                }
                // 如果设置了监听器,就调用其方法..
                if (mIsChgLsnOn && (lastChoose != mNowChoose)) {
                    mChgLsn.onChanged(this, mNowChoose);
                }
                break;
            default:

        }
        // 重画控件
        invalidate();

        return true;
    }

    private void initBitmapTop() {
        int height = this.getHeight();
        mBitmapOnTop = height / 2 - mBitmapOn.getHeight() / 2;
        mBitmapOffTop = height / 2 - mBitmapOff.getHeight() / 2;
        mSlipButtonTop = height / 2 - mSlipButtonThumb.getHeight() / 2;
    }

    /**
     * 设置监听器,当状态修改的时候
     *
     * @param l 监听器
     */
    public void setOnChangedListener(OnChangedListener l) {
        mIsChgLsnOn = true;
        mChgLsn = l;
    }

    /**
     * @return 当前的状态
     */
    public boolean getCheck() {

        return mNowChoose;
    }

    /**
     * 设置按钮选择的状态值
     *
     * @param isChecked 按钮选择值
     */
    public void setCheck(boolean isChecked) {
        this.mIsChecked = isChecked;
        mNowChoose = isChecked;
        if (isChecked) {
            mNowX = mBitmapOn.getWidth() / 2;
        } else {
            mNowX = 0;
        }
        // 重画控件
        invalidate();

    }

    /**
     * 监听器接口，用户状态变化的时候
     *
     * @author wang.xy
     */
    public interface OnChangedListener {
        /**
         * 状态变化时的调用方法
         *
         * @param checkState 状态的变化值
         */
        void onChanged(View view, boolean checkState);
    }

}