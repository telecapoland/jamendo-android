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

package com.teleca.jamendo.media;

import com.teleca.jamendo.api.PlaylistEntry;


/**
 * Listener to PlayerEngine events
 * 
 * @author Lukasz Wisniewski
 */
public interface PlayerEngineListener {
	
	/**
	 * Callback invoked before starting a new track, return false to prevent
	 * playback from happening
	 * 
	 * @param playlistEntry
	 */
	public boolean onTrackStart(); 
	
	/**
	 * Callback invoked when a new track is played
	 * 
	 * @param playlistEntry
	 */
	public void onTrackChanged(PlaylistEntry playlistEntry); 
	
	/**
	 * Callback invoked periodically, contains info on track 
	 * playing progress
	 * 
	 * @param seconds int value
	 */
	public void onTrackProgress(int seconds);
	
	/**
	 * Callback invoked when buffering a track
	 * 
	 * @param percent
	 */
	public void onTrackBuffering(int percent);
	
	/**
	 * Callback invoked when track is stoped
	 */
	public void onTrackStop();
	
	/**
	 * Callback invoked when track is paused
	 */
	public void onTrackPause();
	
	/**
	 * Callback invoked when there was an error with
	 * streaming
	 */
	public void onTrackStreamError();

}
