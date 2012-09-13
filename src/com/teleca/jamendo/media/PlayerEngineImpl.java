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

package com.teleca.jamendo.media;

import java.io.IOException;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.Playlist.PlaylistPlaybackMode;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.audiofx.Equalizer;
import android.os.Handler;
import android.util.Log;

/**
 * Player core engine allowing playback, in other words, a
 * wrapper around Android's <code>MediaPlayer</code>, supporting
 * <code>Playlist</code> classes
 * 
 * @author Lukasz Wisniewski
 */
public class PlayerEngineImpl implements PlayerEngine {
	
	/**
	 * Time frame - used for counting number of fails within that time 
	 */
	private static final long FAIL_TIME_FRAME = 1000;
	
	/**
	 * Acceptable number of fails within FAIL_TIME_FRAME
	 */
	private static final int ACCEPTABLE_FAIL_NUMBER = 2;
	
	/**
	 * Beginning of last FAIL_TIME_FRAME
	 */
	private long mLastFailTime;
	
	/**
	 * Number of times failed within FAIL_TIME_FRAME
	 */
	private long mTimesFailed; 
	
	/**
	 * Simple MediaPlayer extensions, adds reference to the current track
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class InternalMediaPlayer extends MediaPlayer {

		/**
		 * Keeps record of currently played track, useful when dealing
		 * with multiple instances of MediaPlayer
		 */
		public PlaylistEntry playlistEntry;

		/**
		 * Still buffering
		 */
		public boolean preparing = false;

