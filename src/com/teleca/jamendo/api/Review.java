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

package com.teleca.jamendo.api;

import java.io.Serializable;
import java.util.Date;

/**
 * unit review
 * 
 * @author Lukasz Wisniewski
 */
public class Review implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Numeric id of the review
	 */
	private int id;
	
	/**
	 * Title of the review
	 */
	private String name;
	
	/**
	 * Text of the review
	 */
	private String text;
	
	/**
	 * Rating of the review 0..10
	 */
	private int rating;
	
	/**
	 * Lang the review is written in "fr", "en", "sv",...
	 */
	private String lang;
	
	/**
	 * array containing the date when the review has been added, and when it has been updated for the last time 	"2005-08-30T19:50:36+01"
	 */
	private Date[] dates;
	
	/**
	 * name of the user who wrote the review
	 */
	private String userName;
	
	/**
	 * image of the user who wrote the review
	 */
	private String userImage;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public Date[] getDates() {
		return dates;
	}

	public void setDates(Date[] dates) {
		this.dates = dates;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}

	public String getUserImage() {
		return userImage;
	}
}
