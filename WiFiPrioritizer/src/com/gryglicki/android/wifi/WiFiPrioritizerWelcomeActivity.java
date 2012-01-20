package com.gryglicki.android.wifi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class WiFiPrioritizerWelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (prefs.contains(PrefsActivity.DEFAULT_WIFI_NETWORK_ID_KEY) && prefs.contains(PrefsActivity.DEFAULT_WIFI_SSID_KEY)) {
			startActivity(new Intent(this, WiFiPrioritizerActivity.class));
		} else {
			startActivity(new Intent(this, PrefsActivity.class));
			Toast.makeText(this, getString(R.string.error_no_default_wifi), Toast.LENGTH_LONG).show();
		}
		finish();
	}
}