		/**
		 * Determines if we should play after preparation,
		 * e.g. we should not start playing if we are pre-buffering
		 * the next track and the old one is still playing
		 */
		public boolean playAfterPrepare = false;

	}

	/**
	 * InternalMediaPlayer instance (maybe add another one for cross-fading)
	 */
	private InternalMediaPlayer mCurrentMediaPlayer;
	
	/**
	 * Listener to the engine events
	 */
	private PlayerEngineListener mPlayerEngineListener;

	/**
	 * Playlist
	 */
	private Playlist mPlaylist = null;
	
	/**
	 * Playlist of song played before
	 */
	private Playlist prevPlaylist = null;
	
	/**
	 * Handler to the context thread
	 */
	private Handler mHandler;
	
	/**
     * Runnable periodically querying Media Player
     * about the current position of the track and
     * notifying the listener
     */
    private Runnable mUpdateTimeTask = new Runnable() {
            public void run() {

                    if(mPlayerEngineListener != null){
                        // TODO use getCurrentPosition less frequently (usage of currentTimeMillis or uptimeMillis)
                    	if(mCurrentMediaPlayer != null)
                    		mPlayerEngineListener.onTrackProgress(mCurrentMediaPlayer.getCurrentPosition()/1000);
                        mHandler.postDelayed(this, 1000);
                    }
            }
    };

	/**
	 * Default constructor
	 */
	public PlayerEngineImpl() {
		mLastFailTime = 0;
		mTimesFailed = 0;
		mHandler = new Handler();
	}

	@Override
	public void next() {
		if(mPlaylist != null){
			mPlaylist.selectNext();
			play();
		}
	}

	@Override
	public void openPlaylist(Playlist playlist) {
		if(!playlist.isEmpty()){
			prevPlaylist = mPlaylist;
			mPlaylist = playlist;
		}
		else
			mPlaylist = null;
	}

	@Override
	public void pause() {
		if(mCurrentMediaPlayer != null){
			// still preparing
			if(mCurrentMediaPlayer.preparing){
				mCurrentMediaPlayer.playAfterPrepare = false;
				return;
			}

			// check if we play, then pause
			if(mCurrentMediaPlayer.isPlaying()){
				mCurrentMediaPlayer.pause();
				if(mPlayerEngineListener != null)
					mPlayerEngineListener.onTrackPause();
				return;
			}
		}
	}

	@Override
	public void play() {
		
		if( mPlayerEngineListener.onTrackStart() == false ){
			return; // apparently sth prevents us from playing tracks
		}

		// check if there is anything to play
		if(mPlaylist != null){

			// check if media player is initialized
			if(mCurrentMediaPlayer == null){
				mCurrentMediaPlayer = build(mPlaylist.getSelectedTrack());
			}

			// check if current media player is set to our song
			if(mCurrentMediaPlayer != null && mCurrentMediaPlayer.playlistEntry != mPlaylist.getSelectedTrack()){
				cleanUp(); // this will do the cleanup job				
				mCurrentMediaPlayer = build(mPlaylist.getSelectedTrack());
			}
			
			// check if there is any player instance, if not, abort further execution 
			if(mCurrentMediaPlayer == null)
				return;

			// check if current media player is not still buffering
			if(!mCurrentMediaPlayer.preparing){

				// prevent double-press
				if(!mCurrentMediaPlayer.isPlaying()){
					// i guess this mean we can play the song
					Log.i(JamendoApplication.TAG, "Player [playing] "+mCurrentMediaPlayer.playlistEntry.getTrack().getName());
					
					final JamendoApplication app = JamendoApplication.getInstance();

					// starting timer
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    mHandler.postDelayed(mUpdateTimeTask, 1000);
                    
                    // Maintain the settings of the equalizer for the new media
                    Equalizer newEqualizer = new Equalizer(0, mCurrentMediaPlayer.getAudioSessionId());
                    short preset = app.getEqualizerPreset();
                    // special case when the preset was chosen when there was no media stream running
                    if (preset > -2) {
                        newEqualizer.usePreset(preset);
                        app.setEqualizerPreset((short) -2);
                    } else {
	                    Equalizer.Settings eqSettings = app.getEqualizerSettigns();
	                    if (eqSettings != null) {
	                        newEqualizer.setProperties(eqSettings);
						}
                    }
                    // save settings for the next equalizer
                    app.updateEqualizerSettings(newEqualizer.getProperties());
                    // Enable equalizer before media starts
                    JamendoApplication.getInstance().setMyEqualizer(newEqualizer);
                    JamendoApplication.getInstance().getMyEqualizer().setEnabled(true);
                    mCurrentMediaPlayer.start();
				}
			} else {
				// tell the mediaplayer to play the song as soon as it ends preparing
				mCurrentMediaPlayer.playAfterPrepare = true;
			}
		}
		
		// Change application media
		JamendoApplication.getInstance().setMyCurrentMedia(mCurrentMediaPlayer);
	}

	@Override
	public void prev() {
		if(mPlaylist != null){ 
			mPlaylist.selectPrev();
			play();	
		}
	}

	@Override
	public void skipTo(int index) {
		mPlaylist.select(index);
		play();
	}
	
	@Override
	public void stop() {
		cleanUp();
			
		if(mPlayerEngineListener != null){
			mPlayerEngineListener.onTrackStop();
		}
	}

	/**
	 * Stops & destroys media player
	 */
	private void cleanUp(){
		// nice clean-up job
		if(mCurrentMediaPlayer != null) {
			try{
				mCurrentMediaPlayer.stop();
			} catch (IllegalStateException e){
				// this may happen sometimes
			} finally {
				mCurrentMediaPlayer.release();
				mCurrentMediaPlayer = null;
			}
			// reset equalizer - it cannot be reused that way on API level 14+
			Equalizer eq = JamendoApplication.getInstance().getMyEqualizer();
			if (eq != null) {
				eq.release();
				JamendoApplication.getInstance().setMyEqualizer(null);
			}
		}
	}

	private InternalMediaPlayer build(PlaylistEntry playlistEntry){
		final InternalMediaPlayer mediaPlayer = new InternalMediaPlayer();
		
		// try to setup local path
		String path = JamendoApplication.getInstance().getDownloadManager().getTrackPath(playlistEntry);
		if(path == null)
			// fallback to remote one
			path = playlistEntry.getTrack().getStream();
		
		// some albums happen to contain empty stream url, notify of error, abort playback
		if(path.length() == 0){
			if(mPlayerEngineListener != null){
				mPlayerEngineListener.onTrackStreamError();
				mPlayerEngineListener.onTrackChanged(mPlaylist.getSelectedTrack());
			}
			stop();
			return null;
		}
		
		try {
			mediaPlayer.setDataSource(path);
			mediaPlayer.playlistEntry = playlistEntry;
			//mediaPlayer.setScreenOnWhilePlaying(true);

			mediaPlayer.setOnCompletionListener(new OnCompletionListener(){

				@Override
				public void onCompletion(MediaPlayer mp) {
					if(!mPlaylist.isLastTrackOnList() || mPlaylist.getPlaylistPlaybackMode() == PlaylistPlaybackMode.REPEAT || mPlaylist.getPlaylistPlaybackMode() == PlaylistPlaybackMode.SHUFFLE_AND_REPEAT ){
						next();
					}else{
						stop();
					}
				}

			});

			mediaPlayer.setOnPreparedListener(new OnPreparedListener(){

				@Override
				public void onPrepared(MediaPlayer mp) {
					mediaPlayer.preparing = false;

					// we may start playing
					if(mPlaylist.getSelectedTrack() == mediaPlayer.playlistEntry 
							&& mediaPlayer.playAfterPrepare){
						mediaPlayer.playAfterPrepare = false;
						play();
					}

				}

			});
			
			mediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener(){

				@Override
				public void onBufferingUpdate(MediaPlayer mp, int percent) {
					if(mPlayerEngineListener != null){
						mPlayerEngineListener.onTrackBuffering(percent);
					}
				}
				
			});
			
			mediaPlayer.setOnErrorListener(new OnErrorListener() {
				
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.w(JamendoApplication.TAG, "PlayerEngineImpl fail, what ("+what+") extra ("+extra+")");
						
					if(what == MediaPlayer.MEDIA_ERROR_UNKNOWN){
						// we probably lack network
						if(mPlayerEngineListener != null){
							mPlayerEngineListener.onTrackStreamError();
						}
						stop();
						return true;
					}
					
					// not sure what error code -1 exactly stands for but it causes player to start to jump songs
					// if there are more than 5 jumps without playback during 1 second then we abort 
					// further playback
					if(what == -1){
						long failTime = System.currentTimeMillis();
						if(failTime - mLastFailTime > FAIL_TIME_FRAME){
							// outside time frame
							mTimesFailed = 1;
							mLastFailTime = failTime;
							Log.w(JamendoApplication.TAG, "PlayerEngineImpl "+mTimesFailed+" fail within FAIL_TIME_FRAME");
						} else {
							// inside time frame
							mTimesFailed++;
							if(mTimesFailed > ACCEPTABLE_FAIL_NUMBER){
								Log.w(JamendoApplication.TAG, "PlayerEngineImpl too many fails, aborting playback");
								if(mPlayerEngineListener != null){
									mPlayerEngineListener.onTrackStreamError();
								}
								stop();
								return true;
							}
						}
					}
					return false;
				}
			});

			// start preparing
			Log.i(JamendoApplication.TAG, "Player [buffering] "+mediaPlayer.playlistEntry.getTrack().getName());
			mediaPlayer.preparing = true;
			mediaPlayer.prepareAsync();
			
			// this is a new track, so notify the listener
			if(mPlayerEngineListener != null){
				mPlayerEngineListener.onTrackChanged(mPlaylist.getSelectedTrack());
			}

			return mediaPlayer;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Playlist getPlaylist() {
		return mPlaylist;
	}

	@Override
	public boolean isPlaying() {

		// no media player instance
		if(mCurrentMediaPlayer == null)
			return false;

		// so there is one, let's see if it's not preparing
		if(mCurrentMediaPlayer.preparing)
			return false;

		// finally
		return mCurrentMediaPlayer.isPlaying();
	}

	@Override
	public void setListener(PlayerEngineListener playerEngineListener) {
		mPlayerEngineListener = playerEngineListener;
	}

	@Override
	public void setPlaybackMode(PlaylistPlaybackMode aMode) {
		mPlaylist.setPlaylistPlaybackMode(aMode);
	}

	@Override
	public PlaylistPlaybackMode getPlaybackMode() {
		return mPlaylist.getPlaylistPlaybackMode();
	}

	public void forward(int time) {		
		mCurrentMediaPlayer.seekTo( mCurrentMediaPlayer.getCurrentPosition()+time );
		
	}

	@Override
	public void rewind(int time) {
		mCurrentMediaPlayer.seekTo( mCurrentMediaPlayer.getCurrentPosition()-time );
	}
	
	@Override
	public void prevList() {
		if(prevPlaylist != null){
			openPlaylist(prevPlaylist);
			play();
		}
	}
}
