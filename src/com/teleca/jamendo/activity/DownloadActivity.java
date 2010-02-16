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

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.adapter.DownloadJobAdapter;
import com.teleca.jamendo.util.download.DownloadInterface;
import com.teleca.jamendo.util.download.DownloadJob;
import com.teleca.jamendo.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * @author Lukasz Wisniewski
 */
public class DownloadActivity extends Activity {
	
	/**
     * Runnable periodically querying DownloadService about
     * downloads
     */
    private Runnable mUpdateTimeTask = new Runnable() {
            public void run() {
            	updateListView(mDownloadSpinner.getSelectedItemPosition());
            	mHandler.postDelayed(this, 1000);
            }
    };
    
    private Handler mHandler;
	
	private Spinner mDownloadSpinner;
	private TextView mItemCountTextView;
	private ListView mListView;
	private ViewFlipper mViewFlipper;
	
	private DownloadInterface mDownloadInterface;
	
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
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.download);
		
		mDownloadInterface = JamendoApplication.getInstance().getDownloadInterface();
		
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
	}
	
	@Override
	protected void onPause() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		super.onPause();
	}

	@Override
	protected void onResume() {
		mHandler.postDelayed(mUpdateTimeTask, 1000);
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
		if(position == lastSpinnerPosition){
			DownloadJobAdapter adapter = (DownloadJobAdapter)mListView.getAdapter();
			adapter.notifyDataSetChanged();
			return;
		}
		
		ArrayList<DownloadJob> jobs = null;
		switch (position) {
		case 0:
			// Display ALL
			jobs = mDownloadInterface.getAllDownloads();
			break;
			
		case 1:
			// Display Completed
			jobs = mDownloadInterface.getCompletedDownloads();
			break;
			
		case 2:
			// Display Queued
			jobs = mDownloadInterface.getQueuedDownloads();
			break;

		default:
			break;
		}
		
		if(jobs != null){
			mItemCountTextView.setText(jobs.size()+" "+getString(R.string.items));
			
			DownloadJobAdapter adapter = new DownloadJobAdapter(DownloadActivity.this);
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

}
