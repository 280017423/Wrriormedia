package com.warriormedia.app.model;

import com.warriormedia.library.orm.BaseModel;

/**
 * 指令model
 */
public class CmdModel extends BaseModel {
    private int sys_status;//设备状态控制信号（0：正常播放广告；1：暂停播放，展示默认图；2：系统关闭屏幕，停止播放），当这个信号为0时才继续判断下面的字段，如果不为0，则立即进入指定的状态，停止播放广告
    private String start_time; //开始播放的时间
    private String end_time; //结束播放时间，在这两个时间段内正常播放广告，时间外关闭屏幕；
    private int log_time; //向服务端发日志的时间
    private int volume; // 声音大小 值为0-10（0：静音，1：最小值，10：最大值）
    private int brightness; // 屏幕亮度 值为0-10（0：屏幕关闭，1：最小值；10：最大值）；

    public int getBrightness() {
        if (brightness < 3) {
            brightness = 3;
        } else if (brightness > 10) {
            brightness = 10;
        }
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public int getLog_time() {
        return log_time;
    }

    public void setLog_time(int log_time) {
        this.log_time = log_time;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public int getSys_status() {
        return sys_status;
    }

    public void setSys_status(int sys_status) {
        this.sys_status = sys_status;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "CmdModel{" +
                "brightness=" + brightness +
                ", sys_status=" + sys_status +
                ", start_time='" + start_time + '\'' +
                ", end_time='" + end_time + '\'' +
                ", log_time=" + log_time +
                ", volume=" + volume +
                '}';
    }
}
