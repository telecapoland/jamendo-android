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

import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Widget bar represenation of album, used with AlbumBarFlipper
 * 
 * @author Lukasz Wisniewski
 */
public class AlbumBar extends LinearLayout {
	
	protected TextView mArtistTextView;
	protected TextView mAlbumTextView;
	protected TextView mDescriptionTextView;
	protected RemoteImageView mCoverImageView;

	public AlbumBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AlbumBar(Context context) {
		super(context);
		init();
	}
	
	/**
	 * Sharable code between constructors
	 */
	private void init(){
		LayoutInflater.from(getContext()).inflate(R.layout.album_bar, this);
		
		mArtistTextView = (TextView)findViewById(R.id.ArtistTextView);
		mAlbumTextView = (TextView)findViewById(R.id.AlbumTextView);
		mCoverImageView = (RemoteImageView)findViewById(R.id.CoverImageView);
		//mCoverImageView.setDefaultImage(R.drawable.no_cd);
		mCoverImageView.setImageResource(R.drawable.no_cd);
		mDescriptionTextView = (TextView)findViewById(R.id.DescriptionTextView);
	}
	
	public void setAlbum(Album album){
		mArtistTextView.setText(album.getArtistName());
		mAlbumTextView.setText(album.getName());
		mCoverImageView.setDefaultImage(R.drawable.no_cd);
		mCoverImageView.setImageUrl(album.getImage());
	}
	
	public void setDescription(String description){
		mDescriptionTextView.setText(description);
	}
	
	public void setDescription(int resid){
		mDescriptionTextView.setText(resid);
	}

}
