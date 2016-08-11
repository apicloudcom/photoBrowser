/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.photoBrowser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import co.senab.photoview.PhotoView;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.uzmap.pkg.uzmodules.photoBrowser.ImageLoader.OnLoadCompleteListener;

public class PhotoBrowser extends UZModule {

	public static final String EVENT_TYPE_SHOW = "show";
	public static final String EVENT_TYPE_CHANGE = "change";
	public static final String EVENT_TYPE_CLICK = "click";
	public static final String EVENT_TYPE_LOADSUCCESSED = "loadImgSuccess";
	public static final String EVENT_TYPE_LONG_CLICK = "longPress";

	public static final String EVENT_TYPE_LOADFAILED = "loadImgFail";

	public PhotoBrowser(UZWebView webView) {
		super(webView);
		mLoader = new ImageLoader(mContext.getCacheDir().getAbsolutePath());
	}

	private View mBrowserMainLayout;
	private HackyViewPager mBrowserPager;
	private ImageLoader mLoader;
	private ImageBrowserAdapter mAdapter;

	private Config mConfig;

	private UZModuleContext mUZContext;

	public void jsmethod_open(final UZModuleContext uzContext) {

		if (mBrowserMainLayout != null) {
			removeViewFromCurWindow(mBrowserMainLayout);
			insertViewToCurWindow(mBrowserMainLayout, (RelativeLayout.LayoutParams)mBrowserMainLayout.getLayoutParams());
			return;
		}

		mConfig = new Config(uzContext, this.getWidgetInfo());
		int main_pager_id = UZResourcesIDFinder.getResLayoutID("photobrowser_main_layout");

		mBrowserMainLayout = View.inflate(mContext, main_pager_id, null);
		mBrowserMainLayout.setBackgroundColor(mConfig.bgColor);

		int browserPagerId = UZResourcesIDFinder.getResIdID("browserPager");
		mBrowserPager = (HackyViewPager) mBrowserMainLayout.findViewById(browserPagerId);

		Bitmap placeHolderBitmap = getBitmap(mConfig.placeholdImg);
		mLoader.setPlaceHolderBitmap(placeHolderBitmap);

		mAdapter = new ImageBrowserAdapter(mContext, uzContext, mConfig.imagePaths, mLoader);
		mBrowserPager.setAdapter(mAdapter);
		mAdapter.setZoomEnable(mConfig.zoomEnabled);

		mBrowserPager.setCurrentItem(mConfig.activeIndex);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		insertViewToCurWindow(mBrowserMainLayout, params);

		callback(uzContext, EVENT_TYPE_SHOW, -1);

		// pageChangeListener
		mBrowserPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				callback(uzContext, EVENT_TYPE_CHANGE, arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});

	}

	public void jsmethod_show(UZModuleContext uzContext) {
		if (mBrowserMainLayout != null) {
			mBrowserMainLayout.setVisibility(View.VISIBLE);
		}
	}

	public void jsmethod_hide(UZModuleContext uzContext) {
		if (mBrowserMainLayout != null) {
			mBrowserMainLayout.setVisibility(View.GONE);
		}
	}

	public void jsmethod_close(UZModuleContext uzContext) {
		removeViewFromCurWindow(mBrowserMainLayout);
		mBrowserMainLayout = null;
	}

	public void jsmethod_setIndex(UZModuleContext uzContext) {
		int index = uzContext.optInt("index");
		if (mBrowserPager != null && index < mBrowserPager.getAdapter().getCount() && index >= 0) {
			mBrowserPager.setCurrentItem(index);
		}
	}

	public void jsmethod_getIndex(UZModuleContext uzContext) {
		if (mBrowserPager != null) {
			callback(uzContext, mBrowserPager.getCurrentItem());
		}
	}

	public static void callback(UZModuleContext uzContext, String eventType, int index) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("eventType", eventType);
			if (index >= 0) {
				ret.put("index", index);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		uzContext.success(ret, false);
	}

	public void callback(UZModuleContext uzContext, int index) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("index", index);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		uzContext.success(ret, false);
	}

	public void callback(UZModuleContext uzContext, String path) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("path", path);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		uzContext.success(ret, false);
	}

	public void jsmethod_getImage(UZModuleContext uzContext) {
		int index = uzContext.optInt("index");
		if (mAdapter != null && mLoader != null && index < mAdapter.getDatas().size() && index >= 0) {
			String imagePath = mAdapter.getDatas().get(index);
			if (!TextUtils.isEmpty(imagePath) && imagePath.startsWith("http")) {
				callback(uzContext, mLoader.getCachePath(imagePath));
			} else {
				callback(uzContext, imagePath);
			}
		}
	}

	public void jsmethod_setImage(UZModuleContext uzContext) {
		int index = uzContext.optInt("index");
		String imagePath = uzContext.optString("image");

		if (!imagePath.startsWith("http")) {
			imagePath = UZUtility.makeRealPath(imagePath, getWidgetInfo());
		}

		if (index >= 0 && index < mAdapter.getCount() && !TextUtils.isEmpty(imagePath)) {
			View view = getExistChild(index);
			if (view != null) {

				int photo_view_id = UZResourcesIDFinder.getResIdID("photoView");
				final PhotoView imageView = (PhotoView) view.findViewById(photo_view_id);

				int load_progress_id = UZResourcesIDFinder.getResIdID("loadProgress");
				final ProgressBar progress = (ProgressBar) view.findViewById(load_progress_id);

				mLoader.load(imageView, progress, imagePath);

				if (mUZContext == null) {
					return;
				}

				mLoader.setOnLoadCompleteListener(new OnLoadCompleteListener() {
					@Override
					public void onLoadComplete(ProgressBar bar) {
						PhotoBrowser.callback(mUZContext, PhotoBrowser.EVENT_TYPE_LOADSUCCESSED, (Integer) bar.getTag());
					}

					@Override
					public void onLoadFailed(final ProgressBar bar) {
						PhotoBrowser.callback(mUZContext, PhotoBrowser.EVENT_TYPE_LOADFAILED, (Integer) bar.getTag());
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								bar.setVisibility(View.GONE);
							}
						});
					}
				});

			} else {
				mConfig.imagePaths.set(index, imagePath);
			}
		}
	}

	public View getExistChild(int index) {
		if (mAdapter != null && mAdapter.getViewContainer() != null) {
			for (int i = 0; i < mAdapter.getViewContainer().getChildCount(); i++) {
				if ((Integer) (mAdapter.getViewContainer().getChildAt(i).getTag()) == index) {
					return mAdapter.getViewContainer().getChildAt(i);
				}
			}
		}
		return null;
	}

	public void jsmethod_appendImage(UZModuleContext uzContext) {

		JSONArray appendedPathArr = uzContext.optJSONArray("images");

		if (mConfig == null) {
			return;
		}

		if (appendedPathArr != null) {
			ArrayList<String> appendedPaths = new ArrayList<String>();
			for (int i = 0; i < appendedPathArr.length(); i++) {
				appendedPaths.add(appendedPathArr.optString(i));
			}
			mConfig.imagePaths.addAll(appendedPaths);
		}

		int curItemIndex = mBrowserPager.getCurrentItem();
		mBrowserPager.setAdapter(mAdapter);
		mBrowserPager.setCurrentItem(curItemIndex);

	}

	public void jsmethod_deleteImage(UZModuleContext uzContext) {
		if (mConfig == null) {
			return;
		}
		int index = uzContext.optInt("index");
		if (index >= 0 && index < mConfig.imagePaths.size()) {

			int curItemIndex = mBrowserPager.getCurrentItem();
			mConfig.imagePaths.remove(index);
			mBrowserPager.setAdapter(mAdapter);
			mBrowserPager.setCurrentItem(curItemIndex - 1);
			
		}
	}

	public void jsmethod_clearCache(UZModuleContext uzContext) {
		if (mLoader != null) {

			new Thread(new Runnable() {
				@Override
				public void run() {
					mLoader.clearCache();
				}
			}).start();
		}
	}

	public Bitmap getBitmap(String path) {
		InputStream input = null;
		Bitmap mBitmap = null;
		if (!TextUtils.isEmpty(path)) {
			String iconPath = makeRealPath(path);
			try {
				input = UZUtility.guessInputStream(iconPath);
				mBitmap = BitmapFactory.decodeStream(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mBitmap;
	}
}
