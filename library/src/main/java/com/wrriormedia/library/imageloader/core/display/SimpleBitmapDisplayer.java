package com.wrriormedia.library.imageloader.core.display;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Just displays {@link Bitmap} in {@link ImageView}
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class SimpleBitmapDisplayer implements BitmapDisplayer {
    @Override
    public Bitmap display(Bitmap bitmap, ImageView imageView) {
        imageView.setImageBitmap(bitmap);
        return bitmap;
    }
}