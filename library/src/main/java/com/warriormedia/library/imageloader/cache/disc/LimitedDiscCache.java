package com.warriormedia.library.imageloader.cache.disc;

import com.warriormedia.library.imageloader.cache.disc.naming.FileNameGenerator;
import com.warriormedia.library.imageloader.core.DefaultConfigurationFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Abstract disc cache limited by some parameter. If cache exceeds specified
 * limit then file with the most oldest last usage date will be deleted.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see BaseDiscCache
 * @see FileNameGenerator
 */
public abstract class LimitedDiscCache extends BaseDiscCache {

    private final Map<File, Long> mLastUsageDates = Collections.synchronizedMap(new HashMap<File, Long>());
    private int mCacheSize;
    private int mSizeLimit;

    /**
     * @param cacheDir  Directory for file caching. <b>Important:</b> Specify separate
     *                  folder for cached files. It's needed for right cache limit
     *                  work.
     * @param sizeLimit Cache limit value. If cache exceeds this limit then file with
     *                  the most oldest last usage date will be deleted.
     */
    public LimitedDiscCache(File cacheDir, int sizeLimit) {
        this(cacheDir, DefaultConfigurationFactory.createFileNameGenerator(), sizeLimit);
    }

    /**
     * @param cacheDir          Directory for file caching. <b>Important:</b> Specify separate
     *                          folder for cached files. It's needed for right cache limit
     *                          work.
     * @param fileNameGenerator Name generator for cached files
     * @param sizeLimit         Cache limit value. If cache exceeds this limit then file with
     *                          the most oldest last usage date will be deleted.
     */
    public LimitedDiscCache(File cacheDir, FileNameGenerator fileNameGenerator, int sizeLimit) {
        super(cacheDir, fileNameGenerator);
        this.mSizeLimit = sizeLimit;
        calculateCacheSizeAndFillUsageMap();
    }

    private void calculateCacheSizeAndFillUsageMap() {
        int size = 0;
        File[] cachedFiles = getCacheDir().listFiles();
        for (File cachedFile : cachedFiles) {
            size += getSize(cachedFile);
            mLastUsageDates.put(cachedFile, cachedFile.lastModified());
        }
        mCacheSize = size;
    }

    @Override
    public void put(String key, File file) {
        int valueSize = getSize(file);
        while (mCacheSize + valueSize > mSizeLimit) {
            int freedSize = removeNext();
            if (freedSize == 0) {
                break; // cache is empty (have nothing to delete)
            }
            mCacheSize -= freedSize;
        }
        mCacheSize += valueSize;

        Long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        mLastUsageDates.put(file, currentTime);
    }

    @Override
    public File get(String key) {
        File file = super.get(key);

        Long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        mLastUsageDates.put(file, currentTime);

        return file;
    }

    @Override
    public void clear() {
        mLastUsageDates.clear();
        mCacheSize = 0;
        super.clear();
    }

    /**
     * Remove next file and returns it's size
     */
    private int removeNext() {
        if (mLastUsageDates.isEmpty()) {
            return 0;
        }

        Long oldestUsage = null;
        File mostLongUsedFile = null;
        Set<Entry<File, Long>> entries = mLastUsageDates.entrySet();
        synchronized (mLastUsageDates) {
            for (Entry<File, Long> entry : entries) {
                if (mostLongUsedFile == null) {
                    mostLongUsedFile = entry.getKey();
                    oldestUsage = entry.getValue();
                } else {
                    Long lastValueUsage = entry.getValue();
                    if (lastValueUsage < oldestUsage) {
                        oldestUsage = lastValueUsage;
                        mostLongUsedFile = entry.getKey();
                    }
                }
            }
        }

        int fileSize = getSize(mostLongUsedFile);
        if (mostLongUsedFile.delete()) {
            mLastUsageDates.remove(mostLongUsedFile);
        }
        return fileSize;
    }

    protected abstract int getSize(File file);
}
