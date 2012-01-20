package com.gryglicki.android.wifi;

import java.util.List;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class WiFiPrioritizerApplication extends Application {

	/* Managers */
	private WifiManager wifiManager;
	private NotificationManager notificationManager;
	
	/* Service info */
	private boolean prioritizerServiceRunning;
	
	/* SharedPreferences cache */
	private int defaultWifiNetworkId;
	private String defaultWifiSSID;
	private int minWifiSignalLevel;
	private boolean reconnectNotifications;
	private int wifiCheckInterval;
	
	@Override
	public void onCreate() {
		super.onCreate();
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        invalidateSharedPreferences();
        
	}
	
	public synchronized void invalidateSharedPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		defaultWifiNetworkId = prefs.getInt(PrefsActivity.DEFAULT_WIFI_NETWORK_ID_KEY, -1);
		defaultWifiSSID = prefs.getString(PrefsActivity.DEFAULT_WIFI_SSID_KEY, null);
		minWifiSignalLevel =  prefs.getInt(PrefsActivity.MIN_WIFI_SIGNAL_LEVEL_KEY, 0);
		reconnectNotifications = prefs.getBoolean(PrefsActivity.RECONNECT_NOTIFICATION_KEY, false);
		wifiCheckInterval = prefs.getInt(PrefsActivity.WIFI_CHECK_INTERVAL_KEY, 60);
	}
	
	public synchronized void checkReconnect() {
		Log.d("TEST", "Application.checkReconnect()");

		if (wifiManager.isWifiEnabled() && (wifiManager.getConnectionInfo() != null) && (wifiManager.getConnectionInfo().getNetworkId() != defaultWifiNetworkId)) {
			List<ScanResult> scanResults = wifiManager.getScanResults();
			if (scanResults != null) {
				for (ScanResult sr : scanResults) {
					if (sr.SSID.equals(defaultWifiSSID)) {
						if (sr.level >= minWifiSignalLevel) {
							reconnect();
						}
						break;
					} /* SSID found */
				} /* for each */
			} /* scanResults != null */
		}
	}
	
	
	private void reconnect() {
		Log.d("TEST", "Application.reconnect()");
		
		wifiManager.enableNetwork(defaultWifiNetworkId, true);
		if (reconnectNotifications) {
			Notification reconnectedNotification = new Notification();
			reconnectedNotification.vibrate = new long[] {100, 100, 100, 500};
			notificationManager.notify(0, reconnectedNotification);
		}
	}

	/* Application status accessors */
	public boolean isPrioritizerServiceRunning() {
		return prioritizerServiceRunning;
	}

	public void setPrioritizerServiceRunning(boolean prioritizerServiceRunning) {
		this.prioritizerServiceRunning = prioritizerServiceRunning;
	}
	
	public int getCheckInterval() {
		return wifiCheckInterval;
	}

}
