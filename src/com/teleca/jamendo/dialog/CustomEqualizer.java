package com.teleca.jamendo.dialog;

import com.teleca.jamendo.JamendoApplication;

import android.R;
import android.app.Dialog;
import android.app.Activity;
import android.content.Context;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

public class CustomEqualizer extends Dialog{
	private Equalizer mEqualizer;
	
    private LinearLayout mLinearLayout;
    private Button mButton;
    private Context mActivity;
    private Dialog mDialog = this;

    public CustomEqualizer(Activity context) {
    	super(context);
    	
    	this.mActivity = context;
    	this.mEqualizer = JamendoApplication.getInstance().getMyEqualizer();
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mLinearLayout = new LinearLayout(this.mActivity);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setPadding(20, 0, 20, 5);
        setContentView(mLinearLayout);

        getWindow().setLayout( LayoutParams.FILL_PARENT,
                			   LayoutParams.WRAP_CONTENT);

        setupEqualizerFxAndUI();
    }

    private void setupEqualizerFxAndUI() {
        short bands = mEqualizer.getNumberOfBands();

        final short minEQLevel = mEqualizer.getBandLevelRange()[0];
        final short maxEQLevel = mEqualizer.getBandLevelRange()[1];

        for (short i = 0; i < bands; i++) {
            final short band = i;

            TextView freqTextView = new TextView(this.mActivity);
            freqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            freqTextView.setText((mEqualizer.getCenterFreq(band) / 1000) + " Hz");
            mLinearLayout.addView(freqTextView);

            LinearLayout row = new LinearLayout(this.mActivity);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView minDbTextView = new TextView(this.mActivity);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            minDbTextView.setText((minEQLevel / 100) + " dB");

            TextView maxDbTextView = new TextView(this.mActivity);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            maxDbTextView.setText((maxEQLevel / 100) + " dB");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            layoutParams.setMargins(0, 0, 0, 10);
            SeekBar bar = new SeekBar(this.mActivity);
            bar.setLayoutParams(layoutParams);
            bar.setMax(maxEQLevel - minEQLevel);
            bar.setProgress(mEqualizer.getBandLevel(band) - minEQLevel);

            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    mEqualizer.setBandLevel(band, (short) (progress + minEQLevel));
                }

                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            row.addView(minDbTextView);
            row.addView(bar);
            row.addView(maxDbTextView);

            mLinearLayout.addView(row);
        }
        
        this.mButton = new Button(this.mActivity);
        this.mButton.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        this.mButton.setGravity(Gravity.CENTER_HORIZONTAL);
        this.mButton.setText(R.string.ok);
        this.mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
        
        this.mLinearLayout.addView(this.mButton);
    }
}
