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

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;

import com.teleca.jamendo.R;
import com.teleca.jamendo.widget.RemoteImageView;

public class ImageAdapter extends AlbumAdapter {
	
	int mIconSize;
	

	public ImageAdapter(Activity context) {
		super(context);
		mIconSize = (int)context.getResources().getDimension(R.dimen.icon_size); 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RemoteImageView i;

        if (convertView == null) {
            i = new RemoteImageView(mContext);
            i.setScaleType(RemoteImageView.ScaleType.FIT_CENTER);
            i.setLayoutParams(new Gallery.LayoutParams(mIconSize, mIconSize));
        } else {
            i = (RemoteImageView) convertView;
        }

        i.setDefaultImage(R.drawable.no_cd);
        i.setImageUrl(mList.get(position).getImage());

        return i;
	}
	
	/**
	 * Class implementing holder pattern,
	 * performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		RemoteImageView image;
	}
}
