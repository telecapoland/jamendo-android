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
import android.widget.ImageView;

/**
 * Reflection of items provided by <code>RelectableLayout</code> instance.
 * 
 * @author Lukasz Wisniewski
 */
public class ReflectiveSurface extends ImageView{
	
	private ReflectableLayout mReflectableLayout;

	public ReflectiveSurface(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ReflectiveSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ReflectiveSurface(Context context) {
		super(context);
	}
	
	public void setReflectableLayout(ReflectableLayout mReflectableLayout) {
		this.mReflectableLayout = mReflectableLayout;
	}

	public ReflectableLayout getReflectableLayout() {
		return mReflectableLayout;
	}

	@Override
	protected void onDraw(Canvas canvas) {		
		
		if(mReflectableLayout == null){
			super.onDraw(canvas);
			return;
		}
		
		// reflect & copy
		canvas.translate(0, mReflectableLayout.getHeight());
		canvas.scale(1f, -1f);
		// render
		mReflectableLayout.draw(canvas);
		
		super.onDraw(canvas);
	}
	
	

}
