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

import java.util.ArrayList;

import com.teleca.jamendo.api.PlaylistEntry;

/**
 * Interface to all download related actions.
 * 
 * @author Bartosz Cichosz
 * 
 */
public interface DownloadManager {

	/**
	 * Starts download of track passed in playlistEntry
	 * 
	 * @param playlistEntry
	 */
	public abstract void download(PlaylistEntry playlistEntry);

	/**
	 * Returns path to downloaded track, or null if there is no such file.
	 * 
	 * @param playlistEntry
	 * @return
	 */
	public abstract String getTrackPath(PlaylistEntry playlistEntry);

	/**
	 * Returns all download jobs.
	 * 
	 * @return
	 */
	public abstract ArrayList<DownloadJob> getAllDownloads();

	/**
	 * Returns completed download jobs.
	 * 
	 * @return
	 */
	public abstract ArrayList<DownloadJob> getCompletedDownloads();

	/**
	 * Returns queued download jobs.
	 * 
	 * @return
	 */
	public abstract ArrayList<DownloadJob> getQueuedDownloads();

	/**
	 * Returns download jobs provider.
	 * 
	 * @return
	 */
	public abstract DownloadProvider getProvider();

	/**
	 * Deletes the download job and related files.
	 * 
	 * @param job
	 */
	public abstract void deleteDownload(DownloadJob job);

	/**
	 * Adds passed object to the download observers list
	 * 
	 * @param observer
	 */
	public abstract void registerDownloadObserver(DownloadObserver observer);

	/**
	 * Removes passed object to the download observers list
	 * 
	 * @param observer
	 */
	public abstract void deregisterDownloadObserver(DownloadObserver observer);

	/**
	 * Notifies all observers that the state of the downloads has changed.
	 */
	public abstract void notifyObservers();

}
