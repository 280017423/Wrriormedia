package com.warriormedia.library.imageloader.core;

import android.net.Uri;
import android.widget.ImageView;

import com.warriormedia.library.imageloader.core.assist.ImageLoadingListener;
import com.warriormedia.library.imageloader.core.assist.ImageSize;
import com.warriormedia.library.imageloader.core.assist.MemoryCacheKeyUtil;

/**
 * Information for load'n'display image task
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see MemoryCacheKeyUtil
 * @see DisplayImageOptions
 * @see ImageLoadingListener
 */
final class ImageLoadingInfo {

    final String uri;
    final String memoryCacheKey;
    final ImageView imageView;
    final ImageSize targetSize;
    final com.warriormedia.library.imageloader.core.DisplayImageOptions options;
    final ImageLoadingListener listener;

    public ImageLoadingInfo(String uri, ImageView imageView, ImageSize targetSize, com.warriormedia.library.imageloader.core.DisplayImageOptions options,
                            ImageLoadingListener listener) {
        this.uri = Uri.encode(uri, "@#&=*+-_.,:!?()/~'%");
        this.imageView = imageView;
        this.targetSize = targetSize;
        this.options = options;
        this.listener = listener;
        memoryCacheKey = MemoryCacheKeyUtil.generateKey(uri, targetSize);
    }
}
