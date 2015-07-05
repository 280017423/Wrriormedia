package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 设备状态model
 */
public class PushLogModel extends BaseModel {
    private int log_time;

    public int getLog_time() {
        return log_time;
    }

    public void setLog_time(int log_time) {
        this.log_time = log_time;
    }
}
