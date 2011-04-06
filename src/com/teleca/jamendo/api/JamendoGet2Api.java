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

import org.json.JSONException;


/**
 * Java interface to the jamendo.get2 API ("The Free Music API")<br>
 * <br>
 * <a href="http://developer.jamendo.com/en/wiki/Musiclist2Api">Musiclist2Api</a><br>
 * <br>
 * <b>USAGE:</b><br>
 * Basically, since this service does not require any sort of authorization, just write:<br>
 * <br>
 * <code>
 * JamendoGet2Api service = new JamendoGet2ApiImpl(); 
 * </code><br>
 * <br>
 * <b>NOTE:</b><br> 
 * This library is meant to cover most of the api documented on the wiki. It is
 * not side-by-side copy though. These Java bindings where designed with an Android application
 * in mind, therefore they provide certain optimizations for this platform, e.g. Apache HTTP 
 * Client 4 backend<br>
 * 
 * 
 * @author Lukasz Wisniewski
 */
public interface JamendoGet2Api {
	
	public static final String ENCODING_MP3 = "mp31";
	public static final String ENCODING_OGG = "ogg2";

	/**
	 * Retrieve "this week's most popular albums" 
	 * <br><br>
	 * http://api.jamendo.com/get2/id+name+url+image+artist_name/album/jsonpretty/?n=5&order=ratingweek_desc
	 * @return an array of albums
	 * @throws JSONException 
	 * @throws WSError 
	 */
	Album[] getPopularAlbumsWeek() throws JSONException, WSError;
	
	/**
	 * Retrieve info on all track from the given album 
	 * <br><br>
	 * http://api.jamendo.com/get2/id+name+duration+url+stream/track/jsonpretty/?album_id=33
	 * @param album an <code>Album</code> instance
	 * @return a <code>Track</code> array from the given album
	 * @throws JSONException
	 * @throws WSError 
	 */
	Track[] getAlbumTracks(Album album, String encoding) throws JSONException, WSError;

	/**
	 * Retrieve info on tracks from the given album using pagination.
	 * Tracks retrieved from given page will be appended to existing <code>Album</code> <code>Track</code>s
	 * and whole list will be returned.
	 * <br><br>
	 * http://api.jamendo.com/get2/id+name+duration+url+stream/track/jsonpretty/?album_id=33
	 * @param album an <code>Album</code> instance
	 * @param count of items returned per page
	 * @param page number to retrieve <code>count</code> tracks from
	 * @return a <code>Track</code> array from the given album
	 * @throws JSONException
	 * @throws WSError 
	 */
	Track[] getAlbumTracks(Album album, String encoding, int count, int page) throws JSONException, WSError;
	
	/**
	 * Search for albums with artist like name
	 * 
	 * @param artistName
	 * @return an array of albums
	 * @throws JSONException
	 * @throws WSError 
	 */
	Album[] searchForAlbumsByArtist(String artistName) throws JSONException, WSError;
	
	/**
	 * Search for given artist's albums
	 * 
	 * @param artistName
	 * @return an array of albums
	 * @throws JSONException
	 * @throws WSError 
	 */
	Album[] searchForAlbumsByArtistName(String artistName) throws JSONException, WSError;
	
	/**
	 * Search for albums matching the given tag<br>
	 * <br>
	 * http://api.jamendo.com/get2/id+name+url+image+artist_name/album/jsonpretty/?tag_idstr=rock&n=50&order=rating_desc
	 * 
	 * @param tag
	 * @return
	 * @throws JSONException
	 * @throws WSError 
	 */
	Album[] searchForAlbumsByTag(String tag) throws JSONException, WSError;
	
	/**
	 * Gets Artist info<br>
	 * <br>
	 * http://api.jamendo.com/get2/id+idstr+name+url+image+rating+mbgid+mbid+genre/artist/jsonpretty/?name=triface
	 * 
	 * @param name Artist name
	 * @return <code>Artist</code> instance
	 * @throws JSONException 
	 * @throws WSError 
	 */
	Artist getArtist(String name) throws JSONException, WSError;
	
	/**
	 * Returns tracks ids of top week tracks<br>
	 * <br>
	 * http://www.jamendo.com/pl/rss/top-track-week (via XML backend)
	 * 
	 * @return
	 * @throws WSError 
	 */
	int[] getTop100Listened() throws WSError;
	
