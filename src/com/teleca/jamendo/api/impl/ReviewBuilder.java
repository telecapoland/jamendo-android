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

import com.teleca.jamendo.api.Review;

/**
 * @author Lukasz Wisniewski
 */
public class ReviewBuilder extends JSONBuilder<Review>{

	@Override
	public Review build(JSONObject jsonObject) throws JSONException {
		Review review = new Review();
		// TODO review.setDates(dates);
		review.setId(jsonObject.getInt("id"));
		review.setLang(jsonObject.getString("lang"));
		review.setName(jsonObject.getString("name"));
		try {
			review.setRating(jsonObject.getInt("rating"));
		} catch (JSONException e) {
			// this may happen and rating can be set to null
			review.setRating(0);
		}
		review.setText(jsonObject.getString("text").replace("\r", ""));
		review.setUserImage(jsonObject.getString("user_image"));
		review.setUserName(jsonObject.getString("user_name"));
		return review;
	}

}
