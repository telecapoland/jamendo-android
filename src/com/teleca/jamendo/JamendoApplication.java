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

package com.teleca.jamendo;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Equalizer.Settings;
import android.preference.PreferenceManager;

import com.teleca.jamendo.activity.EqualizerActivity;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.Playlist.PlaylistPlaybackMode;
import com.teleca.jamendo.api.util.Caller;
import com.teleca.jamendo.api.util.RequestCache;
import com.teleca.jamendo.gestures.GesturesHandler;
import com.teleca.jamendo.gestures.PlayerGestureCommandRegiser;
import com.teleca.jamendo.media.PlayerEngine;
import com.teleca.jamendo.media.PlayerEngineListener;
import com.teleca.jamendo.service.PlayerService;
import com.teleca.jamendo.util.ImageCache;
import com.teleca.jamendo.util.download.DownloadManager;
import com.teleca.jamendo.util.download.DownloadManagerImpl;

/**
 * Singleton with hooks to Player and Download Service
 * 
 * @author Lukasz Wisniewski
 */
public class JamendoApplication extends Application {

	/**
	 * Tag used for DDMS logging
	 */
	public static String TAG = "jamendo";

	/**
	 * Singleton pattern
	 */
	private static JamendoApplication instance;

	/**
	 * Image cache, one for all activities and orientations
	 */
	private ImageCache mImageCache;

	/**
	 * Web request cache, one for all activities and orientations
	 */
	private RequestCache mRequestCache;

	/**
	 * Service player engine
	 */
	public PlayerEngine mServicePlayerEngine;
	
	/**
	 * Media player playing
	 */
	private MediaPlayer mCurrentMedia;

	/**
	 * Equalizer instance for runtime manipulation
	 */
	private Equalizer mEqualizer;
	
	/**
	 * Equalizer settings
	 */
	private Equalizer.Settings mEqualizerSettings;

	/**
	 * Intent player engine
	 */
	private PlayerEngine mIntentPlayerEngine;

	/**
	 * Player engine listener
	 */
	private PlayerEngineListener mPlayerEngineListener;

	/**
	 * Stored in Application instance in case we destroy Player service
	 */

	private Playlist mPlaylist;

	/**
	 * Provides interface for download related actions.
	 */
	private DownloadManager mDownloadManager;

	/**
	 * Handler for player related gestures.
	 */
	private GesturesHandler mPlayerGestureHandler;

	public static JamendoApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mImageCache = new ImageCache();
		mRequestCache = new RequestCache();

		Caller.setRequestCache(mRequestCache);
		instance = this;

