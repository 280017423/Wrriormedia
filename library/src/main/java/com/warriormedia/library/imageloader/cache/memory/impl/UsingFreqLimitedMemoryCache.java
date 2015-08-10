package com.warriormedia.library.imageloader.cache.memory.impl;

import android.graphics.Bitmap;

import com.warriormedia.library.imageloader.cache.memory.LimitedMemoryCache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Limited {@link Bitmap bitmap} cache. Provides {@link Bitmap bitmaps} storing.
 * Size of all stored bitmaps will not to exceed size limit. When cache reaches
 * limit size then the bitmap which used the least frequently is deleted from
 * cache.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class UsingFreqLimitedMemoryCache extends LimitedMemoryCache<String, Bitmap> {

    /**
     * Contains strong references to stored objects (keys) and last object usage
     * date (in milliseconds). If hard cache size will exceed limit then object
     * with the least frequently usage is deleted (but it continue exist at
     * {@link #softMap} and can be collected by GC at any time)
     */
    private final Map<Bitmap, Integer> mUsingCounts = Collections.synchronizedMap(new HashMap<Bitmap, Integer>());

    /**
     * 构造函数
     *
     * @param sizeLimit 内存大小
     */
    public UsingFreqLimitedMemoryCache(int sizeLimit) {
        super(sizeLimit);
    }

    @Override
    public boolean put(String key, Bitmap value) {
        if (super.put(key, value)) {
            mUsingCounts.put(value, 0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Bitmap get(String key) {
        Bitmap value = super.get(key);
        if (value != null) {
            Integer usageCount = mUsingCounts.get(value);
            if (usageCount != null) {
                mUsingCounts.put(value, usageCount + 1);
            }
        }
        return value;
    }

    @Override
    public void remove(String key) {
        Bitmap value = super.get(key);
        if (value != null) {
            mUsingCounts.remove(value);
        }
        super.remove(key);
    }

    @Override
    public void clear() {
        mUsingCounts.clear();
        super.clear();
    }

    @Override
    protected int getSize(Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    @Override
    protected Bitmap removeNext() {
        Integer minUsageCount = null;
        Bitmap leastUsedValue = null;
        Set<Entry<Bitmap, Integer>> entries = mUsingCounts.entrySet();
        synchronized (mUsingCounts) {
            for (Entry<Bitmap, Integer> entry : entries) {
                if (leastUsedValue == null) {
                    leastUsedValue = entry.getKey();
                    minUsageCount = entry.getValue();
                } else {
                    Integer lastValueUsage = entry.getValue();
                    if (lastValueUsage < minUsageCount) {
                        minUsageCount = lastValueUsage;
                        leastUsedValue = entry.getKey();
                    }
                }
            }
        }
        mUsingCounts.remove(leastUsedValue);
        return leastUsedValue;
    }

    @Override
    protected Reference<Bitmap> createReference(Bitmap value) {
        return new WeakReference<>(value);
    }
}
