package com.gryglicki.android.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {

	public static final String DEFAULT_WIFI_PREFERENCE_KEY = "defaultWifiPref";
	public static final String MIN_WIFI_SIGNAL_LEVEL_PREFERENCE_KEY = "minWifiSignalLevelPref";
	
	/* Managers */
	WifiManager wifiManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		
		ListPreference defaultWifiPref = (ListPreference) findPreference(DEFAULT_WIFI_PREFERENCE_KEY);

		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

		int numberOfEntries = wifiManager.getConfiguredNetworks().size();
		CharSequence entries[] = new String[numberOfEntries];
		CharSequence entryValues[] = new String[numberOfEntries];

		StringBuilder sb = new StringBuilder();
		for (int i=0; i<numberOfEntries; i++) {
			sb.setLength(0);
			WifiConfiguration conf = wifiManager.getConfiguredNetworks().get(i);
			sb.append(conf.SSID+" (");
			switch(conf.status) {
	    		case WifiConfiguration.Status.CURRENT:
	    			sb.append("current");
	    			break;
	    		case WifiConfiguration.Status.DISABLED:
	    			sb.append("disabled");
	    			break;
	    		case WifiConfiguration.Status.ENABLED:
	    			sb.append("enabled");
	    			break;
			}
			sb.append(")");
			
			entries[i] = sb.toString();
			entryValues[i] = Integer.toString(conf.networkId);
		}
		
		defaultWifiPref.setEntries(entries);
		defaultWifiPref.setEntryValues(entryValues);
	}
	
}
