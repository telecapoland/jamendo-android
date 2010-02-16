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

/**
 * Unit track<br>
 * <br>
 * <table>
 * <tr><td><b>field</b></td><td><b>description</b></td><td><b>example</b></td></tr>
 * <tr><td>id</td><td>numeric id of the track</td><td>108254</td></tr>
 * <tr><td>name</td><td>name of the track</td><td>"Tout se passera bien"</td></tr>
 * <tr><td>duration</td><td>length of the track (in seconds)</td><td>310</td></tr>
 * <tr><td>url</td><td>link to the page of the track on Jamendo (lyrics)</td><td>http://www.jamendo.com/track/108254</td></tr>
 * <tr><td>stream</td><td>music stream of the track</td><td>http://www.jamendo.com/track/108254</td></tr>
 * </table>
 * 
 * 
 * @author Lukasz Wisniewski
 */
public class Track implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * numeric id of the track e.g. 108254
	 */
	private int id;
	
	/**
	 * Name of the track e.g. "Tout se passera bien"
	 */
	private String name;
	
	/**
	 * Length of the track (in seconds) e.g. 310
	 */
	private int duration;
	
	/**
	 * Link to the page of the track on Jamendo (lyrics)
	 */
	private String url;
	
	/**
	 * Music stream
	 */
	private String stream;
	
	/**
	 * Track rating 0..1
	 */
	private double rating;
	
	/**
	 * Track number on album
	 */
	private int numalbum;

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

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setStream(String stream) {
		this.stream = stream;
	}

	public String getStream() {
		return stream;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public double getRating() {
		return rating;
	}

	public void setNumAlbum(int numalbum) {
		this.numalbum = numalbum;
	}

	public int getNumAlbum() {
		return numalbum;
	}
}
