package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 软件版本model
 */
public class DeleteAdModel extends BaseModel {
    private String aid; // 广告id

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }
}
