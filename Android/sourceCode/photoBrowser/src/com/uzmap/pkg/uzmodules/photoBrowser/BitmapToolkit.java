/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.photoBrowser;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

public class BitmapToolkit {

	/**
	 *  Read the rotated degree of the image 
	 *  
	 * @param 
	 * 			path Represent the image path 
	 * @return 
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}

		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		System.out.println("angle2=" + angle);

		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}
	
	public static Bitmap scaleBitmap(float scale, Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}

		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		return resizedBitmap;
	}

	public static Bitmap scaleBitmapWithSize(Bitmap bitmap, int width, int height) {

		if (bitmap == null) {
			return null;
		}

		if (width > bitmap.getWidth() || height > bitmap.getHeight()) {
			return null;
		}

		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
		return resizedBitmap;
	}
	
	
	public static void setPictureDegreeZero(String path, int degree) {
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			// 修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
			// 例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
			switch (degree) {
			case 90:
				exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90 + "");
				break;
			case 180:
				exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_180 + "");
				break;
			case 270:
				exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_270 + "");
				break;
			default:
				exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "no");
				break;
			}
			exifInterface.saveAttributes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
