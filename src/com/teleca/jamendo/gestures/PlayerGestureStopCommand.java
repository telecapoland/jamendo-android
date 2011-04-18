package com.teleca.jamendo.gestures;

import android.util.Log;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.media.PlayerEngine;

public class PlayerGestureStopCommand implements GestureCommand {

	PlayerEngine mPlayerEngine;
	
	public PlayerGestureStopCommand( PlayerEngine engine ){
		mPlayerEngine = engine;
	}
	@Override
	public void execute() {
		Log.v(JamendoApplication.TAG, "PlayerGestureStopCommand");
		mPlayerEngine.stop();
	}

}
