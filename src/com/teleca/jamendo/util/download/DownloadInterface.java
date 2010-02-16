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

import java.util.ArrayList;

import com.teleca.jamendo.api.PlaylistEntry;


/**
 * @author Lukasz Wisniewski
 */
public interface DownloadInterface {
	
	/**
	 * Add an entry to the download queue
	 * 
	 * @param entry
	 */
	public void addToDownloadQueue(PlaylistEntry entry);
	
	/**
	 * Returns tracks path (if is available locally, on the SD Card)
	 * 
	 * @param entry
	 * @return
	 */
	public String getTrackPath(PlaylistEntry entry);
	
	/**
	 * Returns complete and queued downloads
	 * 
	 * @return
	 */
	public ArrayList<DownloadJob> getAllDownloads();
	
	/**
	 * Returns queued downloads
	 * 
	 * @return
	 */
	public ArrayList<DownloadJob> getQueuedDownloads();
	
	/**
	 * Returns completed downloads
	 * 
	 * @return
	 */
	public ArrayList<DownloadJob> getCompletedDownloads();

}
