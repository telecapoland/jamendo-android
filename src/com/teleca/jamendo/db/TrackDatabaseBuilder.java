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

package com.teleca.jamendo.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.teleca.jamendo.api.Track;

public class TrackDatabaseBuilder extends DatabaseBuilder<Track> {

	@Override
	public Track build(Cursor query) {
		int columnName = query.getColumnIndex("track_name");
		int columnStream = query.getColumnIndex("track_stream");
		int columnUrl = query.getColumnIndex("track_url");
		int columnDuration = query.getColumnIndex("track_duration");
		int columnId = query.getColumnIndex("track_id");
		int columnRating = query.getColumnIndex("track_rating");
		
		Track track = new Track();
		track.setDuration(query.getInt(columnDuration));
		track.setId(query.getInt(columnId));
		track.setName(query.getString(columnName));
		track.setRating(query.getDouble(columnRating));
		track.setStream(query.getString(columnStream));
		track.setUrl(query.getString(columnUrl));
		return track;
	}

	@Override
	public ContentValues deconstruct(Track track) {
		ContentValues values = new ContentValues();
		values.put("track_name", track.getName());
		values.put("track_stream", track.getStream());
		values.put("track_url", track.getUrl());
		values.put("track_duration", track.getDuration());
		values.put("track_id", track.getId());
		values.put("track_rating", track.getRating());
		return values;
	}

}
