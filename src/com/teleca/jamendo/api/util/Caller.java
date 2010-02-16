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
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.api.WSError;

import android.util.Log;

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
		
		// initialize HTTP GET request objects
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse httpResponse;
		
		try {
			// execute request
			try {
				httpResponse = httpClient.execute(httpGet);
			} catch (UnknownHostException e) {
				WSError wsError = new WSError();
				wsError.setMessage(e.getLocalizedMessage());
				throw wsError;
			} catch (SocketException e){
				WSError wsError = new WSError();
				wsError.setMessage(e.getLocalizedMessage());
				throw wsError;
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
