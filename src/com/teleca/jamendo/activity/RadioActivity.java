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

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ViewFlipper;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;
import com.teleca.jamendo.adapter.RadioAdapter;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.Radio;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;
import com.teleca.jamendo.db.DatabaseImpl;
import com.teleca.jamendo.dialog.LoadingDialog;

/**
 * Radio navigation activity
 * 
 * @author Lukasz Wisniewski
 * @author Marcin Gil
 */
public class RadioActivity extends Activity {
	
	/**
	 * statically (don't blame me) inserted recommended radios
	 */
	private static int[] recommended_ids = {
		9, // rock
		4, // dance
		5, // hiphop
		6, // jazz
		7, // lounge
		8, // pop
		283 // metal
	};
	
	/**
	 * Recommended radios
	 */
	private Radio[] mRecommendedRadios;

	/**
	 * Launch this Activity from the outside
	 *
	 * @param c context from which Activity should be started
	 */
	public static void launch(Context c){
		Intent intent = new Intent(c, RadioActivity.class);
		c.startActivity(intent);
	}

	private ListView mRadioListView;
	private RadioAdapter mRadioAdapter;
	private Button mButton;
	private EditText mEditText;
	private Spinner mSpinner;
	private ViewFlipper mViewFlipper;
	private GestureOverlayView mGestureOverlayView;

	private RadioLoadingDialog mRadioLoadingDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search);

		mRadioListView = (ListView)findViewById(R.id.SearchListView);
		mRadioAdapter = new RadioAdapter(this);
		mRadioListView.setAdapter(mRadioAdapter);
		mRadioListView.setOnItemClickListener(mRadioListListener);
		mButton = (Button)findViewById(R.id.SearchButton);
		mButton.setText(R.string.radio);
		mButton.setOnClickListener(mButtonClickListener);
		mEditText = (EditText)findViewById(R.id.SearchEditText);
		mViewFlipper = (ViewFlipper)findViewById(R.id.SearchViewFlipper);

		mSpinner = (Spinner)findViewById(R.id.SearchSpinner);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(
				this, R.array.radio_modes, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(adapter);
		mEditText.setHint(R.string.radio_hint);

		mRadioLoadingDialog = new RadioLoadingDialog(this,
				R.string.loading_recomended_radios,
				R.string.failed_recomended_radios);

		mRadioLoadingDialog.execute();

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				switch (position) {
				case 0:
					// recent
					mRadioAdapter.setList(new DatabaseImpl(RadioActivity.this).getRecentRadios(20));
					break;
				case 1:
					// recommended
					switch(mRadioLoadingDialog.getStatus()){
						case RUNNING:
							break;
						case FINISHED:
							mRadioLoadingDialog = new RadioLoadingDialog(RadioActivity.this,
								R.string.loading_recomended_radios,
								R.string.failed_recomended_radios);
							mRadioLoadingDialog.execute();
							break;
						case PENDING:
							mRadioLoadingDialog.execute();
							break;
					}
					break;

				default:
					break;
				}
				
				setupListView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
			
		});
		
		// TODO (maybe) if recent.count > 0 set to recent
		mSpinner.setSelection(1);

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

	/**
	 * Open radio by id or idstr
	 */
	private OnClickListener mButtonClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			
			if(mEditText.getText().toString().length() == 0)
				return;

			new RadioSearchDialog(RadioActivity.this, R.string.searching,
					R.string.search_fail).execute(mEditText.getText().toString());
		}

	};
	
	/**
	 * Displays no result message or results on ListView
	 */
	private void setupListView(){
		if(mRadioAdapter.getCount() > 0){
			mViewFlipper.setDisplayedChild(0); // display results
		} else {
			mViewFlipper.setDisplayedChild(1); // display no results message
		}
	}

	/**
	 * Launch radio
	 */
	private OnItemClickListener mRadioListListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			Radio radio = (Radio)mRadioAdapter.getItem(position);
			new RadioPlaylistLoadingDialog(RadioActivity.this, R.string.loading_playlist,
					R.string.loading_playlist_fail).execute(radio);
		}

	};
	
	private void loadRecommendedRadios() throws WSError {
		try {
			mRecommendedRadios = new JamendoGet2ApiImpl().getRadiosByIds(recommended_ids);
		} catch (JSONException e) {
			mRecommendedRadios = new Radio[0];
			e.printStackTrace();
		}
	}

	/**
	 * Dialog displayed while downloading recomended radios list.
	 */
	private class RadioLoadingDialog extends LoadingDialog<Void, Boolean>{

		public RadioLoadingDialog(Activity activity, int loadingMsg, int failMsg) {
			super(activity, loadingMsg, failMsg);
		}

		@Override
		public Boolean doInBackground(Void... params) {
			try {
				loadRecommendedRadios();
				if (mRecommendedRadios == null || mRecommendedRadios.length == 0) {
					return null;
				} else {
					return true;
				}
			} catch (WSError e) {
				// connection problem or sth/ finish
				publishProgress(e);
				mActivity.finish();
				return null;
			}
		}

		@Override
		public void doStuffWithResult(Boolean result) {
			if(mSpinner.getSelectedItemPosition() == 1){
				mRadioAdapter.setList(mRecommendedRadios);
				setupListView();
			}
		}
	}

	/**
	 * Dialog displayed while searching for a radio.
	 */
	private class RadioSearchDialog extends LoadingDialog<String, Radio[]> {

		public RadioSearchDialog(Activity activity, int loadingMsg, int failMsg) {
			super(activity, loadingMsg, failMsg);
		}

		@Override
		public Radio[] doInBackground(String... params) {
			String input = params[0];
			int id = 0;
			String idstr = null;
			try {
				id = Integer.parseInt(input); // search by id
			} catch (NumberFormatException e) {
				idstr = input; // search by name
			}

			Radio[] radio = null;
			try {
				JamendoGet2Api service = new JamendoGet2ApiImpl();
				if(idstr == null && id > 0){
					int[] ids = {id};
					radio = service.getRadiosByIds(ids);
				} else if (idstr != null) {
					radio = service.getRadiosByIdstr(idstr);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (WSError e) {
				// connection problem or sth/ finish
				publishProgress(e);
				finish();
			}
			return radio;
		}

		@Override
		public void doStuffWithResult(Radio[] radio) {
			mRadioAdapter.setList(radio);
			setupListView();
		}
	}

	/**
	 * Dialog displayed while downloading selected radio playlist.
	 */
	private static class RadioPlaylistLoadingDialog extends LoadingDialog<Radio, Playlist> {

		public RadioPlaylistLoadingDialog(Activity activity, int loadingMsg, int failMsg) {
			super(activity, loadingMsg, failMsg);
		}

		@Override
		public Playlist doInBackground(Radio... params) {
			Playlist playlist = null;
			try {
				Radio radio = params[0];
				playlist = new JamendoGet2ApiImpl().getRadioPlaylist(radio, 20, JamendoApplication.getInstance().getStreamEncoding());
				new DatabaseImpl(mActivity).addRadioToRecent(radio);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (WSError e) {
				// connection problem or sth/ finish
				publishProgress(e);
			}
			return playlist;
		}

		@Override
		public void doStuffWithResult(Playlist playlist) {
			if (playlist != null)
				PlayerActivity.launch(mActivity, playlist);
		}
	}
}
