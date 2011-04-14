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
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.db.AlbumDatabaseBuilder;
import com.teleca.jamendo.db.TrackDatabaseBuilder;

/**
 * Database implementation
 * 
 * Version 1: 	Added album_track_num column to database describing track number.
 * 
 * @author Lukasz Wisniewski, Bartosz Cichosz
 */
public class DownloadDatabaseImpl implements DownloadDatabase {

	private String mPath;

	private static final String TABLE_LIBRARY = "library";

	private static final int DB_VERSION = 1;

	private SQLiteDatabase mDb;

	/**
	 * Default constructor
	 * 
	 * @param path
	 *            Database file
	 */
	public DownloadDatabaseImpl(String path) {
		mPath = path;

		mDb = getDb();
		if (mDb == null)
			return;

		if (mDb.getVersion() < DB_VERSION) {
			new UpdaterBuilder().getUpdater(DB_VERSION).update();
		}
	}

	private SQLiteDatabase getDb() {
		// FIXME hardcoded path
		boolean success = (new File("/sdcard/music")).mkdirs();
		if (success) {
			Log.i(JamendoApplication.TAG, "Directory: " + "/sdcard/music"
					+ " created");
		}
		try {
			return SQLiteDatabase.openDatabase(mPath, null,
					SQLiteDatabase.CREATE_IF_NECESSARY);
		} catch (SQLException e) {
			Log.e(JamendoApplication.TAG, "Failed creating database");
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean addToLibrary(PlaylistEntry entry) {
		if (mDb == null) {
			// database was not created
			return false;
		}
		// put playlistentry data the table
		ContentValues values = new ContentValues();

		values.put("downloaded", 0);
		values.putAll(new TrackDatabaseBuilder().deconstruct(entry.getTrack()));
		values.putAll(new AlbumDatabaseBuilder().deconstruct(entry.getAlbum()));

		String[] whereArgs = { "" + entry.getTrack().getId() };
		long row_count = mDb.update(TABLE_LIBRARY, values, "track_id=?",
				whereArgs);

		if (row_count == 0) {
			mDb.insert(TABLE_LIBRARY, null, values);
		}

		return row_count != -1l;
	}

	@Override
	public void setStatus(PlaylistEntry entry, boolean downloaded) {
		if(mDb == null){
			return;
		}
		ContentValues values = new ContentValues();

		values.put("downloaded", downloaded ? 1 : 0);

		String[] whereArgs = { "" + entry.getTrack().getId() };
		int row_count = mDb.update(TABLE_LIBRARY, values, "track_id=?",
				whereArgs);

		if (row_count == 0) {
			Log.e(JamendoApplication.TAG, "Failed to update " + TABLE_LIBRARY);
		}
	}

	@Override
	public boolean trackAvailable(Track track) {
		if (mDb == null)
			return false;

		String[] selectionArgs = { "" + track.getId() };

		Cursor query = mDb.query(TABLE_LIBRARY, null,
				"track_id=? and downloaded>0", selectionArgs, null, null, null);
		boolean value = query.getCount() > 0;
		query.close();

		return value;
	}

	@Override
	public ArrayList<DownloadJob> getAllDownloadJobs() {
		ArrayList<DownloadJob> jobs = new ArrayList<DownloadJob>();
		if (mDb == null)
			return jobs;

		Cursor query = mDb.query(TABLE_LIBRARY, null, null, null, null, null,
				null);
		if (query.moveToFirst()) {
			while (!query.isAfterLast()) {
				jobs.add(new DownloadJobBuilder().build(query));
				query.moveToNext();
			}
		}
		query.close();
		return jobs;
	}

	@Override
	public void remove(DownloadJob job) {
		if(mDb == null){
			return;
		}

		String[] whereArgs = { "" + job.getPlaylistEntry().getTrack().getId() };
		mDb.delete(TABLE_LIBRARY, "track_id=?", whereArgs);
	}

	protected void finalize(){
		mDb.close();
	}

	/**
	 * Updater from version 0 to 1.
	 * 
	 * Updates from previous version without copying data.
	 * Creates database if it doesn't exist.
	 * @author Bartosz Cichosz
	 *
	 */
	private class DatabaseUpdaterV1 extends DatabaseUpdater {

		//TODO Add copying and updating data from previous db version

		private static final int VERSION = 1;
		public DatabaseUpdaterV1() {
		}

		public DatabaseUpdaterV1(DatabaseUpdater updater) {
			setUpdater(updater);
		}

		@Override
		public void update() {
			if (getUpdater() != null) {
				getUpdater().update();
			}

			/*if(checkTableExistence()){
				copyDataAndCreate();
			} else {
				createTables();
			}*/
			try{
				mDb.execSQL("DROP TABLE " + TABLE_LIBRARY + ";");
			} catch (SQLiteException e) {
				Log.v(JamendoApplication.TAG, "Library table not existing");
			}
			createTables();
			mDb.setVersion(VERSION);
		}

		/*private void copyDataAndCreate() {
			ArrayList<DownloadJob> data = getAllDownloadJobs();
			mDB.execSQL("DROP TABLE " + TABLE_LIBRARY + ";");
			createTables();
		}*/

		private void createTables(){
			mDb.execSQL("CREATE TABLE IF NOT EXISTS "
					+ TABLE_LIBRARY
					+ " (track_id INTEGER UNIQUE, downloaded INTEGER, track_name VARCHAR,"
					+ " track_duration INTEGER, track_url VARCHAR, track_stream VARCHAR, track_rating REAL,"
					+ " album_id INTEGER, album_name VARCHAR, album_image VARCHAR, album_rating REAL, artist_name VARCHAR, album_track_num INTEGER);");
		}

		/*private boolean checkTableExistence(){
			try{
				mDB.query(TABLE_LIBRARY, null, null, null, null, null, null);
				return true;
			} catch (SQLiteException e) {
				return false;
			}
		}*/

	}

	/**
	 * DatabaseUpdater Builder.
	 * Builds an updater for updating database from its current version
	 * to version passed as parameter of getUpadater method.
	 * 
	 * Each updater uses a previous db version as input.
	 * Builder builds a decorated updater to update to the desired version.
	 * @author Bartosz Cichosz
	 *
	 */
	private class UpdaterBuilder {

		public DatabaseUpdater getUpdater(int version){
			DatabaseUpdater updater = null;

			switch(version){
			case 1: updater = new DatabaseUpdaterV1();
				break;
			case 0: updater = null;
				break;
			}

			if(version > mDb.getVersion() + 1){
				updater.setUpdater(getUpdater(version - 1));
			}
			return updater;
		}
	}

}
/**
 * Database updater. Build around Decorator pattern.
 * 
 * @author Bartosz Cichosz
 *
 */
abstract class DatabaseUpdater {
	private DatabaseUpdater mUpdater;

	abstract void update();

	public void setUpdater(DatabaseUpdater mUpdater) {
		this.mUpdater = mUpdater;
	}

	public DatabaseUpdater getUpdater() {
		return mUpdater;
	}
}
