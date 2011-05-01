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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Window;
import android.widget.Toast;

import com.teleca.jamendo.R;

/**
 * Control of application-wide settings:
 * - WiFi only mode (default: disabled)
 * - Roaming protection (default: enabled)
 * - Last.fm integration (default: disabled)
 * 
 * @author Lukasz Wisniewski
 */
public class SettingsActivity extends PreferenceActivity {
	public static final String RESET_FIRST_RUN_PREFERENCE = "reset_firstrun";
	
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

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference.getKey().equals(RESET_FIRST_RUN_PREFERENCE)) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			prefs.edit().putBoolean(SplashscreenActivity.FIRST_RUN_PREFERENCE, true).commit();
			Toast.makeText(this, R.string.preference_firstrun_reset_title, Toast.LENGTH_SHORT).show();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
}