package com.warriormedia.app.model;

import com.warriormedia.library.orm.BaseModel;

/**
 * 广告数据的model
 *
 * @author zou.sq
 */
public class TextModel extends BaseModel {

    private String msg;
    private int pos; // 10，屏幕顶部轮播; 11，屏幕底部轮播

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "TextModel{" +
                "msg='" + msg + '\'' +
                ", pos=" + pos +
                '}';
    }
}
