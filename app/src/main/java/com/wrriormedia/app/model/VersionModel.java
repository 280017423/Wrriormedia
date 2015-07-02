package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 设备状态model
 */
public class VersionModel extends BaseModel {
    private String num; // 0，新的版本号
    private String url; // 新版本下载地址

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
