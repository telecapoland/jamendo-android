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

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Container for UI elements to be reflected, requires <code>ReflectiveSurface</code>
 * instance for full functionality
 * 
 * @author Lukasz Wisniewski
 */
public class ReflectableLayout extends RelativeLayout {
	
	protected ReflectiveSurface mReflectiveImageView;

	public ReflectiveSurface getReflectiveImageView() {
		return mReflectiveImageView;
	}

	public void setReflectiveSurface(ReflectiveSurface reflectiveImageView) {
		mReflectiveImageView = reflectiveImageView;
	}

	public ReflectableLayout(Context context) {
		super(context);
	}

	public ReflectableLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ReflectableLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(mReflectiveImageView == null)
			return;
		
		// We need to notify ImageView to redraw itself 
		mReflectiveImageView.postInvalidate();
	}
	

}
