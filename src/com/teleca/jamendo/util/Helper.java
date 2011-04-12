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

package com.teleca.jamendo.util;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.api.Review;
import com.teleca.jamendo.R;

/**
 * Various random functions
 * 
 * @author Lukasz Wisniewski
 */
public class Helper {
	
	/**
	 * Converts seconds to string, format mm:ss e.g. 04:12
	 * 
	 * @param seconds
	 * @return
	 */
	public static String secondsToString(int seconds){
		String s = ""+seconds/60+":";
		int t = seconds%60;
		s += t < 10 ? "0"+t : t; 
		return s;
	}
	
	/**
	 * Gets all available language codes in the reviews
	 * 
	 * @param reviews
	 * @return
	 */
	public static ArrayList<String> getLanguageCodes(ArrayList<Review> reviews){
		ArrayList<String> languages = new ArrayList<String>();
		//HashMap<String, String> languages = new HashMap<String, String>();
		for(Review r : reviews){
			String lang = r.getLang();
			if(lang != null && !languages.contains(lang)){
				languages.add(lang);
			}
		}
		return languages;
	}
	
	/**
	 * Converts an array of language codes to full names e.g. {"en","pl"} --> {"English","Polish"}
	 * 
	 * @param langCodes
	 * @return
	 */
	public static ArrayList<String> getLanguageNames(ArrayList<String> langCodes, Context context){
		ArrayList<String> languages = new ArrayList<String>();
		for(String c : langCodes){
			if(c.equals("all")){
				languages.add(context.getString(R.string.all_languages));
			} else {
				Locale l = new Locale(c);
				languages.add(l.getDisplayLanguage(l));
			}
		}
		return languages;
	}

	public static void share(Activity activity, PlaylistEntry entry){
           String text = activity.getString(R.string.song_recommendation) + ": "
           + String.format("http://www.jamendo.com/track/%d", entry.getTrack().getId());
           
           Intent shareIntent = new Intent(Intent.ACTION_SEND);
           shareIntent.setType("text/plain");
           shareIntent.putExtra(Intent.EXTRA_TEXT, text);
           shareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
           activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.share)));
	}

	public static void share(Activity activity, String shareUri) {
		String text = activity.getString(R.string.song_recommendation) + ": "
		+ shareUri.substring(0, shareUri.lastIndexOf('/'));;
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.share)));
	}
}
