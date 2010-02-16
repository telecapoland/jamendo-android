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
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Widget notifying user of ongoing action 
 * 
 * @author Lukasz Wisniewski
 */
public class ProgressBar extends LinearLayout {
	
	protected TextView mTextView;
	
	public ProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ProgressBar(Context context) {
		super(context);
		init();
	}
	
	/**
	 * Sharable code between constructors
	 */
	private void init(){
		LayoutInflater.from(getContext()).inflate(R.layout.progress_bar, this);
		
		mTextView = (TextView)findViewById(R.id.ProgressTextView);
	}
	
	/**
	 * Sets informative text
	 * 
	 * @param resid
	 */
	public void setText(int resid){
		mTextView.setText(resid);
	}

}
