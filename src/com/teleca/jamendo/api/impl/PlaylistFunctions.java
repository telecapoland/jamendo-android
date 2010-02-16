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

import org.json.JSONArray;
import org.json.JSONException;

import com.teleca.jamendo.api.PlaylistRemote;

/**
 * @author Lukasz Wisniewski
 */
public class PlaylistFunctions {
	
	public static PlaylistRemote[] getPlaylists(JSONArray jsonArrayReviews) throws JSONException {
		int n = jsonArrayReviews.length();
		PlaylistRemote[] playlists = new PlaylistRemote[n];
		PlaylistBuilder playlistBuilder = new PlaylistBuilder();
		
		for(int i=0; i < n; i++){
			playlists[i] = playlistBuilder.build(jsonArrayReviews.getJSONObject(i));
		}
		
		return playlists;
	}
}
