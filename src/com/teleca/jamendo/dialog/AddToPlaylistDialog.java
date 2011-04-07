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

import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.db.Database;
import com.teleca.jamendo.db.DatabaseImpl;
import com.teleca.jamendo.R;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Allows adding track/album to a new playlist or a selected one from the list
 * 
 * @author Lukasz Wisniewski
 */
public class AddToPlaylistDialog extends Dialog {
	
	private Activity mActivity;
	
	private ListView mListView;
	private EditText mEditText;
	private Button mButton;
	
	private PlaylistEntry mPlaylistEntry;
	private Album mAlbum;
	
	private Database mDatabase;

	public AddToPlaylistDialog(Activity context) {
		super(context);
		init(context);
	}

	public AddToPlaylistDialog(Activity context, int theme) {
		super(context, theme);
		init(context);
	}

	public AddToPlaylistDialog(Activity context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init(context);
	}
	
	/**
	 * Sharable code between constructors
	 */
	private void init(Activity context){
		mActivity = context;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_to_playlist);

		mDatabase = new DatabaseImpl(mActivity);
		ArrayList<String> availablePlaylistsAL = mDatabase.getAvailablePlaylists();
		String[] availablePlaylists = new String[availablePlaylistsAL.size()];
		availablePlaylistsAL.toArray(availablePlaylists);
		
		mListView = (ListView)findViewById(R.id.PlaylistListView);
		mListView.setAdapter(new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_list_item_1, availablePlaylists));
		mListView.setOnItemClickListener(mPlaylistItemClick);
		
		mButton = (Button)findViewById(R.id.PlaylistNewButton);
		mButton.setOnClickListener(mButtonClick);
		
		mEditText = (EditText)findViewById(R.id.PlaylistEditText);
	}
	
	public void setPlaylistEntry(PlaylistEntry mPlaylistEntry) {
		this.mPlaylistEntry = mPlaylistEntry;
	}
	
	public void setPlaylistAlbum(Album album) {
		this.mAlbum = album;
	}

	public PlaylistEntry getPlaylistEntry() {
		return mPlaylistEntry;
	}

	private android.view.View.OnClickListener mButtonClick = new android.view.View.OnClickListener(){

		@Override
		public void onClick(View v) {
			String playlistName = mEditText.getText().toString();
			addToPlaylist(playlistName);
		}
		
	};
	
	private OnItemClickListener mPlaylistItemClick = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position,
				long time) {
			String playlistName = (String)adapterView.getAdapter().getItem(position);
			addToPlaylist(playlistName);
		}
		
	};
	
	private void addToPlaylist(String playlistName){
		Playlist playlist = mDatabase.loadPlaylist(playlistName);
		if(playlist == null){
			playlist = new Playlist();
		}
		
		if(getPlaylistEntry() != null)
			playlist.addPlaylistEntry(getPlaylistEntry());
		
		if (mAlbum != null)
			playlist.addTracks(mAlbum);
		
		if(playlistName.length() == 0 || playlistName.startsWith(" "))
			return;
		
		mDatabase.savePlaylist(playlist, playlistName);
		
		Toast.makeText(AddToPlaylistDialog.this.getContext(), R.string.added_to_playlist, Toast.LENGTH_SHORT).show();
		AddToPlaylistDialog.this.cancel();
	}

}
