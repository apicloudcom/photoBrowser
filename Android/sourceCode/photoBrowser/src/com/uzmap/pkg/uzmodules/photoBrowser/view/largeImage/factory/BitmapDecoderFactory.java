package com.uzmap.pkg.uzmodules.photoBrowser.view.largeImage.factory;

import android.graphics.BitmapRegionDecoder;

import java.io.IOException;

public interface BitmapDecoderFactory {
    BitmapRegionDecoder made() throws IOException;
    int[] getImageInfo();
    String getImagePath();
}