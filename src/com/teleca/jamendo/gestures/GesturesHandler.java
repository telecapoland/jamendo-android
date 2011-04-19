package com.teleca.jamendo.gestures;

import java.util.ArrayList;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.util.Log;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;

/**
 * Handler for gestures. When gesture is performed, it is checked if thr
 * register contains apropriate command to execute.
 * 
 * @author Bartosz Cichosz
 * 
 */
public class GesturesHandler implements OnGesturePerformedListener {

	private GestureLibrary mLibrary;
	private boolean mLoaded = false;

	private GestureCommandRegister mRegister;

	public GesturesHandler(Context context, GestureCommandRegister register) {
		// mLibrary = GestureLibraries.fromRawResource(context, R.raw.gestures);
		mLibrary = GestureLibraries.fromRawResource(context, R.raw.gestures);
		load();
		setRegister(register);
	}

	private boolean load() {
		mLoaded = mLibrary.load();
		return mLoaded;
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		if (!mLoaded) {
			if (!load()) {
				return;
			}
		}

		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		if (predictions.size() > 0) {
			Prediction prediction = predictions.get(0);
			Log.v(JamendoApplication.TAG, "Gesture " + prediction.name
					+ " recognized with score " + prediction.score);
			if (prediction.score > 2.0) {
				GestureCommand command = getRegister().getCommand(
						prediction.name);
				if (command != null) {
					command.execute();
				}
			}
		}
	}

	public void setRegister(GestureCommandRegister mRegister) {
		this.mRegister = mRegister;
	}

	public GestureCommandRegister getRegister() {
		return mRegister;
	}

}
