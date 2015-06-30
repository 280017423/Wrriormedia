package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 广告数据的model
 *
 * @author zou.sq
 */
public class MediaModel extends BaseModel {

    private int index;
    private int aid;
    private int type;
    private MediaVideoModel video;
    private MediaImageModel image;

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public MediaImageModel getImage() {
        return image;
    }

    public void setImage(MediaImageModel image) {
        this.image = image;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public MediaVideoModel getVideo() {
        return video;
    }

    public void setVideo(MediaVideoModel video) {
        this.video = video;
    }
}
