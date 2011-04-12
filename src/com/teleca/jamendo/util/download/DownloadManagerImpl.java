/*
 * Copyright (C) 2011 Teleca Poland Sp. z o.o. <android@teleca.com>
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
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.service.DownloadService;

/**
 * DownloadManager implementation. Using DownloadProviderDbImpl as DownloadJobs
 * provider.
 * 
 * @author Bartosz Cichosz
 * 
 */
public class DownloadManagerImpl implements DownloadManager {

	private Context mContext;
	private DownloadProvider mProvider;
	private ArrayList<DownloadObserver> mObservers;

	public DownloadManagerImpl(Context context) {
		mContext = context;
		mObservers = new ArrayList<DownloadObserver>();
		mProvider = new DownloadProviderDbImpl(this);
	}

	public void download(PlaylistEntry playlistEntry) {
		Intent intent = new Intent(mContext, DownloadService.class);
		intent.setAction(DownloadService.ACTION_ADD_TO_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_PLAYLIST_ENTRY, playlistEntry);
		mContext.startService(intent);
	}

	@Override
	public String getTrackPath(PlaylistEntry playlistEntry) {
		if (playlistEntry == null) {
			return null;
		}

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			// we need to check the database to be sure whether file was
			// downloaded completely
			if (!mProvider.trackAvailable(playlistEntry.getTrack()))
				return null;
		}

		// now we may give a reference to this file after we check it really
		// exists
		// in case file was somehow removed manually
		String trackPath = DownloadHelper.getAbsolutePath(playlistEntry,
				DownloadHelper.getDownloadPath());
		String fileNameMP3 = DownloadHelper.getFileName(playlistEntry,
				JamendoGet2Api.ENCODING_MP3);
		File fileMP3 = new File(trackPath, fileNameMP3);
		if (fileMP3.exists()) {
			String path = fileMP3.getAbsolutePath();
			Log
					.i(JamendoApplication.TAG, "Playing from local file: "
							+ fileMP3);
			return path;
		}
		String fileNameOGG = DownloadHelper.getFileName(playlistEntry,
				JamendoGet2Api.ENCODING_OGG);
		File fileOGG = new File(trackPath, fileNameOGG);
		if (fileOGG.exists()) {
			String path = fileOGG.getAbsolutePath();
			Log
					.i(JamendoApplication.TAG, "Playing from local file: "
							+ fileOGG);
			return path;
		}
		return null;
	}

	@Override
	public ArrayList<DownloadJob> getAllDownloads() {
		return mProvider.getAllDownloads();
	}

	@Override
	public ArrayList<DownloadJob> getCompletedDownloads() {
		return mProvider.getCompletedDownloads();
	}

	@Override
	public ArrayList<DownloadJob> getQueuedDownloads() {
		return mProvider.getQueuedDownloads();
	}

	@Override
	public DownloadProvider getProvider() {
		return mProvider;
	}

	@Override
	public void deleteDownload(DownloadJob job) {
		mProvider.removeDownload(job);
		removeDownloadFromDisk(job);
	}

	private void removeDownloadFromDisk(DownloadJob job) {
		PlaylistEntry playlistEntry = job.getPlaylistEntry();

		String trackPath = DownloadHelper.getAbsolutePath(playlistEntry,
				DownloadHelper.getDownloadPath());
		String fileNameMP3 = DownloadHelper.getFileName(playlistEntry,
				JamendoGet2Api.ENCODING_MP3);
		File fileMP3 = new File(trackPath, fileNameMP3);
		if (fileMP3.exists()) {
			fileMP3.delete();
		}
		String fileNameOGG = DownloadHelper.getFileName(playlistEntry,
				JamendoGet2Api.ENCODING_OGG);
		File fileOGG = new File(trackPath, fileNameOGG);
		if (fileOGG.exists()) {
			fileOGG.delete();
		}
	}

	@Override
	public synchronized void deregisterDownloadObserver(
			DownloadObserver observer) {
		mObservers.remove(observer);
	}

	@Override
	public synchronized void registerDownloadObserver(DownloadObserver observer) {
		mObservers.add(observer);
	}

	@Override
	public synchronized void notifyObservers() {
		for (DownloadObserver observer : mObservers) {
			observer.onDownloadChanged(this);
		}
	}
}
