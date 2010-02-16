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
 * unit artist
 * 
 * @author Lukasz Wisniewski
 */
public class Artist implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * numeric id of the artist
	 */
	private int id;
	
	/**
	 * string id of the artist
	 */
	private String idstr;
	
	/**
	 * Display name of the artist. different of idstr
	 */
	private String name;
	
	/**
	 * link to the image of the artist
	 */
	private String image;
	
	/**
	 * link to the page of the artist on Jamendo
	 */
	private String url;
	
	/**
	 * String id of the artist on MusicBrainz
	 */
	private String mbgid;
	
	/**
	 * Integer id of the artist on MusicBrainz
	 */
	private int mbid;
	
	/**
	 * Description of the artist (written by the artist)
	 */
	private String[] genre;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIdstr() {
		return idstr;
	}

	public void setIdstr(String idstr) {
		this.idstr = idstr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMbgid() {
		return mbgid;
	}

	public void setMbgid(String mbgid) {
		this.mbgid = mbgid;
	}

	public int getMbid() {
		return mbid;
	}

	public void setMbid(int mbid) {
		this.mbid = mbid;
	}

	public String[] getGenre() {
		return genre;
	}

	public void setGenre(String[] genre) {
		this.genre = genre;
	}

}
