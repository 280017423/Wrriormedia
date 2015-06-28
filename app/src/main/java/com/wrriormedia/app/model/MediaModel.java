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
}
