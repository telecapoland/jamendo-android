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

package com.teleca.jamendo.activity;

import java.util.ArrayList;

import org.json.JSONException;

import com.teleca.jamendo.adapter.AlbumGridAdapter;
import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.Artist;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;
import com.teleca.jamendo.dialog.AddToPlaylistDialog;
import com.teleca.jamendo.dialog.ArtistLoadingDialog;
import com.teleca.jamendo.dialog.LoadingDialog;
import com.teleca.jamendo.widget.ArtistBar;
import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;

import android.app.Activity;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * Artist View - discography, webpage & donate buttons
 * 
 * @author Lukasz Wisniewski
 */
public class ArtistActivity extends Activity {

	private ArtistBar mArtistBar;
	private AlbumGridAdapter mAlbumGridAdapter;
	private GridView mAlbumGridView;
	private Button mDonateButton;
	private Button mWebpageButton;

	private GestureOverlayView mGestureOverlayView;

	private Artist mArtist;

	/**
	 * Launch this Activity from the outside
	 *
	 * @param c context from which Activity should be started
	 * @param artistName Artist to be presented
	 */
	public static void launch(Activity c, String artistName){
		new ArtistLoadingDialog(c,R.string.artist_loading, R.string.artist_fail).execute(artistName);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.artist);

		mArtistBar = (ArtistBar)findViewById(R.id.ArtistBar);
		mAlbumGridView = (GridView)findViewById(R.id.AlbumGridView);

		mArtistBar.setDescription(R.string.discography);

		mArtist = (Artist) getIntent().getSerializableExtra("artist");
		mAlbumGridAdapter = new AlbumGridAdapter(this);
		mArtistBar.setArtist(mArtist);

		loadAlbums();

		mAlbumGridView.setOnItemClickListener(mOnItemClickListener);
		mAlbumGridView.setOnItemLongClickListener(mOnItemLongClickListener);

		mDonateButton = (Button)findViewById(R.id.DonateButton);
		mDonateButton.setOnClickListener(mDonateClick);
		mWebpageButton = (Button)findViewById(R.id.WebpageButton);
		mWebpageButton.setOnClickListener(mWebpageClick);
		
		Toast.makeText(ArtistActivity.this, R.string.long_press_playlist, Toast.LENGTH_SHORT).show();

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.gestures);
		mGestureOverlayView.addOnGesturePerformedListener(JamendoApplication.getInstance().getPlayerGestureHandler());
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean gesturesEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("gestures", true);
		mGestureOverlayView.setEnabled(gesturesEnabled);
	}

	@SuppressWarnings("unchecked")
	private void loadAlbums() {
//		JamendoGet2Api server = new JamendoGet2ApiImpl();
//		Album[] albums = server.searchForAlbumsByArtist(artistName);
		ArrayList<Album> albums = (ArrayList<Album>)getIntent().getSerializableExtra("albums");
		mAlbumGridAdapter.setList(albums);
		mAlbumGridView.setAdapter(mAlbumGridAdapter);
	}

	/**
	 * Album grid OnItemClickListener, launches Album Activity
	 */
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position,
				long time) {
			Album album = (Album)mAlbumGridAdapter.getItem(position);
			AlbumActivity.launch(ArtistActivity.this, album);
		}

	};

	/**
	 * Long press adds album to the current playlist
	 */
	private OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener(){

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view,
				int position, long time) {
			Album album = (Album)mAlbumGridView.getAdapter().getItem(position);
			new AddToPlaylistLoadingDialog(
					ArtistActivity.this, 
					R.string.adding_to_playlist, 
					R.string.adding_to_playlist_fail
					).execute(album);
			return true;
		}

	};

	/**
	 * Donation link
	 */
	private OnClickListener mDonateClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			Intent myIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(mArtist.getUrl()+"/donate"));
			startActivity(myIntent); 
		}

	};

	/**
	 * Artist's webpage
	 */
	private OnClickListener mWebpageClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			Intent myIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(mArtist.getUrl()));
			startActivity(myIntent); 
		}

	};
	
	/**
	 * Loading progress dialog adding asynchronously 
	 * tracks from album to playlist
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class AddToPlaylistLoadingDialog extends LoadingDialog<Album, Track[] >{
		
		private Album mAlbum;

		public AddToPlaylistLoadingDialog(Activity activity, int loadingMsg,
				int failMsg) {
			super(activity, loadingMsg, failMsg);
		}

		@Override
		public Track[] doInBackground(Album... params) {
			mAlbum = params[0];
			
			JamendoGet2Api service = new JamendoGet2ApiImpl();
			Track[] tracks = null;
			
			try {
				tracks =  service.getAlbumTracks(mAlbum, JamendoApplication.getInstance().getStreamEncoding());
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (WSError e){
				publishProgress(e);
			}
			
			return tracks;
		}

		@Override
		public void doStuffWithResult(Track[] tracks) {
			AddToPlaylistDialog dialog = new AddToPlaylistDialog(ArtistActivity.this);
			mAlbum.setTracks(tracks);
			dialog.setPlaylistAlbum(mAlbum);
			dialog.show();
		}
		
	}

}
