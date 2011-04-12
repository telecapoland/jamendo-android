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

import com.teleca.jamendo.api.Album;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * @author Lukasz Wisniewski
 */
public class AlbumDatabaseBuilder extends DatabaseBuilder<Album> {

	private static final String ALBUM_ID = "album_id";
	private static final String ALBUM_NAME = "album_name";
	private static final String ALBUM_IMAGE = "album_image";
	private static final String ALBUM_RATING = "album_rating";
	private static final String ARTIST_NAME = "artist_name";

	@Override
	public Album build(Cursor query) {
		int columnArtistName = query.getColumnIndex(ARTIST_NAME);
		int columnName = query.getColumnIndex(ALBUM_NAME);
		int columnImage = query.getColumnIndex(ALBUM_IMAGE);
		int columnId = query.getColumnIndex(ALBUM_ID);
		int columnRating = query.getColumnIndex(ALBUM_RATING);
		
		Album album = new Album();
		album.setId(query.getInt(columnId));
		album.setArtistName(query.getString(columnArtistName));
		album.setName(query.getString(columnName));
		album.setRating(query.getDouble(columnRating));
		album.setImage(query.getString(columnImage));
		return album;
	}

	@Override
	public ContentValues deconstruct(Album album) {
		ContentValues values = new ContentValues();
		values.put(ARTIST_NAME, album.getArtistName());
		values.put(ALBUM_IMAGE, album.getImage());
		values.put(ALBUM_NAME, album.getName());
		values.put(ALBUM_ID, album.getId());
		values.put(ALBUM_RATING, album.getRating());
		return values;
	}

}
