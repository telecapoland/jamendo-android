/*
 * Copyright (C) 2009 Teleca Poland Sp. z o.o. <android@teleca.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teleca.jamendo.widget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.util.ImageCache;

/**
 * ImageView extended class allowing easy downloading
 * of remote images
 * 
 * @author Lukasz Wisniewski
 */
public class RemoteImageView extends ImageView{
	
	/**
	 * Maximum number of unsuccesful tries of downloading an image
	 */
	private static int MAX_FAILURES = 3;

	public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	public RemoteImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public RemoteImageView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	/**
	 * Sharable code between constructors
	 */
	private void init(){
		mTimeDiff = DAYS_OF_CACHE*24*60*60*1000L;	
	}
	
	/**
	 * Remote image location
	 */
	private String mUrl;
	
	/**
	 * Currently successfully grabbed url
	 */
	private String mCurrentlyGrabbedUrl;
	
	/**
	 * Remote image download failure counter
	 */
	private int mFailure;

	/**
	 * Position of the image in the mListView
	 */
	private int mPosition;

	/**
	 * ListView containg this image
	 */
	private ListView mListView;
	
	/**
	 * Default image shown while loading or on url not found
	 */
	private Integer mDefaultImage;

	private long mTimeDiff;
	
	//init value diffrent that possible values of mCacheSize
	private static int mPrevCacheSize= 1;
	private static int mCacheSize= 150;
	
	private Context mContext;
	
	private final static String ALBUMS= "albums";
	private final static String ALBUMS_CONV= "bgc";
	private final static String RADIOS= "radios";
	private final static String RADIOS_CONV= "sbf";
	private final static String COVERS= "covers";
	private final static String COVERS_CONV= "dpw";
	private final static String ALBUM_COVER_MARKER= "1.500";
	private final static String JAMENDO_DIR= "Android/data/com.teleca.jamendo";
	private final static int MB = 1048576;	
	//every DAYS_OF_CACHE the radio and album thumbnails jpegs are deleted
	private final static int DAYS_OF_CACHE= 45;
	//minimum free space on sd card to enable cache
	private final static int FREE_SD_SPACE_NEEDED_TO_CACHE= 10;
	
	

	/**
	 * Loads image from remote location
	 * 
	 * @param url eg. http://random.com/abz.jpg
	 */
	public void setImageUrl(String url){
		
		
		if (mUrl != null && mUrl.equals(url) && (mCurrentlyGrabbedUrl == null ||
				(mCurrentlyGrabbedUrl != null && !mCurrentlyGrabbedUrl.equals(url)))) {
			mFailure++;			
			if(mFailure > MAX_FAILURES){
				Log.e(JamendoApplication.TAG, "Failed to download "+url+", falling back to default image");
				loadDefaultImage();
				return;
			}
		} else {
			mUrl = url;
			mFailure = 0;
		}

		updateCacheSize();
		
		if (mCacheSize>0 && (url.contains(ALBUMS) || url.contains(RADIOS))) {			
			String fileName = convertUrlToFileName(url);
			String dir = getDirectory(fileName);
			String pathFileName = dir + "/" + fileName;						
			Bitmap tbmp = BitmapFactory.decodeFile(pathFileName);
			if (tbmp == null) {
				Log.d(JamendoApplication.TAG, "Image is not present, try to download");
				try{
					new DownloadTask().execute(url);
				} catch (RejectedExecutionException e) {
					// do nothing, just don't crash
				}
			} else {
				Log.i(JamendoApplication.TAG, "Loading album cover from file");
				this.setImageBitmap(tbmp);
				updateFileTime(dir,fileName );				
			}
			removeAlbumCoversCache(dir, fileName);
			removeRadioCoversCache(dir, fileName);
			
		}
		else {
			Log.i(JamendoApplication.TAG, "File not cached supported" + url);
			ImageCache imageCache = JamendoApplication.getInstance()
					.getImageCache();
			if (imageCache.isCached(url)) {				
				this.setImageBitmap(imageCache.get(url));
			} else {
				try {
					Log.i(JamendoApplication.TAG, "Image is not present, try to download");
					new DownloadTask().execute(url);
				} catch (RejectedExecutionException e) {
					// do nothing, just don't crash
				}
			}
		}
	}
	
