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

package com.teleca.jamendo.api.impl;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Lukasz Wisniewski
 *
 * @param <T>
 */
public abstract class JSONBuilder<T> {
	
	protected String root;
	
	public JSONBuilder(){
		root = "";
	}
	
	/**
	 * Sets root of the JSON params, useful when building object in joins
	 * 
	 * @param root
	 */
	public void setRoot(String root){
		this.root = root;
	}
	
	public abstract T build(JSONObject jsonObject) throws JSONException;
}
