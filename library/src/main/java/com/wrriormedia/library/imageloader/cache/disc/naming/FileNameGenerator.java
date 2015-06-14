package com.wrriormedia.library.imageloader.cache.disc.naming;

/**
 * Generates names for files at disc cache
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public interface FileNameGenerator {
	/**
	 * Generates unique file name for image defined by URI
	 * 
	 * @param imageUri
	 *            图片路径
	 * @return String file name
	 * 
	 */
	String generate(String imageUri);
}
