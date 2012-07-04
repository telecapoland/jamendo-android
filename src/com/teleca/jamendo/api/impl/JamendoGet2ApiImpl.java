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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.Artist;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.License;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.PlaylistRemote;
import com.teleca.jamendo.api.Radio;
import com.teleca.jamendo.api.Review;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.util.Caller;

/**
 * Jamendo Get2 API implementation, Apache HTTP Client used for web requests
 * 
 * @author Lukasz Wisniewski
 * @author Marcin Gil
 */
public class JamendoGet2ApiImpl implements JamendoGet2Api {
	
	private static String GET_API = "http://api.jamendo.com/get2/";
	private static final String TAG = "JamendoGet2ApiImpl";
	private static final int TRACKS_PER_PAGE = 10;

	private String doGet(String query) throws WSError{
		return Caller.doGet(GET_API + query);
	}

	@Override
	public Album[] getPopularAlbumsWeek() throws JSONException, WSError {
		
		String jsonString = doGet("id+name+url+image+rating+artist_name/album/json/?n=20&order=ratingweek_desc");
		if (jsonString == null)
			return null;
		
		try {
			JSONArray jsonArrayAlbums = new JSONArray(jsonString); 
			return AlbumFunctions.getAlbums(jsonArrayAlbums);
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}
	
	@Override
	public Track[] getAlbumTracks(Album album, String encoding) throws JSONException, WSError {
		if (album == null || encoding == null) {
			return null;
		}
		
		if (album.getTracks() != null)
			return album.getTracks();
		
		Track[] tracks = null;
		ArrayList<Track> allTracks = new ArrayList<Track>();
		
		int currentPage = 1;
		
		while ((tracks = getAlbumTracks(album, encoding, TRACKS_PER_PAGE, currentPage++)) != null) {
			allTracks.addAll(Arrays.asList(tracks));
		}
		
		return allTracks.toArray(new Track[0]);
	}
	
	@Override
	public Track[] getAlbumTracks(Album album, String encoding, int count, int page) throws JSONException, WSError {
		try {
			String pagination = "&n=all";
			if (count != 0 && page != 0) {
				pagination = "&n=" + count + "&pn=" + page;
			}
			String jsonString = doGet("numalbum+id+name+duration+rating+url+stream/track/json/?album_id=" + album.getId() + "&streamencoding=" + encoding + pagination);
			JSONArray jsonArrayTracks = new JSONArray(jsonString);
			
			return getTracks(jsonArrayTracks, true);
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}

	@Override
	public Album[] searchForAlbumsByArtist(String artistName) throws JSONException, WSError {
		
		try {
			artistName = URLEncoder.encode(artistName, "UTF-8" );
			String jsonString = doGet("id+name+url+image+rating+artist_name/album/json/?order=ratingweek_desc&n=50&searchquery="+artistName);
			JSONArray jsonArrayAlbums = new JSONArray(jsonString); 
			return AlbumFunctions.getAlbums(jsonArrayAlbums);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}

	@Override
	public Album[] searchForAlbumsByTag(String tag) throws JSONException, WSError {
		try {
			tag = URLEncoder.encode(tag, "UTF-8" );
			String jsonString = doGet("id+name+url+image+rating+artist_name/album/json/?order=ratingweek_desc&tag_idstr="+tag+"&n=50");
			JSONArray jsonArrayAlbums = new JSONArray(jsonString); 
			return AlbumFunctions.getAlbums(jsonArrayAlbums);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}

	@Override
	public Album[] searchForAlbumsByArtistName(String artistName)
			throws JSONException, WSError {
		try {
			artistName = URLEncoder.encode(artistName, "UTF-8" );
			String jsonString = doGet("id+name+url+image+rating+artist_name/album/json/?order=ratingweek_desc&n=50&artist_name="+artistName);
			JSONArray jsonArrayAlbums = new JSONArray(jsonString); 
			return AlbumFunctions.getAlbums(jsonArrayAlbums);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}

	@Override
	public Artist getArtist(String name) throws JSONException, WSError {
		try {
			name = URLEncoder.encode(name, "UTF-8" );
			String jsonString = doGet("id+idstr+name+url+image+rating+mbgid+mbid+genre/artist/jsonpretty/?name="+name);
			JSONArray jsonArrayAlbums = new JSONArray(jsonString);
			return ArtistFunctions.getArtist(jsonArrayAlbums)[0];
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}

	@Override
	public int[] getTop100Listened() throws WSError {
		String rssString = Caller.doGet("http://www.jamendo.com/en/rss/top-track-week");
		return RSSFunctions.getTracksIdFromRss(rssString);
	}

	@Override
	public Album[] getAlbumsByTracksId(int[] id) throws JSONException, WSError {
		if(id == null)
			return null;
		
		String id_query = Caller.createStringFromIds(id);
		
		try {
			String jsonString = doGet("id+name+url+image+rating+artist_name/album/json/?n="+id.length+"&track_id="+id_query);
			JSONArray jsonArrayAlbums = new JSONArray(jsonString);
			return AlbumFunctions.getAlbums(jsonArrayAlbums);
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}

	@Override
	public Track[] getTracksByTracksId(int[] id, String encoding) throws JSONException, WSError {
		if(id == null)
			return null;
		
		String id_query = Caller.createStringFromIds(id);
		try {
			String jsonString = doGet("id+numalbum+name+duration+rating+url+stream/track/json/?streamencoding="+encoding+"&n="+id.length+"&id="+id_query);
			JSONArray jsonArrayTracks = new JSONArray(jsonString);
			return getTracks(jsonArrayTracks, false);
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}

	@Override
	public Review[] getAlbumReviews(Album album) throws JSONException, WSError {
		try {
			String jsonString = doGet("id+name+text+rating+lang+user_name+user_image/review/json/?album_id="+album.getId());
			JSONArray jsonReviewAlbums = new JSONArray(jsonString);
			return ReviewFunctions.getReviews(jsonReviewAlbums);
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}

	@Override
	public Playlist getRadioPlaylist(Radio radio, int n, String encoding) throws JSONException, WSError  {
		Log.i(JamendoApplication.TAG, "TESTs");
		String jsonString = doGet("track_id/track/json/radio_track_inradioplaylist/?radio_id="+radio.getId()+"&nshuffle="+n*10+"&n="+n);

		JSONArray jsonArrayTracks = new JSONArray(jsonString);
		int trackSize = jsonArrayTracks.length();
		int[] tracks_id = new int[trackSize];

		for(int i=0; i<trackSize; i++){
			tracks_id[i] = jsonArrayTracks.getInt(i);
		}

		Album[] albums = getAlbumsByTracksId(tracks_id);
		Track[] tracks = getTracksByTracksId(tracks_id, encoding);

		if(albums == null || tracks == null)
			return null;
		Log.i(JamendoApplication.TAG,"Pobieram liste");
		return createPlaylist(tracks, albums,tracks_id);
	}

	private Track[] getTracks(JSONArray jsonArrayTracks, boolean sort) throws JSONException {
		int n = jsonArrayTracks.length();
		if (n == 0)
			return null;
		
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

	private Playlist createPlaylist(Track[] aTracks, Album[] aAlbums, int[] aOrderBy) throws JSONException, WSError{
		if(aAlbums.length != aTracks.length)
			aAlbums = null;
		Playlist playlist = new Playlist();
		Hashtable<Integer, PlaylistEntry> bufferForOredr = new Hashtable<Integer, PlaylistEntry>();

		for(int i = 0; i < aTracks.length; i++){
			PlaylistEntry playlistEntry = new PlaylistEntry();
			Album album;
			if(aAlbums != null){
				album = aAlbums[i];
				playlistEntry.setAlbum(album);
			} else {
				album = getAlbumByTrackId(aTracks[i].getId());
				if(album == null){
					album = Album.emptyAlbum;
				}
				playlistEntry.setAlbum(album);
			}
			playlistEntry.setTrack(aTracks[i]);
			bufferForOredr.put(aTracks[i].getId(), playlistEntry);

			if(album != Album.emptyAlbum){
				Log.i("jamendroid", aTracks[i].getName() +" by "+album.getArtistName());
			}else{
				Log.i("jamendroid", aTracks[i].getName() +" without album");
			}
		}
		for(int i=0;i<aOrderBy.length;i++){
			// Adding to playlist in correct order
			playlist.addPlaylistEntry(bufferForOredr.get(aOrderBy[i]));
		}
		return playlist;
	}

	@Override
	public Radio[] getRadiosByIds(int[] id) throws JSONException, WSError {
		try {
			String id_query = Caller.createStringFromIds(id);
			String jsonString = doGet("id+idstr+name+image/radio/json/?id="+id_query);
			return RadioFunctions.getRadios(new JSONArray(jsonString));
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}

	@Override
	public Radio[] getRadiosByIdstr(String idstr) throws JSONException, WSError {
		try {
			String jsonString = doGet("id+idstr+name+image/radio/json/?idstr="+idstr);
			return RadioFunctions.getRadios(new JSONArray(jsonString));
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}

	@Override
	public PlaylistRemote[] getUserPlaylist(String user) throws JSONException, WSError {
		try {
			user = URLEncoder.encode(user, "UTF-8" );
			String jsonString = doGet("id+name+url+duration/playlist/json/playlist_user/?order=starred_desc&user_idstr="+user);
			return PlaylistFunctions.getPlaylists(new JSONArray(jsonString));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JSONException(e.getLocalizedMessage());
		}
	}

	@Override
	public Playlist getPlaylist(PlaylistRemote playlistRemote) throws JSONException, WSError {
		String jsonString = doGet("stream+name+duration+url+id+rating/track/json/?playlist_id="+playlistRemote.getId());
		JSONArray jsonArrayTracks = new JSONArray(jsonString);

		int n = jsonArrayTracks.length();

		Track[] tracks = new Track[n];
		int[] tracks_id = new int[n];

		TrackBuilder trackBuilder = new TrackBuilder();
		// building tracks and getting tracks_id
		for(int i=0; i < n; i++){
			tracks[i] = trackBuilder.build(jsonArrayTracks.getJSONObject(i));
			tracks_id[i] = tracks[i].getId();
		}

		Album[] albums = new JamendoGet2ApiImpl().getAlbumsByTracksId(tracks_id);
		Log.i("jamendroid", ""+tracks.length+" tracks & "+albums.length+" albums");

		return createPlaylist(tracks, albums,tracks_id);
	}

	@Override
	public String getTrackLyrics(Track track) throws WSError{
		String jsonString = doGet("text/track/json/?id="+track.getId());
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(jsonString);
			if(jsonArray.length() > 0)
				return jsonArray.getString(0).replace("\r", "");
			else
				return null;
		} catch (JSONException e) {
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new WSError(e.getLocalizedMessage());
		}
	}

	@Override
	public License getAlbumLicense(Album album) throws WSError {
		String jsonString = doGet("id+url+image/license/json/?album_id="+album.getId());
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(jsonString);
			if(jsonArray.length() > 0)
				return new LicenseBuilder().build(jsonArray.getJSONObject(0));
			else
				return null;
		} catch (JSONException e) {
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new WSError(e.getLocalizedMessage());
		}
	}

	@Override
	public Album getAlbumById(int id) throws JSONException, WSError {
		try {
			String jsonString = doGet("id+name+url+image+rating+artist_name/album/json/?id="+id);
			JSONArray jsonArrayAlbums = new JSONArray(jsonString); 
			Album[] album =  AlbumFunctions.getAlbums(jsonArrayAlbums);
			if(album != null && album.length > 0)
				return album[0];
			return null;
		} catch (JSONException e) {
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new WSError(e.getLocalizedMessage());
		}
	}

	@Override
	public Album[] getUserStarredAlbums(String user) throws JSONException, WSError {
		
		try {
			user = URLEncoder.encode(user, "UTF-8" );
			String jsonString = doGet("id+name+url+image+rating+artist_name/album/json/album_user_starred/?user_idstr="+user+"&n=all&order=rating_desc");
			JSONArray jsonArrayAlbums = new JSONArray(jsonString);
			return AlbumFunctions.getAlbums(jsonArrayAlbums);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new WSError(e.getLocalizedMessage());
		}
	}

	@Override
	public Album getAlbumByTrackId(int track_id) throws JSONException, WSError {
		try {
			String jsonString = doGet("id+name+url+image+rating+artist_name/album/json/?n=1&track_id="+track_id);
			JSONArray jsonArrayAlbums = new JSONArray(jsonString);
			Album[] album =  AlbumFunctions.getAlbums(jsonArrayAlbums);
			if(album != null && album.length > 0)
				return album[0];
			return null;
		} catch (JSONException e) {
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new WSError(e.getLocalizedMessage());
		}
	}
	
	// TODO private String nameToIdstr(String name);

}
