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

package com.teleca.jamendo.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.teleca.jamendo.api.Radio;

public class RadioDatabaseBuilder extends DatabaseBuilder<Radio> {

	@Override
	public Radio build(Cursor query) {
		int columnId = query.getColumnIndex("radio_id");
		int columnIdstr = query.getColumnIndex("radio_idstr");
		int columnName = query.getColumnIndex("radio_name");
		int columnImage = query.getColumnIndex("radio_image");
		
		Radio radio = new Radio();
		radio.setId(query.getInt(columnId));
		radio.setIdstr(query.getString(columnIdstr));
		radio.setImage(query.getString(columnImage));
		radio.setName(query.getString(columnName));
		return radio;
	}

	@Override
	public ContentValues deconstruct(Radio radio) {
		ContentValues values = new ContentValues();
		values.put("radio_id", radio.getId());
		values.put("radio_idstr", radio.getIdstr());
		values.put("radio_name", radio.getName());
		values.put("radio_image", radio.getImage());
		values.put("radio_date", System.currentTimeMillis());
		return values;
	}

}
