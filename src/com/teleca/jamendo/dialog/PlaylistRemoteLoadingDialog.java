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

import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.PlaylistRemote;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;

/**
 * pre-Player playlist loading dialog
 * 
 * @author Łukasz Wiśniewski
 */
public class PlaylistRemoteLoadingDialog extends LoadingDialog<PlaylistRemote, Playlist> {
	
	private Intent mIntent;

	public PlaylistRemoteLoadingDialog(Activity activity, int loadingMsg,
			int failMsg, Intent intent) {
		super(activity, loadingMsg, failMsg);
		mIntent = intent;
	}

	@Override
	public Playlist doInBackground(PlaylistRemote... params) {
		
		JamendoGet2Api service = new JamendoGet2ApiImpl();
		Playlist playlist = null;
		try {
			playlist = service.getPlaylist(params[0]);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (WSError e) {
			publishProgress(e);
			cancel(true);
		}
		return playlist;
	}

	@Override
	public void doStuffWithResult(Playlist playlist) {
		mIntent.putExtra("playlist", playlist);
		mActivity.startActivity(mIntent);
	}

}
