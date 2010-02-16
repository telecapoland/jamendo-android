/*
 * Copyright (C) 2009 Teleca Poland Sp. z o.o. <android@teleca.com>
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.Activity;
import android.database.Cursor;
import android.provider.Contacts.People;
import android.telephony.gsm.SmsManager;

@SuppressWarnings("deprecation")
public class ContactsAccessorSdk3 extends ContactsAccessor {
	
	@Override
	public Cursor smsQuery(Activity a) {
		return a.getContentResolver().query(
				People.CONTENT_URI, 
				new String[]{People._ID, People.NAME},
				People.NUMBER +" is not null", 
				null, 
				People.NAME+" ASC");
	}

	@Override
	public Cursor phoneQuery(Activity a, String id) {
		return a.getContentResolver().query(
				People.CONTENT_URI, 
				new String[]{People.NUMBER},		// projection 
				"people."+People._ID +"=?",			// selection 
				new String[]{id},				// selection args 
				null);
	}

	@Override
	public String getDisplayNameColumn() {
		return People.NAME;
	}

	@Override
	public String getPhoneNumberFromCursor(Cursor cursor) {
		if(cursor != null && cursor.getCount() > 0){
			cursor.moveToFirst();
			int phoneColumn = cursor.getColumnIndex(People.NUMBER);
			String number = cursor.getString(phoneColumn);
			return number;
		}
		return null;
	}

	@Override
	public void sendSms(String destination, String text) {
		SmsManager.getDefault().sendTextMessage(destination, null, text, null, null);
	}

}
