package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 软件版本model
 */
public class SysStatusModel extends BaseModel {
    private int sys_status;

    public int getSys_status() {
        return sys_status;
    }

    public void setSys_status(int sys_status) {
        this.sys_status = sys_status;
    }
}
