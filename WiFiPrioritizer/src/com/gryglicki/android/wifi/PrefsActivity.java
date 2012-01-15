package com.gryglicki.android.wifi;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PrefsActivity extends PreferenceActivity {

	public static final String DEFAULT_WIFI_PREF_KEY = "defaultWifiPref";
	public static final String DEFAULT_WIFI_PREF_KEY_DEFAULT_VALUE = null;
	public static final String MIN_WIFI_SIGNAL_LEVEL_PREF_KEY = "minWifiSignalLevelPref";
	public static final String MIN_WIFI_SIGNAL_LEVEL_PREF_KEY_DEFAULT_VALUE = "-80";
	public static final String WIFI_CHECK_INTERVAL_PREF_KEY = "wifiCheckIntervalPref";
	public static final String WIFI_CHECK_INTERVAL_PREF_KEY_DEFAULT_VALUE = "60";
	public static final String RECONNECT_NOTIFICATION_PREF_KEY = "reconnectNotificationPref";
	public static final boolean RECONNECT_NOTIFICATION_PREF_KEY_DEFAULT_VALUE = false;
	
	/* Managers */
	WifiManager wifiManager;
	
	/* Shared Preferences */
	SharedPreferences prefs;
	
	/* View Preferences */
	ListPreference defaultWifiPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		defaultWifiPref = (ListPreference) findPreference(DEFAULT_WIFI_PREF_KEY);

		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		
		/**
		 * wifiManager.isWifiEnabled()
		 * - get WifiConfiguration && do checkPrefsValues ONLY when wifi is enabled !!!
		 * - when wifi is disabled then getConfiguredNetworks is empty !!!
		 */

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
		
//		if (! checkPrefsValues()) {
//			Editor prefsEditor = prefs.edit();
//			prefsEditor.putString(DEFAULT_WIFI_PREF_KEY, DEFAULT_WIFI_PREF_KEY_DEFAULT_VALUE);
//			prefsEditor.commit();
//		}
	}
	
//	private boolean checkPrefsValues() {
//		boolean contains = false;
//		String defaultWifi = prefs.getString(DEFAULT_WIFI_PREF_KEY, DEFAULT_WIFI_PREF_KEY_DEFAULT_VALUE);
//		CharSequence[] values = defaultWifiPref.getEntryValues();
//		for (int i=0; i<values.length; i++) {
//			if (values[i].toString().equals(defaultWifi)) {
//				contains = true;
//				break;
//			}
//		}
//	
//		return contains;
//	}
	
}
