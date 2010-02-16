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

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.util.ContactsAccessor;
import com.teleca.jamendo.R;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

// TODO e-mail and MMS in the future
/**
 * Allows sharing a track over SMS with people from contact list
 * 
 * @author Lukasz Wisniewski
 */
public class ShareDialog extends Dialog {
	
	private Activity mActivity;
	
	private ListView mListView;
	private EditText mEditText;
	private Button mButton;
	
	private ListAdapter mAdapter;
	
	private PlaylistEntry mPlaylistEntry;

	public ShareDialog(Activity context) {
		super(context);
		init(context);
	}

	public ShareDialog(Activity context, int theme) {
		super(context, theme);
		init(context);
	}

	public ShareDialog(Activity context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init(context);
	}
	
	/**
	 * Sharable code between constructors
	 */
	private void init(Activity context){
		mActivity = context;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_to_playlist);
		
		
		mListView = (ListView)findViewById(R.id.PlaylistListView);
		
		Cursor cursor = smsQuery();
		mActivity.startManagingCursor(cursor);
		
        mAdapter = new SimpleCursorAdapter(mActivity, 
                // Use a template that displays a text view
                android.R.layout.simple_list_item_1, 
                // Give the cursor to the list adatper
                cursor, 
                // Map the NAME column in the people database to...
                new String[] {ContactsAccessor.getInstance().getDisplayNameColumn()} ,
                // The "text1" view defined in the XML template
                new int[] {android.R.id.text1}); 

		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mOnItemClickListener);
		
		mButton = (Button)findViewById(R.id.PlaylistNewButton);
		mButton.setText(R.string.send);
		mButton.setOnClickListener(mButtonClicked);
		
		mEditText = (EditText)findViewById(R.id.PlaylistEditText);
		mEditText.setHint(R.string.phone_hint);
	}
	
	private android.view.View.OnClickListener mButtonClicked = new android.view.View.OnClickListener(){

		@Override
		public void onClick(View v) {
			sendSms(mEditText.getText().toString());
		}
		
	};
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapterView, View arg1, int position,
				long arg3) {
			SimpleCursorAdapter adapter = (SimpleCursorAdapter) adapterView.getAdapter();
			Cursor cursor = adapter.getCursor();
			cursor.moveToPosition(position);
			int columnIndex = cursor.getColumnIndex("_id");
			int personId = cursor.getInt(columnIndex);


			// get phone number based on personID
			cursor = ContactsAccessor.getInstance().phoneQuery(mActivity, Integer.toString(personId)); 
			
			// we find the phone number
			String number = ContactsAccessor.getInstance().getPhoneNumberFromCursor(cursor); 
			if(number == null){
				if(cursor != null)
					cursor.close();
				return;
			}
			sendSms(number);
			
			if(cursor != null){
				// TODO show toast "Contact has no phone number"
				cursor.close();
			}
			
			
		}
		
	};
	
	private void sendSms(String destinationAddress){
		String text = getRecommendationText();
		Log.i("jamendroid", "[sms:"+destinationAddress+"] "+text);
		try {
			ContactsAccessor.getInstance().sendSms(destinationAddress, text);
			Toast.makeText(mActivity, mActivity.getString(R.string.recommendation_sent), Toast.LENGTH_LONG).show();
		} catch (IllegalArgumentException e) {
			Toast.makeText(mActivity, mActivity.getString(R.string.sms_fail), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		this.dismiss();
	};
	
	private String getRecommendationText(){
		// TODO getPhoneLocale... or not if we're going to use intent filter
		PlaylistEntry entry = getPlaylistEntry();
		String text = mActivity.getString(R.string.song_recommendation) + ": "
		+ String.format("http://www.jamendo.com/track/%d", entry.getTrack().getId());
		return text;
	}

	public void setPlaylistEntry(PlaylistEntry mPlaylistEntry) {
		this.mPlaylistEntry = mPlaylistEntry;
	}

	public PlaylistEntry getPlaylistEntry() {
		
		// fallback to currently playing track
		if(mPlaylistEntry == null){
			return JamendoApplication.getInstance().getPlayerEngineInterface().getPlaylist().getSelectedTrack();
		}
		
		return mPlaylistEntry;
	}
	
	private Cursor smsQuery(){		
		return ContactsAccessor.getInstance().smsQuery(mActivity);
	}
}
