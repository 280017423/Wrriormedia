package com.warriormedia.app.model;

import com.warriormedia.library.orm.BaseModel;

/**
 * wifi数据的model
 *
 * @author zou.sq
 */
public class WifiModel extends BaseModel {

    private String ssid;
    private String password;
    private String type;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
