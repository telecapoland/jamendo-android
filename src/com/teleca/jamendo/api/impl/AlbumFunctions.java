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

import com.teleca.jamendo.api.Album;

/**
 * @author Lukasz Wisniewski
 */
public class AlbumFunctions {
	
	public static Album[] getAlbums(JSONArray jsonArrayAlbums) throws JSONException {
		int n = jsonArrayAlbums.length();
		Album[] albums = new Album[n];
		AlbumBuilder albumBuilder = new AlbumBuilder();
		
		for(int i=0; i < n; i++){
			albums[i] = albumBuilder.build(jsonArrayAlbums.getJSONObject(i));
		}
		
		return albums;
	}
}
