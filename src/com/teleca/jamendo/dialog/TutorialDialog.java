/**
 * 
 */
package com.teleca.jamendo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.teleca.jamendo.R;

/**
 * Tutorial dialog
 * 
 * @author Marcin Gil
 */
public class TutorialDialog extends Dialog {

	/**
	 * @param context
	 */
	public TutorialDialog(Context context) {
		super(context);
		initialize(context);
	}

	/**
	 * @param context
	 * @param theme
	 */
	public TutorialDialog(Context context, int theme) {
		super(context, theme);
		initialize(context);
	}

	/**
	 * @param context
	 * @param cancelable
	 * @param cancelListener
	 */
	public TutorialDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		initialize(context);
	}

	/**
	 * Common initialization code
	 */
	private final void initialize(final Context context) {
		setContentView(R.layout.tutorial);
		setTitle(R.string.tutorial_title);
		
		Button mCloseButton = (Button)findViewById(R.id.closeTutorial);
		if (mCloseButton != null) {
			mCloseButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}
	}
}
