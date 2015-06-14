package com.wrriormedia.library.widget.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wrriormedia.library.R;

public class XFooterView extends LinearLayout {
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_LOADING = 2;

    private final int ROTATE_ANIM_DURATION = 180;

    private View mLayout;

    private View mProgressBar;

    private TextView mHintView;

    // private ImageView mHintImage;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;

    private int mState = STATE_NORMAL;
    private boolean mHasMoreData = true;

    public XFooterView(Context context) {
        super(context);
        initView(context);
    }

    public XFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mLayout = View.inflate(context, R.layout.vw_footer, null);
        mLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(mLayout);

        mProgressBar = mLayout.findViewById(R.id.footer_progressbar);
        mHintView = (TextView) mLayout.findViewById(R.id.footer_hint_text);
        // mHintImage = (ImageView) mLayout.findViewById(R.id.footer_arrow);

        mRotateUpAnim = new RotateAnimation(
                0.0f, 180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);

        mRotateDownAnim = new RotateAnimation(
                180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

    /**
     * Set footer view state
     *
     * @param state
     * @see #STATE_LOADING
     * @see #STATE_NORMAL
     * @see #STATE_READY
     */
    public void setState(int state) {
        if (state == mState)
            return;

        switch (state) {
            case STATE_NORMAL:
                setVisibility(View.GONE);
                mHintView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                mHintView.setText(R.string.footer_hint_load_normal);
                break;
            case STATE_READY:
                setVisibility(View.VISIBLE);
                mHintView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                if (mHasMoreData) {
                    mHintView.setText(R.string.pull_to_refresh_release_label);
                } else {
                    mHintView.setText(R.string.no_more_data);
                }
                break;

            case STATE_LOADING:
                mHintView.setVisibility(View.VISIBLE);
                if (mHasMoreData) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mHintView.setText(R.string.pull_to_refresh_release_label);
                } else {
                    mHintView.setText(R.string.no_more_data);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                break;
        }

        mState = state;
    }

    /**
     * Get footer view bottom margin.
     *
     * @return
     */
    public int getBottomMargin() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        return lp.bottomMargin;
    }

    /**
     * Set footer view bottom margin.
     *
     * @param margin
     */
    public void setBottomMargin(int margin) {
        if (margin < 0)
            return;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        lp.bottomMargin = margin;
        mLayout.setLayoutParams(lp);
    }

    /**
     * normal status
     */
    public void normal() {
        mHintView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * loading status
     */
    public void loading() {
        mHintView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * hide footer when disable pull load more
     */
    public void hide() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        lp.height = 0;
        mLayout.setLayoutParams(lp);
    }

    /**
     * show footer
     */
    public void show() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        lp.height = LayoutParams.WRAP_CONTENT;
        mLayout.setLayoutParams(lp);
    }

    public void setNoMoreData() {
        setNoMoreData(true);
    }

    public void setNoMoreData(boolean hasMoreData) {
        mHasMoreData = hasMoreData;
    }

}
