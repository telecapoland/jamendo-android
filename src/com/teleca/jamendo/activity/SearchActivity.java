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

import com.teleca.jamendo.adapter.AlbumAdapter;
import com.teleca.jamendo.adapter.PlaylistRemoteAdapter;
import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.PlaylistRemote;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;
import com.teleca.jamendo.dialog.LoadingDialog;
import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Allows content searching on Jamendo services
 * 
 * @author Lukasz Wisniewski
 */
public class SearchActivity extends Activity {

	enum SearchMode{
		Artist,
		Tag,
		UserPlaylist,
		UserStarredAlbums
	};

	private Spinner mSearchSpinner;
	private ListView mSearchListView;
	private EditText mSearchEditText;
	private Button mSearchButton;
	private ViewFlipper mViewFlipper;
	private GestureOverlayView mGestureOverlayView;

	private PlaylistRemote[] mPlaylistRemotes = null;

	private SearchMode mSearchMode;

	/**
	 * Launch this Activity from the outside
	 *
	 * @param c context from which Activity should be started
	 */
	public static void launch(Context c){
		Intent intent = new Intent(c, SearchActivity.class);
		c.startActivity(intent);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search);

		mSearchSpinner = (Spinner)findViewById(R.id.SearchSpinner);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(
				this, R.array.search_modes, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSearchSpinner.setAdapter(adapter);

		mSearchButton = (Button)findViewById(R.id.SearchButton);
		mSearchButton.setOnClickListener(mSearchButtonListener);

		mSearchEditText = (EditText)findViewById(R.id.SearchEditText);
		mSearchListView = (ListView)findViewById(R.id.SearchListView);

		mViewFlipper = (ViewFlipper)findViewById(R.id.SearchViewFlipper);
		if(mSearchListView.getCount() == 0){
			mViewFlipper.setDisplayedChild(2); // search list hint
		}

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.gestures);
		mGestureOverlayView.addOnGesturePerformedListener(JamendoApplication
				.getInstance().getPlayerGestureHandler());
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean gesturesEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("gestures", true);
		mGestureOverlayView.setEnabled(gesturesEnabled);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mSearchMode = (SearchMode) savedInstanceState.getSerializable("mode");
		if(mSearchMode != null){
			if(mSearchMode.equals(SearchMode.Artist) 
					|| mSearchMode.equals(SearchMode.Tag)
					|| mSearchMode.equals(SearchMode.UserStarredAlbums)){
				AlbumAdapter adapter = new AlbumAdapter(this);
				adapter.setList((ArrayList<Album>) savedInstanceState.get("values"));
				mSearchListView.setAdapter(adapter);
				mSearchListView.setOnItemClickListener(mAlbumClickListener);
			}
			
			if(mSearchMode.equals(SearchMode.UserPlaylist)) {
				PlaylistRemoteAdapter adapter = new PlaylistRemoteAdapter(this); 
				adapter.setList((ArrayList<PlaylistRemote>) savedInstanceState.get("values"));
				mSearchListView.setAdapter(adapter);
				mSearchListView.setOnItemClickListener(mPlaylistClickListener);
			}
			
			mViewFlipper.setDisplayedChild(savedInstanceState.getInt("flipper_page"));
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(mSearchMode != null){
			outState.putSerializable("mode", mSearchMode);
			if(mSearchMode.equals(SearchMode.Artist) 
					|| mSearchMode.equals(SearchMode.Tag) 
					|| mSearchMode.equals(SearchMode.UserStarredAlbums)){
				AlbumAdapter adapter = (AlbumAdapter)mSearchListView.getAdapter();
				outState.putSerializable("values", adapter.getList());
			}

			if(mSearchMode.equals(SearchMode.UserPlaylist)) {
				PlaylistRemoteAdapter adapter = (PlaylistRemoteAdapter)mSearchListView.getAdapter();
				outState.putSerializable("values", adapter.getList());
			}
			
			outState.putInt("flipper_page", mViewFlipper.getDisplayedChild());
		}
		super.onSaveInstanceState(outState);
	}

