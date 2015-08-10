package com.warriormedia.app.model;

import com.warriormedia.library.orm.BaseModel;

/**
 * 软件版本model
 */
public class PushVersionModel extends BaseModel {
    private String version; // 0，新的版本号
    private String url; // 新版本下载地址

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "PushVersionModel{" +
                "url='" + url + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
