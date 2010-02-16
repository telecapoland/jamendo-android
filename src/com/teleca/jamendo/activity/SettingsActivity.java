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

import com.teleca.jamendo.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;

/**
 * Control of application-wide settings:
 * - WiFi only mode (default: disabled)
 * - Roaming protection (default: enabled)
 * - Last.fm integration (default: disabled)
 * 
 * @author Lukasz Wisniewski
 */
public class SettingsActivity extends PreferenceActivity {
	
	/**
	 * Launch this Activity from the outside
	 *
	 * @param c context from which Activity should be started
	 */
	public static void launch(Context c){
		Intent intent = new Intent(c, SettingsActivity.class);
		c.startActivity(intent);
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		setContentView(R.layout.settings);
	}
	
}