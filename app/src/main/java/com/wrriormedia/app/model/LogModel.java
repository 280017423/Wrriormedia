package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 日志model
 */
public class LogModel extends BaseModel {
    private int logTime; // 日志时间
    private String log; // 日志信息
    private int logType; // 日志类型

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public int getLogTime() {
        return logTime;
    }

    public void setLogTime(int logTime) {
        this.logTime = logTime;
    }

    public int getLogType() {
        return logType;
    }

    public void setLogType(int logType) {
        this.logType = logType;
    }
}
