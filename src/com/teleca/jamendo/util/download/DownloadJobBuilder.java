/*
 * Copyright (C) 2011 Teleca Poland Sp. z o.o. <android@teleca.com>
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

import android.content.ContentValues;
import android.database.Cursor;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.db.AlbumDatabaseBuilder;
import com.teleca.jamendo.db.DatabaseBuilder;
import com.teleca.jamendo.db.TrackDatabaseBuilder;

/**
 * Download job builder
 * 
 * @author Bartosz Cichosz
 * 
 */
public class DownloadJobBuilder extends DatabaseBuilder<DownloadJob> {

	private static final String DOWNLOADED = "downloaded";

	@Override
	public DownloadJob build(Cursor query) {
		Track track = new TrackDatabaseBuilder().build(query);
		Album album = new AlbumDatabaseBuilder().build(query);

		PlaylistEntry pEntry = new PlaylistEntry();
		pEntry.setAlbum(album);
		pEntry.setTrack(track);

		DownloadJob dJob = new DownloadJob(pEntry, DownloadHelper
				.getDownloadPath(), 0, JamendoApplication.getInstance()
				.getDownloadFormat());
		int progress = query.getInt(query.getColumnIndex(DOWNLOADED));
		if (progress == 1) {
			dJob.setProgress(100);
		}
		return dJob;
	}

	@Override
	public ContentValues deconstruct(DownloadJob t) {
		ContentValues values = new ContentValues();

		values.putAll(new TrackDatabaseBuilder().deconstruct(t
				.getPlaylistEntry().getTrack()));
		values.putAll(new AlbumDatabaseBuilder().deconstruct(t
				.getPlaylistEntry().getAlbum()));
		values.put(DOWNLOADED, (t.getProgress() == 100) ? 1 : 0);

		return values;
	}

}
