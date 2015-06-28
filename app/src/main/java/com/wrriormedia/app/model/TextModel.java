package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

import java.util.List;

/**
 * 广告数据的model
 *
 * @author zou.sq
 */
public class TextModel extends BaseModel {

    private List<UpTextModel> up;
    private List<DownTextModel> down;

    public List<DownTextModel> getDown() {
        return down;
    }

    public void setDown(List<DownTextModel> down) {
        this.down = down;
    }

    public List<UpTextModel> getUp() {
        return up;
    }

    public void setUp(List<UpTextModel> up) {
        this.up = up;
    }
}
