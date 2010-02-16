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

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;

import com.teleca.jamendo.activity.ArtistActivity;
import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.Artist;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;

/**
 * pre-ArtistActivity loading dialog
 * 
 * @author Lukasz Wisniewski
 */
public class ArtistLoadingDialog extends LoadingDialog<String, Artist>{

	public ArtistLoadingDialog(Activity activity, int loadingMsg,
			int failMsg) {
		super(activity, loadingMsg, failMsg);
	}
	
	/**
	 * Artist discography
	 */
	Album[] mAlbums = null; 

	@Override
	public Artist doInBackground(String... params) {
		JamendoGet2Api jamendoGet2Api = new JamendoGet2ApiImpl();
		Artist artist = null;
		try {
			artist = jamendoGet2Api.getArtist(params[0]);
			mAlbums =  jamendoGet2Api.searchForAlbumsByArtistName(params[0]);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (WSError e) {
			publishProgress(e);
			this.cancel(true);
		}
		return artist;
	}

	@Override
	public void doStuffWithResult(Artist artist) {
		Intent intent = new Intent(mActivity, ArtistActivity.class);
		intent.putExtra("artist", artist);
		ArrayList<Album> albums = new ArrayList<Album>();
		for(Album album : mAlbums){
			albums.add(album);
		}
		intent.putExtra("albums", albums);
		mActivity.startActivity(intent);
	}
	
}
