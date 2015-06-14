package com.wrriormedia.library.imageloader.cache.memory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Limited cache. Provides object storing. Size of all stored bitmaps will not
 * to exceed size limit ( {@link #getSizeLimit()}).
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see BaseMemoryCache
 */
public abstract class LimitedMemoryCache<K, V> extends BaseMemoryCache<K, V> {

	private final int mSizeLimit;

	private int mCacheSize;

	/**
	 * Contains strong references to stored objects. Each next object is added
	 * last. If hard cache size will exceed limit then first object is deleted
	 * (but it continue exist at {@link #softMap} and can be collected by GC at
	 * any time)
	 */
	private final List<V> mHardCache = Collections.synchronizedList(new LinkedList<V>());

	/**
	 * @param sizeLimit
	 *            Maximum size for cache (in bytes)
	 */
	public LimitedMemoryCache(int sizeLimit) {
		this.mSizeLimit = sizeLimit;
	}

	@Override
	public boolean put(K key, V value) {
		boolean putSuccessfully = false;
		// Try to add value to hard cache
		int valueSize = getSize(value);
		int sizeLimit = getSizeLimit();
		if (valueSize < sizeLimit) {
			while (mCacheSize + valueSize > sizeLimit) {
				V removedValue = removeNext();
				if (mHardCache.remove(removedValue)) {
					mCacheSize -= getSize(removedValue);
				}
			}
			mHardCache.add(value);
			mCacheSize += valueSize;

			putSuccessfully = true;
		}
		// Add value to soft cache
		super.put(key, value);
		return putSuccessfully;
	}

	@Override
	public void remove(K key) {
		V value = super.get(key);
		if (value != null) {
			if (mHardCache.remove(value)) {
				mCacheSize -= getSize(value);
			}
		}
		super.remove(key);
	}

	@Override
	public void clear() {
		mHardCache.clear();
		mCacheSize = 0;
		super.clear();
	}

	protected int getSizeLimit() {
		return mSizeLimit;
	}

	protected abstract int getSize(V value);

	protected abstract V removeNext();
}
