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
import android.os.Build;


/**
 * This abstract class defines SDK-independent API for communication with
 * Contacts Provider. The actual implementation used by the application depends
 * on the level of API available on the device. If the API level is 
 * Donut, we want to use the {@link ContactsAccessorSdk4} class. If it is
 * Eclair or higher, we want to use {@link ContactsAccessorSdk5} etc...
 */
public abstract class ContactsAccessor {
	
    /**
     * Static singleton instance of {@link ContactsAccessor} holding the
     * SDK-specific implementation of the class.
     */
    private static ContactsAccessor sInstance;

    public static ContactsAccessor getInstance() {
        if (sInstance == null) {
            String className = null;

            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
            if (sdkVersion == Build.VERSION_CODES.ECLAIR) {
                className = "com.teleca.jamendo.util.ContactsAccessorSdk5";
            } else if (sdkVersion == Build.VERSION_CODES.DONUT) {
                className = "com.teleca.jamendo.util.ContactsAccessorSdk4";
            } else if (sdkVersion == Build.VERSION_CODES.CUPCAKE) {
            	className = "com.teleca.jamendo.util.ContactsAccessorSdk3";
            }

            /*
             * Find the required class by name and instantiate it.
             */
            try {
                Class<? extends ContactsAccessor> clazz =
                        Class.forName(className).asSubclass(ContactsAccessor.class);
                sInstance = clazz.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        return sInstance;
    }
    
    public abstract Cursor smsQuery(Activity a);
    
    public abstract Cursor phoneQuery(Activity a, String id);
    
    public abstract String getDisplayNameColumn();
    
    public abstract String getPhoneNumberFromCursor(Cursor c);
    
    public abstract void sendSms(String destination, String text);

}
