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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teleca.jamendo.R;

/**
 * Simple ListView adapter presenting icon and text, used
 * in HomeActivity
 * 
 * @author Lukasz Wisniewski
 */
public class PurpleAdapter extends ArrayListAdapter<PurpleEntry> {
	
	public PurpleAdapter(Activity context) {
		super(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;

		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.purple_row, null);

			holder = new ViewHolder();
			holder.image = (ImageView)row.findViewById(R.id.PurpleImageView);
			holder.text = (TextView)row.findViewById(R.id.PurpleRowTextView);

			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}

		if(mList.get(position).getText() != null){
			holder.text.setText(mList.get(position).getText());
		} else if(mList.get(position).getTextId() != null){
			holder.text.setText(mList.get(position).getTextId());
		}
		if(mList.get(position).getDrawable() != null){
			holder.image.setImageResource(mList.get(position).getDrawable());
		} else {
			holder.image.setVisibility(View.GONE);
		}

		return row;
	}
	
	/**
	 * Class implementing holder pattern,
	 * performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		ImageView image;
		TextView text;
	}

}
