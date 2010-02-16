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

package com.teleca.jamendo.widget;

import com.teleca.jamendo.api.Artist;
import com.teleca.jamendo.R;

import android.content.Context;
import android.util.AttributeSet;

public class ArtistBar extends AlbumBar {

	public ArtistBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ArtistBar(Context context) {
		super(context);
		init();
	}
	
	private void init(){
		//mAlbumTextView.setVisibility(View.GONE);
	}
	
	/**
	 * Sets artist associated with the bar
	 * 
	 * @param artist
	 */
	public void setArtist(Artist artist){
		mArtistTextView.setText(artist.getName());
		mAlbumTextView.setText("");
		mCoverImageView.setDefaultImage(R.drawable.no_avatar);
		mCoverImageView.setImageUrl(artist.getImage());
	}

}
