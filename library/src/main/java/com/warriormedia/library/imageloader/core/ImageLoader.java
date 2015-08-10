package com.warriormedia.library.imageloader.core;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.warriormedia.library.imageloader.cache.disc.DiscCacheAware;
import com.warriormedia.library.imageloader.cache.memory.MemoryCacheAware;
import com.warriormedia.library.imageloader.core.assist.ImageLoadingListener;
import com.warriormedia.library.imageloader.core.assist.ImageScaleType;
import com.warriormedia.library.imageloader.core.assist.ImageSize;
import com.warriormedia.library.imageloader.core.assist.MemoryCacheKeyUtil;
import com.warriormedia.library.imageloader.core.assist.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singletone for image loading and displaying at {@link ImageView ImageViews}<br />
 * <b>NOTE:</b> {@link #init(ImageLoaderConfiguration)} method must be called
 * before any other method.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImageLoader {

    public static final String TAG = ImageLoader.class.getSimpleName();

    private static final String ERROR_WRONG_ARGUMENTS = "Wrong arguments were passed to displayImage() method (ImageView reference are required)";
    private static final String ERROR_NOT_INIT = "ImageLoader must be init with configuration before using";
    private static final String ERROR_INIT_CONFIG_WITH_NULL = "ImageLoader configuration can not be initialized with null";
    private static final String LOG_LOAD_IMAGE_FROM_MEMORY_CACHE = "Load image from memory cache [%s]";
    private volatile static ImageLoader instance;
    private ImageLoaderConfiguration configuration;
    private ExecutorService imageLoadingExecutor;
    private ExecutorService cachedImageLoadingExecutor;
    private ImageLoadingListener emptyListener;
    private Map<ImageView, String> cacheKeyForImageView = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());

    private ImageLoader() {
    }

    /**
     * Returns singletone class instance
     */
    public static ImageLoader getInstance() {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes ImageLoader's singletone instance with configuration. Method
     * shoiuld be called <b>once</b> (each following call will have no effect)<br />
     *
     * @param configuration {@linkplain ImageLoaderConfiguration ImageLoader
     *                      configuration}
     * @throws IllegalArgumentException if <b>configuration</b> parameter is null
     */
    public synchronized void init(ImageLoaderConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(ERROR_INIT_CONFIG_WITH_NULL);
        }
        if (this.configuration == null) {
            this.configuration = configuration;
            emptyListener = new SimpleImageLoadingListener();
        }
    }

    /**
     * Adds display image task to execution pool. Image will be set to ImageView
     * when it's turn. <br/>
     * Default {@linkplain DisplayImageOptions display image options} from
     * {@linkplain ImageLoaderConfiguration configuration} will be used.<br />
     * <b>NOTE:</b> {@link #init(ImageLoaderConfiguration)} method must be
     * called before this method call
     *
     * @param uri       Image URI (i.e. "http://site.com/image.png",
     *                  "file:///mnt/sdcard/image.png")
     * @param imageView {@link ImageView} which should display image
     * @throws RuntimeException if {@link #init(ImageLoaderConfiguration)} method wasn't
     *                          called before
     */
    public void displayImage(String uri, ImageView imageView) {
        displayImage(uri, imageView, null, null, null);
    }

    /**
     * Adds display image task to execution pool. Image will be set to ImageView
     * when it's turn.<br />
     * <b>NOTE:</b> {@link #init(ImageLoaderConfiguration)} method must be
     * called before this method call
     *
     * @param uri       Image URI (i.e. "http://site.com/image.png",
     *                  "file:///mnt/sdcard/image.png")
     * @param imageView {@link ImageView} which should display image
     * @param options   {@linkplain DisplayImageOptions Display image options} for
     *                  image displaying. If <b>null</b> - default display image
     *                  options
     *                  {@linkplain ImageLoaderConfiguration.Builder#defaultDisplayImageOptions(DisplayImageOptions)
     *                  from configuration} will be used.
     * @throws RuntimeException if {@link #init(ImageLoaderConfiguration)} method wasn't
     *                          called before
     */
    public void displayImage(String uri, ImageView imageView, com.warriormedia.library.imageloader.core.DisplayImageOptions options) {
        displayImage(uri, imageView, options, null, null);
    }

    /**
     * Adds display image task to execution pool. Image will be set to ImageView
     * when it's turn.<br />
     * Default {@linkplain DisplayImageOptions display image options} from
     * {@linkplain ImageLoaderConfiguration configuration} will be used.<br />
     * <b>NOTE:</b> {@link #init(ImageLoaderConfiguration)} method must be
     * called before this method call
     *
     * @param uri       Image URI (i.e. "http://site.com/image.png",
     *                  "file:///mnt/sdcard/image.png")
     * @param imageView {@link ImageView} which should display image
     * @param listener  {@linkplain ImageLoadingListener Listener} for image loading
     *                  process. Listener fires events only if there is no image for
     *                  loading in memory cache. If there is image for loading in
     *                  memory cache then image is displayed at ImageView but listener
     *                  does not fire any event. Listener fires events on UI thread.
     * @throws RuntimeException if {@link #init(ImageLoaderConfiguration)} method wasn't
     *                          called before
     */
    public void displayImage(String uri, ImageView imageView, ImageLoadingListener listener) {
        displayImage(uri, imageView, null, listener, null);
    }

    public void displayImage(String uri, ImageView imageView, com.warriormedia.library.imageloader.core.DisplayImageOptions options, ImageLoadingListener listener) {
        displayImage(uri, imageView, options, listener, null);
    }

    public void displayImage(String uri, ImageView imageView, ImageLoadingListener listener, String smallUrl) {
        displayImage(uri, imageView, null, listener, smallUrl);
    }

    /**
     * Adds display image task to execution pool. Image will be set to ImageView
     * when it's turn.<br />
     * <b>NOTE:</b> {@link #init(ImageLoaderConfiguration)} method must be
     * called before this method call
     *
     * @param uri       Image URI (i.e. "http://site.com/image.png",
     *                  "file:///mnt/sdcard/image.png")
     * @param imageView {@link ImageView} which should display image
     * @param options   {@linkplain DisplayImageOptions Display image options} for
     *                  image displaying. If <b>null</b> - default display image
     *                  options
     *                  {@linkplain ImageLoaderConfiguration.Builder#defaultDisplayImageOptions(DisplayImageOptions)
     *                  from configuration} will be used.
     * @param listener  {@linkplain ImageLoadingListener Listener} for image loading
     *                  process. Listener fires events only if there is no image for
     *                  loading in memory cache. If there is image for loading in
     *                  memory cache then image is displayed at ImageView but listener
     *                  does not fire any event. Listener fires events on UI thread.
     * @throws RuntimeException if {@link #init(ImageLoaderConfiguration)} method wasn't
     *                          called before
     */
    public void displayImage(String uri, ImageView imageView, com.warriormedia.library.imageloader.core.DisplayImageOptions options,
                             ImageLoadingListener listener, String smallUrl) {
        if (configuration == null) {
            throw new RuntimeException(ERROR_NOT_INIT);
        }
        if (imageView == null) {
            Log.w(TAG, ERROR_WRONG_ARGUMENTS);
            return;
        }
        if (listener == null) {
            listener = emptyListener;
        }
        if (options == null) {
            options = configuration.defaultDisplayImageOptions;
        }

        if (uri == null || uri.length() == 0) {
            cacheKeyForImageView.remove(imageView);
            listener.onLoadingStarted();
            if (options.isShowImageForEmptyUri()) {
                imageView.setImageResource(options.getImageForEmptyUri());
            } else {
                imageView.setImageBitmap(null);
            }
            listener.onLoadingComplete(null);
            return;
        }

        ImageSize targetSize = getImageSizeScaleTo(imageView);
        String memoryCacheKey = MemoryCacheKeyUtil.generateKey(uri, targetSize);
        cacheKeyForImageView.put(imageView, memoryCacheKey);

        // 如果有小图，先显示小图
        if (smallUrl != null && smallUrl.length() > 0) {
            String smallMemoryCacheKey = MemoryCacheKeyUtil.generateKey(smallUrl, targetSize);
            Bitmap smallBmp = configuration.memoryCache.get(smallMemoryCacheKey);
            imageView.setImageBitmap(smallBmp);
        }

        Bitmap bmp = configuration.memoryCache.get(memoryCacheKey);
        if (bmp != null && !bmp.isRecycled()) {
            if (configuration.loggingEnabled)
                Log.i(TAG, String.format(LOG_LOAD_IMAGE_FROM_MEMORY_CACHE, memoryCacheKey));
            listener.onLoadingStarted();
            Bitmap displayedBitmap = options.getDisplayer().display(bmp, imageView);
            imageView.setImageBitmap(displayedBitmap);
            listener.onLoadingComplete(bmp);
        } else {
            listener.onLoadingStarted();

            if (options.isShowStubImage()) {
                imageView.setImageResource(options.getStubImage());
            } else {
                if (options.isResetViewBeforeLoading()) {
                    imageView.setImageBitmap(null);
                }
            }

            checkExecutors();
            ImageLoadingInfo imageLoadingInfo = new ImageLoadingInfo(uri, imageView, targetSize, options, listener);
            if (smallUrl != null && smallUrl.length() > 0)
                getSmallBmp(smallUrl, imageLoadingInfo);

            LoadAndDisplayImageTask displayImageTask = new LoadAndDisplayImageTask(
                    configuration, imageLoadingInfo, new Handler());
            boolean isImageCachedOnDisc = configuration.discCache.get(uri).exists();
            if (isImageCachedOnDisc) {
                cachedImageLoadingExecutor.submit(displayImageTask);
            } else {
                imageLoadingExecutor.submit(displayImageTask);
            }
        }
    }

    // 如果有小图，先显示小图
    private void getSmallBmp(String smallUrl, ImageLoadingInfo imageLoadingInfo) {
        String smallMemoryCacheKey = MemoryCacheKeyUtil.generateKey(smallUrl, imageLoadingInfo.targetSize);
        Bitmap smallBmp = configuration.memoryCache.get(smallMemoryCacheKey);
        if (smallBmp != null) {
            imageLoadingInfo.imageView.setImageBitmap(smallBmp);
            return;
        }
        File imageFile = configuration.discCache.get(smallUrl);
        if (imageFile.exists()) {
            Options options = new Options();
            try {
                options.inSampleSize = computeImageScale(imageLoadingInfo.targetSize,
                        imageLoadingInfo.options.getImageScaleType(), imageLoadingInfo.imageView.getScaleType(),
                        imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap b = BitmapFactory.decodeFile(imageFile.getPath(), options);
            if (b != null) {
                imageLoadingInfo.imageView.setImageBitmap(b);
            }
        }
    }

    private int computeImageScale(ImageSize targetSize, ImageScaleType scaleType, ScaleType viewScaleType, File file)
            throws IOException {
        int targetWidth = targetSize.getWidth();
        int targetHeight = targetSize.getHeight();

        // decode image size
        Options options = new Options();
        options.inJustDecodeBounds = true;
        FileInputStream imageStream = new FileInputStream(file);
        try {
            BitmapFactory.decodeStream(imageStream, null, options);
        } finally {
            imageStream.close();
        }

        int scale = 1;
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;
        int widthScale = imageWidth / targetWidth;
        int heightScale = imageWidth / targetHeight;
        switch (viewScaleType) {
            case FIT_XY:
            case FIT_START:
            case FIT_END:
            case CENTER_INSIDE:
                switch (scaleType) {
                    default:
                    case POWER_OF_2:
                        while (imageWidth / 2 >= targetWidth || imageHeight / 2 >= targetHeight) { // ||
                            imageWidth /= 2;
                            imageHeight /= 2;
                            scale *= 2;
                        }
                        break;
                    case EXACT:
                        scale = Math.max(widthScale, heightScale); // max
                        break;
                }
                break;
            case MATRIX:
            case CENTER:
            case CENTER_CROP:
            default:
                switch (scaleType) {
                    default:
                    case POWER_OF_2:
                        while (imageWidth / 2 >= targetWidth && imageHeight / 2 >= targetHeight) { // &&
                            imageWidth /= 2;
                            imageHeight /= 2;
                            scale *= 2;
                        }
                        break;
                    case EXACT:
                        scale = Math.min(widthScale, heightScale); // min
                        break;
                }
        }

        if (scale < 1) {
            scale = 1;
        }

        return scale;
    }

    private void checkExecutors() {
        if (imageLoadingExecutor == null || imageLoadingExecutor.isShutdown()) {
            imageLoadingExecutor = Executors.newFixedThreadPool(configuration.threadPoolSize,
                    configuration.displayImageThreadFactory);
        }
        if (cachedImageLoadingExecutor == null || cachedImageLoadingExecutor.isShutdown()) {
            cachedImageLoadingExecutor = Executors.newFixedThreadPool(configuration.threadPoolSize,
                    configuration.displayImageThreadFactory);
        }
    }

    /**
     * Returns memory cache
     */
    public MemoryCacheAware<String, Bitmap> getMemoryCache() {
        return configuration.memoryCache;
    }

    /**
     * Clear memory cache.<br />
     * Do nothing if {@link #init(ImageLoaderConfiguration)} method wasn't
     * called before.
     */
    public void clearMemoryCache() {
        if (configuration != null) {
            configuration.memoryCache.clear();
        }
    }

    /**
     * Returns disc cache
     */
    public DiscCacheAware getDiscCache() {
        return configuration.discCache;
    }

    /**
     * Clear disc cache.<br />
     * Do nothing if {@link #init(ImageLoaderConfiguration)} method wasn't
     * called before.
     */
    public void clearDiscCache() {
        if (configuration != null) {
            configuration.discCache.clear();
        }
    }

    /**
     * Returns URI of image which is loading at this moment into passed
     * {@link ImageView}
     */
    public String getLoadingUriForView(ImageView imageView) {
        return cacheKeyForImageView.get(imageView);
    }

    /**
     * Cancel the task of loading and displaying image for passed
     * {@link ImageView}.
     *
     * @param imageView {@link ImageView} for which display task will be cancelled
     */
    public void cancelDisplayTask(ImageView imageView) {
        cacheKeyForImageView.remove(imageView);
    }

    /**
     * Stops all running display image tasks, discards all other scheduled tasks
     */
    public void stop() {
        if (imageLoadingExecutor != null) {
            imageLoadingExecutor.shutdown();
        }
        if (cachedImageLoadingExecutor != null) {
            cachedImageLoadingExecutor.shutdown();
        }
    }

    /**
     * Defines image size for loading at memory (for memory economy) by
     * {@link ImageView} parameters.<br />
     * Size computing algorithm:<br />
     * 1) Get <b>layout_width</b> and <b>layout_height</b>. If both of them
     * haven't exact value then go to step #2.</br> 2) Get <b>maxWidth</b> and
     * <b>maxHeight</b>. If both of them are not set then go to step #3.<br />
     * 3) Get device screen dimensions.
     */
    private ImageSize getImageSizeScaleTo(ImageView imageView) {
        LayoutParams params = imageView.getLayoutParams();
        int width = params.width; // Get layout width parameter
        if (width <= 0)
            width = getFieldValue(imageView, "mMaxWidth"); // Check maxWidth
        // parameter
        if (width <= 0)
            width = configuration.maxImageWidthForMemoryCache;

        int height = params.height; // Get layout height parameter
        if (height <= 0)
            height = getFieldValue(imageView, "mMaxHeight"); // Check maxHeight
        // parameter
        if (height <= 0)
            height = configuration.maxImageHeightForMemoryCache;

        // Consider device screen orientation
        int screenOrientation = imageView.getContext().getResources().getConfiguration().orientation;
        if ((screenOrientation == Configuration.ORIENTATION_PORTRAIT && width > height)
                || (screenOrientation == Configuration.ORIENTATION_LANDSCAPE && width < height)) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        return new ImageSize(width, height);
    }

    private int getFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return value;
    }
}
