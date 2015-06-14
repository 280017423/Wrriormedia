package com.wrriormedia.library.widget.wheelview;

/**
 * Wheel scrolled listener interface.
 */
public interface OnWheelScrollListener {
    /**
     * Callback method to be invoked when scrolling started.
     *
     * @param wheel the wheel view whose state has changed.
     */
    void onScrollingStarted(com.wrriormedia.library.widget.wheelview.WheelView wheel);

    /**
     * Callback method to be invoked when scrolling ended.
     *
     * @param wheel the wheel view whose state has changed.
     */
    void onScrollingFinished(com.wrriormedia.library.widget.wheelview.WheelView wheel);
}
