package com.wrriormedia.library.imageloader.core;

import android.content.Context;
import android.graphics.Bitmap;

import com.wrriormedia.library.imageloader.cache.disc.DiscCacheAware;
import com.wrriormedia.library.imageloader.cache.disc.impl.FileCountLimitedDiscCache;
import com.wrriormedia.library.imageloader.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.wrriormedia.library.imageloader.cache.disc.impl.UnlimitedDiscCache;
import com.wrriormedia.library.imageloader.cache.disc.naming.FileNameGenerator;
import com.wrriormedia.library.imageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.wrriormedia.library.imageloader.cache.memory.MemoryCacheAware;
import com.wrriormedia.library.imageloader.cache.memory.impl.FuzzyKeyMemoryCache;
import com.wrriormedia.library.imageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.wrriormedia.library.imageloader.core.assist.MemoryCacheKeyUtil;
import com.wrriormedia.library.imageloader.core.display.BitmapDisplayer;
import com.wrriormedia.library.imageloader.core.display.SimpleBitmapDisplayer;
import com.wrriormedia.library.imageloader.core.download.ImageDownloader;
import com.wrriormedia.library.imageloader.core.download.URLConnectionImageDownloader;
import com.wrriormedia.library.imageloader.utils.StorageUtils;

import java.io.File;

/**
 * Factory for providing of default options for
 * {@linkplain ImageLoaderConfiguration configuration}
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class DefaultConfigurationFactory {

    /**
     * Create {@linkplain HashCodeFileNameGenerator default implementation} of
     * FileNameGenerator
     */
    public static FileNameGenerator createFileNameGenerator() {
        return new HashCodeFileNameGenerator();
    }

    public static DiscCacheAware createDiscCache(Context context, FileNameGenerator discCacheFileNameGenerator,
                                                 int discCacheSize, int discCacheFileCount) {
        if (discCacheSize > 0) {
            File individualCacheDir = StorageUtils.getIndividualCacheDirectory(context);
            return new TotalSizeLimitedDiscCache(individualCacheDir, discCacheFileNameGenerator, discCacheSize);
        } else if (discCacheFileCount > 0) {
            File individualCacheDir = StorageUtils.getIndividualCacheDirectory(context);
            return new FileCountLimitedDiscCache(individualCacheDir, discCacheFileNameGenerator, discCacheFileCount);
        } else {
            // 如果不做限制条件，那么我们就用manifest文件的配置的目录作为缓存目录
            File cacheDir = StorageUtils.getOwnCacheDirectory(context);
            return new UnlimitedDiscCache(cacheDir, discCacheFileNameGenerator);
        }
    }

    /**
     * Create default implementation of {@link MemoryCacheAware} depends on
     * incoming parameters
     */
    public static MemoryCacheAware<String, Bitmap> createMemoryCache(int memoryCacheSize,
                                                                     boolean denyCacheImageMultipleSizesInMemory) {
        MemoryCacheAware<String, Bitmap> memoryCache = new UsingFreqLimitedMemoryCache(memoryCacheSize);
        if (denyCacheImageMultipleSizesInMemory) {
            memoryCache = new FuzzyKeyMemoryCache<>(
                    memoryCache, MemoryCacheKeyUtil.createFuzzyKeyComparator());
        }
        return memoryCache;
    }

    /**
     * Create default implementation of {@link ImageDownloader}
     */
    public static ImageDownloader createImageDownloader() {
        return new URLConnectionImageDownloader();
    }

    /**
     * Create default implementation of {@link BitmapDisplayer}
     */
    public static BitmapDisplayer createBitmapDisplayer() {
        return new SimpleBitmapDisplayer();
    }
}
