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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;

/**
 * @author Lukasz Wisniewski
 */
public class DrawableAccessor {
	
	@SuppressWarnings("unchecked")
	public static BitmapDrawable construct(Resources resources, Bitmap bitmap){
		// find class for bitmap drawable
		Class bitmapDrawableClass = null;
		try {
			bitmapDrawableClass = Class.forName("android.graphics.drawable.BitmapDrawable");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// select right constructor and params depending what the SDK is
		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		try {
			
			Constructor bitmapDrawableConstructor = null;
			Object params[] = null;
			
			if (sdkVersion > Build.VERSION_CODES.CUPCAKE) {
				Class args[] = new Class[]{Resources.class, Bitmap.class};
				bitmapDrawableConstructor = bitmapDrawableClass.getConstructor(args);
				params = new Object[]{resources, bitmap};

			} else {
				Class args[] = new Class[]{Bitmap.class};
				bitmapDrawableConstructor = bitmapDrawableClass.getConstructor(args);
				params = new Object[]{bitmap};
			}
			
			return (BitmapDrawable) bitmapDrawableConstructor.newInstance(params);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}		
		
		// sth failed return null
		return null;
	}

}
