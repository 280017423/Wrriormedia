package com.wrriormedia.library.imageloader.cache.memory.impl;

import android.graphics.Bitmap;

import com.wrriormedia.library.imageloader.cache.memory.LimitedMemoryCache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Limited {@link Bitmap bitmap} cache. Provides {@link Bitmap bitmaps} storing.
 * Size of all stored bitmaps will not to exceed size limit. When cache reaches
 * limit size then the least recently used bitmap is deleted from cache.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class LRULimitedMemoryCache extends LimitedMemoryCache<String, Bitmap> {

    private static final int INITIAL_CAPACITY = 10;
    private static final float LOAD_FACTOR = 1.1f;

    /**
     * Cache providing Least-Recently-Used logic
     */
    private final Map<String, Bitmap> mLruCache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(
            INITIAL_CAPACITY, LOAD_FACTOR, true));

    /**
     * 构造函数
     *
     * @param sizeLimit 内存大小
     */
    public LRULimitedMemoryCache(int sizeLimit) {
        super(sizeLimit);
    }

    @Override
    public boolean put(String key, Bitmap value) {
        if (super.put(key, value)) {
            mLruCache.put(key, value);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Bitmap get(String key) {
        mLruCache.get(key); // call "get" for LRU logic
        return super.get(key);
    }

    @Override
    public void remove(String key) {
        mLruCache.remove(key);
        super.remove(key);
    }

    @Override
    public void clear() {
        mLruCache.clear();
        super.clear();
    }

    @Override
    protected int getSize(Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    @Override
    protected Bitmap removeNext() {
        Bitmap mostLongUsedValue = null;
        synchronized (mLruCache) {
            Iterator<Entry<String, Bitmap>> it = mLruCache.entrySet().iterator();
            if (it.hasNext()) {
                Entry<String, Bitmap> entry = it.next();
                mostLongUsedValue = entry.getValue();
                it.remove();
            }
        }
        return mostLongUsedValue;
    }

    @Override
    protected Reference<Bitmap> createReference(Bitmap value) {
        return new WeakReference<>(value);
    }
}
