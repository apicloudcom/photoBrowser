/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.photoBrowser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
/**
 * @author Robot
 */
@SuppressLint("NewApi") 
public class ImageLoader {

	public static final String TAG = "lyh";

	/**
	 * the max size of the cache
	 */
	public static int MAX_CACHE_SIZE = (int) Runtime.getRuntime().maxMemory() / 6;

	/**
	 * the path of the cache
	 */
	private String CACHE_PATH = Environment.getExternalStorageDirectory()
			+ "/.image";

	/**
	 * the number of concurrent threads
	 */
	public static int THREAD_NUMS = 3;

	/**
	 * read buffer size
	 */
	public static final int BUF_SIZE = 1024;

	/**
	 * cached image format
	 */
	public static final String CACHED_IMAGE_FORMAT = ".jpg";

	/**
	 * sample flag (avoid OOM, just applies to local image file)
	 */
	public static boolean SAMPLED_FLAG = true;

	/**
	 * placeholder image path;
	 */
	public Bitmap placeHolderBmp;
	
	/**
	 * connect timeout
	 */
	public static final int TIME_OUT = 3000;
	
	private Context mCtx;
	
	public ImageLoader(Context context){
		this.mCtx = context;
	}
	
	public ImageLoader(Context context, String cachePath){
		this.mCtx = context;
		CACHE_PATH = cachePath;
	}

	private static LruCache<String, Bitmap> caches = new LruCache<String, Bitmap>(
			MAX_CACHE_SIZE) {

		@Override
		protected void entryRemoved(boolean evicted, String key,
				Bitmap oldValue, Bitmap newValue) {
			Log.i(TAG, "Entry Removed");
		}

		@Override
		protected int sizeOf(String key, Bitmap value) {
			int size = value.getRowBytes() * value.getHeight();
			return size;
		}

	};
	private static ExecutorService mThreadPool = Executors
			.newFixedThreadPool(THREAD_NUMS);

	@SuppressWarnings("deprecation")
	public void load(View view, final String path) {

		if (placeHolderBmp != null) {
			if (view instanceof ImageView) {
				((ImageView) view).setImageBitmap(placeHolderBmp);
			} else {
				view.setBackgroundDrawable(new BitmapDrawable(placeHolderBmp));
			}
		}

		// load from memory at first
		Bitmap cacheBitmap = caches.get(md5(path));

		if (cacheBitmap == null) {
			ImageDownTask task = new ImageDownTask(view, path);
			mThreadPool.execute(task);
		} else {
			setImage(view, cacheBitmap, 0, null);
		}

	}

	@SuppressWarnings("deprecation")
	public void load(View view, ProgressBar mProgressbar, final String path) {

		if (placeHolderBmp != null) {
			if (view instanceof ImageView) {
				((ImageView) view).setImageBitmap(placeHolderBmp);
			} else {
				view.setBackgroundDrawable(new BitmapDrawable(placeHolderBmp));
			}
		}

		// load from memory at first
		Bitmap cacheBitmap = caches.get(md5(path));

		if (cacheBitmap == null) {
			ImageDownTask task = new ImageDownTask(view, path, mProgressbar);
			mThreadPool.execute(task);
		} else {
			setImage(view, cacheBitmap, 0, mProgressbar);
		}

	}

	public void load(View view, final String path,
			OnLoadProgressListener progressListener) {

		// load from memory at first
		Bitmap cacheBitmap = caches.get(md5(path));

		if (cacheBitmap == null) {
			ImageDownTask task = new ImageDownTask(view, path, progressListener);
			mThreadPool.execute(task);
		} else {
			setImage(view, cacheBitmap, 0, null);
		}

	}

	public void load(View view, final String path, int corner) {
		// load from memory
		Bitmap cacheBitmap = caches.get(md5(path));

		if (cacheBitmap == null) {
			ImageDownTask task = new ImageDownTask(view, path, corner);
			mThreadPool.execute(task);
		} else {
			setImage(view, cacheBitmap, corner, null);
		}
	}

	public void setPlaceHolderBitmap(Bitmap bmp) {
		this.placeHolderBmp = bmp;
	}

