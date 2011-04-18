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

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.adapter.PurpleAdapter;
import com.teleca.jamendo.adapter.PurpleEntry;
import com.teleca.jamendo.adapter.SeparatedListAdapter;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.PlaylistRemote;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;
import com.teleca.jamendo.db.Database;
import com.teleca.jamendo.db.DatabaseImpl;
import com.teleca.jamendo.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * <code>BrowsePlaylistActivity</code>
 * 
 * @author Lukasz Wisniewski
 */
public class BrowsePlaylistActivity extends Activity {

	public static final int SAVE_REQUEST_CODE = 666668;

	private Playlist mPlaylist;
	private Mode mCurrentMode;
	
	private SeparatedListAdapter mSeparatedAdapter;
	private GestureOverlayView mGestureOverlayView;

	/**
	 * Modes in which this activity can be launched (top bar)
	 */
	enum Mode{
		/**
		 * Normal navigation
		 */
		Normal,
		/**
		 * like Normal but removes activity from the stack
		 */
		Load,
		/**
		 * Save button at the top bar
		 */
		Save
	}

	/**
	 * Launch this Activity from the outside
	 *
	 * @param c
	 */
	public static void launch(Context c, Mode mode){
		Intent intent = new Intent(c, BrowsePlaylistActivity.class);
		intent.putExtra("mode", mode);
		c.startActivity(intent);
	}

	/**
	 * Save playlist, launch as a subActivity
	 * 
	 * @param a
	 * @param playlist
	 */
	public static void launchSave(Activity a, Playlist playlist){
		Intent intent = new Intent(a, BrowsePlaylistActivity.class);
		intent.putExtra("mode", Mode.Save);
		intent.putExtra("playlist", playlist);
		a.startActivityForResult(intent, SAVE_REQUEST_CODE);
	}

