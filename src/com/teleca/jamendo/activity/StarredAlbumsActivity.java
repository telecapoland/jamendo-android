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

package com.teleca.jamendo.activity;

import java.util.ArrayList;

import com.teleca.jamendo.R;
import com.teleca.jamendo.adapter.AlbumAdapter;
import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.dialog.StarredAlbumLoading;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Browses given list of albums, on album selection opens it in AlbumActivity 
 * 
 * @author Lukasz Wisniewski
 */
public class StarredAlbumsActivity extends ListActivity implements OnItemClickListener {
	
	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.starredalbums);
		
		Intent i = getIntent();
		ArrayList<Album> albums = (ArrayList<Album>) i.getSerializableExtra("albums");
		
		AlbumAdapter albumAdapter = new AlbumAdapter(this);
		albumAdapter.setListView(getListView());
		albumAdapter.setList(albums);
		getListView().setAdapter(albumAdapter);
		getListView().setOnItemClickListener(this);
	}
	
	public static void launch(Activity a, String userName){
		new StarredAlbumLoading(a, R.string.loading, R.string.loading_fail).execute(userName);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		Album album = (Album)adapterView.getItemAtPosition(position);
		PlayerActivity.launch(this, album);
	}
}
