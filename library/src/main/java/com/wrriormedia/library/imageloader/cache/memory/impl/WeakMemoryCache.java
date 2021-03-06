package com.wrriormedia.library.imageloader.cache.memory.impl;

import android.graphics.Bitmap;

import com.wrriormedia.library.imageloader.cache.memory.BaseMemoryCache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Memory cache with {@linkplain WeakReference weak references} to
 * {@linkplain android.graphics.Bitmap bitmaps}
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class WeakMemoryCache extends BaseMemoryCache<String, Bitmap> {
    @Override
    protected Reference<Bitmap> createReference(Bitmap value) {
        return new WeakReference<>(value);
    }
}
