/*
 * Copyright (C) 2012 Michał Gryglicki
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

package com.gryglicki.android.wifi;

import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class WiFiPrioritizerPrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private static final String PREF_HOME_WIFI_KEY = "homeWifiPref";
	private static final String PREF_MIN_WIFI_SIGNAL_LEVEL_KEY = "minWifiSignalLevelPref";
	private static final String PREF_MIN_WIFI_SIGNAL_LEVEL_KEY_DEFAULT_VALUE = "-80";
	private static final String PREF_WIFI_CHECK_INTERVAL_KEY = "wifiCheckIntervalPref";
	private static final String PREF_WIFI_CHECK_INTERVAL_KEY_DEFAULT_VALUE = "60";
	private static final String PREF_RECONNECT_NOTIFICATION_KEY = "reconnectNotificationPref";
	private static final boolean PREF_RECONNECT_NOTIFICATION_KEY_DEFAULT_VALUE = false;
	
	public static final String HOME_WIFI_NETWORK_ID_KEY = "defaultWifiNetworkId";
	public static final String HOME_WIFI_SSID_KEY = "defaultWifiSSID";
	public static final String MIN_WIFI_SIGNAL_LEVEL_KEY = "minWifiSignalLevel";
	public static final String WIFI_CHECK_INTERVAL_KEY = "wifiCheckInterval";
	public static final String RECONNECT_NOTIFICATION_KEY = "reconnectNotification";
	
	/* Managers */
	WifiManager wifiManager;
	
	/* Shared Data */
	WiFiPrioritizerApplication application;
	SharedPreferences prefs;
	
	/* View Preferences */
	ListPreference homeWifiPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		homeWifiPref = (ListPreference) findPreference(PREF_HOME_WIFI_KEY);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		application = (WiFiPrioritizerApplication) getApplication();
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
		
		homeWifiPref.setEntries(entries);
		homeWifiPref.setEntryValues(entryValues);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
		/* to prevent recursive calling of this method */
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		
		Editor editor;
		if (key.equals(PREF_HOME_WIFI_KEY)) {
			Integer homeWifiNetworkId = Integer.valueOf(sharedPref.getString(PREF_HOME_WIFI_KEY, null));
			if ((homeWifiNetworkId != null) && wifiManager.isWifiEnabled()) {
				List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
				if (configuredNetworks != null) {
					for (WifiConfiguration conf : configuredNetworks) {
						if (conf.networkId == homeWifiNetworkId) {
							editor = sharedPref.edit();
							editor.putInt(HOME_WIFI_NETWORK_ID_KEY, homeWifiNetworkId);
							editor.putString(HOME_WIFI_SSID_KEY, extractProperSSID(conf.SSID));
							editor.commit();
							break;
						}
					}
				}
			}
		}
		
		int minWifiSignalLevel = Integer.valueOf(sharedPref.getString(PREF_MIN_WIFI_SIGNAL_LEVEL_KEY, PREF_MIN_WIFI_SIGNAL_LEVEL_KEY_DEFAULT_VALUE));
		int wifiCheckInterval = Integer.valueOf(sharedPref.getString(PREF_WIFI_CHECK_INTERVAL_KEY, PREF_WIFI_CHECK_INTERVAL_KEY_DEFAULT_VALUE));
		boolean reconnectNotifications = sharedPref.getBoolean(PREF_RECONNECT_NOTIFICATION_KEY, PREF_RECONNECT_NOTIFICATION_KEY_DEFAULT_VALUE);
		
		editor = sharedPref.edit();
		editor.putInt(MIN_WIFI_SIGNAL_LEVEL_KEY, minWifiSignalLevel);
		editor.putInt(WIFI_CHECK_INTERVAL_KEY, wifiCheckInterval);
		editor.putBoolean(RECONNECT_NOTIFICATION_KEY, reconnectNotifications);
		editor.commit();

		prefs.registerOnSharedPreferenceChangeListener(this);
		application.invalidateSharedPreferences();
	}
	
	private String extractProperSSID(String confSSID) {
		if ((confSSID.startsWith("\"")) && confSSID.endsWith("\"")) {
			return confSSID.substring(1, confSSID.length() - 1);
		} else {
			return confSSID;
		}
	}
}
