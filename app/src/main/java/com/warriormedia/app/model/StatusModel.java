package com.warriormedia.app.model;

import com.warriormedia.library.orm.BaseModel;

/**
 * 设备状态model
 */
public class StatusModel extends BaseModel {
    private String ready; // 0，未完成录入；1，已完成录入
    private String serial;
    private String address;

    public String getReady() {
        return ready;
    }

    public void setReady(String ready) {
        this.ready = ready;
    }

    public String getAddress() {
        return null == address ? "" : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSerial() {
        return null == serial ? "" : serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Override
    public String toString() {
        return "StatusModel{" +
                "address='" + address + '\'' +
                ", ready='" + ready + '\'' +
                ", serial='" + serial + '\'' +
                '}';
    }
}
