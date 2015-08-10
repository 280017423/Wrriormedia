package com.warriormedia.app.model;

import com.warriormedia.library.orm.BaseModel;

/**
 * 设备状态model
 */
public class PushBrightnessModel extends BaseModel {
    private int brightness;

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }
}