	public Bitmap getImageFromNet(String path) {

		if (TextUtils.isEmpty(path)) {
			return null;
		}
		
		URL url = null;
		try {
			url = new URL(path);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return null;
		}

		if (path.startsWith("https")) {
			
			try {
				HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
				urlConnection.setConnectTimeout(TIME_OUT);
				urlConnection.setReadTimeout(TIME_OUT);
				Bitmap bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());
				return bitmap;
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			try {
				URLConnection urlConnection = url.openConnection();
				urlConnection.setConnectTimeout(TIME_OUT);
				urlConnection.setReadTimeout(TIME_OUT);
				Bitmap bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());

				return bitmap;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return null;
	}

	public Bitmap getImageFromNetWithProgress(String path,
			OnLoadProgressListener progressListener) {

		URL url;
		try {
			url = new URL(path);
			
			InputStream inputStream;
			int totalCount = 0;
			
			if(path.startsWith("https")){
				HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
				inputStream = urlConnection.getInputStream();
				totalCount =  urlConnection.getInputStream().available();
			} else {
				URLConnection urlConnection = url.openConnection();
				inputStream = urlConnection.getInputStream();
				totalCount = urlConnection.getInputStream().available();
			}

			byte[] array = readStream(inputStream, totalCount, progressListener);
			Bitmap bitmap = BitmapFactory.decodeByteArray(array, 0,
					array.length);

			return bitmap;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public Bitmap getBitmapFromLocal(String path) {

		if (SAMPLED_FLAG) {
			return getSampledBitmap(path);
		}

		File file = new File(path);
		try {
			FileInputStream input = new FileInputStream(file);
			Bitmap bitmap = BitmapFactory.decodeStream(input);
			return bitmap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String md5(String string) {

		if (TextUtils.isEmpty(string)) {
			return null;
		}

		byte[] hash;

		try {
			hash = MessageDigest.getInstance("MD5").digest(
					string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}
		return hex.toString();
	}

	public void saveBitmap(Bitmap bitmap, String path, String fileName) {

		if (bitmap == null || TextUtils.isEmpty(path)
				|| TextUtils.isEmpty(fileName)) {
			return;
		}

		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}

		try {
			FileOutputStream output = new FileOutputStream(new File(file,
					fileName));
			if (CACHED_IMAGE_FORMAT.endsWith(".png")) {
				bitmap.compress(CompressFormat.PNG, 100, output);
			} else {
				bitmap.compress(CompressFormat.JPEG, 100, output);
			}
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	class ImageDownTask implements Runnable {

		private Bitmap mBitmap;
		private String url;
		private View view;
		private int corner;

		private ProgressBar mProgressBar;

		private OnLoadProgressListener mProgressListener;

		public ImageDownTask(View view, String url) {
			this.url = url;
			this.view = view;
		}

		public ImageDownTask(View view, String url, ProgressBar bar) {
			this.url = url;
			this.view = view;
			this.mProgressBar = bar;
		}

		public ImageDownTask(View view, String url,
				OnLoadProgressListener mProgressListener) {
			this(view, url);
			this.mProgressListener = mProgressListener;
		}

		public ImageDownTask(View view, String url, int corner) {
			this(view, url);
			this.corner = corner;

		}

		public ImageDownTask(View view, String url,
				OnLoadProgressListener mProgressListener, int corner) {
			this(view, url, mProgressListener);
			this.corner = corner;
		}

		@Override
		public void run() {

			// load from disk
			File file = new File(url);
			if (file.exists()) {
				mBitmap = getBitmapFromLocal(url);
				if(mBitmap != null){
				 caches.put(md5(url), mBitmap);
				}
				Bitmap bitmap = caches.get(md5(url));
				setImage(view, bitmap, corner, mProgressBar);

				return;
			}

			mBitmap = getBitmapFromLocal(CACHE_PATH + "/" + md5(url)
					+ CACHED_IMAGE_FORMAT);

			Log.i(TAG, " -- cache path -- : " + CACHE_PATH);

			if (mBitmap != null) {

				caches.put(md5(url), mBitmap);
				setImage(view, caches.get(md5(url)), corner, mProgressBar);

			} else {
				if (mProgressListener != null) {
					mBitmap = getImageFromNetWithProgress(this.url,
							mProgressListener);
				} else {
					mBitmap = getImageFromNet(this.url);
				}

				if (mBitmap == null) {
					if(mOnLoadCompleteListener != null){
						mOnLoadCompleteListener.onLoadFailed(mProgressBar);
					}
					return;
				}
				caches.put(md5(url), mBitmap);
				saveBitmap(mBitmap, CACHE_PATH, md5(url) + CACHED_IMAGE_FORMAT);
				setImage(view, caches.get(md5(url)), corner, mProgressBar);

				if (mOnLoadCompleteListener != null)
					mOnLoadCompleteListener.onLoadComplete(mProgressBar);
			}

		}
	}

	public String getCachePath(String url) {
		return CACHE_PATH + "/" + md5(url) + CACHED_IMAGE_FORMAT;
	}

	@SuppressWarnings("deprecation")
	public void setImage(final View view, final Bitmap bitmap,
			final int corner, final ProgressBar progressBar) {

		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {

				if (view instanceof ImageView) {
					((ImageView) view).setImageBitmap(bitmap);
				} else {
					view.setBackgroundDrawable(new BitmapDrawable(bitmap));
				}

				if (progressBar != null) {
					progressBar.setVisibility(View.GONE);
				}

			}

		});
	}
	
	public float getScreenWidth(){
		DisplayMetrics dm = new DisplayMetrics();  
        ((Activity)mCtx).getWindowManager().getDefaultDisplay().getMetrics(dm);  
        return dm.widthPixels * dm.density;   
	}
	
	public float getScreenHeight(){
		DisplayMetrics dm = new DisplayMetrics();  
        ((Activity)mCtx).getWindowManager().getDefaultDisplay().getMetrics(dm);  
        return dm.heightPixels * dm.density;  
	}

	/**
	 * <P>
	 * Generally only to the local image sampling read, local image is compared
	 * commonly large, some even more than 5M, in order to prevent the OOM need
	 * sampled
	 * </P>
	 * 
	 * @param localFile
	 * @return
	 */
	public Bitmap getSampledBitmap(String localFile) {
		
		Log.i(TAG, "=== " + localFile);

		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		
		Bitmap bitmap = BitmapFactory.decodeFile(localFile, newOpts);

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float hh = 1280f;//getScreenHeight();//
		float ww = 720f;//getScreenWidth();//
		int be = 1;
		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 1)
			be = 1;
		
		newOpts.inSampleSize = be;
		newOpts.inPreferredConfig = Config.ARGB_8888;
		newOpts.inPurgeable = true;
		newOpts.inInputShareable = true;

		bitmap = BitmapFactory.decodeFile(localFile, newOpts);

		return bitmap;
	}

	public interface OnLoadProgressListener {
		public void onLoadProgress(int progress);
	}

	public byte[] readStream(InputStream in, int totalCount,
			OnLoadProgressListener mProgressListener) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		byte[] buffer = new byte[BUF_SIZE];
		int len = -1;
		int curByteCount = 0;
		while ((len = in.read(buffer)) != -1) {
			outputStream.write(buffer, 0, len);
			curByteCount += len;

			float progressRange = (float) curByteCount / (float) totalCount * 100f;
			mProgressListener.onLoadProgress((int) progressRange);
		}
		outputStream.close();
		in.close();
		return outputStream.toByteArray();
	}

	public interface OnLoadCompleteListener {
		public void onLoadComplete(ProgressBar bar);
		public void onLoadFailed(ProgressBar bar);
	}

	public OnLoadCompleteListener mOnLoadCompleteListener;

	public void setOnLoadCompleteListener(OnLoadCompleteListener listener) {
		this.mOnLoadCompleteListener = listener;
	}

	/**
	 * cancel all downloading task
	 */
	public void cancelTasks() {
		mThreadPool.shutdown();
	}

	/**
	 * clear the cache
	 */
	public void clearCache() {

		File tmpFile = new File(CACHE_PATH);
		final int size = tmpFile.listFiles().length;
		final File[] fileList = tmpFile.listFiles();
		for (int i = 0; i < size; i++) {
			fileList[i].delete();
		}
	}
}
