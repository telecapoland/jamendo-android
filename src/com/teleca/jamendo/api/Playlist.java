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

package com.teleca.jamendo.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.util.Log;

/**
 * @author Lukasz Wisniewski
 */
public class Playlist implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String TAG = "Playlist";

	public enum PlaylistPlaybackMode {
		NORMAL, SHUFFLE, REPEAT, SHUFFLE_AND_REPEAT
	}

	/**
	 * Keep order in which tracks will be play
	 */
	private ArrayList<Integer> mPlayOrder = new ArrayList<Integer>();

	/**
	 * Keep playlist playback mode
	 */
	private PlaylistPlaybackMode mPlaylistPlaybackMode = PlaylistPlaybackMode.NORMAL;

	/**
	 * Give playlist playback mode
	 * 
	 * @return enum with playback mode
	 */
	public PlaylistPlaybackMode getPlaylistPlaybackMode() {
		return mPlaylistPlaybackMode;
	}

	/**
	 * Set playlist playback mode
	 * 
	 * @param aPlaylistPlaybackMode
	 */
	public void setPlaylistPlaybackMode(
			PlaylistPlaybackMode aPlaylistPlaybackMode) {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "(Set mode) selected = " + selected);
			Log.d(TAG, "Plyback mode set on: " + aPlaylistPlaybackMode);
		}
		boolean force = false;
		switch (aPlaylistPlaybackMode) {
		case NORMAL:
		case REPEAT:
			if (mPlaylistPlaybackMode == PlaylistPlaybackMode.SHUFFLE
					|| mPlaylistPlaybackMode == PlaylistPlaybackMode.SHUFFLE_AND_REPEAT) {
				force = true;
			}
			break;
		case SHUFFLE:
		case SHUFFLE_AND_REPEAT:
			if (mPlaylistPlaybackMode == PlaylistPlaybackMode.NORMAL
					|| mPlaylistPlaybackMode == PlaylistPlaybackMode.REPEAT) {
				force = true;
			}
			break;
		}
		mPlaylistPlaybackMode = aPlaylistPlaybackMode;
		calculateOrder(force);
	}

	/**
	 * Keeps playlist's entries
	 */
	protected ArrayList<PlaylistEntry> playlist = null;

	/**
	 * Keeps record of currently selected track
	 */
	protected int selected = -1;

	public Playlist() {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "Playlist constructor start");
		}
		playlist = new ArrayList<PlaylistEntry>();
		calculateOrder(true);
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "Playlist constructor stop");
		}
	}

	/**
	 * Add single track to the playlist
	 * 
	 * @param track
	 *            <code>Track</code> instance
	 * @param album
	 *            <code>Album</code> instance
	 */
	public void addTrack(Track track, Album album) {
		PlaylistEntry playlistEntry = new PlaylistEntry();
		playlistEntry.setAlbum(album);
		playlistEntry.setTrack(track);

		playlist.add(playlistEntry);
		mPlayOrder.add(size() - 1);
	}

	/**
	 * Add multiple tracks from one album to the playlist
	 * 
	 * @param album
	 *            <code>Album</code> instance with loaded tracks
	 */
	public void addTracks(Album album) {
		for (Track track : album.getTracks()) {
			addTrack(track, album);
		}
	}

	/**
	 * Checks if the playlist is empty
	 * 
	 * @return boolean value
	 */
	public boolean isEmpty() {
		return playlist.size() == 0;
	}

	/**
	 * Selects next song from the playlist
	 */
	public void selectNext() {
		if (!isEmpty()) {
			selected++;
			selected %= playlist.size();
			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d("TAG", "Current (next) selected = " + selected);
			}
		}
	}

	/**
	 * Selects previous song from the playlist
	 */
	public void selectPrev() {
		if (!isEmpty()) {
			selected--;
			if (selected < 0)
				selected = playlist.size() - 1;
		}
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d("TAG", "Current (prev) selected = " + selected);
		}
	}

	/**
	 * Select song with a given index
	 * 
	 * @param index
	 */
	public void select(int index) {
		if (!isEmpty()) {
			if (index >= 0 && index < playlist.size())
				selected = mPlayOrder.indexOf(index);
		}
	}

	public void selectOrAdd(Track track, Album album) {

		// first search thru available tracks
		for (int i = 0; i < playlist.size(); i++) {
			if (playlist.get(i).getTrack().getId() == track.getId()) {
				select(i);
				return;
			}
		}

		// add track if necessary
		addTrack(track, album);
		select(playlist.size() - 1);
	}

	/**
	 * Return index of the currently selected song
	 * 
	 * @return int value (-1 if the playlist is empty)
	 */
	public int getSelectedIndex() {
		if (isEmpty()) {
			selected = -1;
		}
		if (selected == -1 && !isEmpty()) {
			selected = 0;
		}
		return selected;
	}

	/**
	 * Return currently selected song
	 * 
	 * @return <code>PlaylistEntry</code> instance
	 */
	public PlaylistEntry getSelectedTrack() {
		PlaylistEntry playlistEntry = null;

		int index = getSelectedIndex();
		if (index == -1) {
			return null;
		}
		index = mPlayOrder.get(index);
		if (index == -1) {
			return null;
		}
		playlistEntry = playlist.get(index);		

		return playlistEntry;

	}

	/**
	 * Adds PlaylistEntry object to the playlist
	 * 
	 * @param playlistEntry
	 */
	public void addPlaylistEntry(PlaylistEntry playlistEntry) {
		if (playlistEntry != null) {
			playlist.add(playlistEntry);
			mPlayOrder.add(size() - 1);
		}
	}

	/**
	 * Count of playlist entries
	 * 
	 * @return
	 */
	public int size() {
		return playlist == null ? 0 : playlist.size();
	}

	/**
	 * Given track index getter
	 * 
	 * @param index
	 * @return
	 */
	public PlaylistEntry getTrack(int index) {
		return playlist.get(index);
	}

	/**
	 * Give all entrys in playlist
	 * 
	 * @return
	 */
	public PlaylistEntry[] getAllTracks() {
		PlaylistEntry[] out = new PlaylistEntry[playlist.size()];
		playlist.toArray(out);
		return out;
	}

	/**
	 * Remove a track with a given index from the playlist
	 * 
	 * @param position
	 */
	public void remove(int position) {
		if (playlist != null && position < playlist.size() && position >= 0) {

			if (selected >= position) {
				selected--;
			}

			playlist.remove(position);
			mPlayOrder.remove(position);
		}
	}

	/**
	 * Change order playback list when it is needed
	 * 
	 * @param force
	 */
	private void calculateOrder(boolean force) {
		if (mPlayOrder.isEmpty() || force) {
			int oldSelected = 0;

			if (!mPlayOrder.isEmpty()) {
				oldSelected = mPlayOrder.get(selected);
				mPlayOrder.clear();
			}

			for (int i = 0; i < size(); i++) {
				mPlayOrder.add(i, i);
			}

			if (mPlaylistPlaybackMode == null) {
				mPlaylistPlaybackMode = PlaylistPlaybackMode.NORMAL;
			}

			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, "Playlist has been maped in "
						+ mPlaylistPlaybackMode + " mode.");
			}

			switch (mPlaylistPlaybackMode) {
			case NORMAL:
			case REPEAT:
				selected = oldSelected;
				break;
			case SHUFFLE:
			case SHUFFLE_AND_REPEAT:
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, "Before shuffle: "
							+ Arrays.toString(mPlayOrder.toArray()));
				}
				Collections.shuffle(mPlayOrder);
				selected = mPlayOrder.indexOf(selected);
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, "After shuffle: "
							+ Arrays.toString(mPlayOrder.toArray()));
				}
				break;
			}
		}
	}

	/**
	 * Inform weather it is last track on playlist
	 * 
	 * @return
	 */
	public boolean isLastTrackOnList() {
		if (selected == size() - 1)
			return true;
		else
			return false;
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		//This method is used when playlist is deserializable form DB
		in.defaultReadObject();
		if(mPlayOrder == null){
			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, "mPlayOrder is NULL");
			}
			mPlayOrder = new ArrayList<Integer>();
			calculateOrder(true);
		}
	}
}