		mDownloadManager = new DownloadManagerImpl(this);
		restoreEqualizerSettings();
	}

	/**
	 * Access to global image cache across Activity instances
	 * 
	 * @return
	 */
	public ImageCache getImageCache() {
		return mImageCache;
	}

	/**
	 * This setter should be only used for setting real player engine interface,
	 * e.g. used to pass Player Service's engine
	 * 
	 * @param playerEngine
	 */
	public void setConcretePlayerEngine(PlayerEngine playerEngine) {
		this.mServicePlayerEngine = playerEngine;
	}
	
	public void setMyCurrentMedia(MediaPlayer player){
		this.mCurrentMedia = player;
	}

	public void setMyEqualizer(Equalizer equalizer){
		this.mEqualizer = equalizer;
	}
	
	public Equalizer getMyEqualizer(){
		return this.mEqualizer;
	}

	public Equalizer.Settings getEqualizerSettigns() {
		return mEqualizerSettings;
	}

	public void updateEqualizerSettings(Equalizer.Settings settings) {
		if (isEqualizerRunning()) {
			// update running equalizer
			try {
				mEqualizer.setProperties(settings);
			} catch (UnsupportedOperationException e) {
				// applying equalizer settings after resuming
				// from pause is not supported
				// it may be ignored - the settings will remain unchanged
			}
		}
		mEqualizerSettings = settings;
		storeEqualizerSettings(mEqualizerSettings);
	}

	private void storeEqualizerSettings(Settings equalizerSettings) {
		PreferenceManager.getDefaultSharedPreferences(this).edit()
			.putString(EqualizerActivity.PREFERENCE_EQUALIZER, equalizerSettings.toString())
			.apply();
	}

	private void restoreEqualizerSettings() {
		String settingsStr = PreferenceManager.getDefaultSharedPreferences(this)
			.getString(EqualizerActivity.PREFERENCE_EQUALIZER, null);
		if (settingsStr != null && settingsStr.length() > 0) {
			mEqualizerSettings = new Settings(settingsStr);
		}
	}

	public boolean isEqualizerRunning() {
		if (mEqualizer != null) {
			try {
				mEqualizer.getProperties();
				return true;
			} catch (RuntimeException e) {
				// this will be thrown if Equalizer instance is unusable
				// it happens e.g. on ICS and JB
			}
		}
		return false;
	}

	/**
	 * Equalizer preset to use when the next time an Equalizer
	 * instance is created.
	 * -1 is reserved for custom preset
	 * -2 is reserved for no preset
	 */
	private short mEqualizerPreset = -2;
	public void setEqualizerPreset(short preset) {
		mEqualizerPreset = preset;
	}

	public short getEqualizerPreset() {
		return mEqualizerPreset;
	}

	/**
	 * This getter allows performing logical operations on the player engine's
	 * interface from UI space
	 * 
	 * @return
	 */
	public PlayerEngine getPlayerEngineInterface() {
		// request service bind
		if (mIntentPlayerEngine == null) {
			mIntentPlayerEngine = new IntentPlayerEngine();
		}
		return mIntentPlayerEngine;
	}

	public GesturesHandler getPlayerGestureHandler(){
		if(mPlayerGestureHandler == null){
			mPlayerGestureHandler = new GesturesHandler(this, new PlayerGestureCommandRegiser(getPlayerEngineInterface()));
		}
		return mPlayerGestureHandler;
	}

	/**
	 * This function allows to add listener to the concrete player engine
	 * 
	 * @param l
	 */
	public void setPlayerEngineListener(PlayerEngineListener l) {
		getPlayerEngineInterface().setListener(l);
	}

	/**
	 * This function is used by PlayerService on ACTION_BIND_LISTENER in order
	 * to get to Application's exposed listener.
	 * 
	 * @return
	 */
	public PlayerEngineListener fetchPlayerEngineListener() {
		return mPlayerEngineListener;
	}

	/**
	 * Returns current playlist, used in PlayerSerive in onStart method
	 * 
	 * @return
	 */
	public Playlist fetchPlaylist() {
		return mPlaylist;
	}

	/**
	 * Retrieves application's version number from the manifest
	 * 
	 * @return
	 */
	public String getVersion() {
		String version = "0.0.0";

		PackageManager packageManager = getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(
					getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return version;
	}

	public String getDownloadFormat() {
		return PreferenceManager.getDefaultSharedPreferences(this).getString(
				"download_format", JamendoGet2Api.ENCODING_MP3);
	}

	public String getStreamEncoding() {
		// http://groups.google.com/group/android-developers/msg/c546760177b22197
		// According to JBQ: ogg files are supported but not streamable
		return JamendoGet2Api.ENCODING_MP3;
	}

	public DownloadManager getDownloadManager() {
		return mDownloadManager;
	}

	/**
	 * Since 0.9.8.7 we embrace "bindless" PlayerService thus this adapter. No
	 * big need of code refactoring, we just wrap sending intents around defined
	 * interface
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class IntentPlayerEngine implements PlayerEngine {

		@Override
		public Playlist getPlaylist() {
			return mPlaylist;
		}

		@Override
		public boolean isPlaying() {
			if (mServicePlayerEngine == null) {
				// service does not exist thus no playback possible
				return false;
			} else {
				return mServicePlayerEngine.isPlaying();
			}
		}

		@Override
		public void next() {
			if (mServicePlayerEngine != null) {
				playlistCheck();
				mServicePlayerEngine.next();
			} else {
				startAction(PlayerService.ACTION_NEXT);
			}
		}

		@Override
		public void openPlaylist(Playlist playlist) {
			mPlaylist = playlist;
			if(mServicePlayerEngine != null){
				mServicePlayerEngine.openPlaylist(playlist);
			}
		}

		@Override
		public void pause() {
			if (mServicePlayerEngine != null) {
				mServicePlayerEngine.pause();
			}
		}

		@Override
		public void play() {
			if (mServicePlayerEngine != null) {
				playlistCheck();
				mServicePlayerEngine.play();
			} else {
				startAction(PlayerService.ACTION_PLAY);
			}
		}

		@Override
		public void prev() {
			if (mServicePlayerEngine != null) {
				playlistCheck();
				mServicePlayerEngine.prev();
			} else {
				startAction(PlayerService.ACTION_PREV);
			}
		}

		@Override
		public void setListener(PlayerEngineListener playerEngineListener) {
			mPlayerEngineListener = playerEngineListener;
			// we do not want to set this listener if Service
			// is not up and a new listener is null
			if (mServicePlayerEngine != null || mPlayerEngineListener != null) {
				startAction(PlayerService.ACTION_BIND_LISTENER);
			}
		}

		@Override
		public void skipTo(int index) {
			if (mServicePlayerEngine != null) {
				mServicePlayerEngine.skipTo(index);
			}
		}

		@Override
		public void stop() {
			startAction(PlayerService.ACTION_STOP);
			// stopService(new Intent(JamendoApplication.this,
			// PlayerService.class));
		}

		private void startAction(String action) {
			Intent intent = new Intent(JamendoApplication.this,
					PlayerService.class);
			intent.setAction(action);
			startService(intent);
		}

		/**
		 * This is required if Player Service was binded but playlist was not
		 * passed from Application to Service and one of buttons: play, next,
		 * prev is pressed
		 */
		private void playlistCheck() {
			if (mServicePlayerEngine != null) {
				if (mServicePlayerEngine.getPlaylist() == null
						&& mPlaylist != null) {
					mServicePlayerEngine.openPlaylist(mPlaylist);
				}
			}
		}

		@Override
		public void setPlaybackMode(PlaylistPlaybackMode aMode) {
			mPlaylist.setPlaylistPlaybackMode(aMode);
		}

		@Override
		public PlaylistPlaybackMode getPlaybackMode() {
			return mPlaylist.getPlaylistPlaybackMode();
		}
		
		@Override
		public void forward(int time) {
			if(mServicePlayerEngine != null){				
				mServicePlayerEngine.forward( time );
			}
			
		}

		@Override
		public void rewind(int time) {
			if(mServicePlayerEngine != null){				
				mServicePlayerEngine.rewind( time );
			}
			
		}

		@Override
		public void prevList() {
			if(mServicePlayerEngine != null){
				mServicePlayerEngine.prevList();
			}
			
		}
		
	}
}
