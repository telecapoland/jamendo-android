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

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.service.DownloadService;

import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;

/**
 * Forces mp3 file scan on a downloaded file and adds it to the Android MusicPlayer's
 * library
 * 
 * @author Lukasz Wisniewski
 */
public class MediaScannerNotifier implements MediaScannerConnectionClient{
	
    private MediaScannerConnection mConnection;
    private DownloadJob mDownloadJob;
    private DownloadService mService;
    static private int mScannedFilesInProgress = 0;

	public MediaScannerNotifier(DownloadService service, DownloadJob job) {
		mDownloadJob = job;
		mService = service;
		mConnection = new MediaScannerConnection(mService, this);
		mConnection.connect();
	} 

	@Override
	public void onMediaScannerConnected() {
		//String path = mDownloadJob.getDestination();
		String path = DownloadHelper.getAbsolutePath(mDownloadJob.getPlaylistEntry(), mDownloadJob.getDestination());
		String fileName = DownloadHelper.getFileName(mDownloadJob.getPlaylistEntry(), mDownloadJob.getFormat());
		Log.i(JamendoApplication.TAG, "Adding to media library -> "+fileName);		
		if(mConnection.isConnected())
		{
			mConnection.scanFile(path+"/"+fileName, null);
			mScannedFilesInProgress++;			
		}
		

	}

	@Override
	public void onScanCompleted(String text, Uri uri) {
		Log.i(JamendoApplication.TAG, "Added to media library -> "+uri.toString());
		
		mScannedFilesInProgress--;
		mConnection.disconnect();
		
		// stop service if there is no downloads left
		if(mScannedFilesInProgress == 0){
			mService.notifyScanCompleted();
		}
	}

}
