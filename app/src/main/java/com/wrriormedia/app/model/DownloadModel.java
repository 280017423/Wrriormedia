package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 广告数据的model
 *
 * @author zou.sq
 */
public class DownloadModel extends BaseModel {

    private int aid;
    private DownLoadVideoModel video;
    private DownLoadImageModel image;

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public DownLoadImageModel getImage() {
        return image;
    }

    public void setImage(DownLoadImageModel image) {
        this.image = image;
    }

    public DownLoadVideoModel getVideo() {
        return video;
    }

    public void setVideo(DownLoadVideoModel video) {
        this.video = video;
    }
}
