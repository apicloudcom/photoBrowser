package com.uzmap.pkg.uzmodules.photoBrowser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

import android.os.AsyncTask;
import android.text.TextUtils;

public class ImageDownLoader extends AsyncTask<String, Void, String>{
	
	public static String mCachePath;
	
	public interface DownLoadListener{
		void onCancel();
		void onStart();
		void onFinish(String savePath);
		void onFailed();
	}
	
	private DownLoadListener mDownLoadListener;
	
	public ImageDownLoader(DownLoadListener listener){
		this.mDownLoadListener = listener;
	}
	
	
	@Override
	protected void onCancelled() {
		if(this.mDownLoadListener != null){
			mDownLoadListener.onCancel();
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if(this.mDownLoadListener != null){
			mDownLoadListener.onFinish(result);
		}
	}

	@Override
	protected void onPreExecute() {
		if(this.mDownLoadListener != null){
			mDownLoadListener.onStart();
		}
	}

	@Override
	protected String doInBackground(String... arg0) {
		
		if(arg0 == null || arg0.length <= 0){
			return null;
		}
		return getDataFromNet(arg0[0]);
	}
	
	public String getDataFromNet(String netPath){
		if(TextUtils.isEmpty(netPath)){
			return null;
		}
		
		File tmpFile = new File(mCachePath + "/" + md5(netPath));
		if(tmpFile.exists()){
			return tmpFile.getAbsolutePath();
		}
		
		URL url = null;
		try {
			url = new URL(netPath);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			if(mDownLoadListener != null){
				mDownLoadListener.onFailed();
			}
			return null;
		}
		
		if(netPath.startsWith("https")){
			HttpsURLConnection urlConnection;
			try {
				urlConnection = (HttpsURLConnection)url.openConnection();
				InputStream inputStream = urlConnection.getInputStream();
				return saveFile(inputStream, netPath);
				
			} catch (IOException e) {
				e.printStackTrace();
				if(mDownLoadListener != null){
					mDownLoadListener.onFailed();
				}
				return null;
			}
		} else {
			HttpURLConnection urlConnection;
			try {
				urlConnection = (HttpURLConnection)url.openConnection();
				InputStream inputStream = urlConnection.getInputStream();
				return saveFile(inputStream, netPath);
			} catch (IOException e) {
				e.printStackTrace();
				if(mDownLoadListener != null){
					mDownLoadListener.onFailed();
				}
				return null;
			}
		}
	}
	
	public String saveFile(InputStream inputStream, String url){
		byte[] buf = new byte[2048];
		
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(mCachePath + "/" + md5(url));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			if(mDownLoadListener != null){
				mDownLoadListener.onFailed();
			}
			return null;
		}
		
		int count = -1;
		try {
			while((count = inputStream.read(buf)) > 0){
				outputStream.write(buf, 0, count);
			}
			return mCachePath + "/" + md5(url);
		} catch (IOException e) {
			e.printStackTrace();
			
			File tmpFile = new File(mCachePath + "/" + md5(url));
			if(tmpFile.exists()){
				tmpFile.delete();
			}
			return null;
		} finally {
			if(outputStream != null){
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
	
}
