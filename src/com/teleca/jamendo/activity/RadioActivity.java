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

import com.teleca.jamendo.adapter.RadioAdapter;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.Radio;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;
import com.teleca.jamendo.db.DatabaseImpl;
import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Radio navigation activity
 * 
 * @author Lukasz Wisniewski
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

	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
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
		
		loadRecommendedRadios();
		
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
					mRadioAdapter.setList(mRecommendedRadios);
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
	}

	/**
	 * Open radio by id or idstr
	 */
	private OnClickListener mButtonClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			
			if(mEditText.getText().toString().length() == 0)
				return;
			
			int id = 0;
			String idstr = null;
			try {
				id = Integer.parseInt(mEditText.getText().toString()); // search by id
			} catch (NumberFormatException e) {
				idstr = mEditText.getText().toString(); // search by name
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
				mRadioAdapter.setList(radio);
				
				setupListView();
				
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (WSError e) {
				// connection problem or sth/ finish
				Toast.makeText(RadioActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
				finish();
			}

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
			try {
				Playlist playlist = new JamendoGet2ApiImpl().getRadioPlaylist(radio, 20, JamendoApplication.getInstance().getStreamEncoding());
				new DatabaseImpl(RadioActivity.this).addRadioToRecent(radio);
				PlayerActivity.launch(RadioActivity.this, playlist);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (WSError e) {
				// connection problem or sth/ finish
				Toast.makeText(RadioActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
				finish();
			}
		}

	};
	
	private void loadRecommendedRadios(){
		try {
			mRecommendedRadios = new JamendoGet2ApiImpl().getRadiosByIds(recommended_ids);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (WSError e) {
			// connection problem or sth/ finish
			Toast.makeText(RadioActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
	}
}