	/**
	 * Sets default local image shown when remote one is unavailable
	 * 
	 * @param resid
	 */
	public void setDefaultImage(Integer resid){
		mDefaultImage = resid;
	}
	
	/**
	 * Loads default image
	 */
	private void loadDefaultImage(){
		if(mDefaultImage != null)
			setImageResource(mDefaultImage);
	}
	
	/**
	 * Loads image from remote location in the ListView
	 * 
	 * @param url eg. http://random.com/abz.jpg
	 * @param position ListView position where the image is nested
	 * @param listView ListView to which this image belongs
	 */
	public void setImageUrl(String url, int position, ListView listView){
		mPosition = position;
		mListView = listView;
		setImageUrl(url);
	}

	/**
	 * Asynchronous image download task
	 * 
	 * @author Lukasz Wisniewski
	 */
	class DownloadTask extends AsyncTask<String, Void, String>{
		
		private String mTaskUrl;
		private Bitmap mBmp = null;

		@Override
		public void onPreExecute() {
			loadDefaultImage();
			super.onPreExecute();
		}

		@Override
		public String doInBackground(String... params) {

			mTaskUrl = params[0];
			InputStream stream = null;
			URL imageUrl;
			Bitmap bmp = null;

			try {
				imageUrl = new URL(mTaskUrl);
				try {
					stream = imageUrl.openStream();
					bmp = BitmapFactory.decodeStream(stream);
					try {
						if(bmp != null){
								mBmp = bmp;
								JamendoApplication.getInstance().getImageCache().put(mTaskUrl, bmp);
								Log.d(JamendoApplication.TAG, "Image cached "+mTaskUrl);
							
						} else {
							Log.w(JamendoApplication.TAG, "Failed to cache "+mTaskUrl);
						}
					} catch (NullPointerException e) {
						Log.w(JamendoApplication.TAG, "Failed to cache "+mTaskUrl);
					}
				} catch (IOException e) {
					Log.w(JamendoApplication.TAG, "Couldn't load bitmap from url: " + mTaskUrl);
				} finally {
					try {
						if(stream != null){
							stream.close();
						}
					} catch (IOException e) {}
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return mTaskUrl;
		}

		@Override
		public void onPostExecute(String url) {
			super.onPostExecute(url);
			
			// target url may change while loading
			if(!mTaskUrl.equals(mUrl))			{
				
				if(url.contains(ALBUMS) || url.contains(RADIOS) ){
					saveBmpToSd(mBmp, url);					
				}
				return;
			}
			
			
			Bitmap bmp = JamendoApplication.getInstance().getImageCache().get(url);
			if(bmp == null){
				Log.w(JamendoApplication.TAG, "Trying again to download " + url);
				RemoteImageView.this.setImageUrl(url);
			} else {
				
				// if image belongs to a list update it only if it's visible
				if(mListView != null)
					if(mPosition < mListView.getFirstVisiblePosition() || mPosition > mListView.getLastVisiblePosition())
						return;
				
						
				RemoteImageView.this.setImageBitmap(bmp);				
				mCurrentlyGrabbedUrl = url;				
				if(url.contains(ALBUMS) || url.contains(RADIOS) ){				
					saveBmpToSd(mBmp, url);
				}				
			}
		}

	};
	
	private void saveBmpToSd(Bitmap bm, String url) {
		
		if (bm == null) {			
			return;
		}
		
		if (mCacheSize == 0){			
			return;
		}

		if (FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
			Log.w(JamendoApplication.TAG, "Low free space on sd, do not cache");
			return;
		}
		String filename = convertUrlToFileName(url);
		String dir = getDirectory(filename);

		File file = new File(dir + "/" + filename);

		try {
			file.createNewFile();
			OutputStream outStream = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.flush();
			outStream.close();

			Log.i(JamendoApplication.TAG, "Image saved to sd");

		} catch (FileNotFoundException e) {
			Log.w(JamendoApplication.TAG, "FileNotFoundException");

		} catch (IOException e) {
			Log.w(JamendoApplication.TAG, "IOException");
		}

	}

	private String convertUrlToFileName(String url) {
		String filename = url;
		filename = filename.replace("http://imgjam.com/", "");
		filename = filename.replace("/", ".");
		filename = filename.replace("jpg", "dat");

		// do filename complicated, hard to read by user while using sd
		if (filename.contains(ALBUMS)) {
			filename = filename.replace(ALBUMS, ALBUMS_CONV);
		}
		if (filename.contains(COVERS)) {
			filename = filename.replace(COVERS, COVERS_CONV);
		}
		if (filename.contains(RADIOS)) {
			filename = filename.replace(RADIOS, RADIOS_CONV);
		}
		
		return filename;
	}

	private String getDirectory(String filename) {

		String extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();

		String dirPath = extStorageDirectory + "/" + JAMENDO_DIR;
		File dirFile = new File(dirPath);
		dirFile.mkdirs();

		dirPath = dirPath + "/dat0";
		dirFile = new File(dirPath);
		dirFile.mkdir();

		return dirPath;
	}

	private void updateFileTime(String dir, String fileName) {
		// update time of album large covers		
		if (!fileName.contains(ALBUM_COVER_MARKER)) {
			return;
		}
		File file = new File(dir, fileName);		
		long newModifiedTime = System.currentTimeMillis();
		file.setLastModified(newModifiedTime);
		
	}

	private void removeAlbumCoversCache(String dirPath, String filename) {

		if (!filename.contains(ALBUM_COVER_MARKER)) {
			return;

		}

		File dir = new File(dirPath);
		File[] files = dir.listFiles();

		if (files == null) {
			// possible sd card is not present/cant write
			return;
		}

		int dirSize = 0;

		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().contains(ALBUM_COVER_MARKER)) {
				dirSize += files[i].length();
			}
		}

		
		
