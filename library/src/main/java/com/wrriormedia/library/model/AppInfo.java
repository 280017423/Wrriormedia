package com.wrriormedia.library.model;

import android.graphics.Bitmap;

import com.wrriormedia.library.orm.BaseModel;
import com.wrriormedia.library.orm.annotation.Transient;

/**
 * 获取更多应用时对应的每一个应用程序的基本信息
 * 
 * @author cui.yp
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AppInfo extends BaseModel {

	/**
	 * 应用名称
	 */
	public String AppName;

	/**
	 * 应用logo地址
	 */
	public String AppLogoPath;

	/**
	 * 应用标识
	 */
	public String AppSign;

	/**
	 * 应用下载地址
	 */
	public String AppUrl;

	/**
	 * 应用程序大小，以M为单位
	 */
	public String AppSize;

	/**
	 * 应用描述
	 */
	public String AppDetails;

	/**
	 * 对外版本号
	 */
	public String VersionNo;
	/**
	 * 下载之后的App图片
	 */
	@Transient
	public Bitmap Bitmap;

}
