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

package com.teleca.jamendo.api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.api.WSError;

/**
 * @author Lukasz Wisniewski
 */
public class Caller {
	
	/**
	 * Cache for most recent request
	 */
	private static RequestCache requestCache = null;

	/**
	 * Performs HTTP GET using Apache HTTP Client v 4
	 * 
	 * @param url
	 * @return
	 * @throws WSError 
	 */
	public static String doGet(String url) throws WSError{
		
		String data = null;
		if(requestCache != null){
			data = requestCache.get(url);
			if(data != null){
				Log.d(JamendoApplication.TAG, "Caller.doGet [cached] "+url);
				return data;
			}
		}
		
		URI encodedUri = null;
		HttpGet httpGet = null;
		
		try {
			encodedUri = new URI(url);
			httpGet = new HttpGet(encodedUri);
		} catch (URISyntaxException e1) {
			// at least try to remove spaces
			String encodedUrl = url.replace(' ', '+');
			httpGet = new HttpGet(encodedUrl);
			e1.printStackTrace();
		}
		
		// initialize HTTP GET request objects
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse;
		
		try {
			// execute request
			try {
				httpResponse = httpClient.execute(httpGet);
			} catch (UnknownHostException e) {
				throw new WSError("Unable to access " + e.getLocalizedMessage());
			} catch (SocketException e){
				throw new WSError(e.getLocalizedMessage());
			}
			
			// request data
			HttpEntity httpEntity = httpResponse.getEntity();
			
			if(httpEntity != null){
				InputStream inputStream = httpEntity.getContent();
				data = convertStreamToString(inputStream);
				// cache the result
				if(requestCache != null){
					requestCache.put(url, data);
				}
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.d(JamendoApplication.TAG, "Caller.doGet "+url);
		return data;
	}

	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public static void setRequestCache(RequestCache requestCache) {
		Caller.requestCache = requestCache;
	}
	
	public static String createStringFromIds(int[] ids){
		if(ids == null)
			return "";
		
		String query ="";
		
		for(int id : ids){
			query = query + id + "+";
		}
		
		return query;
	}

}
