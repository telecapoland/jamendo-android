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
		//String rssString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss xmlns:content=\"http://purl.org/rss/1.0/modules/content/\" xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\" version=\"2.0\">  <channel xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">    <title><![CDATA[Latest releases]]></title>    <link>http://www.jamendo.com/releases</link>    <description><![CDATA[Latest releases on Jamendo.com]]></description>    <pubDate>Thu, 21 Jun 2012 14:04:48 +0200</pubDate>    <lastBuildDate>Thu, 21 Jun 2012 14:04:48 +0200</lastBuildDate>    <managingEditor>support@jamendo.com (Jamendo.com)</managingEditor>    <webMaster>support@jamendo.com</webMaster>    <copyright>See each item license on jamendo.com</copyright>    <image>      <url>url to image</url>      <title><![CDATA[Latest releases]]></title>      <link>http://www.jamendo.com/releases</link>    </image>    <generator>Jamendo</generator>    <docs>http://blogs.law.harvard.edu/tech/rss</docs>    <ttl>180</ttl>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Flame (the virus) by Danubio Rodriguez]]></title>      <link>http://www.jamendo.com/list/a111208</link>      <description><![CDATA[A new playlist by Danubio Rodriguez]]></description>      <pubDate>Thu, 21 Jun 2012 04:15:49 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111208#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111208</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946570\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[away by the guta jasna]]></title>      <link>http://www.jamendo.com/list/a111200</link>      <description><![CDATA[A new playlist by the guta jasna]]></description>      <pubDate>Wed, 20 Jun 2012 23:04:25 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111200#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111200</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946499\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946500\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946501\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946502\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946503\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946504\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946505\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946506\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946507\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946508\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946509\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946510\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Fugue Robert Haas / Brzozowski  by Marcin brzozowski]]></title>      <link>http://www.jamendo.com/list/a111199</link>      <description><![CDATA[A new playlist by Marcin brzozowski]]></description>      <pubDate>Wed, 20 Jun 2012 20:29:15 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111199#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111199</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946483\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Amalia DJ by Snabisch]]></title>      <link>http://www.jamendo.com/list/a111197</link>      <description><![CDATA[A new playlist by Snabisch]]></description>      <pubDate>Wed, 20 Jun 2012 16:08:57 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111197#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111197</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946461\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946462\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[The Best Of Paul Zach by Pawel Zach]]></title>      <link>http://www.jamendo.com/list/a111187</link>      <description><![CDATA[A new playlist by Pawel Zach]]></description>      <pubDate>Wed, 20 Jun 2012 09:53:24 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111187#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111187</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946423\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946424\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946425\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946426\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946427\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946428\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946429\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946430\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946431\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946432\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[TEKNIKOKO by NICOCO]]></title>      <link>http://www.jamendo.com/list/a111183</link>      <description><![CDATA[A new playlist by NICOCO]]></description>      <pubDate>Wed, 20 Jun 2012 06:03:12 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111183#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111183</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946412\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946413\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946414\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946415\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946416\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946417\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946418\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946419\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946420\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[mix tribe hardtek by Spasmatek]]></title>      <link>http://www.jamendo.com/list/a111176</link>      <description><![CDATA[A new playlist by Spasmatek]]></description>      <pubDate>Wed, 20 Jun 2012 00:52:48 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111176#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111176</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=945440\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Proxima Centauri II by Lobo Loco]]></title>      <link>http://www.jamendo.com/list/a111175</link>      <description><![CDATA[A new playlist by Lobo Loco]]></description>      <pubDate>Wed, 20 Jun 2012 00:26:35 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111175#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111175</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946365\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946366\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946367\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946368\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946369\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946370\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946371\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946372\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946373\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946374\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Bleu Roy Bleu Nuit by Régis V. Gronoff]]></title>      <link>http://www.jamendo.com/list/a111168</link>      <description><![CDATA[A new playlist by Régis V. Gronoff]]></description>      <pubDate>Tue, 19 Jun 2012 18:53:15 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111168#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111168</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946336\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946337\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946338\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946339\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946340\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946341\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946342\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946343\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946344\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Mutation of the organism by pierecall]]></title>      <link>http://www.jamendo.com/list/a111167</link>      <description><![CDATA[A new playlist by pierecall]]></description>      <pubDate>Tue, 19 Jun 2012 17:16:14 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111167#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111167</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946317\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946318\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946319\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946334\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946335\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Tania Australis by Lar Clobsay]]></title>      <link>http://www.jamendo.com/list/a111166</link>      <description><![CDATA[A new playlist by Lar Clobsay]]></description>      <pubDate>Tue, 19 Jun 2012 16:44:11 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111166#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111166</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946332\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Electro Music by M.S]]></title>      <link>http://www.jamendo.com/list/a111164</link>      <description><![CDATA[A new playlist by M.S]]></description>      <pubDate>Tue, 19 Jun 2012 15:18:18 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111164#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111164</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946298\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946299\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946300\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946301\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946302\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946303\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946304\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946305\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Kompozitsia X by Avi Rosenfeld]]></title>      <link>http://www.jamendo.com/list/a111159</link>      <description><![CDATA[A new playlist by Avi Rosenfeld]]></description>      <pubDate>Tue, 19 Jun 2012 09:30:51 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111159#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111159</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946195\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946196\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946197\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946198\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946199\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946200\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946201\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946202\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946203\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946204\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946205\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Minimal Smile by Sonntagsrausch]]></title>      <link>http://www.jamendo.com/list/a111154</link>      <description><![CDATA[A new playlist by Sonntagsrausch]]></description>      <pubDate>Mon, 18 Jun 2012 23:46:02 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111154#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111154</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946263\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[\" Good Luck \" by Lil'fellow]]></title>      <link>http://www.jamendo.com/list/a111151</link>      <description><![CDATA[A new playlist by Lil'fellow]]></description>      <pubDate>Mon, 18 Jun 2012 21:56:11 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111151#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111151</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=945843\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=945844\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=945845\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=945847\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946241\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Come On All You Fair And Tender Ladies by SaftJesus]]></title>      <link>http://www.jamendo.com/list/a111150</link>      <description><![CDATA[A new playlist by SaftJesus]]></description>      <pubDate>Mon, 18 Jun 2012 21:47:56 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111150#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111150</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946247\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Images  by ¥oru]]></title>      <link>http://www.jamendo.com/list/a111148</link>      <description><![CDATA[A new playlist by ¥oru]]></description>      <pubDate>Mon, 18 Jun 2012 21:38:12 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111148#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111148</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=944754\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946244\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Musik von zu Hause (Teil 1) by Südwestlicht]]></title>      <link>http://www.jamendo.com/list/a111144</link>      <description><![CDATA[A new playlist by Südwestlicht]]></description>      <pubDate>Mon, 18 Jun 2012 20:48:41 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111144#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111144</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946239\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946240\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946242\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[The road of love (12\"mix) by d27m]]></title>      <link>http://www.jamendo.com/list/a111143</link>      <description><![CDATA[A new playlist by d27m]]></description>      <pubDate>Mon, 18 Jun 2012 20:40:55 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111143#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111143</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946211\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946212\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946231\" type=\"audio/mpeg\" length=\"\"/>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946237\" type=\"audio/mpeg\" length=\"\"/>    </item>    <item xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">      <title><![CDATA[Sexy Toys by Audiomagma]]></title>      <link>http://www.jamendo.com/list/a111141</link>      <description><![CDATA[A new playlist by Audiomagma]]></description>      <pubDate>Mon, 18 Jun 2012 19:49:15 +0200</pubDate>      <source url=\"http://www.jamendo.com\">Jamendo</source>      <comments>http://www.jamendo.com/list/a111141#reviews</comments>      <wfw:commentRss xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\">http://www.jamendo.com/rss/playlistReviews/a111141</wfw:commentRss>      <enclosure url=\"http://storage-new.newjamendo.com/?trackid=946209\" type=\"audio/mpeg\" length=\"\"/>    </item>  </channel></rss>";
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
