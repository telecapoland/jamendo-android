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

import org.json.JSONException;
import org.json.JSONObject;

import com.teleca.jamendo.api.Album;

/**
 * @author Lukasz Wisniewski
 */
public class AlbumBuilder extends JSONBuilder<Album> {

	@Override
	public Album build(JSONObject jsonObject) throws JSONException {
		Album album = new Album();
		album.setImage(jsonObject.getString(root+"image"));
		album.setName(jsonObject.getString(root+"name"));
		album.setId(jsonObject.getInt(root+"id"));
		try {
			album.setArtistName(jsonObject.getString(root+"artist_name"));
		} catch (JSONException e) {
			// if we miss artist name an we are not in a subquery, abort!
			if(root.length()==0)
				throw e;
		}
		try {
			album.setRating(jsonObject.getDouble(root+"rating"));
		} catch (JSONException e) {
			album.setRating(-1);
		}
		return album;
	}

}
