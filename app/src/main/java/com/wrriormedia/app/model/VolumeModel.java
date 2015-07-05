package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 设备状态model
 */
public class VolumeModel extends BaseModel {
    private int volume;

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
