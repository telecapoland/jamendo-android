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

package com.teleca.jamendo.adapter;

public class PurpleEntry {
	private Integer mDrawable;
	private String mText;
	private Integer mTextId;
	private PurpleListener mListener;
	
	public Integer getDrawable() {
		return mDrawable;
	}
	
	public void setDrawable(Integer drawable) {
		mDrawable = drawable;
	}
	
	public Integer getTextId() {
		return mTextId;
	}
	
	public void setTextId(Integer textId) {
		mTextId = textId;
	}

	public void setListener(PurpleListener listener) {
		this.mListener = listener;
	}

	public PurpleListener getListener() {
		return mListener;
	}

	public void setText(String mText) {
		this.mText = mText;
	}

	public String getText() {
		return mText;
	}

	public PurpleEntry(Integer drawable, Integer textId) {
		mDrawable = drawable;
		mTextId = textId;
	}
	
	public PurpleEntry(Integer drawable, String text) {
		mDrawable = drawable;
		mText = text;
	}

	public PurpleEntry(Integer drawable, Integer text, PurpleListener listener) {
		mDrawable = drawable;
		mTextId = text;
		mListener = listener;
	}
}
