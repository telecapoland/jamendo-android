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

package com.teleca.jamendo.dialog;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.activity.PlayerActivity;
import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;

/**
 * pre-Player album loading dialog
 * 
 * @author Łukasz Wiśniewski
 */
public class PlayerAlbumLoadingDialog extends LoadingDialog<Album, Track[]>{
	
	private Album mAlbum;

	public PlayerAlbumLoadingDialog(Activity activity, int loadingMsg, int failMsg) {
		super(activity, loadingMsg, failMsg);
	}

	@Override
	public Track[] doInBackground(Album... params) {
		mAlbum = params[0];
		
		JamendoGet2Api service = new JamendoGet2ApiImpl();
		Track[] tracks = null;
		
		try {
			tracks = service.getAlbumTracks(mAlbum, JamendoApplication.getInstance().getStreamEncoding());
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (WSError e) {
			publishProgress(e);
			cancel(true);
		}
		return tracks;
		
	}

	@Override
	public void doStuffWithResult(Track[] tracks) {
		
		Intent intent = new Intent(mActivity, PlayerActivity.class);
		Playlist playlist = new Playlist();
		mAlbum.setTracks(tracks);
		playlist.addTracks(mAlbum);

		intent.putExtra("playlist", playlist);
		mActivity.startActivity(intent);
	}

}
