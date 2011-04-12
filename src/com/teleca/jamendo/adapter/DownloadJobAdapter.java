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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.teleca.jamendo.api.PlaylistEntry;
import com.teleca.jamendo.util.download.DownloadJob;
import com.teleca.jamendo.R;

/**
 * @author Lukasz Wisniewski
 */
public class DownloadJobAdapter extends ArrayListAdapter<DownloadJob> {

	public DownloadJobAdapter(Activity context) {
		super(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;

		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.download_row, null);

			holder = new ViewHolder();
			holder.songName = (TextView)row.findViewById(R.id.TrackRowName);
			holder.songArtistAlbum = (TextView)row.findViewById(R.id.TrackRowArtistAlbum);
			holder.songProgressText = (TextView)row.findViewById(R.id.TrackRowProgress);
			holder.progressBar = (ProgressBar)row.findViewById(R.id.ProgressBar);
			row.setTag(holder);

			mContext.registerForContextMenu(row);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}

		PlaylistEntry playlistEntry = mList.get(position).getPlaylistEntry();
		holder.songName.setText(playlistEntry.getTrack().getName());
		holder.songArtistAlbum.setText(playlistEntry.getAlbum().getArtistName()+" - "+playlistEntry.getAlbum().getName());

		if(mList.get(position).getProgress() == 100){
			holder.progressBar.setVisibility(View.GONE);
			holder.songProgressText.setText("COMPLETE");
		} else {
			holder.progressBar.setVisibility(View.VISIBLE);
			holder.progressBar.setMax(100);
			holder.progressBar.setProgress(mList.get(position).getProgress());
			holder.songProgressText.setText(mList.get(position).getProgress()+"%");
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
		TextView songName;
		TextView songArtistAlbum;
		TextView songProgressText;
		ProgressBar progressBar;
	}

}
