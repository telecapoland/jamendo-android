package com.teleca.jamendo.gestures;

import java.util.HashMap;

/**
 * Simple register for gesture commands.
 * 
 * @author Bartosz Cichosz
 * 
 */
public class GestureCommandRegister {

	private HashMap<String, GestureCommand> mGestures;

	public GestureCommandRegister() {
		mGestures = new HashMap<String, GestureCommand>();
	}

	public void registerCommand(String name, GestureCommand gestureCommand) {
		mGestures.put(name, gestureCommand);
	}

	public GestureCommand getCommand(String name) {
		return mGestures.get(name);
	}

}
