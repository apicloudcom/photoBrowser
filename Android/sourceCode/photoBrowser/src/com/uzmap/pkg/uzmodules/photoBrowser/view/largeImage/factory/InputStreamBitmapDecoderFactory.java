package com.uzmap.pkg.uzmodules.photoBrowser.view.largeImage.factory;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamBitmapDecoderFactory implements BitmapDecoderFactory {
    private InputStream inputStream;

    public InputStreamBitmapDecoderFactory(InputStream inputStream) {
        super();
        this.inputStream = inputStream;
    }

    @SuppressLint("NewApi") 
    @Override
    public BitmapRegionDecoder made() throws IOException {
        return BitmapRegionDecoder.newInstance(inputStream, false);
    }

    @Override
    public int[] getImageInfo() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, new Rect(),options);
        return new int[]{options.outWidth, options.outHeight};
    }

	@Override
	public String getImagePath() {
		// TODO Auto-generated method stub
		return null;
	}
}