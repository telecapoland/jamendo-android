package com.teleca.jamendo.gestures;

import java.util.HashMap;

import android.util.Log;
import android.widget.Toast;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.media.PlayerEngine;

public class PlayerGestureNextCommand implements GestureCommand {

	PlayerEngine mPlayerEngine;
	
	public PlayerGestureNextCommand( PlayerEngine engine ){
		mPlayerEngine = engine;
	}
	
	@Override
	public void execute() {
		Log.v(JamendoApplication.TAG, "PlayerGestureNextCommand");
		mPlayerEngine.next();
	}
	
}

