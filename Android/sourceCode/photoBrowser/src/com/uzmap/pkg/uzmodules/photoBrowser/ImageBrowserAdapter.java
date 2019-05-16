/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

package com.uzmap.pkg.uzmodules.photoBrowser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.photoBrowser.view.largeImage.LargeImageView;
import com.uzmap.pkg.uzmodules.photoBrowser.view.largeImage.factory.FileBitmapDecoderFactory;
import com.uzmap.pkg.uzmodules.photoBrowser.view.largeImage.factory.InputStreamBitmapDecoderFactory;

public class ImageBrowserAdapter extends PagerAdapter{
	
	private ArrayList<String> mImagePaths;
	private Context mContext;

	private UZModuleContext mUZContext;
	
	private ViewGroup mViewContainer;
	
	private boolean zoomEnable = true;
	
	public void setZoomEnable(boolean zoomable){
		this.zoomEnable = zoomable;
	}
	
	private String mPlaceholdImg;
	
	public void setPlaceholdImg(String path){
		this.mPlaceholdImg = path;
	}

	public ImageBrowserAdapter(Context context, UZModuleContext uzContext, ArrayList<String> imagePaths, ImageLoader imageLoader) {
		this.mImagePaths = imagePaths;
		this.mContext = context;
		this.mUZContext = uzContext;
	}

	@Override
	public int getCount() {
		return mImagePaths.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	public ViewGroup getViewContainer(){
		return this.mViewContainer;
	}
	
	@SuppressLint("NewApi")
	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {
		
		mViewContainer = container;
		
		int item_view_id = UZResourcesIDFinder.getResLayoutID("photo_browser_item_layout");
		View itemView = View.inflate(mContext, item_view_id, null);
		
		itemView.setTag(position);
		
		int photo_view_id = UZResourcesIDFinder.getResIdID("photoView");
		final LargeImageView imageView = (LargeImageView)itemView.findViewById(photo_view_id);
		
		imageView.setCanZoom(this.zoomEnable);
		
		int load_progress_id = UZResourcesIDFinder.getResIdID("loadProgress");
		final ProgressBar progress = (ProgressBar)itemView.findViewById(load_progress_id);
		progress.setTag(position);
		
		String imagePath = mImagePaths.get(position);
		
		
		if(!TextUtils.isEmpty(imagePath)){
			if(imagePath.startsWith("http")){
				new ImageDownLoader(new ImageDownLoader.DownLoadListener() {
					
					@Override
					public void onStart() {
						progress.setVisibility(View.VISIBLE);
						
						if(!TextUtils.isEmpty(mPlaceholdImg)){
							if(mPlaceholdImg.startsWith("file://")){
								try {
									imageView.setImage(new InputStreamBitmapDecoderFactory(mContext.getAssets().open(mPlaceholdImg.replace("file:///android_asset/", ""))), true);
									Log.i("debug", "== 1" + mPlaceholdImg.replace("file:///android_asset/", ""));
								} catch (IOException e) {
									e.printStackTrace();
									imageView.setImage(new FileBitmapDecoderFactory(mPlaceholdImg.replaceAll("file://", "")), true);
									Log.i("debug", "== 2" + mPlaceholdImg.replaceAll(".+widget", "widget"));
								}
							} else {
								imageView.setImage(new FileBitmapDecoderFactory(mPlaceholdImg), true);
							}
							
						}
					}
					
					@Override
					public void onFailed(){
						PhotoBrowser.callback(mUZContext, PhotoBrowser.EVENT_TYPE_LOADFAILED, (Integer)progress.getTag());
					}
					
					@Override
					public void onFinish(String savePath) {
						imageView.setImage(new FileBitmapDecoderFactory(savePath), false);
						progress.setVisibility(View.GONE);
						PhotoBrowser.callback(mUZContext, PhotoBrowser.EVENT_TYPE_LOADSUCCESSED, (Integer)progress.getTag());
					}
					
					@Override
					public void onCancel() {
						progress.setVisibility(View.GONE);
					}
				}).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imagePath);
				
			} else if (imagePath.startsWith("/") || imagePath.startsWith("fs") || imagePath.startsWith("widget") || imagePath.startsWith("file")) {
				try{
//					String modifiedPath = fixedImage(imagePath, container.getContext());
//					imageView.setImage(new FileBitmapDecoderFactory(modifiedPath));
//					progress.setVisibility(View.GONE);
					new LoadImageTask(imageView, imagePath, container.getContext(), progress).execute();
				}catch(Exception e){
					e.printStackTrace();
				}
			}else {
				byte[] data = Base64.decode(imagePath, Base64.DEFAULT);
				imageView.setImage(BitmapFactory.decodeByteArray(data, 0, data.length));
				progress.setVisibility(View.GONE);
			}
		}
		
		imageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				PhotoBrowser.callback(mUZContext, PhotoBrowser.EVENT_TYPE_CLICK, position);
			}
		});
		
		imageView.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				PhotoBrowser.callback(mUZContext, PhotoBrowser.EVENT_TYPE_LONG_CLICK, position);
				return false;
			}
		});
		container.addView(itemView);
		return itemView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
	
	public ArrayList<String> getDatas(){
		return mImagePaths;
	} 
	
	class LoadImageTask extends AsyncTask<Void, Void, String>{
		
		private LargeImageView mImageView;
		private Context mContext;
		private String srcPath;
		private ProgressBar mBar;
		
		public LoadImageTask(LargeImageView imageView, String path, Context context, ProgressBar bar){
			this.mImageView = imageView;
			this.mContext = context;
			this.srcPath = path;
			this.mBar = bar;
		}

		@Override
		protected String doInBackground(Void... arg0) {
			return fixedImage(srcPath, mContext);
		}

		@Override
		protected void onPostExecute(String result) {
			mImageView.setImage(new FileBitmapDecoderFactory(result), false);
			mBar.setVisibility(View.GONE);
		}
		
	}
	
	public String fixedImage(String filePath, Context context){
		File srcFile = new File(filePath);
		int degree = 0;
		if(srcFile != null && srcFile.exists()){
			degree = BitmapToolkit.readPictureDegree(filePath);
			if(degree == 0){
				return filePath;
			}
			
			String cachePath = context.getExternalCacheDir().getAbsolutePath() + "/image/modifiedImg.jpg";
			File file = new File(cachePath);
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			
			Log.i("debug", "fixed rotated");
			Bitmap bitmap = BitmapFactory.decodeFile(filePath);
			bitmap = BitmapToolkit.rotaingImageView(degree, bitmap);
			try {
				bitmap.compress(CompressFormat.JPEG, 80, new FileOutputStream(cachePath));
				return cachePath;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return filePath;
	}
	
}
