package com.wrriormedia.library.imageloader.cache.disc;

import java.io.File;

/**
 * Interface for disc cache
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public interface DiscCacheAware {

    /**
     * This method must not to save file on file system in fact. It is called
     * after image was cached in cache directory and it was decoded to bitmap in
     * memory. Such order is required to prevent possible deletion of file after
     * it was cached on disc and before it was tried to decode to bitmap.
     *
     * @param key  主键
     * @param file 文件
     */
    void put(String key, File file);

    /**
     * Returns {@linkplain File file object} appropriate incoming key.<br />
     * <b>NOTE:</b> Must <b>not to return</b> a null. Method must return
     * specific {@linkplain File file object} for incoming key whether file
     * exists or not.
     *
     * @param key 主键
     * @return File 文件
     */
    File get(String key);

    /**
     * Clears cache directory
     */
    void clear();
}
