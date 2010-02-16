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

import android.app.Activity;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;

public class ContactsAccessorSdk5 extends ContactsAccessor {

	@Override
	public Cursor smsQuery(Activity a) {
		return a.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, 
				new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.LOOKUP_KEY},
				ContactsContract.Contacts.HAS_PHONE_NUMBER +" = 1", 
				null, 
				ContactsContract.Contacts.DISPLAY_NAME+" ASC");
	}

	@Override
	public String getDisplayNameColumn() {
		return ContactsContract.Contacts.DISPLAY_NAME;
	}

	@Override
	public Cursor phoneQuery(Activity a, String id) {
		return a.getContentResolver().query(
				Phone.CONTENT_URI, 
				new String[]{Phone.NUMBER},		// projection 
				Phone.CONTACT_ID +"=?",			// selection 
				new String[]{id},				// selection args 
				null);
	}

	@Override
	public String getPhoneNumberFromCursor(Cursor cursor) {
		if(cursor != null && cursor.getCount() > 0){
			cursor.moveToFirst();
			int phoneColumn = cursor.getColumnIndex(Phone.NUMBER);
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
