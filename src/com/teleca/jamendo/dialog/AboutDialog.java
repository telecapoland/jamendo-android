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

package com.teleca.jamendo.dialog;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * About dialog
 * 
 * @author Lukasz Wisniewski
 */
public class AboutDialog extends Dialog {

	private TextView mVersionTextView;
	private Button mTermsButton;
	private Button mCompanyButton;
	private Button mCancelButton;
	private ViewFlipper mCompanyViewFlipper;

	public AboutDialog(Activity context) {
		super(context);
		init(context);
	}

	public AboutDialog(Activity context, int theme) {
		super(context, theme);
		init(context);
	}

	public AboutDialog(Activity context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init(context);
	}

	/**
	 * Sharable code between constructors
	 */
	private void init(final Activity context){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about);

		
		String topText = "v "+JamendoApplication.getInstance().getVersion() + ", " + context.getString(R.string.about_note);
		
		mVersionTextView = (TextView)findViewById(R.id.VersionText);
		mVersionTextView.setText(topText);

		mTermsButton = (Button)findViewById(R.id.TermsButton);
		mTermsButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://www.jamendo.com/en/cgu_user"));
				context.startActivity(myIntent); 				
			}

		});
		
		mCompanyViewFlipper = (ViewFlipper)findViewById(R.id.CompanyViewFlipper);
		mCompanyButton = (Button)findViewById(R.id.AboutCompanyButton);
		mCompanyButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				int currentCompany = mCompanyViewFlipper.getDisplayedChild();
				if(currentCompany == 0 ){
					mCompanyButton.setText(R.string.about_jamendo);
					mCompanyViewFlipper.setDisplayedChild(1); // display Teleca info
				}
				else {
					mCompanyButton.setText(R.string.about_teleca);
					mCompanyViewFlipper.setDisplayedChild(0); // display Jamendo info
				}
			}

		});
		
		mCancelButton = (Button)findViewById(R.id.CancelButton);
		mCancelButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AboutDialog.this.dismiss();
			}

		});
	}

}
