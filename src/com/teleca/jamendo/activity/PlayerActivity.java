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

import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;
import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.License;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.PlaylistRemote;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.Playlist.PlaylistPlaybackMode;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;
import com.teleca.jamendo.dialog.AddToPlaylistDialog;
import com.teleca.jamendo.dialog.LoadingDialog;
import com.teleca.jamendo.dialog.LyricsDialog;
import com.teleca.jamendo.dialog.PlayerAlbumLoadingDialog;
import com.teleca.jamendo.dialog.PlaylistRemoteLoadingDialog;
import com.teleca.jamendo.media.PlayerEngine;
import com.teleca.jamendo.media.PlayerEngineListener;
import com.teleca.jamendo.util.Helper;
import com.teleca.jamendo.util.OnSeekToListenerImp;
import com.teleca.jamendo.util.SeekToMode;
import com.teleca.jamendo.widget.ReflectableLayout;
import com.teleca.jamendo.widget.ReflectiveSurface;
import com.teleca.jamendo.widget.RemoteImageView;

/**
 * Central part of the UI. Touching cover fades in 4-way media buttons. 
 * 4-way media buttons fade out after certain amount of time. Other parts 
 * of layout are progress bar, total play time, played time, song title, 
 * artist name and jamendo slider.<br><br>
 * 
 * License information is implemented overlaying CreativeCommons logo over 
 * the album picture. Information about type of license is retrieved concurrently
 * to track bufferring.
 * 
 * @author Lukasz Wisniewski
 */
public class PlayerActivity extends Activity {

	private PlayerEngine getPlayerEngine(){
		return JamendoApplication.getInstance().getPlayerEngineInterface();
	};

	private Playlist mPlaylist;
	
	private Album mCurrentAlbum;

	// XML layout

	private TextView mArtistTextView;
	private TextView mSongTextView;
	private TextView mCurrentTimeTextView;
	private TextView mTotalTimeTextView;
	private RatingBar mRatingBar;
	private ProgressBar mProgressBar;

	private ImageButton mPlayImageButton;
	private ImageButton mNextImageButton;
	private ImageButton mPrevImageButton;
	private ImageButton mStopImageButton;
	private ImageButton mPrevListImageButton;
	private ImageButton mShuffleImageButton;
	private ImageButton mRepeatImageButton;
	private RemoteImageView mCoverImageView;
	private RemoteImageView mLicenseImageView;

	private GestureOverlayView mGesturesOverlayView;

	private Animation mFadeInAnimation;
	private Animation mFadeOutAnimation;
	
	private ReflectableLayout mReflectableLayout;
	private ReflectiveSurface mReflectiveSurface;
	
	private String mBetterRes;	
	private SlidingDrawer mSlidingDrawer;	
	private LoadingDialog mUriLoadingDialog;

	SeekToMode seekToMode;
	Handler mHandlerOfFadeOutAnimation; 
	Runnable mRunnableOfFadeOutAnimation; 

	/**
	 * Launch this Activity from the outside, with defined playlist
	 *
	 * @param c context from which Activity should be started
	 * @param a playlist to be played
	 */
	public static void launch(Context c, Playlist playlist){
		Intent intent = new Intent(c, PlayerActivity.class);
		intent.putExtra("playlist", playlist);

		/*
		 * For example, consider a task consisting of the activities: 
		 * A, B, C, D. If D calls startActivity() with an Intent that
		 * resolves to the component of activity B, then C and D will
		 * be finished and B receive the given Intent, resulting in 
		 * the stack now being: A, B. 
		 */
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
		c.startActivity(intent);
	}

	/**
	 * Launch this Activity from the outside, with defined playlist on a remote server
	 * 
	 * @param c
	 * @param playlistRemote
	 */
	public static void launch(Activity c, PlaylistRemote playlistRemote){
		Intent intent = new Intent(c, PlayerActivity.class);
		new PlaylistRemoteLoadingDialog(c, R.string.loading_playlist, R.string.loading_playlist_fail, intent).execute(playlistRemote);
	}

