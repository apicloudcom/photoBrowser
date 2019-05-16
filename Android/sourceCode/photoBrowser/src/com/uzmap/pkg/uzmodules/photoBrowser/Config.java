/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

package com.uzmap.pkg.uzmodules.photoBrowser;


import java.util.ArrayList;

import org.json.JSONArray;

import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.uzmap.pkg.uzkit.data.UZWidgetInfo;

public class Config {
	
	public ArrayList<String> imagePaths = new ArrayList<String>();
	
	public int activeIndex;
	
	public String placeholdImg;
	
	public int bgColor;
	
	public boolean zoomEnabled = true;
	
	public int openAnimTime = 0;
	
	public Config(UZModuleContext uzContext, UZWidgetInfo widgetInfo){
		
		JSONArray imagesArray = uzContext.optJSONArray("images");
		if(imagesArray != null){
			for(int i=0; i<imagesArray.length(); i++){
				String imagePath = imagesArray.optString(i);
				if(imagePath.startsWith("http")){
					imagePaths.add(imagePath);
				} else if (imagePath.startsWith("/") || imagePath.startsWith("fs") || imagePath.startsWith("widget")) {
					
					imagePaths.add(UZUtility.makeRealPath(imagePath, widgetInfo));
				}else {
					imagePaths.add(imagePath);
				}
			}
		}
		
		activeIndex = uzContext.optInt("activeIndex");
		String placeholdImgPath = uzContext.optString("placeholderImg");
		placeholdImg = UZUtility.makeRealPath(placeholdImgPath, widgetInfo);
		
		if(!uzContext.isNull("zoomEnabled")){
			zoomEnabled = uzContext.optBoolean("zoomEnabled");
		}
		
		bgColor = UZUtility.parseCssColor(uzContext.optString("bgColor"));
		
		openAnimTime = (int) (uzContext.optDouble("atime",0)*1000);
	}
	
}
