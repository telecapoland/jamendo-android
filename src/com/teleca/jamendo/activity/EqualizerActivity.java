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
import com.teleca.jamendo.dialog.CustomEqualizer;

import android.app.Activity;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.view.View;
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
    private Activity mActivity = this;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.equalizer);
        mRadioGroup = (RadioGroup) findViewById(R.id.equalizerPreset);

        setupEqualizerFxAndUI();
    }

    /**
     * Create the equalizer view
     */
    private void setupEqualizerFxAndUI() {
    	// Get equalizer instance
    	mEqualizer = JamendoApplication.getInstance().getMyEqualizer();
    	mEqualizer.setEnabled(true);
    	
    	// radio button that custom the equalization
    	RadioButton custom = (RadioButton) this.mRadioGroup.getChildAt(this.mRadioGroup.getChildCount() - 1);
    	
    	// Association between radio button and prest equalization 
    	final HashMap<Integer, Short> group = new HashMap<Integer, Short>();
    	
    	// The last child of radioGroup will be the costum, so it doesn't have a associated preset
    	int radios = this.mRadioGroup.getChildCount() - 2;

    	for (int i = mEqualizer.getNumberOfPresets() - 1; i >= 0; i--) {
			RadioButton button = (RadioButton) this.mRadioGroup.getChildAt(radios);
			group.put(button.getId(), (short) i);
			
			if(mEqualizer.getCurrentPreset() == i){
				mRadioGroup.check(button.getId());
			}
			
			radios--;
		}
    	
    	// If neither preset was choosed so custom button is checked
    	if (mEqualizer.getCurrentPreset() == -1) {
			mRadioGroup.check(custom.getId());
		}
    	
    	// Activity for custom button, will always show the dialog
    	custom.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new CustomEqualizer(mActivity).show();
			}
		});
    	
    	// Event for radio button
    	this.mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
				if(group.containsKey(checkedId)){
					mRadioGroup.check(checkedId);

					// Sets preset and equalizer for the application
					mEqualizer.usePreset(group.get(checkedId));
					JamendoApplication.getInstance().getMyEqualizer().setProperties(mEqualizer.getProperties());
				}
			}
    	});
    	
    }
}