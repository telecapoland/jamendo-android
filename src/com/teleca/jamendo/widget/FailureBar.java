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

package com.teleca.jamendo.widget;

import com.teleca.jamendo.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Top bar widget notifying user of failure, with possibillity of retry 
 * 
 * 
 * @author Lukasz Wisniewski
 */
public class FailureBar extends LinearLayout{
	
	protected TextView mTextView;
	protected Button mRetryButton;
	
	public FailureBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FailureBar(Context context) {
		super(context);
		init();
	}
	
	/**
	 * Sharable code between constructors
	 */
	private void init(){
		LayoutInflater.from(getContext()).inflate(R.layout.failure_bar, this);
		
		mTextView = (TextView)findViewById(R.id.ProgressTextView);
		mRetryButton = (Button)findViewById(R.id.RetryButton);
	}
	
	/**
	 * Sets informative text
	 * 
	 * @param resid
	 */
	public void setText(int resid){
		mTextView.setText(resid);
	}
	
	/**
	 * Sets action to be performed when Retry button is clicked 
	 * 
	 * @param l
	 */
	public void setOnRetryListener(OnClickListener l){
		mRetryButton.setOnClickListener(l);
	}

}
