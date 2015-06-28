package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 广告数据的model
 *
 * @author zou.sq
 */
public class AdModel extends BaseModel {

    private int update;
    private int next_time;
    private AdContentModel list;

    public AdContentModel getList() {
        return list;
    }

    public void setList(AdContentModel list) {
        this.list = list;
    }

    public int getNext_time() {
        return next_time;
    }

    public void setNext_time(int next_time) {
        this.next_time = next_time;
    }

    public int getUpdate() {
        return update;
    }

    public void setUpdate(int update) {
        this.update = update;
    }
}
