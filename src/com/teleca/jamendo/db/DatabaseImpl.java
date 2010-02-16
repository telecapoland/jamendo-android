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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.Radio;
import com.teleca.jamendo.api.Track;

/**
 * Database implementation using Android SQLite
 * 
 * @author Lukasz Wisniewski
 */
public class DatabaseImpl implements Database {

	private static final String DB_NAME = "jamendroid";
	private static final String TABLE_PLAYLIST = "playlist";
	private static final String TABLE_RECENT_RADIOS = "recent_radios";
	private static final String TABLE_FAVORITES = "favorites";

	/**
	 * Serializable Jamendroid Playlist (file extension)
	 */
	private static final String SJP_EXT = ".sjp";

	private Activity mActivity;

	public DatabaseImpl(Activity activity){
		this.mActivity = activity;
		create();
	}

	/**
	 * Initializes database and tables
	 */
	private void create(){
		SQLiteDatabase db = mActivity.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

		// create tables if necessary
		
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE_PLAYLIST
				+ " (PlaylistName VARCHAR UNIQUE,"
				+ " FileName INTEGER PRIMARY KEY AUTOINCREMENT);");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE_RECENT_RADIOS
				+ " (radio_id INTEGER UNIQUE, radio_idstr VARCHAR, radio_name VARCHAR, radio_image VARCHAR, radio_date INTEGER);");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE_FAVORITES
				+ " (track_id INTEGER UNIQUE, track_name VARCHAR," 
				+ " track_duration INTEGER, track_url VARCHAR, track_stream VARCHAR, track_rating REAL," 
				+ " album_id INTEGER, album_name VARCHAR, album_image VARCHAR, album_rating REAL, artist_name VARCHAR);");

		db.close();
	}

	@Override
	public void deletePlaylist(String playlistName) {
		SQLiteDatabase db = mActivity.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

		String fileName = queryForFileName(playlistName, db);
		if(fileName != null)
			mActivity.deleteFile(fileName);
		//mActivity.openFileOutput(fileName, Context.MODE_PRIVATE).getFD().

		String[] whereArgs = {playlistName};
		db.delete(TABLE_PLAYLIST, "PlaylistName = ?", whereArgs);

		db.close();
	}

	@Override
	public ArrayList<String> getAvailablePlaylists() {
		ArrayList<String> playlists = new ArrayList<String>(); 
		SQLiteDatabase db = mActivity.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

		String[] columns = {"PlaylistName"};
		Cursor query = db.query(TABLE_PLAYLIST, columns, null, null, null, null, "PlaylistName ASC");

		if(query != null){
			query.moveToFirst();
			int columnIndex = query.getColumnIndex("PlaylistName");
			while(!query.isAfterLast()){
				playlists.add(query.getString(columnIndex));
				query.moveToNext();
			}
		}

		query.close();

		db.close();
		return playlists;
	}

	@Override
	public Playlist loadPlaylist(String playlistName) {
		
		if(!getAvailablePlaylists().contains(playlistName))
			return null;
		
		Playlist playlist = null;
		
		SQLiteDatabase db = mActivity.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

		String fileName = queryForFileName(playlistName, db);
		try {
			FileInputStream fis = mActivity.openFileInput(fileName);
			ObjectInputStream in = new ObjectInputStream(fis);
			playlist = (Playlist)in.readObject();
			in.close();
			if(playlist == null)
				playlist = new Playlist();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		db.close();
		return playlist;
	}

	@Override
	public void savePlaylist(Playlist playlist, String playlistName) {
		deletePlaylist(playlistName);

		SQLiteDatabase db = mActivity.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

		// put playlist reference into the table
		ContentValues values = new ContentValues();
		values.put("PlaylistName", playlistName);
		db.insert(TABLE_PLAYLIST, null, values);

		// query for filename
		String fileName = queryForFileName(playlistName, db);

		// save playlist to file
		try {
			FileOutputStream fos = mActivity.openFileOutput(fileName, Context.MODE_PRIVATE);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(playlist);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		db.close();
	}

	@Override
	public boolean playlistExists(String playlistName) {
		boolean value = false;
		SQLiteDatabase db = getDb();

		String[] columns = {"PlaylistName"};
		String[] selectionArgs = {playlistName};
		Cursor query = db.query(TABLE_PLAYLIST, columns, "PlaylistName = ?", selectionArgs, null, null, null);

		if(query != null && query.getCount() > 0 ){
			value = true;
		}

		query.close();

		db.close();
		return value;
	}

	@Override
	public void addRadioToRecent(Radio radio) {
		SQLiteDatabase db = getDb();
		
		// put radio data into the table
		ContentValues values = new RadioDatabaseBuilder().deconstruct(radio);
		
		String[] whereArgs = {""+radio.getId()};
		int row_count = db.update(TABLE_RECENT_RADIOS, values, "radio_id=?", whereArgs);
		
		if(row_count == 0){
			db.insert(TABLE_RECENT_RADIOS, null, values);
		}
		
		db.close();
	}

	@Override
	public ArrayList<Radio> getRecentRadios(int limit) {
		ArrayList<Radio> radios = new ArrayList<Radio>(); 
		SQLiteDatabase db = getDb();
		
		String[] columns = {"radio_id","radio_idstr","radio_name","radio_image"};
		Cursor query = db.query(TABLE_RECENT_RADIOS, columns, "", null, null, null, "radio_date DESC", ""+limit);
		
		if(query != null){
			query.moveToFirst();
			
			while(!query.isAfterLast()){
				Radio radio = new RadioDatabaseBuilder().build(query);
				radios.add(radio);
				query.moveToNext();
			}
		}
		
		// TODO probably remove the rest
		
		db.close();
		return radios;
	}

	@Override
	public void addToFavorites(PlaylistEntry entry) {
		SQLiteDatabase db = getDb();
		
		// put playlistentry data the table
		ContentValues values = new ContentValues();
		
		values.putAll(new TrackDatabaseBuilder().deconstruct(entry.getTrack()));
		values.putAll(new AlbumDatabaseBuilder().deconstruct(entry.getAlbum()));
		
		
		String[] whereArgs = {""+entry.getTrack().getId()};
		int row_count = db.update(TABLE_FAVORITES, values, "track_id=?", whereArgs);
		
		if(row_count == 0){
			db.insert(TABLE_FAVORITES, null, values);
		}
		
		
		db.close();
	}

	@Override
	public Playlist getFavorites() {
		Playlist playlist = new Playlist();
		SQLiteDatabase db = getDb();
		
//		String[] columns = {"track_id","track_name","track_duration","track_stream",
//				"track_rating", "album_id", "album_name", "album_image", "album_rating", "artist_name"};
		Cursor query = db.query(TABLE_FAVORITES, null, null, null, null, null, null);
		
		if(query != null){
			query.moveToFirst();
			
			while(!query.isAfterLast()){
				Track track = new TrackDatabaseBuilder().build(query);
				Album album = new AlbumDatabaseBuilder().build(query);
				PlaylistEntry entry = new PlaylistEntry();
				entry.setAlbum(album);
				entry.setTrack(track);
				playlist.addPlaylistEntry(entry);
				query.moveToNext();
			}
		}
		
		db.close();
		return playlist;
	}
	
	@Override
	public void removeFromFavorites(PlaylistEntry entry) {
		SQLiteDatabase db = getDb();
		String[] whereArgs = {""+entry.getTrack().getId()};
		db.delete(TABLE_FAVORITES, "track_id = ?", whereArgs);
		db.close();
	}

	private String queryForFileName(String playlistName, SQLiteDatabase db){
		String fileName = null;
		String[] columns = {"FileName"};
		String[] selectionArgs = {playlistName};
		Cursor query = db.query(TABLE_PLAYLIST, columns, "PlaylistName = ?", selectionArgs, null, null, null);
	
		if(query != null && query.getCount() > 0){
			int columnIndex = query.getColumnIndex("FileName");
			query.moveToFirst();
			int file_id = query.getInt(columnIndex);
			fileName = ""+file_id+SJP_EXT;
		}
	
		query.close();
	
		return fileName;
	}

	private SQLiteDatabase getDb(){
		return mActivity.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
	}

}