		if (dirSize > mCacheSize * MB || FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
			int removeFactor = (int) ((0.4 * files.length) + 1);
			Arrays.sort(files, new FileLastModifSort());
			Log.i(JamendoApplication.TAG, "Clear some album covers cache files ");
			for (int i = 0; i < removeFactor; i++) {
				if (files[i].getName().contains(ALBUM_COVER_MARKER)) {
					files[i].delete();				
				}
			}
		}

	}

	private void removeRadioCoversCache(String dirPath, String filename) {

		if (filename.contains(ALBUM_COVER_MARKER)) {
			return;
		}

		File file = new File(dirPath, filename);
		if (file.lastModified() != 0
				&& System.currentTimeMillis() - file.lastModified() > mTimeDiff) {
			
						
			Log.i(JamendoApplication.TAG, "Clear some album or radio thumbnail cache files ");
			file.delete();
		}

	}

	private void clearCache() {
		
		String extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();

		String dirPath = extStorageDirectory + "/" + JAMENDO_DIR + "/dat0";
		File dir = new File(dirPath);
		File[] files = dir.listFiles();

		if (files == null) {
			// possible that sd card is not present/can't write
			return;
		}

		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
		
	}

	private int freeSpaceOnSd() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
				.getBlockSize()) / MB;

		return (int) sdFreeMB;
	}
	
	public void updateCacheSize() {
		
		mPrevCacheSize = mCacheSize;		
		mCacheSize = Integer.parseInt(PreferenceManager
				.getDefaultSharedPreferences(mContext).getString(
						"cache_option", "100"));
		
		if(mPrevCacheSize!= 0 && mCacheSize == 0 ){
		//do it only once after changing mCacheSize value to 0
			clearCache();
		}

	}

}


class FileLastModifSort implements Comparator<File>{

	public int compare(File arg0, File arg1) {
		if (arg0.lastModified() > arg1.lastModified()) {
			return 1;
		} else if (arg0.lastModified() == arg1.lastModified()) {
			return 0;
		} else {
			return -1;
		}

	}

}
