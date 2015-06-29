package com.wrriormedia.app.model;

import com.wrriormedia.library.orm.BaseModel;

import java.util.List;

/**
 * 广告数据详细的model
 *
 * @author zou.sq
 */
public class AdContentModel extends BaseModel {

    private List<MediaModel> media;
    private TextModel text;

    public List<MediaModel> getMedia() {
        return media;
    }

    public void setMedia(List<MediaModel> media) {
        this.media = media;
    }

    public TextModel getText() {
        return text;
    }

    public void setText(TextModel text) {
        this.text = text;
    }
}
