/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.photoBrowser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class ViewUtil {

	public static StateListDrawable addStateDrawable(int nomalColor,
			int pressColor) {
		StateListDrawable sd = new StateListDrawable();
		sd.addState(new int[] { android.R.attr.state_pressed },
				new ColorDrawable(pressColor));
		sd.addState(new int[] { android.R.attr.state_focused },
				new ColorDrawable(nomalColor));
		sd.addState(new int[] {}, new ColorDrawable(nomalColor));
		return sd;
	}

	public static StateListDrawable addStateDrawable(Drawable pressDrawable,
			Drawable normalDrawable) {
		StateListDrawable sd = new StateListDrawable();
		sd.addState(new int[] { android.R.attr.state_pressed }, pressDrawable);
		sd.addState(new int[] { android.R.attr.state_focused }, normalDrawable);
		sd.addState(new int[] {}, normalDrawable);
		return sd;
	}

	@SuppressWarnings("deprecation")
	public static int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay().getWidth();
	}

	@SuppressWarnings("deprecation")
	public static int getScreenHeight(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay().getHeight();
	}
	
	public static int getScreenWidthPX(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
		
	}

	public static int getScreenHeightPX(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	public static ShapeDrawable createRoundCornerShapeDrawable(float Radii,
			float borderLength, int borderColor) {

		float[] outerR = new float[] { Radii, Radii, Radii, Radii, Radii,
				Radii, Radii, Radii };
		RoundRectShape rr = new RoundRectShape(outerR, null, null);
		ShapeDrawable bgDrawable = new ShapeDrawable(rr);

		bgDrawable.getPaint().setColor(borderColor);

		return bgDrawable;
	}

	public static ColorStateList getColorStateList(int titleLight, int titleNormal) {
		int[][] states = new int[2][];
		states[0] = new int[] { android.R.attr.state_pressed,
				android.R.attr.state_enabled };
		states[1] = new int[] { android.R.attr.state_enabled };
		int[] colors = new int[] { titleLight, titleNormal };
		ColorStateList colorList = new ColorStateList(states, colors);
		
		return colorList;
	}
	
	/**
     * dp转换成px
     */
    public static float dp2px(Context context,float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return dpValue*scale+0.5f;
    }

    /**
     * px转换成dp
     */
    public static int px2dp(Context context,float pxValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/scale+0.5f);
    }
    /**
     * sp转换成px
     */
    public static int sp2px(Context context,float spValue){
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue*fontScale+0.5f);
    }
    /**
     * px转换成sp
     */
    public static int px2sp(Context context,float pxValue){
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue/fontScale+0.5f);
    }
    
    public static int dp2sp(Context context,float dpValue){
    	float scale=context.getResources().getDisplayMetrics().density;
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int) ((dpValue*scale+0.5f)/fontScale+0.5f);
    }
    
    public static Bitmap setImgSize(Bitmap bm, int newWidth ,int newHeight){
        // 获得图片的宽高.
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

	public static int getNavigationBarHeight(Context context) {
		int result = -1;
		if (hasNavBar(context)) {
			Resources res = context.getResources();
			int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
			if (resourceId > 0) {
				result = res.getDimensionPixelSize(resourceId);
			}
		}
		return result;
	}

	@SuppressLint("NewApi")
	public static void hideBottomUIMenu(Activity activity){
		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
			View v = activity.getWindow().getDecorView();
			v.setSystemUiVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 19) {View decorView = activity.getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_FULLSCREEN| View.SYSTEM_UI_FLAG_IMMERSIVE;
			decorView.setSystemUiVisibility(uiOptions);
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
	}
	/**
	 * 检查是否存在虚拟按键栏
	 *
	 * @param context
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static boolean hasNavBar(Context context) {
		Log.i("asher","module -- "+Build.MODEL);
		if (TextUtils.equals(Build.MODEL,"Nokia X6")){
			return false;
		}
		Resources res = context.getResources();
		int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
		if (resourceId != 0) {
			boolean hasNav = res.getBoolean(resourceId);
			// check override flag
			String sNavBarOverride = getNavBarOverride();
			if ("1".equals(sNavBarOverride)) {
				hasNav = false;
			} else if ("0".equals(sNavBarOverride)) {
				hasNav = true;
			}
			return hasNav;
		} else { // fallback
			return !ViewConfiguration.get(context).hasPermanentMenuKey();
		}
	}

	private static String getNavBarOverride() {
		String sNavBarOverride = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			try {
				Class c = Class.forName("android.os.SystemProperties");
				Method m = c.getDeclaredMethod("get", String.class);
				m.setAccessible(true);
				sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
			} catch (Throwable e) {
			}
		}
		return sNavBarOverride;
	}

	public static int getStatusBarHeight(Activity activity){
		int statusViewHeight = -1;
		int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			statusViewHeight = activity.getResources().getDimensionPixelSize(resourceId);
		}
		return statusViewHeight;
	}
}
