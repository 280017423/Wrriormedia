package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 指令model
 */
public class CmdModel extends BaseModel {
    private int update; // 0，无更新；1，有更新
    private int sys_time; // 服务端系统时间，客户端需要对比本地时间，相差超过10秒，需要校准本地时间
    private int sys_status;//设备状态控制信号（0：正常播放广告；1：暂停播放，展示默认图；2：系统关闭屏幕，停止播放），当这个信号为0时才继续判断下面的字段，如果不为0，则立即进入指定的状态，停止播放广告
    private int next_time;
    private WifiModel wifi;
    private VersionModel version;
    private int ad;
    private int download;
    private int start_time; //开始播放的时间
    private int end_time; //结束播放时间，在这两个时间段内正常播放广告，时间外关闭屏幕；
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

    public int getEnd_time() {
        return end_time;
    }

    public void setEnd_time(int end_time) {
        this.end_time = end_time;
    }

    public int getLog_time() {
        return log_time;
    }

    public void setLog_time(int log_time) {
        this.log_time = log_time;
    }

    public int getNext_time() {
        return next_time;
    }

    public void setNext_time(int next_time) {
        this.next_time = next_time;
    }

    public int getStart_time() {
        return start_time;
    }

    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }

    public int getSys_status() {
        return sys_status;
    }

    public void setSys_status(int sys_status) {
        this.sys_status = sys_status;
    }

    public int getSys_time() {
        return sys_time;
    }

    public void setSys_time(int sys_time) {
        this.sys_time = sys_time;
    }

    public int getUpdate() {
        return update;
    }

    public void setUpdate(int update) {
        this.update = update;
    }

    public VersionModel getVersion() {
        return version;
    }

    public void setVersion(VersionModel version) {
        this.version = version;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public WifiModel getWifi() {
        return wifi;
    }

    public void setWifi(WifiModel wifi) {
        this.wifi = wifi;
    }

    public int getAd() {
        return ad;
    }

    public void setAd(int ad) {
        this.ad = ad;
    }

    public int getDownload() {
        return download;
    }

    public void setDownload(int download) {
        this.download = download;
    }
}