	private OnClickListener mSearchButtonListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			new SearchingDialog(SearchActivity.this,
					R.string.searching,
					R.string.search_fail)
			.execute(mSearchSpinner.getSelectedItemPosition());	
		}

	};

	private OnItemClickListener mAlbumClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position,
				long time) {
			Album album = (Album)adapterView.getItemAtPosition(position);
			PlayerActivity.launch(SearchActivity.this, album);
		}

	};

	private OnItemClickListener mPlaylistClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapterView, View arg1, int position,
				long arg3) {
			PlaylistRemote playlistRemote = (PlaylistRemote) adapterView.getItemAtPosition(position);
			PlayerActivity.launch(SearchActivity.this, playlistRemote);
		}

	};

	/**
	 * Allows cancelling search query
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class SearchingDialog extends LoadingDialog<Integer, Integer>{

		private Integer mSearchMode;
		private BaseAdapter mAdapter;

		public SearchingDialog(Activity activity, int loadingMsg, int failMsg) {
			super(activity, loadingMsg, failMsg);
		}

		@Override
		public Integer doInBackground(Integer... params) {
			mSearchMode = params[0];
			switch(mSearchMode){
			case 0:
				// artist search
				albumSearch(0); 
				break;
			case 1:
				// tag search
				albumSearch(1); 
				break;
			case 2:
				// playlist search
				playlistSearch(); 
				break;
			case 3:
				// starred album search
				albumSearch(3); 
				break;
			default:
			}
			return mSearchMode;
		}

		@Override
		public void doStuffWithResult(Integer result) {
			mSearchListView.setAdapter(mAdapter);

			if(mSearchListView.getCount() > 0){
				mViewFlipper.setDisplayedChild(0); // display results
			} else {
				mViewFlipper.setDisplayedChild(1); // display no results message
			}

			// results are albums
			if(mSearchMode.equals(0) || mSearchMode.equals(1) ||  mSearchMode.equals(3)){
				mSearchListView.setOnItemClickListener(mAlbumClickListener);
			}

			// results are playlists
			if(mSearchMode.equals(2)){
				mSearchListView.setOnItemClickListener(mPlaylistClickListener);
			}
		}

		private void albumSearch(int id){
			JamendoGet2Api service = new JamendoGet2ApiImpl();
			String query = mSearchEditText.getText().toString();
			Album[] albums = null;
			try {
				switch (id) {
				case 0:
					albums = service.searchForAlbumsByArtist(query);
					SearchActivity.this.mSearchMode = SearchMode.Artist;
					break;
				case 1:
					albums = service.searchForAlbumsByTag(query);
					SearchActivity.this.mSearchMode = SearchMode.Tag;
					break;
				case 3:
					albums = service.getUserStarredAlbums(query);
					SearchActivity.this.mSearchMode = SearchMode.UserStarredAlbums;
					break;

				default:
					return;
				}

				AlbumAdapter albumAdapter = new AlbumAdapter(SearchActivity.this); 
				albumAdapter.setList(albums);
				albumAdapter.setListView(mSearchListView);
				mAdapter = albumAdapter;

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (WSError e) {
				publishProgress(e);
				this.cancel(true);
			}
		}

		private void playlistSearch(){
			JamendoGet2Api service = new JamendoGet2ApiImpl();
			String user = mSearchEditText.getText().toString();
			try {
				mPlaylistRemotes = service.getUserPlaylist(user);
				if(mPlaylistRemotes != null){
					PlaylistRemoteAdapter purpleAdapter = new PlaylistRemoteAdapter(SearchActivity.this);				
					purpleAdapter.setList(mPlaylistRemotes);
					mAdapter = purpleAdapter;
					SearchActivity.this.mSearchMode = SearchMode.UserPlaylist;
				}

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (WSError e) {
				publishProgress(e);
				this.cancel(true);
			}
		}

	}

}
