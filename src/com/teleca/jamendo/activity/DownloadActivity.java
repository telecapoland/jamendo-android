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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;
import com.teleca.jamendo.adapter.DownloadJobAdapter;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.media.PlayerEngine;
import com.teleca.jamendo.util.download.DownloadManager;
import com.teleca.jamendo.util.download.DownloadJob;
import com.teleca.jamendo.util.download.DownloadObserver;
import com.teleca.jamendo.util.download.DownloadProvider;

/**
 * @author Lukasz Wisniewski
 */
public class DownloadActivity extends Activity implements DownloadObserver {
	
	/**
     * Runnable periodically querying DownloadService about
     * downloads
     */
    private Runnable mUpdateTimeTask = new Runnable() {
            public void run() {
            	updateListView(mDownloadSpinner.getSelectedItemPosition());
            }
    };
    
    private Handler mHandler;
	
	private Spinner mDownloadSpinner;
	private TextView mItemCountTextView;
	private ListView mListView;
	private ViewFlipper mViewFlipper;
	private GestureOverlayView mGestureOverlayView;
	
	private DownloadManager mDownloadManager;
	private PlayerEngine mPlayerInterface;
	
	/**
	 * Launch this Activity from the outside
	 *
	 * @param c context from which Activity should be started
	 */
	public static void launch(Context c){
		Intent intent = new Intent(c, DownloadActivity.class);
		c.startActivity(intent);
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.download);
		
		mDownloadManager = JamendoApplication.getInstance().getDownloadManager();
		mPlayerInterface = JamendoApplication.getInstance().getPlayerEngineInterface();
		
		mItemCountTextView = (TextView)findViewById(R.id.ItemsCountTextView);
		
		mDownloadSpinner = (Spinner)findViewById(R.id.DownloadSpinner);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(
				this, R.array.download_modes, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mDownloadSpinner.setAdapter(adapter);
		mDownloadSpinner.setOnItemSelectedListener(mOnItemSelectedListener);
		
		mListView = (ListView) findViewById(R.id.DownloadListView);
		mViewFlipper = (ViewFlipper) findViewById(R.id.DownloadViewFlipper);
		
		mHandler = new Handler();

		registerForContextMenu(mListView);

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.gestures);
		mGestureOverlayView.addOnGesturePerformedListener(JamendoApplication
				.getInstance().getPlayerGestureHandler());
	}
	
	@Override
	protected void onPause() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		mDownloadManager.deregisterDownloadObserver(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		mDownloadManager.registerDownloadObserver(this);
		boolean gesturesEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("gestures", true);
		mGestureOverlayView.setEnabled(gesturesEnabled);
		super.onResume();
	}

	/**
	 * Spinner select action, display all, complete or queue downloads
	 */
	private OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener(){

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			updateListView(position);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
		
	};
	
	private int lastSpinnerPosition = -1;
	
	private void updateListView(int position){
		ArrayList<DownloadJob> jobs = null;
		switch (position) {
		case 0:
			// Display ALL
			jobs = mDownloadManager.getAllDownloads();
			break;
			
		case 1:
			// Display Completed
			jobs = mDownloadManager.getCompletedDownloads();
			break;
			
		case 2:
			// Display Queued
			jobs = mDownloadManager.getQueuedDownloads();
			break;

		default:
			break;
		}

		DownloadJobAdapter adapter = (DownloadJobAdapter)mListView.getAdapter();

		if(lastSpinnerPosition == position && jobs != null && jobs.size() == adapter.getCount()){
			adapter.notifyDataSetChanged();
			return;
		} else {
			mItemCountTextView.setText(jobs.size()+" "+getString(R.string.items));
			adapter = new DownloadJobAdapter(DownloadActivity.this);
			adapter.setList(jobs);
			mListView.setAdapter(adapter);
		}
		lastSpinnerPosition = position;
		setupListView();
	}
	
	private void setupListView(){
		if(mListView.getCount() > 0){
			mViewFlipper.setDisplayedChild(0);
		} else {
			mViewFlipper.setDisplayedChild(1);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(v.getId() == R.id.DownloadListView){
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.download_context, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.add_to_playlist:
			addToPlaylist(getJob(info.position));
			return true;
		case R.id.play_download:
			playNow(info.position);
			return true;
		case R.id.delete_download:
			deleteJob(getJob(info.position));
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void deleteJob(DownloadJob job) {
		mDownloadManager.deleteDownload(job);
		DownloadJobAdapter adapter = (DownloadJobAdapter) mListView.getAdapter();
		adapter.notifyDataSetChanged();
	}

	private void playNow(int position) {
		Playlist playlist = new Playlist();
		playlist.addPlaylistEntry(getJob(position).getPlaylistEntry());
		playlist.select(0);
		mPlayerInterface.openPlaylist(playlist);
		mPlayerInterface.play();
	}

	private DownloadJob getJob(int position) {
		return (DownloadJob) mListView.getAdapter().getItem(position);
	}

	private void addToPlaylist(DownloadJob job) {
		Playlist playlist = mPlayerInterface.getPlaylist();
		if(playlist == null){
			playlist = new Playlist();
			playlist.addPlaylistEntry(job.getPlaylistEntry());
			mPlayerInterface.openPlaylist(playlist);
		} else {
			playlist.addPlaylistEntry(job.getPlaylistEntry());
		}
	}

	@Override
	public void onDownloadChanged(DownloadManager manager) {
		mHandler.post(mUpdateTimeTask);
	}
}