	/**
	 * Launch this Activity from the outside with the given album
	 *
	 * @param c Activity from which PlayerActivity should be started
	 * @param album an album to be played
	 */
	public static void launch(Activity c, Album album){
		new PlayerAlbumLoadingDialog(c, R.string.album_loading, R.string.album_fail).execute(album);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(JamendoApplication.TAG, "PlayerActivity.onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.player);

		// XML binding
		mBetterRes = getResources().getString(R.string.better_res);
		
		mArtistTextView = (TextView)findViewById(R.id.ArtistTextView);
		mSongTextView = (TextView)findViewById(R.id.SongTextView);
		//AutoScrolling of long song titles
		mSongTextView.setEllipsize(TruncateAt.MARQUEE);
		mSongTextView.setHorizontallyScrolling(true);
		mSongTextView.setSelected(true);
		
		mCurrentTimeTextView = (TextView)findViewById(R.id.CurrentTimeTextView);
		mTotalTimeTextView = (TextView)findViewById(R.id.TotalTimeTextView);
		mRatingBar = (RatingBar) findViewById(R.id.TrackRowRatingBar);

		mCoverImageView = (RemoteImageView)findViewById(R.id.CoverImageView);
		mCoverImageView.setOnClickListener(mCoverOnClickListener);
		mCoverImageView.setDefaultImage(R.drawable.no_cd_300);

		mProgressBar = (ProgressBar)findViewById(R.id.ProgressBar);
		
		mReflectableLayout = (ReflectableLayout)findViewById(R.id.ReflectableLayout);
		mReflectiveSurface = (ReflectiveSurface)findViewById(R.id.ReflectiveSurface);
		
		if(mReflectableLayout != null && mReflectiveSurface != null){
			mReflectableLayout.setReflectiveSurface(mReflectiveSurface);
			mReflectiveSurface.setReflectableLayout(mReflectableLayout);
		}

		handleIntent();
		
		//used for Fade Out Animation handle control
		mHandlerOfFadeOutAnimation = new Handler(); 
		mRunnableOfFadeOutAnimation =new Runnable(){
			public void run() {				
				if ( mFadeInAnimation.hasEnded() )
					mPlayImageButton.startAnimation(mFadeOutAnimation);
			}
			
		}; 

		mPlayImageButton = (ImageButton)findViewById(R.id.PlayImageButton);
		mPlayImageButton.setOnClickListener(mPlayOnClickListener);

		mNextImageButton = (ImageButton) findViewById(R.id.NextImageButton);		
		mNextImageButton.setOnTouchListener(mOnForwardTouchListener);

		mPrevImageButton = (ImageButton) findViewById(R.id.PrevImageButton);
		mPrevImageButton.setOnTouchListener(mOnRewindTouchListener);

		mStopImageButton = (ImageButton)findViewById(R.id.StopImageButton);
		mStopImageButton.setOnClickListener(mStopOnClickListener);

		mPrevListImageButton = (ImageButton)findViewById(R.id.PrevListImageButton);
		mPrevListImageButton.setOnClickListener(mPrevListOnClickListener);
		
		mShuffleImageButton = (ImageButton)findViewById(R.id.ShuffleImageButton);
		mShuffleImageButton.setOnClickListener(mShuffleOnClickListener);

		mRepeatImageButton = (ImageButton)findViewById(R.id.RepeatImageButton);
		mRepeatImageButton.setOnClickListener(mRepeatOnClickListener);

		mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		mFadeInAnimation.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				mHandlerOfFadeOutAnimation
						.removeCallbacks(mRunnableOfFadeOutAnimation);
				mHandlerOfFadeOutAnimation.postDelayed(
						mRunnableOfFadeOutAnimation, 7500);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// nothing here
			}

			@Override
			public void onAnimationStart(Animation animation) {
				setMediaVisible();
			}

		});

		mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mFadeOutAnimation.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				setMediaGone();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// nothing here
			}

			@Override
			public void onAnimationStart(Animation animation) {
				setFadeOutAnimation();
			}

		});
		
		mLicenseImageView = (RemoteImageView)findViewById(R.id.LicenseImageView);
		mCurrentAlbum = null;
		
		mSlidingDrawer = (SlidingDrawer)findViewById(R.id.drawer);
		
		// cupcake backwards compability
		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if(sdkVersion == Build.VERSION_CODES.CUPCAKE){
			new CupcakeListener();
		}

		mGesturesOverlayView = (GestureOverlayView) findViewById(R.id.gestures);
		mGesturesOverlayView.addOnGesturePerformedListener(JamendoApplication
				.getInstance().getPlayerGestureHandler());
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(JamendoApplication.TAG, "PlayerActivity.onResume");

		// register UI listener
		JamendoApplication.getInstance().setPlayerEngineListener(mPlayerEngineListener);

		// refresh UI
		if(getPlayerEngine() != null){
			// the playlist is empty, abort playback, show message
			if(getPlayerEngine().getPlaylist() == null || getPlayerEngine().getPlaylist().getSelectedTrack()== null){
				//if playlist comes from link service, don't close activity and wait for playlist
				if(mUriLoadingDialog != null)
				{					
					mUriLoadingDialog = null;
					
				}
				else
				{
					Toast.makeText(this, R.string.no_tracks, Toast.LENGTH_LONG).show();
					finish();	
				}
				
				return;
			}
			mPlayerEngineListener.onTrackChanged(getPlayerEngine().getPlaylist().getSelectedTrack());
		}
		
		if (mSlidingDrawer.isOpened()) {
			mSlidingDrawer.close();
		}
		//refresh shuffle and repeat icons
		if(getPlayerEngine().getPlaylist() != null){
			switch(getPlayerEngine().getPlaylist().getPlaylistPlaybackMode()){
				case NORMAL:
					mShuffleImageButton.setImageResource(R.drawable.player_shuffle_off);
					mRepeatImageButton.setImageResource(R.drawable.player_repeat_off);
					break;
				case REPEAT:
					mShuffleImageButton.setImageResource(R.drawable.player_shuffle_off);
					mRepeatImageButton.setImageResource(R.drawable.player_repeat_on);
					break;
				case SHUFFLE:
					mShuffleImageButton.setImageResource(R.drawable.player_shuffle_on);
					mRepeatImageButton.setImageResource(R.drawable.player_repeat_off);
					break;
				case SHUFFLE_AND_REPEAT:
					mShuffleImageButton.setImageResource(R.drawable.player_shuffle_on);
					mRepeatImageButton.setImageResource(R.drawable.player_repeat_on);
					break;
			}
		}

		boolean gesturesEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("gestures", true);
		mGesturesOverlayView.setEnabled(gesturesEnabled);
	}
	
	
	public void doCloseActivity(){
		finish();	
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.i(JamendoApplication.TAG, "PlayerActivity.onPause");

		// unregister UI listener
		JamendoApplication.getInstance().setPlayerEngineListener(null);
	}

	/**
	 * Makes 4-way media visible
	 */
	private void setMediaVisible(){
		mPlayImageButton.setVisibility(View.VISIBLE);
		mNextImageButton.setVisibility(View.VISIBLE);
		mPrevImageButton.setVisibility(View.VISIBLE);
		mStopImageButton.setVisibility(View.VISIBLE);
		mPrevListImageButton.setVisibility(View.VISIBLE);
		mShuffleImageButton.setVisibility(View.VISIBLE);
		mRepeatImageButton.setVisibility(View.VISIBLE);
	}

	/**
	 * Makes 4-way media gone
	 */
	private void setMediaGone(){
		mPlayImageButton.setVisibility(View.GONE);
		mNextImageButton.setVisibility(View.GONE);
		mPrevImageButton.setVisibility(View.GONE);
		mStopImageButton.setVisibility(View.GONE);
		mPrevListImageButton.setVisibility(View.GONE);
		mShuffleImageButton.setVisibility(View.GONE);
		mRepeatImageButton.setVisibility(View.GONE);
	}

	/**
	 * Sets fade out animation to 4-way media
	 */
	private void setFadeOutAnimation(){
		mPlayImageButton.setAnimation(mFadeOutAnimation);
		mNextImageButton.setAnimation(mFadeOutAnimation);
		mPrevImageButton.setAnimation(mFadeOutAnimation);
		mStopImageButton.setAnimation(mFadeOutAnimation);
		mPrevListImageButton.setAnimation(mFadeOutAnimation);
		mShuffleImageButton.setAnimation(mFadeOutAnimation);
		mRepeatImageButton.setAnimation(mFadeOutAnimation);
	}

	/**
	 * Sets fade out animation to 4-way media
	 */
	private void setFadeInAnimation(){
		mPlayImageButton.setAnimation(mFadeInAnimation);
		mNextImageButton.setAnimation(mFadeInAnimation);
		mPrevImageButton.setAnimation(mFadeInAnimation);
		mStopImageButton.setAnimation(mFadeInAnimation);
		mPrevListImageButton.setAnimation(mFadeInAnimation);
		mShuffleImageButton.setAnimation(mFadeInAnimation);
		mRepeatImageButton.setAnimation(mFadeInAnimation);
	}

	/**
	 * Launches fade in/out sequence
	 */
	private OnClickListener mCoverOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {

			if(mPlayImageButton.getVisibility() == View.GONE)
			{
				setMediaVisible();
				setFadeInAnimation();
				mPlayImageButton.startAnimation(mFadeInAnimation);
			}
		}

	};

	/**
	 * on click play/pause and open playlist if necessary
	 */
	private OnClickListener mPlayOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(getPlayerEngine().isPlaying()){
				getPlayerEngine().pause();
			} else {
				getPlayerEngine().play();
			}
		}

	};

	/**
	 * next button action
	 */
	private OnSeekToListenerImp mOnForwardTouchListener = new OnSeekToListenerImp(
			this, getPlayerEngine(), SeekToMode.EForward);
	
	/**
	 * prev button action
	 */	
	private OnSeekToListenerImp mOnRewindTouchListener = new OnSeekToListenerImp(
			this, getPlayerEngine(), SeekToMode.ERewind);
	
	
	

	/**
	 * stop button action
	 */
	private OnClickListener mStopOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			getPlayerEngine().stop();
		}

	};
	
	/**
	 * previous list button action
	 */
	private OnClickListener mPrevListOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			getPlayerEngine().prevList();
		}

	};

	/**
	 * shuffle button action
	 */
	private OnClickListener mShuffleOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			switch(getPlayerEngine().getPlaybackMode()){
				case NORMAL:
					getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.SHUFFLE);
					mShuffleImageButton.setImageResource(R.drawable.player_shuffle_on);
					break;
				case REPEAT:
					getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.SHUFFLE_AND_REPEAT);
					mShuffleImageButton.setImageResource(R.drawable.player_shuffle_on);
					break;
				case SHUFFLE:
					getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.NORMAL);
					mShuffleImageButton.setImageResource(R.drawable.player_shuffle_off);
					break;
				case SHUFFLE_AND_REPEAT:
					getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.REPEAT);
					mShuffleImageButton.setImageResource(R.drawable.player_shuffle_off);
					break;
			}
		}

	};

	/**
	 * repeat button action
	 */
	private OnClickListener mRepeatOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			switch(getPlayerEngine().getPlaybackMode()){
				case NORMAL:
					getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.REPEAT);
					mRepeatImageButton.setImageResource(R.drawable.player_repeat_on);
					break;
				case REPEAT:
					getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.NORMAL);
					mRepeatImageButton.setImageResource(R.drawable.player_repeat_off);
					break;
				case SHUFFLE:
					getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.SHUFFLE_AND_REPEAT);
					mRepeatImageButton.setImageResource(R.drawable.player_repeat_on);
					break;
				case SHUFFLE_AND_REPEAT:
					getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.SHUFFLE);
					mRepeatImageButton.setImageResource(R.drawable.player_repeat_off);
					break;
			}
		}
	};

	/**
	 * PlayerEngineListener implementation, manipulates UI
	 */
	private PlayerEngineListener mPlayerEngineListener = new PlayerEngineListener(){

		@Override
		public void onTrackChanged(PlaylistEntry playlistEntry) {
			new LicenseTask(playlistEntry.getAlbum(), mCurrentAlbum);
			mCurrentAlbum = playlistEntry.getAlbum();
			mArtistTextView.setText(playlistEntry.getAlbum().getArtistName());
			mSongTextView.setText(playlistEntry.getTrack().getName());
			mCurrentTimeTextView.setText(Helper.secondsToString(0));
			mTotalTimeTextView.setText(Helper.secondsToString(playlistEntry.getTrack().getDuration()));
			mCoverImageView.setImageUrl(playlistEntry.getAlbum().getImage().replaceAll("1.100.jpg", mBetterRes)); // Get higher resolution image 300x300
			mProgressBar.setProgress(0);
			mProgressBar.setMax(playlistEntry.getTrack().getDuration());
			mCoverImageView.performClick();
			if(mRatingBar != null){
				mRatingBar.setProgress((int) (10* playlistEntry.getAlbum().getRating()));
				mRatingBar.setMax(10);
			}
			
			if(getPlayerEngine() != null){
				if(getPlayerEngine().isPlaying()){
					mPlayImageButton.setImageResource(R.drawable.player_pause_light);
				} else {
					mPlayImageButton.setImageResource(R.drawable.player_play_light);
				}
			}
		}

		@Override
		public void onTrackProgress(int seconds) {
			mCurrentTimeTextView.setText(Helper.secondsToString(seconds));
			mProgressBar.setProgress(seconds);
		}

		@Override
		public void onTrackBuffering(int percent) {
//			int secondaryProgress = (int) (((float)percent/100)*mProgressBar.getMax());
//			mProgressBar.setSecondaryProgress(secondaryProgress);
		}

		@Override
		public void onTrackStop() {
			mPlayImageButton.setImageResource(R.drawable.player_play_light);
		}

		@Override
		public boolean onTrackStart() {
			mPlayImageButton.setImageResource(R.drawable.player_pause_light);
			return true;
		}

		@Override
		public void onTrackPause() {
			mPlayImageButton.setImageResource(R.drawable.player_play_light);
		}

		@Override
		public void onTrackStreamError() {
			Toast.makeText(PlayerActivity.this, R.string.stream_error, Toast.LENGTH_LONG).show();
		}

	};

	/**
	 * Loads playlist to the PlayerEngine
	 * 
	 * @param playlist
	 */
	private void handleIntent(){
		Log.i(JamendoApplication.TAG, "PlayerActivity.handleIntent");

		// This will be result of this intent handling
		Playlist playlist = null;

		// We need to handle Uri
		if(getIntent().getData() != null){
			
			// Check if this intent was already once parsed 
			// we don't need to do that again
			if(!getIntent().getBooleanExtra("handled", false)){
				mUriLoadingDialog = (LoadingDialog) new UriLoadingDialog(this, R.string.loading, R.string.loading_fail).execute();
			}
				
		} else {
			playlist = (Playlist) getIntent().getSerializableExtra("playlist");
			loadPlaylist(playlist);
		}
	}
	
	private void loadPlaylist(Playlist playlist){
		Log.i(JamendoApplication.TAG, "PlayerActivity.loadPlaylist");
		if(playlist == null)
			return;
		
		mPlaylist = playlist;
		if(mPlaylist != getPlayerEngine().getPlaylist()){
			//getPlayerEngine().stop();
			getPlayerEngine().openPlaylist(mPlaylist);
			getPlayerEngine().play();
		}
	}

	/**
	 * This creates playlist based on url that was passed in the intent,
	 * e.g. http://www.jamendo.com/pl/track/325654 or http://www.jamendo.com/pl/album/7505
	 * 
	 *  @author Lukasz Wisniewski
	 */
	private class UriLoadingDialog extends LoadingDialog<Void, Playlist>{

		public UriLoadingDialog(Activity activity, int loadingMsg, int failMsg) {
			super(activity, loadingMsg, failMsg);
		}

		@Override
		public Playlist doInBackground(Void... params) {
			Playlist playlist = null;

			Intent intent = getIntent();
			String action = intent.getAction();

			if(Intent.ACTION_VIEW.equals(action)){
				playlist = new Playlist();

				List<String> segments = intent.getData().getPathSegments();
				String mode = segments.get((segments.size()-2));
				int id = 0;
				try{
					id = Integer.parseInt(segments.get((segments.size()-1)));
				} catch (NumberFormatException e){
					doCancel();
					return playlist;
				}
				JamendoGet2Api service = new JamendoGet2ApiImpl();

				if(mode.equals("track")){
					try {
						Track[] tracks = service.getTracksByTracksId(new int[]{id}, JamendoApplication.getInstance().getStreamEncoding());
						Album[] albums = service.getAlbumsByTracksId(new int[]{id});
						albums[0].setTracks(tracks);
						playlist.addTracks(albums[0]);
					} catch (JSONException e) {
						Log.e(JamendoApplication.TAG, "sth went completely wrong");
						PlayerActivity.this.finish();
						e.printStackTrace();
					} catch (WSError e){
						publishProgress(e);
					}
				}

				if(mode.equals("album")){
					try {
						Album album = service.getAlbumById(id);
						Track[] tracks = service.getAlbumTracks(album, JamendoApplication.getInstance().getStreamEncoding());
						
						album.setTracks(tracks);
						playlist.addTracks(album);
					} catch (JSONException e) {
						Log.e("jamendroid", "sth went completely wrong");
						PlayerActivity.this.finish();
						e.printStackTrace();
					} catch (WSError e){
						publishProgress(e);
					}
				}
			}

			intent.putExtra("handled", true);
			return playlist;
		}

		@Override
		public void doStuffWithResult(Playlist result) {
			loadPlaylist(result);
		}

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_MENU){
			mSlidingDrawer.animateToggle();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void albumClickHandler(View target) {
		AlbumActivity.launch(this, getPlayerEngine().getPlaylist().getSelectedTrack().getAlbum());
    }

	public void artistClickHandler(View target) {
		ArtistActivity.launch(this, getPlayerEngine().getPlaylist().getSelectedTrack().getAlbum().getArtistName());
    }
	
	public void playlistClickHandler(View target) {
		PlaylistActivity.launch(this, false);
    }
	
	public void homeClickHandler(View target) {
		Intent intent = new Intent(this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
    }
	
	public void addOnClick(View v) {
		AddToPlaylistDialog dialog = new AddToPlaylistDialog(PlayerActivity.this);
		dialog.setPlaylistEntry(getPlayerEngine().getPlaylist().getSelectedTrack());
		dialog.show();
		mSlidingDrawer.animateClose();
	}
	
	public void equalizerOnClick(View v) {
		Intent intent = new Intent(this, EqualizerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	public void lyricsOnClick(View v) {
		Track track = getPlayerEngine().getPlaylist().getSelectedTrack().getTrack();
		new LyricsDialog(PlayerActivity.this, track).show();
		mSlidingDrawer.animateClose();
	}
	
	public void downloadOnClick(View v) {
		AlertDialog alertDialog = new AlertDialog.Builder(PlayerActivity.this)
		.setTitle(R.string.download_track_q)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				JamendoApplication.getInstance().getDownloadManager().download(getPlayerEngine().getPlaylist().getSelectedTrack());
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create();

		alertDialog.show();
		mSlidingDrawer.animateClose();
	}
	
	public void shareOnClick(View v) {
	        if(mPlaylist == null || mPlaylist.getSelectedTrack() == null){
	                return;
	        }
	        PlaylistEntry entry = mPlaylist.getSelectedTrack();
                Helper.share(PlayerActivity.this, entry);
		mSlidingDrawer.animateClose();
	}
	
	License mLicense;
	
	/**
	 * do background JamendoGet2Api.getAlbumLicense
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class LicenseTask extends AsyncTask<Album, WSError, License>{
		
		public LicenseTask(Album newAlbum, Album oldAlbum){
			
			boolean runQuery = true;
			
			if(oldAlbum != null && newAlbum.getId() == oldAlbum.getId()){
				runQuery = false;
			}
			
			if(runQuery){
				this.execute(newAlbum);
			}
		}
		

		@Override
		protected void onPreExecute() {
			mLicenseImageView.setImageResource(R.drawable.cc_loading);
			super.onPreExecute();
		}

		@Override
		public License doInBackground(Album... params) {	 
			
			JamendoGet2Api service = new JamendoGet2ApiImpl();
			try {
				return service.getAlbumLicense(params[0]);
			} catch (WSError e) {
				return null;
			}
		}

		@Override
		public void onPostExecute(License result) {
			super.onPostExecute(result);
			mLicense = result;
			
			if(mLicense != null){
				mLicenseImageView.setImageUrl(mLicense.getImage());
			}
		}
		
		@Override
		protected void onProgressUpdate(WSError... values) {
			Toast.makeText(PlayerActivity.this, values[0].getMessage(), Toast.LENGTH_LONG).show();
			super.onProgressUpdate(values);
		}
		
	};
	
	public void licenseClickHandler(View v) {
		if(mLicense != null){
			Intent myIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(mLicense.getUrl()));
			startActivity(myIntent);
		}
	}
	
	public class CupcakeListener implements OnClickListener{
		
		public CupcakeListener(){
			// icons
			findViewById(R.id.SliderHome).setOnClickListener(this);
			findViewById(R.id.SliderAlbum).setOnClickListener(this);
			findViewById(R.id.SliderArtist).setOnClickListener(this);
			findViewById(R.id.SliderPlaylist).setOnClickListener(this);
			
			// buttons
			findViewById(R.id.SliderLyrics).setOnClickListener(this);
			findViewById(R.id.SliderAddToPlaylist).setOnClickListener(this);
			findViewById(R.id.SliderEqualizer).setOnClickListener(this);
			findViewById(R.id.SliderShare).setOnClickListener(this);
			findViewById(R.id.SliderDownload).setOnClickListener(this);
			
			// license
			if(mLicenseImageView != null){
				mLicenseImageView.setOnClickListener(this);
			}
		}

		@Override
		public void onClick(View v) {
			
			switch(v.getId()){
			// icons
			case R.id.SliderHome:
				homeClickHandler(v);
				break;
			case R.id.SliderAlbum:
				albumClickHandler(v);
				break;
			case R.id.SliderArtist:
				artistClickHandler(v);
				break;
			case R.id.SliderPlaylist:
				playlistClickHandler(v);
				break;
				
			// buttons
			case R.id.SliderLyrics:
				lyricsOnClick(v);
				break;
			case R.id.SliderAddToPlaylist:
				addOnClick(v);
				break;
			case R.id.SliderEqualizer:
				equalizerOnClick(v);
				break;
			case R.id.SliderShare:
				shareOnClick(v);
				break;
			case R.id.SliderDownload:
				downloadOnClick(v);
				break;
				
			// license
			case R.id.LicenseImageView:
				licenseClickHandler(v);
				break;
			}
			
		}
		
	}
	
	public void onStartSeekToProcess(){		
		mHandlerOfFadeOutAnimation
				.removeCallbacks(mRunnableOfFadeOutAnimation);
	}
	
	public void onFinishSeekToProcess(){		
		mHandlerOfFadeOutAnimation
				.removeCallbacks(mRunnableOfFadeOutAnimation);
		mHandlerOfFadeOutAnimation.postDelayed(
				mRunnableOfFadeOutAnimation, 7500);		
	}
}
