package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

/**
 * 广告数据的model
 *
 * @author zou.sq
 */
public class DownloadModel extends BaseModel {

    public static final String WHERE_CASE = "AID = ?";
    public static final String WHERE_CASE_SUB = "AID";
    public static final String START = "START";
    public static final String END = "END";
    public static final String IS_DOWNLOAD_FINISH = "IS_DOWNLOAD_FINISH";

    private int aid;
    private int type;
    private int start;
    private int end;
    private TextModel text;
    private MediaVideoModel video;
    private MediaImageModel image;
    private int isDownloadFinish;

    public int getIsDownloadFinish() {
        return isDownloadFinish;
    }

    public void setIsDownloadFinish(int isDownloadFinish) {
        this.isDownloadFinish = isDownloadFinish;
    }

    public MediaVideoModel getVideo() {
        return video;
    }

    public void setVideo(MediaVideoModel video) {
        this.video = video;
    }

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public MediaImageModel getImage() {
        return image;
    }

    public void setImage(MediaImageModel image) {
        this.image = image;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public TextModel getText() {
        return text;
    }

    public void setText(TextModel text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
