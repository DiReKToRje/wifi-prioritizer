package com.gryglicki.android.wifi;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class WiFiPrioritizerApplication extends Application implements OnSharedPreferenceChangeListener {

	/* Shared Preferences */
	private SharedPreferences prefs;
	/* Managers */
	private WifiManager wifiManager;
	
	/* Service info */
	private boolean prioritizerServiceRunning;
	
	/* Shared Preferences info */
	private Integer defaultWifiNetworkId;
	private String defaultWifiSSID;
	private int minSignalLevel;
	private int checkInterval;
	private boolean notifications;
	
	@Override
	public void onCreate() {
		super.onCreate();
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        updateSharedPreferences();
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	public synchronized void reconnect() {
		Log.d("TEST", "Application.reconnect()");
		wifiManager.enableNetwork(defaultWifiNetworkId, true);
		
		if (notifications) {
			//notification!!
		}
		
		
	}

	public boolean isPrioritizerServiceRunning() {
		return prioritizerServiceRunning;
	}

	public void setPrioritizerServiceRunning(boolean prioritizerServiceRunning) {
		this.prioritizerServiceRunning = prioritizerServiceRunning;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateSharedPreferences();		
	}
	
	private void updateSharedPreferences() {
		try {
			defaultWifiNetworkId = Integer.parseInt(prefs.getString(PrefsActivity.DEFAULT_WIFI_PREF_KEY, PrefsActivity.DEFAULT_WIFI_PREF_KEY_DEFAULT_VALUE));
			if (wifiManager.isWifiEnabled() && (wifiManager.getConfiguredNetworks() != null)) {
				for (WifiConfiguration conf : wifiManager.getConfiguredNetworks()) {
					if (conf.networkId == defaultWifiNetworkId) {
						defaultWifiSSID = conf.SSID;
						break;
					}
				}
	        }
		} catch (NumberFormatException nfe) {
			//no value set!!!!
		}
    	minSignalLevel = Integer.parseInt(prefs.getString(PrefsActivity.MIN_WIFI_SIGNAL_LEVEL_PREF_KEY, PrefsActivity.MIN_WIFI_SIGNAL_LEVEL_PREF_KEY_DEFAULT_VALUE));
    	checkInterval = Integer.parseInt(prefs.getString(PrefsActivity.WIFI_CHECK_INTERVAL_PREF_KEY, PrefsActivity.WIFI_CHECK_INTERVAL_PREF_KEY_DEFAULT_VALUE));
    	notifications = prefs.getBoolean(PrefsActivity.RECONNECT_NOTIFICATION_PREF_KEY, false);
	}

	public Integer getDefaultWifiNetworkId() {
		return defaultWifiNetworkId;
	}

	public String getDefaultWifiSSID() {
		return defaultWifiSSID;
	}

	public int getMinSignalLevel() {
		return minSignalLevel;
	}

	public int getCheckInterval() {
		return checkInterval;
	}

	public boolean isNotifications() {
		return notifications;
	}
	
}
