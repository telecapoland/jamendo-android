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

package com.teleca.jamendo.util.download;

import java.io.File;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.db.AlbumDatabaseBuilder;
import com.teleca.jamendo.db.TrackDatabaseBuilder;

/**
 * Database implementation
 * 
 * @author Lukasz Wisniewski
 */
public class DownloadDatabaseImpl implements DownloadDatabase {

	private String mPath;

	private static final String TABLE_LIBRARY = "library";


	/**
	 * Default constructor
	 * 
	 * @param path Database file
	 */
	public DownloadDatabaseImpl(String path){
		mPath = path;

		SQLiteDatabase db = getDb();
		if(db == null)
			return;
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE_LIBRARY
				+ " (track_id INTEGER UNIQUE, downloaded INTEGER, track_name VARCHAR," 
				+ " track_duration INTEGER, track_url VARCHAR, track_stream VARCHAR, track_rating REAL," 
				+ " album_id INTEGER, album_name VARCHAR, album_image VARCHAR, album_rating REAL, artist_name VARCHAR);");
		db.close();
	}

	private SQLiteDatabase getDb(){
		// FIXME hardcoded path
		boolean success = (new File("/sdcard/music")).mkdirs();
		if (success) {
			Log.i(JamendoApplication.TAG, "Directory: " + "/sdcard/music" + " created");
		}
		try {
			return SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
		} catch (SQLException e) {
			Log.e(JamendoApplication.TAG, "Failed creating database");
			return null;
		}
	}

	@Override
	public boolean addToLibrary(PlaylistEntry entry) {

		SQLiteDatabase db = getDb();

		// put playlistentry data the table
		ContentValues values = new ContentValues();

		values.put("downloaded", 0);
		values.putAll(new TrackDatabaseBuilder().deconstruct(entry.getTrack()));
		values.putAll(new AlbumDatabaseBuilder().deconstruct(entry.getAlbum()));


		String[] whereArgs = {""+entry.getTrack().getId()};
		int row_count = db.update(TABLE_LIBRARY, values, "track_id=?", whereArgs);

		if(row_count == 0){
			db.insert(TABLE_LIBRARY, null, values);
		}

		db.close();

		return row_count != 0;
	}

	@Override
	public void setStatus(PlaylistEntry entry, boolean downloaded) {
		SQLiteDatabase db = getDb();

		ContentValues values = new ContentValues();

		values.put("downloaded", downloaded ? 1 : 0);

		String[] whereArgs = {""+entry.getTrack().getId()};
		int row_count = db.update(TABLE_LIBRARY, values, "track_id=?", whereArgs);

		if(row_count == 0){
			Log.e(JamendoApplication.TAG, "Failed to update "+TABLE_LIBRARY);
		}

		db.close();
	}

	@Override
	public boolean trackAvailable(Track track) {
		SQLiteDatabase db = getDb();
		if(db == null)
			return false;

		String[] selectionArgs = {""+track.getId()};
		
		Cursor query = db.query(TABLE_LIBRARY, null, "track_id=? and downloaded>0", selectionArgs, null, null, null);
		boolean value = query.getCount() > 0; 
		query.close();

		db.close();
		return value;
	}

}