	/**
	 * Returns an array of albums matching an array of track ids, can be combined with
	 * <code>getTop100Listened</code>
	 * 
	 * @param id
	 * @return
	 * @throws JSONException
	 * @throws WSError 
	 */
	Album[] getAlbumsByTracksId(int id[]) throws JSONException, WSError;
	
	/**
	 * Returns an array of tracks matching an array of track ids, can be combined with
	 * <code>getTop100Listened</code>
	 * 
	 * @param id
	 * @return
	 * @throws JSONException
	 * @throws WSError 
	 */
	Track[] getTracksByTracksId(int id[], String encoding) throws JSONException, WSError;
	
	/**
	 * Gets album reviews<br>
	 * <br>
	 * http://api.jamendo.com/get2/id+name+text+rating+lang/review/jsonpretty/?album_id=7505
	 * 
	 * @param album
	 * @return
	 * @throws JSONException
	 * @throws WSError 
	 */
	Review[] getAlbumReviews(Album album)  throws JSONException, WSError;
	
	/**
	 * Gets playlist with album and track from the remote server
	 * 
	 * @param playlistRemote
	 * @return
	 * @throws JSONException 
	 * @throws WSError 
	 */
	Playlist getPlaylist(PlaylistRemote playlistRemote) throws JSONException, WSError;
	
	/**
	 * Gets radios by given ids<br>
	 * <br>
	 * http://api.jamendo.com/get2/id+idstr+name+image/radio/jsonpretty/?id=2+3
	 * @param id
	 * @return
	 * @throws JSONException 
	 * @throws WSError 
	 */
	Radio[] getRadiosByIds(int[] id) throws JSONException, WSError;
	
	/**
	 * Get radio by given idstr
	 * 
	 * @param idstr
	 * @return
	 * @throws JSONException
	 * @throws WSError 
	 */
	Radio[] getRadiosByIdstr(String idstr) throws JSONException, WSError;
	
	/**
	 * Retrieves tracks for the given radio<br>
	 * <br>
	 * http://www.jamendo.com/get2/stream+name+id+rating+album_id+album_name+album_image/track/jsonpretty/radio_track_inradioplaylist/?radio_id=4&nshuffle=1
	 * 
	 * @param radio
	 * @param n number of tracks to be retrieved at once
	 * @return
	 * @throws JSONException 
	 * @throws WSError 
	 */
	Playlist getRadioPlaylist(Radio radio, int n, String encoding) throws JSONException, WSError;
	
	/**
	 * Gets user defined playlists<br>
	 * <br>
	 * http://www.jamendo.com/get2/id+name+url/playlist/jsonpretty/playlist_user/?user_idstr=pierrotsmnrd&order=starred_desc
	 * 
	 * @param user
	 * @return
	 * @throws JSONException
	 * @throws WSError 
	 */
	PlaylistRemote[] getUserPlaylist(String user) throws JSONException, WSError;
	
	/**
	 * Gets user starred albums<br>
	 * <br>
	 * http://api.jamendo.com/get2/id+name+url+image+artist_name/album/jsonpretty/album_user_starred/?user_idstr=sylvinus&n=all
	 * 
	 * @param user
	 * @return
	 * @throws JSONException
	 * @throws WSError 
	 */
	Album[] getUserStarredAlbums(String user) throws JSONException, WSError;
	
	/**
	 * Get track lyrics<br>
	 * <br>
	 * http://api.jamendo.com/get2/text/track/jsonpretty/?id=241
	 * 
	 * @param track
	 * @return lyrics or null
	 * @throws WSError 
	 */
	String getTrackLyrics(Track track) throws WSError;
	
	/**
	 * Get album's license<br>
	 * <br>
	 * http://api.jamendo.com/get2/id+url+image/license/jsonpretty/?album_id=33
	 * 
	 * @param album
	 * @return
	 * @throws WSError 
	 */
	License getAlbumLicense(Album album) throws WSError;
	
	/**
	 * Get Album by its id<br>
	 * <br>
	 * http://api.jamendo.com/get2/id+name+rating+url+image+artist_name/album/jsonpretty/?id=7505 
	 * 
	 * @param id
	 * @return
	 * @throws JSONException 
	 * @throws WSError 
	 */
	Album getAlbumById(int id) throws JSONException, WSError;

	/**
	 * Get Album by one of its track id
	 *
	 * @param track_id
	 * @return
	 * @throws JSONException
	 * @throws WSError
	 */
	Album getAlbumByTrackId(int track_id) throws JSONException, WSError;
}
