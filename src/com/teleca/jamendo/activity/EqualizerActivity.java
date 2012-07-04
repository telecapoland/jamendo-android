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

import java.util.HashMap;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;

import android.app.Activity;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * Equalizer View - Sound Settings
 * 
 * @author jessica
 *
 */
public class EqualizerActivity extends Activity{
    private Equalizer mEqualizer;

    private RadioGroup mRadioGroup;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.equalizer);
        
        mRadioGroup = (RadioGroup) findViewById(R.id.equalizerPreset);

        setupEqualizerFxAndUI();
    }

    private void setupEqualizerFxAndUI() {
    	mEqualizer = JamendoApplication.getInstance().getMyEqualizer();
    	mEqualizer.setEnabled(true);
    	
    	Log.v("Settings", JamendoApplication.getInstance().getMyEqualizer().getProperties().toString());
    	Log.v("Settings", ""+this.mRadioGroup.getChildCount());
    	
    	final HashMap<Integer, Short> group = new HashMap<Integer, Short>();
    	int radios = this.mRadioGroup.getChildCount() - 1;

    	for (int i = mEqualizer.getNumberOfPresets(); i >= 0; i--) {
			RadioButton button = (RadioButton) this.mRadioGroup.getChildAt(radios);
			group.put(button.getId(), (short) i);
			
			if(mEqualizer.getCurrentPreset() == i){
				mRadioGroup.check(button.getId());
			}
			
			radios--;
		}
    	
    	this.mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
				mEqualizer.usePreset(group.get(checkedId));
				
				mRadioGroup.check(checkedId);
				Log.v("Radio",""+mRadioGroup.getCheckedRadioButtonId());
				JamendoApplication.getInstance().getMyEqualizer().setProperties(mEqualizer.getProperties());
			}
    	});
    	
    	Log.v("Radio",""+mRadioGroup.getCheckedRadioButtonId());
    }
}