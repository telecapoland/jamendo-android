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

import com.teleca.jamendo.api.Radio;
import com.teleca.jamendo.widget.RemoteImageView;
import com.teleca.jamendo.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class RadioAdapter extends ArrayListAdapter<Radio>{
	
	int mIconSize;

	public RadioAdapter(Activity context) {
		super(context);
		mIconSize = (int)context.getResources().getDimension(R.dimen.icon_size); 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;

		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.purple_row, null);

			holder = new ViewHolder();
			holder.image = (RemoteImageView)row.findViewById(R.id.PurpleImageView);
			LayoutParams lp = holder.image.getLayoutParams();
			lp.height = mIconSize;
			lp.width = mIconSize;
			holder.image.setLayoutParams(lp);
			holder.text = (TextView)row.findViewById(R.id.PurpleRowTextView);

			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}

		if(mList.get(position).getName().length() == 0 || mList.get(position).getName().equals("null")){
			holder.text.setText(mList.get(position).getIdstr());
		} else {
			holder.text.setText(mList.get(position).getName());
		}
		holder.image.setDefaultImage(R.drawable.list_radio);
		holder.image.setImageUrl(mList.get(position).getImage(), position, getListView());

		return row;
	}
	
	/**
	 * Class implementing holder pattern,
	 * performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		RemoteImageView image;
		TextView text;
	}

}
