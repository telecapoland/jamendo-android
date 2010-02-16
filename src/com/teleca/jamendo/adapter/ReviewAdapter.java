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

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.teleca.jamendo.api.Review;
import com.teleca.jamendo.widget.RemoteImageView;
import com.teleca.jamendo.R;

/**
 * Adapter representing reviews
 * 
 * @author Lukasz Wisniewski
 */
public class ReviewAdapter extends ArrayListAdapter<Review> {
	
	/**
	 * Reference to reviews in all languages, altered only by setList
	 */
	private ArrayList<Review> mAllLangs;
	
	/**
	 * Reference to reviews in a selected language, altered by setLang
	 */
	private ArrayList<Review> mSelectedLangs;

	public ReviewAdapter(Activity context) {
		super(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;

		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.review_row, null);

			holder = new ViewHolder();
			holder.reviewAvatar = (RemoteImageView)row.findViewById(R.id.ReviewAvatar);
			holder.reviewUserName = (TextView)row.findViewById(R.id.ReviewUserName);
			holder.reviewText = (TextView)row.findViewById(R.id.ReviewRowText);
			holder.reviewRatingBar = (ProgressBar)row.findViewById(R.id.ReviewRowRatingBar);
			holder.reviewTitle = (TextView)row.findViewById(R.id.ReviewTitle);

			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}
		
		holder.reviewAvatar.setDefaultImage(R.drawable.no_avatar);
		holder.reviewAvatar.setImageUrl(mList.get(position).getUserImage(), position, getListView());
		holder.reviewTitle.setText(mList.get(position).getName());
		holder.reviewUserName.setText(mList.get(position).getUserName());
		holder.reviewText.setText(mList.get(position).getText());
		holder.reviewRatingBar.setMax(10);
		holder.reviewRatingBar.setProgress(mList.get(position).getRating());

		return row;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	/**
	 * Class implementing holder pattern,
	 * performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		RemoteImageView reviewAvatar;
		TextView reviewTitle;
		TextView reviewUserName;
		TextView reviewText;
		ProgressBar reviewRatingBar;
	}

	/**
	 * Sets which languages should be displayed
	 * 
	 * @param string
	 */
	public void setLang(String lang) {
		if(lang.equals("all")){
			mList = mAllLangs;
		} else {
			mSelectedLangs = new ArrayList<Review>();
			for(Review r : mAllLangs){
				if(r.getLang().equals(lang)){
					mSelectedLangs.add(r);
				}
			}
			mList = mSelectedLangs;
		}
		notifyDataSetChanged();
	}

	@Override
	public void setList(ArrayList<Review> list) {
		super.setList(list);
		mAllLangs = list;
	}

}
