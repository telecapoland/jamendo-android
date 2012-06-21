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

import com.teleca.jamendo.JamendoApplication;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Equalizer View - Sound Settings
 * 
 * @author jessica
 *
 */
public class EqualizerActivity extends Activity{
	private static final String TAG = "AudioFxDemo";

    private MediaPlayer mMediaPlayer;
    private Equalizer mEqualizer;

    private LinearLayout mLinearLayout;
    private RadioGroup mRadioGroup;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        
        mRadioGroup = new RadioGroup(this);
        mRadioGroup.setOrientation(RadioGroup.VERTICAL);
        mRadioGroup.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
        
        setContentView(mLinearLayout);

        setupEqualizerFxAndUI();
    }

    private void setupEqualizerFxAndUI() {
        // Create the Equalizer object (an AudioEffect subclass) and attach it to our media player,
        // with a default priority (0).
    	mEqualizer = JamendoApplication.getInstance().getMyEqualizer();
    	mEqualizer.setEnabled(true);
    	
    	Log.v("Settings", JamendoApplication.getInstance().getMyEqualizer().getProperties().toString());
    	
    	short presets = mEqualizer.getNumberOfPresets();
    	int checked = 5;
    	
    	for (int i = 0; i < presets; i++) {
    		final short level = (short) i;
    		
    		final RadioButton radioPreset = new RadioButton(this);
    		radioPreset.setLayoutParams(new ViewGroup.LayoutParams(
    				ViewGroup.LayoutParams.WRAP_CONTENT,
    				ViewGroup.LayoutParams.WRAP_CONTENT));
    		radioPreset.setText(mEqualizer.getPresetName((short) i));
    		radioPreset.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						mEqualizer.usePreset(level);
						Log.v("Radio",""+mRadioGroup.getCheckedRadioButtonId());
						JamendoApplication.getInstance().getMyEqualizer().setProperties(mEqualizer.getProperties());
					}
				}
    		});
    		if (((short) i) == (mEqualizer.getCurrentPreset())) {
    			radioPreset.setChecked(true);
    		}
    		
    		mRadioGroup.addView(radioPreset);
    	}
    	
    	Log.v("Radio",""+mRadioGroup.getCheckedRadioButtonId());
    	mRadioGroup.check(mRadioGroup.getCheckedRadioButtonId());
    	mLinearLayout.addView(mRadioGroup);
    }
}