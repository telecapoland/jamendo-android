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

import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;
import com.teleca.jamendo.R;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

/**
 * Dialog representing lyrics
 * 
 * @author Lukasz Wisniewski
 */
public class LyricsDialog extends Dialog {
	
	private Activity mActivity;
	
	/**
	 * Remember last used track
	 */
	private static Track mTrack;
	
	/**
	 * Remember last used lyrics
	 */
	private static String mLyrics;
	
	private ViewFlipper mViewFlipper;
	private TextView mTextView;

	public LyricsDialog(Activity context, Track track) {
		super(context);
		init(context, track);
	}
	
	/**
	 * Sharable code between constructors
	 * 
	 * @param context
	 * @param track
	 */
	private void init(Activity context, Track track){
		mActivity = context;
		setContentView(R.layout.lyrics);
		setTitle(R.string.lyrics);
		
		mViewFlipper = (ViewFlipper)findViewById(R.id.LyricsViewFlipper);
		mTextView = (TextView)findViewById(R.id.LyricsTextView);
		
		if(track != mTrack){
			new LyricsTask().execute(track);
		} else {
			showLyrics();
		}
	}
	
	/**
	 * Show us the lyrics
	 */
	private void showLyrics(){
		if(mLyrics == null || mLyrics.equals("null")){
			mTextView.setText(R.string.no_lyrics);
		}else{
			mTextView.setText(mLyrics);
		}
		mViewFlipper.setDisplayedChild(1);
	}
	
	/**
	 * do background JamendoGet2Api.getTrackLyrics
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class LyricsTask extends AsyncTask<Track, WSError, String>{

		@Override
		public String doInBackground(Track... params) {
			JamendoGet2Api service = new JamendoGet2ApiImpl();
			try {
				return service.getTrackLyrics(params[0]);
			} catch (WSError e) {
				publishProgress(e);
				dismiss();
				this.cancel(true);
				return null;
			}
		}

		@Override
		public void onPostExecute(String result) {
			super.onPostExecute(result);
			mLyrics = result;
			showLyrics();
		}
		
		@Override
		protected void onProgressUpdate(WSError... values) {
			Toast.makeText(mActivity, values[0].getMessage(), Toast.LENGTH_LONG).show();
			super.onProgressUpdate(values);
		}
	}

}
