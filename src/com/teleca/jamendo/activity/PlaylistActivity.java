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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;
import com.teleca.jamendo.activity.BrowsePlaylistActivity.Mode;
import com.teleca.jamendo.adapter.PlaylistAdapter;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.PlaylistRemote;
import com.teleca.jamendo.db.Database;
import com.teleca.jamendo.db.DatabaseImpl;
import com.teleca.jamendo.dialog.PlaylistRemoteLoadingDialog;
import com.teleca.jamendo.util.Helper;
import com.teleca.jamendo.widget.AlbumBar;

// TODO autosave or save question on playlist modification
/**
 * View for playlists or favorites
 * 
 * @author Lukasz Wisniewski
 */
public class PlaylistActivity extends Activity {


	/**
	 * Launch this Activity from the outside
	 *
	 * @param artistName Artist to be presented
	 * @param favorites Favorite mode (true or false)
	 */
	public static void launch(Context c, boolean favorites){
		Intent intent = new Intent(c, PlaylistActivity.class);
		intent.putExtra("favorites", favorites);
		c.startActivity(intent);
	}
	
	/**
	 * Launch this Activity from the outside, with defined playlist on a remote server
	 * 
	 * @param c
	 * @param playlistRemote
	 */
	public static void launch(Activity c, PlaylistRemote playlistRemote){
		Intent intent = new Intent(c, PlaylistActivity.class);
		new PlaylistRemoteLoadingDialog(c, R.string.loading_playlist, R.string.loading_playlist_fail, intent).execute(playlistRemote);
	}

	private boolean mFavorites;

	private Playlist mPlaylist;
	private ListView mTracksListView;
	private PlaylistAdapter mPlaylistAdapter;
	private AlbumBar mAlbumBar;
	private ViewFlipper mViewFlipper;
	private GestureOverlayView mGestureOverlayView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.playlist);

		mFavorites = getIntent().getBooleanExtra("favorites", false);

		mPlaylistAdapter = new PlaylistAdapter(this, R.layout.playlist_row);

		mTracksListView = (ListView)findViewById(R.id.PlaylistTracksListView);
		mAlbumBar = (AlbumBar)findViewById(R.id.AlbumBar);
		mAlbumBar.setDescription(R.string.playlist_small);

		mTracksListView.setAdapter(mPlaylistAdapter);
		mTracksListView.setOnItemClickListener(mOnTracksItemClickListener);
		mTracksListView.setOnCreateContextMenuListener(mOnTracksCreateContextMenuListener);

		mViewFlipper = (ViewFlipper)findViewById(R.id.PlaylistViewFlipper);

		loadTracks();
		setupListView();

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.gestures);
		mGestureOverlayView.addOnGesturePerformedListener(JamendoApplication
				.getInstance().getPlayerGestureHandler());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(!mFavorites){
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.playlist, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Jump to the track (play it)
	 */
	private OnItemClickListener mOnTracksItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int index,
				long time) {
			mPlaylist.select(index);
			JamendoApplication.getInstance().getPlayerEngineInterface().openPlaylist(mPlaylist);
			JamendoApplication.getInstance().getPlayerEngineInterface().play();
			mAlbumBar.setAlbum(mPlaylist.getSelectedTrack().getAlbum());
			PlayerActivity.launch(PlaylistActivity.this, (Playlist)null);
		}

	};

	protected static final int CONTEXT_REMOVE = 0x01; 
	protected static final int CONTEXT_DOWNLOAD = 0x02;
	protected static final int CONTEXT_SHARE = 0x03;

	/**
	 * Operations no the playlist
	 */
	private OnCreateContextMenuListener mOnTracksCreateContextMenuListener = new OnCreateContextMenuListener(){

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			menu.add(0, CONTEXT_REMOVE, 0, R.string.remove);
			menu.add(0, CONTEXT_DOWNLOAD, 0, R.string.download);
			menu.add(0, CONTEXT_SHARE, 0, R.string.share);
			//menu.add(0, CONTEXT_MOVE, 0, R.string.move);
		}

	};

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo(); 

		switch(item.getItemId()){

		case CONTEXT_REMOVE:
			if(mFavorites){
				Database db = new DatabaseImpl(this);
				db.removeFromFavorites(mPlaylist.getTrack(menuInfo.position));
			}
			mPlaylist.remove(menuInfo.position);
			mPlaylistAdapter.notifyDataSetChanged();
			setupListView();
			break;

		case CONTEXT_DOWNLOAD:
			JamendoApplication.getInstance().getDownloadManager().download(mPlaylist.getTrack(menuInfo.position));
			break;

		case CONTEXT_SHARE:
			Helper.share(PlaylistActivity.this, mPlaylist.getTrack(menuInfo.position));
			break;

		default:

		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){

		case R.id.save_menu_item:
			BrowsePlaylistActivity.launchSave(PlaylistActivity.this, mPlaylist);
			break;

		case R.id.load_menu_item:
			BrowsePlaylistActivity.launch(PlaylistActivity.this, Mode.Load);
			break;

		default:

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		loadTracks();
		boolean gesturesEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("gestures", true);
		mGestureOverlayView.setEnabled(gesturesEnabled);
		super.onResume();
	}

	private void loadTracks(){
		if(mFavorites){
			Playlist playlist = new DatabaseImpl(this).getFavorites();
			if(playlist.isEmpty())
				return;
		}
		
		// try loading playlist off the intent
		mPlaylist = (Playlist)getIntent().getSerializableExtra("playlist");
		
		// if there is no intent, playlist might be set in the engine

		if(mPlaylist == null){
			mPlaylist = JamendoApplication.getInstance().getPlayerEngineInterface().getPlaylist();
		}

		if(mPlaylist != null){
			PlaylistEntry playlistEntry = mPlaylist.getSelectedTrack();
			if(playlistEntry != null){
				mAlbumBar.setAlbum(playlistEntry.getAlbum());
			}
		}
		mPlaylistAdapter.setPlaylist(mPlaylist);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == BrowsePlaylistActivity.SAVE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
			Toast.makeText(this, "Playlist saved", 2000).show();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setupListView(){
		if(mTracksListView.getCount() > 0){
			mViewFlipper.setDisplayedChild(0);
			//mAlbumBar.setAlbum(album);
		} else {
			mViewFlipper.setDisplayedChild(1);
		}
	}

}
