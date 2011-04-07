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
 * unit album
 * 
 * @author Lukasz Wisniewski
 * @author Marcin Gil
 */
public class Album implements Serializable {
	
	private static final long serialVersionUID = 8517633545835124349L;
	
	public static Album emptyAlbum = new Album();

	/**
	 * numeric id of the album
	 */
	private int id = 0;
	
	/**
	 * link to the cover of the album
	 */
	private String image = "";
	
	/**
	 * name of the album
	 */
	private String name = "";
	
	/**
	 * Rating of the album
	 */
	private double rating = 0;
	
	/**
	 * Display name of the artist.
	 */
	private String artistName = "";
	
	/**
	 * Album tracks
	 */
	private Track[] tracks = null;
	
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getArtistName() {
		return artistName;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	public double getRating() {
		return rating;
	}
	public void setTracks(Track[] tracks) {
		this.tracks = tracks;
	}
	public Track[] getTracks() {
		return tracks;
	}
}
