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
import android.media.audiofx.Equalizer.Settings;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
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

	public static final String PREFERENCE_EQUALIZER = "equalizer";

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
        // Since there is no guarantee that app equalizer may be usable
        // a global equalizer may be used only to access presets
        final Equalizer equalizer = new Equalizer(0, 0);
        Log.i(JamendoApplication.TAG, "setupEqualizerFxAndUI " + equalizer.getNumberOfPresets());
        Settings settings = JamendoApplication.getInstance().getEqualizerSettigns();

    	// Association between radio button and prest equalization 
        final SparseArray<Short> group = new SparseArray<Short>();
    	
        for (int i = equalizer.getNumberOfPresets() - 1; i >= 0; i--) {
            RadioButton button = new RadioButton(this);
            button.setText(equalizer.getPresetName((short) i));
            mRadioGroup.addView(button);
            group.put(button.getId(), (short) i);
            if (settings != null && settings.curPreset == i) {
                button.setChecked(true);
            }
        }

        // radio button that custom the equalization
        RadioButton custom = new RadioButton(this);
        custom.setText(R.string.custom);
        mRadioGroup.addView(custom);

        if (settings == null || settings.curPreset == -1) {
            mRadioGroup.check(custom.getId());
        }

        equalizer.release();
    	
    	// Activity for custom button, will always show the dialog
    	custom.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new CustomEqualizer(mActivity).show();
			}
		});
    	
    	// Event for radio button
    	this.mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
				Short preset = group.get(checkedId);
				if (preset != null) {
					mRadioGroup.check(checkedId);

					if (JamendoApplication.getInstance().isEqualizerRunning()) {
						final Equalizer eq = JamendoApplication.getInstance().getMyEqualizer();
						eq.usePreset(preset);
						JamendoApplication.getInstance().updateEqualizerSettings(eq.getProperties());
					} else {
						// save for later use
						JamendoApplication.getInstance().setEqualizerPreset(preset);
					}
				}
			}
    	});
    	
    }
}