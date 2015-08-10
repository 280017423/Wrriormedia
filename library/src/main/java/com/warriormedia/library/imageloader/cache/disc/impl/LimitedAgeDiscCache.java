package com.warriormedia.library.imageloader.cache.disc.impl;

import com.warriormedia.library.imageloader.cache.disc.BaseDiscCache;
import com.warriormedia.library.imageloader.cache.disc.naming.FileNameGenerator;
import com.warriormedia.library.imageloader.core.DefaultConfigurationFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache which deletes files which were loaded more than defined time. Cache
 * size is unlimited.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see BaseDiscCache
 */
public class LimitedAgeDiscCache extends BaseDiscCache {
    public static final int MILLI_SECONDS = 1000;
    private final long mMaxFileAge;

    private final Map<File, Long> mLoadingDates = Collections.synchronizedMap(new HashMap<File, Long>());

    /**
     * @param cacheDir Directory for file caching
     * @param maxAge   Max file age (in seconds). If file age will exceed this value
     *                 then it'll be removed on next treatment (and therefore be
     *                 reloaded).
     */
    public LimitedAgeDiscCache(File cacheDir, long maxAge) {
        this(cacheDir, DefaultConfigurationFactory.createFileNameGenerator(), maxAge);
    }

    /**
     * @param cacheDir          Directory for file caching
     * @param fileNameGenerator Name generator for cached files
     * @param maxAge            Max file age (in seconds). If file age will exceed this value
     *                          then it'll be removed on next treatment (and therefore be
     *                          reloaded).
     */
    public LimitedAgeDiscCache(File cacheDir, FileNameGenerator fileNameGenerator, long maxAge) {
        super(cacheDir, fileNameGenerator);
        this.mMaxFileAge = maxAge * MILLI_SECONDS;
        readLoadingDates();
    }

    private void readLoadingDates() {
        File[] cachedFiles = getCacheDir().listFiles();
        for (File cachedFile : cachedFiles) {
            mLoadingDates.put(cachedFile, cachedFile.lastModified());
        }
    }

    @Override
    public void put(String key, File file) {
        long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        mLoadingDates.put(file, currentTime);
    }

    @Override
    public File get(String key) {
        File file = super.get(key);
        if (file.exists()) {
            Long loadingDate = mLoadingDates.get(file);
            if (loadingDate == null) {
                loadingDate = file.lastModified();
            }
            if (System.currentTimeMillis() - loadingDate > mMaxFileAge) {
                file.delete();
                mLoadingDates.remove(file);
            }
        }
        return file;
    }
}
