package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 软件版本model
 */
public class PushAdModel extends BaseModel {
    private String aid; // 广告id
    private String url; // 备用请求地址

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
