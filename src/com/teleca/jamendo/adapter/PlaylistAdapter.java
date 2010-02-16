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
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.util.Helper;
import com.teleca.jamendo.R;

/**
 * Adapter representing tracks 
 * 
 * @author Lukasz Wisniewski
 */
public class PlaylistAdapter extends BaseAdapter {
	
	private Playlist mPlaylist;
	private Activity mContext;
	
	private int mLayoutId;
	
	public PlaylistAdapter(Activity context) {
		mContext = context;
		mLayoutId = R.layout.track_row;
	}
	
	public PlaylistAdapter(Activity context, int layoutId) {
		mContext = context;
		mLayoutId = layoutId;
	}

	@Override
	public int getCount() {
		if(mPlaylist != null)
			return mPlaylist.size();
		else
			return 0;
	}

	@Override
	public Object getItem(int index) {
		return mPlaylist.getTrack(index);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;

		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(mLayoutId, null);

			holder = new ViewHolder();
			
			holder.songName = (TextView)row.findViewById(R.id.TrackRowName);
			holder.songArtistAlbum = (TextView)row.findViewById(R.id.TrackRowArtistAlbum);
			holder.songDuration = (TextView)row.findViewById(R.id.TrackRowDuration);
			holder.songRating = (ProgressBar)row.findViewById(R.id.TrackRowRatingBar);

			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}

		PlaylistEntry playlistEntry = mPlaylist.getTrack(position);
		holder.songName.setText(playlistEntry.getTrack().getName());
		if(holder.songArtistAlbum != null){
			holder.songArtistAlbum.setText(playlistEntry.getAlbum().getArtistName()+" - "+playlistEntry.getAlbum().getName());
		}
		if(holder.songRating != null){
			holder.songRating.setMax(10);
			holder.songRating.setProgress((int) (playlistEntry.getTrack().getRating()*10));
		}
		holder.songDuration.setText(Helper.secondsToString(playlistEntry.getTrack().getDuration()));

		return row;
	}
	
	/**
	 * Class implementing holder pattern,
	 * performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		TextView songName;
		TextView songArtistAlbum;
		TextView songDuration;
		ProgressBar songRating;
	}

	public void setPlaylist(Playlist playlist) {
		this.mPlaylist = playlist;
		notifyDataSetChanged();
	}

	public Playlist getEntryList() {
		return mPlaylist;
	}

}
