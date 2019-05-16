package com.uzmap.pkg.uzmodules.photoBrowser.view.largeImage;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

import com.uzmap.pkg.uzmodules.photoBrowser.view.largeImage.factory.BitmapDecoderFactory;

/**
 * Created by LuckyJayce on 2016/11/24.
 */
public interface ILargeImageView {

    int getImageWidth();

    int getImageHeight();

    boolean hasLoad();

    void setOnImageLoadListener(BlockImageLoader.OnImageLoadListener onImageLoadListener);

    void setImage(BitmapDecoderFactory factory, boolean isPlaceHolder);

    void setImage(BitmapDecoderFactory factory, Drawable defaultDrawable);

    void setImage(Bitmap bm);

    void setImage(Drawable drawable);

    void setImage(@DrawableRes int resId);

    void setImageDrawable(Drawable drawable);

    void setScale(float scale);

    float getScale();

    BlockImageLoader.OnImageLoadListener getOnImageLoadListener();
}
