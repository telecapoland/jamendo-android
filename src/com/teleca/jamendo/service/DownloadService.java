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

package com.teleca.jamendo.service;

import java.util.ArrayList;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.activity.DownloadActivity;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.util.download.DownloadDatabase;
import com.teleca.jamendo.util.download.DownloadDatabaseImpl;
import com.teleca.jamendo.util.download.DownloadJob;
import com.teleca.jamendo.util.download.DownloadJobListener;
import com.teleca.jamendo.util.download.MediaScannerNotifier;
import com.teleca.jamendo.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

// TODO sd card listener
/**
 * Background download manager
 * 
 * @author Lukasz Wisniewski
 */
public class DownloadService extends Service {
	
	public static final String ACTION_ADD_TO_DOWNLOAD = "add_to_download";
	
	public static final String EXTRA_PLAYLIST_ENTRY = "playlist_entry";

	private static final int DOWNLOAD_NOTIFY_ID = 667668;
	
	private NotificationManager mNotificationManager = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();
		Log.i(JamendoApplication.TAG, "DownloadService.onCreate");
		
		// TODO check download database for any not downloaded things, add the to downloadQueue and start
		mNotificationManager = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		if(intent == null){
			return;
		}
		
		String action = intent.getAction();
		Log.i(JamendoApplication.TAG, "DownloadService.onStart - "+action);
		
		if(action.equals(ACTION_ADD_TO_DOWNLOAD)){
			PlaylistEntry entry = (PlaylistEntry) intent.getSerializableExtra(EXTRA_PLAYLIST_ENTRY);
			addToDownloadQueue(entry, startId);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(JamendoApplication.TAG, "DownloadService.onDestroy");
	}

	private DownloadJobListener mDownloadJobListener = new DownloadJobListener(){

		@Override
		public void downloadEnded(DownloadJob job) {
			getQueuedDownloads().remove(job);
			getCompletedDownloads().add(job);
			DownloadDatabase downloadDatabase = getDownloadDatabase();
			if(downloadDatabase != null){
				downloadDatabase.setStatus(job.getPlaylistEntry(), true);
			}
			displayNotifcation(job);
			new MediaScannerNotifier(DownloadService.this, job);
		}

		@Override
		public void downloadStarted() {
		}

	};
	
	private void displayNotifcation(DownloadJob job)
	{

		String notificationMessage = job.getPlaylistEntry().getTrack().getName() + " - " + job.getPlaylistEntry().getAlbum().getArtistName();

		Notification notification = new Notification(
				android.R.drawable.stat_sys_download_done, notificationMessage, System.currentTimeMillis() );

		PendingIntent contentIntent = PendingIntent.getActivity( this, 0,
				new Intent( this, DownloadActivity.class ), 0);

		notification.setLatestEventInfo( this, getString(R.string.downloaded),
				notificationMessage, contentIntent );
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		mNotificationManager.notify( DOWNLOAD_NOTIFY_ID, notification );
	}
	
	/**
	 * Interface to database on the remote storage device
	 */
	public static DownloadDatabase getDownloadDatabase(){
		return new DownloadDatabaseImpl(getDownloadPath()+"/jamendroid.db");
	}

	public static String getDownloadPath(){
		return Environment.getExternalStorageDirectory().getAbsolutePath()+"/music";
	}
	
	public void addToDownloadQueue(PlaylistEntry entry, int startId) {
		
		// check database if record already exists, if so abandon starting
		// another download process
		String downloadPath = getDownloadPath();
		
		String downloadFormat = JamendoApplication.getInstance().getDownloadFormat(); 
		
		DownloadJob downloadJob = new DownloadJob(entry, downloadPath, startId, downloadFormat);
		
		DownloadDatabase downloadDatabase = getDownloadDatabase();
		if(downloadDatabase != null){
			boolean existst = downloadDatabase.addToLibrary(downloadJob.getPlaylistEntry());
			if(existst)
				return;
		}
		
		downloadJob.setListener(mDownloadJobListener);	
		getQueuedDownloads().add(downloadJob);
		downloadJob.start();
	}
	
	public ArrayList<DownloadJob> getQueuedDownloads(){
		return JamendoApplication.getInstance().getQueuedDownloads();
	}
	
	public ArrayList<DownloadJob> getCompletedDownloads(){
		return JamendoApplication.getInstance().getCompletedDownloads();
	}

}
