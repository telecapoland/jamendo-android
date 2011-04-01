package com.teleca.jamendo.activity;

import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.teleca.jamendo.JamendoApplication;
import com.teleca.jamendo.R;
import com.teleca.jamendo.api.Album;
import com.teleca.jamendo.api.JamendoGet2Api;
import com.teleca.jamendo.api.Playlist;
import com.teleca.jamendo.api.Track;
import com.teleca.jamendo.api.WSError;
import com.teleca.jamendo.api.impl.JamendoGet2ApiImpl;
import com.teleca.jamendo.dialog.LoadingDialog;
import com.teleca.jamendo.util.Helper;

public class IntentDistributorActivity extends Activity {

	private static final String REVIEW_DETAILS = "review_detail";
	private static final String SHARE = "share";
	private static final String REVIEW_ID_PARAMETER = "review_id";

	private static final String LOG_TAG = "JAMENDO";

	private Intent mIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.intent_distributor);
		mIntent = getIntent();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mIntent.getBooleanExtra("handled", false)) {
			finish();
		} else {
			distributeIntent();
		}
	}

	private void distributeIntent() {
		mIntent.putExtra("handled", true);
		String lastDataSegment = mIntent.getData().getLastPathSegment();

		if (lastDataSegment.equalsIgnoreCase(REVIEW_DETAILS)) {
			Log.v(LOG_TAG, "review");
			new AlbumLoadingDialog(this, R.string.album_loading,
					R.string.album_fail).execute();
		} else if (lastDataSegment.equalsIgnoreCase(SHARE)) {
			Log.v(LOG_TAG, "share");
			Helper.share(this, mIntent.getDataString());
		} else {
			Log.v(LOG_TAG, "player");
			Intent intent = new Intent();
			intent.setData(mIntent.getData());
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClass(this, PlayerActivity.class);
			startActivity(intent);
		}

	}

	private class AlbumLoadingDialog extends LoadingDialog<Void, Album> {

		public AlbumLoadingDialog(Activity activity, int loadingMsg, int failMsg) {
			super(activity, loadingMsg, failMsg);
		}

		@Override
		public Album doInBackground(Void... params) {
			try {
				List<String> segments = mIntent.getData().getPathSegments();
				int albumId = Integer.parseInt(segments
						.get(segments.size() - 2));
				JamendoGet2Api service = new JamendoGet2ApiImpl();
				Album album = service.getAlbumById(albumId);
				return album;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			} catch (WSError e) {
				e.printStackTrace();
				return null;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public void onPostExecute(Album result) {
			super.onPostExecute(result);
			if (result == null)
				finish();
		}

		@Override
		public void doStuffWithResult(Album result) {
			try {
				int review_id = Integer.parseInt(mIntent.getData()
						.getQueryParameter(REVIEW_ID_PARAMETER));
				AlbumActivity.launch(IntentDistributorActivity.this, result,
						review_id);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				doCancel();
				finish();
			}
		}

	}
}
