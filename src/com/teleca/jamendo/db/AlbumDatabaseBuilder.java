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

	@Override
	public Album build(Cursor query) {
		int columnArtistName = query.getColumnIndex("artist_name");
		int columnName = query.getColumnIndex("album_name");
		int columnImage = query.getColumnIndex("album_image");
		int columnId = query.getColumnIndex("album_id");
		int columnRating = query.getColumnIndex("album_rating");
		
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
		values.put("artist_name", album.getArtistName());
		values.put("album_image", album.getImage());
		values.put("album_name", album.getName());
		values.put("album_id", album.getId());
		values.put("album_rating", album.getRating());
		return values;
	}

}
