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
import com.teleca.jamendo.api.Track;

/**
 * This database is stored on an external sd-card, more specifically, each sd-card
 * that has been used for downloading has it under /jamendroid/downloads.db
 * <br><br>
 * Database consists of one table LIBRARY, which basically contains all data about 
 * the track plus info whether it has already been downloaded or not
 * <br><br>
 * STATUS represents download progress of a track, track records
 * are kept in the LIBRARY table
 * 
 * @author Lukasz Wisniewski
 */
public interface DownloadDatabase {
	
	/**
	 * Adds a track to the playlist entry
	 * 
	 * @param entry
	 * @return true if an entry already exists in the database
	 */
	public boolean addToLibrary(PlaylistEntry entry);
	
	/**
	 * Sets status of the track
	 * 
	 * @param entry
	 * @param downloaded
	 */
	public void setStatus(PlaylistEntry entry, boolean downloaded); 
	
	/**
	 * Checks if tracks is available
	 * 
	 * @param track
	 */
	public boolean trackAvailable(Track track);

	/**
	 * Pulls all Download Jobs from the database.
	 * @return
	 */
	public ArrayList<DownloadJob> getAllDownloadJobs();

	/**
	 * Removes the passed job from the database
	 * @param job
	 */
	public void remove(DownloadJob job);
	
}
