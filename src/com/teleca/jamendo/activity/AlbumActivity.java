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

import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TabHost.TabSpec;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;
import com.teleca.jamendo.adapter.PlaylistAdapter;
import com.teleca.jamendo.adapter.ReviewAdapter;
import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.Review;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.dialog.AlbumLoadingDialog;
import com.teleca.jamendo.util.DrawableAccessor;
import com.teleca.jamendo.util.Helper;
import com.teleca.jamendo.util.download.DownloadManager;

// TODO context menu for tracks
/**
 * Activity representing album
 * 
 * @author Lukasz Wisniewski
 */
public class AlbumActivity extends TabActivity{

	private Album mAlbum;
	private ListView mReviewAlbumListView;
	private ListView mAlbumTrackListView;
	private ReviewAdapter mReviewAdapter;
	private Spinner mLanguageSpinner;
	
	TabSpec mAlbumTabSpec;
	TabSpec mReviewsTabSpec;
	
	private TabHost mTabHost;
	
	private String mBetterRes;

	private GestureOverlayView mGestureOverlayView;
	/**
	 * Launch this Activity from the outside
	 *
	 * @param c Activity from which AlbumActivity should be started
	 * @param album Album to be presented
	 */
	public static void launch(Activity c, Album album){
		new AlbumLoadingDialog(c,R.string.album_loading, R.string.album_fail).execute(album);
	}

	public static void launch(
			IntentDistributorActivity c, Album album,
			int reviewId) {
		new AlbumLoadingDialog(c,R.string.album_loading, R.string.album_fail).execute(album, reviewId);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.album);
		
		mBetterRes = getResources().getString(R.string.better_res);

		mAlbum = (Album) getIntent().getSerializableExtra("album");

		mReviewAlbumListView = (ListView)findViewById(R.id.AlbumListView);
		mAlbumTrackListView = (ListView)findViewById(R.id.AlbumTrackListView);
		mLanguageSpinner = (Spinner)findViewById(R.id.LanguageSpinner);

		mReviewAdapter = new ReviewAdapter(this);
		mReviewAdapter.setListView(mReviewAlbumListView);
		mReviewAlbumListView.setAdapter(mReviewAdapter);
		
		loadReviews();
		loadTracks();
		
		setupTabs();

		int selectedReviewId = getIntent().getIntExtra("selectedReviewId", -1);
		if(selectedReviewId != -1){
			selectReview(selectedReviewId);
		}

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.gestures);
		mGestureOverlayView.addOnGesturePerformedListener(JamendoApplication.getInstance().getPlayerGestureHandler());
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean gesturesEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("gestures", true);
		mGestureOverlayView.setEnabled(gesturesEnabled);
	}

	private void setupTabs(){
		mTabHost = getTabHost();
		Bitmap coverBmp = JamendoApplication.getInstance().getImageCache().get(mAlbum.getImage());
		
		// try 300px size
		if(coverBmp == null){
			coverBmp = JamendoApplication.getInstance().getImageCache().get(mAlbum.getImage().replaceAll("1.100.jpg", mBetterRes));
		}
		
		if(coverBmp != null){
			// resize to match the tab widget
			int tabSize = (int)getResources().getDimension(R.dimen.tab_size); 
			int newWidth = tabSize;
			int newHeight = tabSize;
			float scaleWidth = ((float) newWidth) / coverBmp.getWidth();
			float scaleHeight = ((float) newHeight) / coverBmp.getHeight(); 
			// create matrix for the manipulation
			Matrix matrix = new Matrix();
			// resize the bit map
			matrix.postScale(scaleWidth, scaleHeight); 
			Bitmap resizedCoverBmp = Bitmap.createBitmap(coverBmp, 0, 0, coverBmp.getWidth(), coverBmp.getHeight(), matrix, false); 
			
			// providing backwards compability with SDK 1.5
			BitmapDrawable coverDrawable = DrawableAccessor.construct(getResources(), resizedCoverBmp);
						
			mAlbumTabSpec = mTabHost.newTabSpec("tab1").setIndicator(mAlbum.getName(), coverDrawable).setContent(R.id.TabTracks);
		} else {
			mAlbumTabSpec = mTabHost.newTabSpec("tab1").setIndicator(mAlbum.getName(), getResources().getDrawable(R.drawable.tab_no_cd)).setContent(R.id.TabTracks);
		}
		mReviewsTabSpec = mTabHost.newTabSpec("tab1").setIndicator(getString(R.string.review), getResources().getDrawable(R.drawable.tab_review)).setContent(R.id.TabReviews);
		
		mTabHost.addTab(mAlbumTabSpec);
		mTabHost.addTab(mReviewsTabSpec);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.album, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			menu.findItem(R.id.download_menu_item).setVisible(true);
		} else {
			menu.findItem(R.id.download_menu_item).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.download_menu_item:
			downloadAlbum();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("unchecked")
	private void loadReviews() {
		ArrayList<Review> reviews = (ArrayList<Review>)getIntent().getSerializableExtra("reviews");
		mReviewAdapter.setList(reviews);

		final ArrayList<String> langs = Helper.getLanguageCodes(reviews);
		langs.add(0, "all");

		ArrayAdapter<String> languageAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, Helper.getLanguageNames(langs, AlbumActivity.this));
		languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mLanguageSpinner.setAdapter(languageAdapter);

		mLanguageSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				mReviewAdapter.setLang(langs.get(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				mReviewAdapter.setLang("all");
			}
			
		});
	}
	
	private void loadTracks(){
		PlaylistAdapter playlistAdapter = new PlaylistAdapter(this);
		Playlist playlist = new Playlist();
		playlist.addTracks(mAlbum);
		playlistAdapter.setPlaylist(playlist);
		mAlbumTrackListView.setAdapter(playlistAdapter);
		mAlbumTrackListView.setOnItemClickListener(mOnTracksItemClickListener);
	}
	
	/**
	 * Jump to the track (play it)
	 */
	private OnItemClickListener mOnTracksItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int index,
				long time) {
			
			Playlist playlist = JamendoApplication.getInstance().getPlayerEngineInterface().getPlaylist();
			Track track = mAlbum.getTracks()[index];
			if (playlist == null) {
				// player's playlist is empty, create a new one with whole album and open it in the player
				playlist = new Playlist();
				playlist.addTracks(mAlbum);
				JamendoApplication.getInstance().getPlayerEngineInterface().openPlaylist(playlist);
			} 
			playlist.selectOrAdd(track, mAlbum);
			JamendoApplication.getInstance().getPlayerEngineInterface().play();
			PlayerActivity.launch(AlbumActivity.this, (Playlist)null);
		}

	};
	
	
	/**
	 * Add whole album to the download queue
	 */
	private void downloadAlbum(){
		
		AlertDialog alertDialog = new AlertDialog.Builder(AlbumActivity.this)
		.setTitle(R.string.download_album_q)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
				DownloadManager downloadManager = JamendoApplication.getInstance().getDownloadManager();
				for(Track track : mAlbum.getTracks()) {
					PlaylistEntry entry = new PlaylistEntry();
					entry.setAlbum(mAlbum);
					entry.setTrack(track);
					downloadManager.download(entry);
				}
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create();
		
		alertDialog.show();
	}


	private void selectReview(int selectedReviewId) {
		mTabHost.setCurrentTab(1);
		for(int i = 0; i < mReviewAdapter.getCount(); i++){
			if(((Review)mReviewAdapter.getItem(i)).getId() == selectedReviewId){
				mReviewAlbumListView.setSelection(i);
				return;
			}
		}
	}
}
