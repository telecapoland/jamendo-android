package com.teleca.jamendo.gestures;

import android.content.Context;

import com.teleca.jamendo.media.PlayerEngine;

public class PlayerGestureCommandRegiser extends GestureCommandRegister {

	public PlayerGestureCommandRegiser(PlayerEngine playerEngine) {
		super();

		registerCommand("next", new PlayerGestureNextCommand(playerEngine));
		registerCommand("prev", new PlayerGesturePrevCommand(playerEngine));
		registerCommand("play", new PlayerGesturePlayCommand(playerEngine));
		registerCommand("stop", new PlayerGestureStopCommand(playerEngine));
	}

}
