package com.warriormedia.library.imageloader.cache.memory.impl;

import android.graphics.Bitmap;

import com.warriormedia.library.imageloader.cache.memory.LimitedMemoryCache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Limited {@link Bitmap bitmap} cache. Provides {@link Bitmap bitmaps} storing.
 * Size of all stored bitmaps will not to exceed size limit. When cache reaches
 * limit size then cache clearing is processed by FIFO principle.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class FIFOLimitedMemoryCache extends LimitedMemoryCache<String, Bitmap> {

    private final List<Bitmap> mQueue = Collections.synchronizedList(new LinkedList<Bitmap>());

    /**
     * 构造函数
     *
     * @param sizeLimit 内存大小
     */
    public FIFOLimitedMemoryCache(int sizeLimit) {
        super(sizeLimit);
    }

    @Override
    public boolean put(String key, Bitmap value) {
        if (super.put(key, value)) {
            mQueue.add(value);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void remove(String key) {
        Bitmap value = super.get(key);
        if (value != null) {
            mQueue.remove(value);
        }
        super.remove(key);
    }

    @Override
    public void clear() {
        mQueue.clear();
        super.clear();
    }

    @Override
    protected int getSize(Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    @Override
    protected Bitmap removeNext() {
        return mQueue.remove(0);
    }

    @Override
    protected Reference<Bitmap> createReference(Bitmap value) {
        return new WeakReference<>(value);
    }
}