	// layout
	private ListView mPlaylistsListView;
	private Button mButton;
	private EditText mEditText;
	private ViewFlipper mViewFlipper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.browse_playlist);

		mPlaylistsListView = (ListView)findViewById(R.id.PlaylistsListView);
		mPlaylistsListView.setOnItemLongClickListener(mItemLongClick);
		mButton = (Button)findViewById(R.id.BrowseButton);
		mEditText = (EditText)findViewById(R.id.BrowseEditText);
		mViewFlipper = (ViewFlipper)findViewById(R.id.BrowsePlaylistViewFlipper);

		setupMode((Mode) getIntent().getSerializableExtra("mode"));

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.gestures);
		mGestureOverlayView.addOnGesturePerformedListener(JamendoApplication.getInstance().getPlayerGestureHandler());
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean gesturesEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("gestures", true);
		mGestureOverlayView.setEnabled(gesturesEnabled);
	}

	/**
	 * Save playlist (actually overwrite one selected from the ListView)
	 */
	private OnItemClickListener mSaveListListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int index,
				long time) {
			PurpleEntry entry = (PurpleEntry)adapterView.getAdapter().getItem(index);
			String playlistName = entry.getText();
			savePlaylist(playlistName);
		}
	};

	/**
	 * Load playlist (normal or load mode)
	 */
	private OnItemClickListener mLoadListListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int index,
				long time) {
			PurpleEntry entry = (PurpleEntry)adapterView.getAdapter().getItem(index);
			if(entry instanceof PlaylistPurpleEntry){
				// load from server
				PlaylistActivity.launch(BrowsePlaylistActivity.this, ((PlaylistPurpleEntry)entry).getPlaylistRemote());
			} else {
				String playlistName = entry.getText();
				Playlist playlist = new DatabaseImpl(BrowsePlaylistActivity.this).loadPlaylist(playlistName);
				JamendoApplication.getInstance().getPlayerEngineInterface().openPlaylist(playlist);
				JamendoApplication.getInstance().getPlayerEngineInterface().stop();
				// leave the activity on the stack
				if(mCurrentMode == Mode.Normal){
					PlaylistActivity.launch(BrowsePlaylistActivity.this, false);
				}
			}
			
			// remove activity from the stack
			if(mCurrentMode == Mode.Load){
				finish();
			}
		}

	};
	
	/**
	 * Long press on list, remove playlist, ask for confirmation before
	 */
	private OnItemLongClickListener mItemLongClick = new OnItemLongClickListener(){

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View arg1,
				int position, long arg3) {
			PurpleEntry entry = (PurpleEntry)adapterView.getAdapter().getItem(position);
			mPlaylistName = entry.getText();
			showDialog(DELETE_YES_NO_MESSAGE);
			return true;
		}
		
	};

	/**
	 * Saves playlist
	 */
	private OnClickListener mSaveButtonListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			savePlaylist(mEditText.getText().toString());
		}

	};
	
	/**
	 * Creates new playlist
	 */
	private OnClickListener mNewButtonListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			String playlistName = mEditText.getText().toString();
			
			if(playlistName.length() == 0 || playlistName.startsWith(" "))
				return;
			
			Database db = new DatabaseImpl(BrowsePlaylistActivity.this);
			
			if(db.loadPlaylist(playlistName) != null ){
				Toast.makeText(BrowsePlaylistActivity.this, R.string.playlist_exists, Toast.LENGTH_SHORT).show();
				return;
			}
			
			Playlist playlist = new Playlist();
			db.savePlaylist(playlist, playlistName);
			loadPlaylists();
		}

	};

	/**
	 * Apply mode to the view
	 * 
	 * @param mode
	 */
	private void setupMode(Mode mode){
		this.mCurrentMode = mode;

		if(mCurrentMode == Mode.Save){
			mPlaylist = (Playlist)getIntent().getSerializableExtra("playlist");
			mButton.setText(R.string.save);
			mButton.setOnClickListener(mSaveButtonListener );
			mPlaylistsListView.setOnItemClickListener(mSaveListListener);
		}

		if(mCurrentMode == Mode.Load ){
			mButton.setText(R.string.load);
			mPlaylistsListView.setOnItemClickListener(mLoadListListener);
		}
		
		if(mCurrentMode == Mode.Normal){
			mButton.setText(R.string.neew);
			mButton.setOnClickListener(mNewButtonListener);
			mPlaylistsListView.setOnItemClickListener(mLoadListListener);
		}

		loadPlaylists();
	}

	/**
	 * Loads available playlists from database to the list view
	 */
	private void loadPlaylists(){
		PurpleAdapter purpleAdapter = new PurpleAdapter(this);
		ArrayList<PurpleEntry> list = new ArrayList<PurpleEntry>();

		ArrayList<String> playlists = new DatabaseImpl(this).getAvailablePlaylists();

		for(String playlistName : playlists){
			PurpleEntry entry = new PurpleEntry(null, playlistName);
			list.add(entry);
		}
		purpleAdapter.setList(list);
		
		// check if we have personalized client
		String userName = PreferenceManager.getDefaultSharedPreferences(this).getString("user_name", null);

		if(mCurrentMode.equals(Mode.Save) || userName == null || userName.length() == 0){
			mPlaylistsListView.setAdapter(purpleAdapter);
		} else {
			mSeparatedAdapter = new SeparatedListAdapter(this);
			mSeparatedAdapter.addSection("Playlists on the phone", purpleAdapter);
			mPlaylistsListView.setAdapter(mSeparatedAdapter);
			new RemotePlaylistTask().execute(userName);
		}
		setupListView();
	}

	/**
	 * Saves playlist to the database and quits the activity
	 * 
	 * @param playlistName
	 */
	private void savePlaylist(String playlistName){
		Database db = new DatabaseImpl(this);
		if(db.playlistExists(playlistName)){
			mPlaylistName = playlistName;
			showDialog(OVERWRITE_YES_NO_MESSAGE);
		}
		else {
			db.savePlaylist(mPlaylist, playlistName);
			setResult(RESULT_OK, null); 
			finish();
		}
	}

	private String mPlaylistName;
	private static final int OVERWRITE_YES_NO_MESSAGE = 667;
	private static final int DELETE_YES_NO_MESSAGE = 668; 

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case OVERWRITE_YES_NO_MESSAGE:
			return new AlertDialog.Builder(this)
			.setTitle(R.string.overwrite_playlist_q)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int
						whichButton) {

					// User clicked OK, overwrite the playlist
					Database db = new DatabaseImpl(BrowsePlaylistActivity.this);
					db.savePlaylist(mPlaylist, mPlaylistName);
					setResult(RESULT_OK, null); 
					finish();
				}
			})
			.setNegativeButton(R.string.cancel, null)
			.create();
		case DELETE_YES_NO_MESSAGE:
			return new AlertDialog.Builder(this)
			.setTitle(R.string.delete_playlist_q)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int
						whichButton) {

					// User clicked OK, delete the playlist
					Database db = new DatabaseImpl(BrowsePlaylistActivity.this);
					db.deletePlaylist(mPlaylistName);
					loadPlaylists();
				}
			})
			.setNegativeButton(R.string.cancel, null)
			.create();

		default:
			return null;
		}
	}
	
	private void setupListView(){
		if(mPlaylistsListView.getCount() > 0){
			mViewFlipper.setDisplayedChild(0);
		} else {
			mViewFlipper.setDisplayedChild(1);
		}
	}
	
	public class RemotePlaylistTask extends AsyncTask<String, WSError, PlaylistRemote[]>{
		
		String mUserName;

		@Override
		protected PlaylistRemote[] doInBackground(String... params) {
			String userName = params[0];
			JamendoGet2Api service = new JamendoGet2ApiImpl();
			mUserName = userName;
			try {
				return service.getUserPlaylist(userName);
			} catch (JSONException e) {
				return null;
			} catch (WSError e) {
				publishProgress(e);
				return null;
			}
		}

		@Override
		protected void onPostExecute(PlaylistRemote[] result) {
			super.onPostExecute(result);
			
			if(result == null || result.length == 0)
				return;
			
			ArrayList<PurpleEntry> list = new ArrayList<PurpleEntry>();
			
			for(int i=0; i<result.length; i++){
				PlaylistPurpleEntry entry = new PlaylistPurpleEntry(null, result[i].getName());
				entry.setPlaylistRemote(result[i]);
				list.add(entry);
			}
			
			PurpleAdapter remotePlaylistAdapter = new PurpleAdapter(BrowsePlaylistActivity.this);
			remotePlaylistAdapter.setList(list);
			mPlaylistsListView.setAdapter(null);
			mSeparatedAdapter.addSection("Playlists on the server", remotePlaylistAdapter);
			//mSeparatedAdapter.notifyDataSetChanged();
			mPlaylistsListView.setAdapter(mSeparatedAdapter);
		}
		
		@Override
		protected void onProgressUpdate(WSError... values) {
			Toast.makeText(BrowsePlaylistActivity.this, values[0].getMessage(), Toast.LENGTH_LONG).show();
			super.onProgressUpdate(values);
		}
		
	}
	
	private class PlaylistPurpleEntry extends PurpleEntry{

		private PlaylistRemote mPlaylistRemote;

		public PlaylistPurpleEntry(Integer drawable, String text) {
			super(drawable, text);
		}

		public void setPlaylistRemote(PlaylistRemote mPlaylistRemote) {
			this.mPlaylistRemote = mPlaylistRemote;
		}

		public PlaylistRemote getPlaylistRemote() {
			return mPlaylistRemote;
		}
		
	}

}
