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

package com.teleca.jamendo.util.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;


/**
 * File download thread
 * 
 * @author Lukasz Wisniewski
 */
public class DownloadTask extends AsyncTask<Void, Integer, Boolean>{

	DownloadJob mJob;
	
	public DownloadTask(DownloadJob job){
		mJob = job;
	}
	
	@Override
	public void onPreExecute() {
		mJob.notifyDownloadStarted();
		super.onPreExecute();
	}

	@Override
	public Boolean doInBackground(Void... params) {
		// ogg support
		if(mJob.getFormat().equals(JamendoGet2Api.ENCODING_OGG)){
			Log.i(JamendoApplication.TAG, "Getting path for ogg");
			int track_id = mJob.getPlaylistEntry().getTrack().getId();
			JamendoGet2Api api = new JamendoGet2ApiImpl();
			try {
				Track track[] = api.getTracksByTracksId(new int[]{track_id}, mJob.getFormat());
				if(track == null || track.length != 1){
					return false;
				} else {
					mJob.getPlaylistEntry().setTrack(track[0]);
				}
			} catch (JSONException e) {
				return false;
			} catch (WSError e) {
				return false;
			}
		}
		
		try {
			return downloadFile(mJob);
		} catch (IOException e) {
			Log.e(JamendoApplication.TAG, "Download file faild reason-> " + e.getMessage());
			return false;
		}
	}

	@Override
	public void onPostExecute(Boolean result) {
		mJob.notifyDownloadEnded();
		super.onPostExecute(result);
	}

	public static Boolean downloadFile(DownloadJob job) throws IOException{
		
		// TODO rewrite to apache client
		
		PlaylistEntry mPlaylistEntry = job.getPlaylistEntry();
		String mDestination = job.getDestination();
		
		URL u = new URL(mPlaylistEntry.getTrack().getStream());
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.setRequestMethod("GET");
		c.setDoOutput(true);
		c.connect();
		job.setTotalSize(c.getContentLength());

		Log.i(JamendoApplication.TAG, "creating file");
		
		String path = DownloadHelper.getAbsolutePath(mPlaylistEntry, mDestination);
		String fileName = DownloadHelper.getFileName(mPlaylistEntry, job.getFormat());


		try{
			// Create multiple directory
			boolean success = (new File(path)).mkdirs();
			if (success) {
				Log.i(JamendoApplication.TAG, "Directory: " + path + " created");
			}    

		}catch (Exception e){//Catch exception if any
			Log.e(JamendoApplication.TAG, "Error creating folder", e);
			return false;
		}

		FileOutputStream f = new FileOutputStream(new File(path, fileName));


		InputStream in = c.getInputStream();

		if(in == null){
			// When InputStream is a NULL
			f.close();
			return false;
		}

		byte[] buffer = new byte[1024];
		int lenght = 0;
		while ( (lenght = in.read(buffer)) > 0 ) {
			f.write(buffer,0, lenght);
			job.setDownloadedSize(job.getDownloadedSize()+lenght);
		}

		f.close();
		
		downloadCover(job);
		return true;
		
	}
	
	
	private static void downloadCover(DownloadJob job) {

		PlaylistEntry mPlaylistEntry = job.getPlaylistEntry();
		String mDestination = job.getDestination();
		String path = DownloadHelper.getAbsolutePath(mPlaylistEntry,
				mDestination);
		File file = new File(path + "/" + "cover.jpg");
		// check if cover already exists
		if (file.exists()) {
			Log.v(JamendoApplication.TAG, "File exists - nothing to do");
			return;
		}

		String albumUrl = mPlaylistEntry.getAlbum().getImage();
		if (albumUrl == null) {
			Log.w(JamendoApplication.TAG,
					"album Url = null. This should not happened");
			return;
		}
		albumUrl = albumUrl.replace("1.100", "1.500");

		InputStream stream = null;
		URL imageUrl;
		Bitmap bmp = null;

		// download cover
		try {
			imageUrl = new URL(albumUrl);

			try {
				stream = imageUrl.openStream();
				bmp = BitmapFactory.decodeStream(stream);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.v(JamendoApplication.TAG, "download Cover IOException");
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			Log.v(JamendoApplication.TAG, "download CoverMalformedURLException");
			e.printStackTrace();
		}

		// save cover to album directory
		if (bmp != null) {

			try {
				file.createNewFile();
				OutputStream outStream = new FileOutputStream(file);
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
				outStream.flush();
				outStream.close();

				Log.v(JamendoApplication.TAG, "Album cover saved to sd");

			} catch (FileNotFoundException e) {
				Log.w(JamendoApplication.TAG, "FileNotFoundException");

			} catch (IOException e) {
				Log.w(JamendoApplication.TAG, "IOException");
			}

		}
	}

}
