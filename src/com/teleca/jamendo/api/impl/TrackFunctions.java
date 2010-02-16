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

package com.teleca.jamendo.api.impl;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.api.WSError;

/**
 * @author Lukasz Wisniewski
 */
public class TrackFunctions {
	
	public static Track[] getTracks(JSONArray jsonArrayTracks, boolean sort) throws JSONException {
		int n = jsonArrayTracks.length();
		Track[] tracks = new Track[n];
		TrackBuilder trackBuilder = new TrackBuilder();
		
		for(int i=0; i < n; i++){
			tracks[i] = trackBuilder.build(jsonArrayTracks.getJSONObject(i));
		}
		
		if(sort){
			// sort by track no
			Arrays.sort(tracks, new TrackComparator());
		}
		
		return tracks;
	}
	
	public static Playlist getPlaylist(JSONArray jsonArrayTracks) throws JSONException, WSError {
		int n = jsonArrayTracks.length();
		Playlist playlist = new Playlist();
		TrackBuilder trackBuilder = new TrackBuilder();
		Track[] tracks = new Track[n];
		int[] tracks_id = new int[n];
		
		
		// building tracks and getting tracks_id
		for(int i=0; i < n; i++){ 
			tracks[i] = trackBuilder.build(jsonArrayTracks.getJSONObject(i));
			tracks_id[i] = tracks[i].getId();
		}
		
		/*
		 * This could be done with one request if only artist_track relation did
		 * exist :'(
		 */
		Album[] albums = new JamendoGet2ApiImpl().getAlbumsByTracksId(tracks_id);
		
		// FIXME tracks id may repeat and webservice will trim results
		Log.i("jamendroid", ""+tracks.length+" tracks & "+albums.length+" albums");
		
		// adding everything to the playlist
		for(int i=0; i < n; i++){
			Log.i("jamendroid", tracks[i].getName() +" by "+albums[i].getArtistName());
			playlist.addTrack(tracks[i], albums[i]);
		}
		
		return playlist;
	}
	
	public static int[] getRadioPlaylist(JSONArray jsonArrayTracks) throws JSONException {
		int n = jsonArrayTracks.length();
		int[] tracks_id = new int[n];
		
		for(int i=0; i<n; i++){
			tracks_id[i] = jsonArrayTracks.getInt(i);
		}
		
		return tracks_id;
	}
	
}
