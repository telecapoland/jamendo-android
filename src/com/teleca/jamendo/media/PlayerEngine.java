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

import com.teleca.jamendo.api.Playlist;

/**
 * 
 * Interface to the player engine
 * 
 * @author Lukasz Wisniewski
 */
public interface PlayerEngine {
	
	/**
	 * Opens playlist
	 * 
	 * @param playlist
	 */
	public void openPlaylist(Playlist playlist);
	
	/**
	 * Gets currently opened playlist
	 * 
	 * @return <code>Playlist</code> instance or <code>null</code>
	 */
	public Playlist getPlaylist();
	
	/**
	 * Start playing
	 */
	public void play();
	
	/**
	 * Checks whether player is playing or not 
	 * 
	 * @return boolean value
	 */
	public boolean isPlaying();
	
	/**
	 * Stop playing
	 */
	public void stop();
	
	/**
	 * Pause playing
	 */
	public void pause();
	
	/**
	 * Jump to the next song on the playlist
	 */
	public void next();
	
	/**
	 * Jump to the previous song on the playlist
	 */
	public void prev();
	
	/**
	 * Skip to the track on the playlist
	 * 
	 * @param index Track number on the playlist
	 */
	public void skipTo(int index);
	
	/**
	 * Set events listener
	 * 
	 * @param playerEngineListener
	 */
	public void setListener(PlayerEngineListener playerEngineListener);
}
