package com.warriormedia.library.imageloader.cache.disc;

import com.warriormedia.library.imageloader.cache.disc.naming.FileNameGenerator;
import com.warriormedia.library.imageloader.core.DefaultConfigurationFactory;

import java.io.File;

/**
 * Base disc cache. Implements common functionality for disc cache.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see DiscCacheAware
 * @see FileNameGenerator
 */
public abstract class BaseDiscCache implements DiscCacheAware {

    private File mCacheDir;

    private FileNameGenerator mFileNameGenerator;

    /**
     * 构造函数
     *
     * @param cacheDir 缓存目录
     */
    public BaseDiscCache(File cacheDir) {
        this(cacheDir, DefaultConfigurationFactory.createFileNameGenerator());
    }

    /**
     * 构造函数
     *
     * @param cacheDir          缓存目录
     * @param fileNameGenerator 文件名生成器
     */
    public BaseDiscCache(File cacheDir, FileNameGenerator fileNameGenerator) {
        this.mCacheDir = cacheDir;
        this.mFileNameGenerator = fileNameGenerator;
    }

    @Override
    public File get(String key) {
        String fileName = mFileNameGenerator.generate(key);
        return new File(mCacheDir, fileName);
    }

    @Override
    public void clear() {
        File[] files = mCacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    protected File getCacheDir() {
        return mCacheDir;
    }
}
