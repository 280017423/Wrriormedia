package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 广告数据的model
 *
 * @author zou.sq
 */
public class MediaImageModel extends BaseModel {

    private String first;
    private String second;
    private int pos;
    private int time;
    private String md5;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }
}
