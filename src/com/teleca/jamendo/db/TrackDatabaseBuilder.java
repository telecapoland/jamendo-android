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

	private static final String TRACK_ID = "track_id";
	private static final String TRACK_NAME = "track_name";
	private static final String TRACK_DURATION = "track_duration";
	private static final String TRACK_URL = "track_url";
	private static final String TRACK_STREAM = "track_stream";
	private static final String TRACK_RATING = "track_rating";
	private static final String ALBUM_TRACK_NUM = "album_track_num";

	@Override
	public Track build(Cursor query) {
		int columnName = query.getColumnIndex(TRACK_NAME);
		int columnStream = query.getColumnIndex(TRACK_STREAM);
		int columnUrl = query.getColumnIndex(TRACK_URL);
		int columnDuration = query.getColumnIndex(TRACK_DURATION);
		int columnId = query.getColumnIndex(TRACK_ID);
		int columnRating = query.getColumnIndex(TRACK_RATING);
		int columnAlbumTrackNum = query.getColumnIndex(ALBUM_TRACK_NUM);
		
		Track track = new Track();
		track.setDuration(query.getInt(columnDuration));
		track.setId(query.getInt(columnId));
		track.setName(query.getString(columnName));
		track.setRating(query.getDouble(columnRating));
		track.setStream(query.getString(columnStream));
		track.setUrl(query.getString(columnUrl));
		track.setNumAlbum(query.getInt(columnAlbumTrackNum));
		return track;
	}

	@Override
	public ContentValues deconstruct(Track track) {
		ContentValues values = new ContentValues();
		values.put(TRACK_NAME, track.getName());
		values.put(TRACK_STREAM, track.getStream());
		values.put(TRACK_URL, track.getUrl());
		values.put(TRACK_DURATION, track.getDuration());
		values.put(TRACK_ID, track.getId());
		values.put(TRACK_RATING, track.getRating());
		values.put(ALBUM_TRACK_NUM, track.getNumAlbum());
		return values;
	}

}
