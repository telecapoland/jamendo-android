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

import com.teleca.jamendo.api.Artist;

/**
 * @author Lukasz Wisniewski
 */
public class ArtistBuilder extends JSONBuilder<Artist>{

	@Override
	public Artist build(JSONObject jsonObject) throws JSONException {
		Artist artist = new Artist();
		// TODO artist.setGenre()
		artist.setId(jsonObject.getInt("id"));
		artist.setIdstr(jsonObject.getString("idstr"));
		artist.setImage(jsonObject.getString("image"));
		artist.setMbgid(jsonObject.getString("mbgid"));
		artist.setMbid(jsonObject.getInt("mbid"));
		artist.setName(jsonObject.getString("name"));
		artist.setUrl(jsonObject.getString("url"));
		return artist;
	}

}
