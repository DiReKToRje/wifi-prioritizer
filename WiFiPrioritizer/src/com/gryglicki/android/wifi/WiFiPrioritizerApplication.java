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

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class WiFiPrioritizerApplication extends Application {
	public static final String ACTION_SERVICE_STATUS_CHANGED = "com.gryglicki.android.wifi.ACTION_SERVICE_STATUS_CHANGED";
	public static final String ACTION_PREFERENCES_CHANGED = "com.gryglicki.android.wifi.ACTION_PREFERENCES_CHANGED";
	
	/* Managers */
	private WifiManager wifiManager;
	private NotificationManager notificationManager;
	
	/* Service info */
	private boolean prioritizerServiceRunning;
	
	/* SharedPreferences cache */
	private int homeWifiNetworkId;
	private String homeWifiSSID;
	private int minWifiSignalLevel;
	private boolean reconnectNotifications;
	private int wifiCheckIntervalInSeconds;
	
	@Override
	public void onCreate() {
		super.onCreate();
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        invalidateSharedPreferences();
	}
	
	public synchronized void invalidateSharedPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		homeWifiNetworkId = prefs.getInt(WiFiPrioritizerPrefsActivity.HOME_WIFI_NETWORK_ID_KEY, -1);
		homeWifiSSID = prefs.getString(WiFiPrioritizerPrefsActivity.HOME_WIFI_SSID_KEY, null);
		minWifiSignalLevel =  prefs.getInt(WiFiPrioritizerPrefsActivity.MIN_WIFI_SIGNAL_LEVEL_KEY, 0);
		reconnectNotifications = prefs.getBoolean(WiFiPrioritizerPrefsActivity.RECONNECT_NOTIFICATION_KEY, false);
		wifiCheckIntervalInSeconds = prefs.getInt(WiFiPrioritizerPrefsActivity.WIFI_CHECK_INTERVAL_KEY, 60);
		sendBroadcast(new Intent(ACTION_PREFERENCES_CHANGED));
	}
	
	public synchronized void checkReconnect() {
		if (wifiManager.isWifiEnabled() && (wifiManager.getConnectionInfo() != null) && (wifiManager.getConnectionInfo().getNetworkId() != homeWifiNetworkId)) {
			List<ScanResult> scanResults = wifiManager.getScanResults();
			if (scanResults != null) {
				for (ScanResult sr : scanResults) {
					if (sr.SSID.equals(homeWifiSSID)) {
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
		wifiManager.enableNetwork(homeWifiNetworkId, true);
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
		sendBroadcast(new Intent(ACTION_SERVICE_STATUS_CHANGED));
	}
	
	public int getCheckIntervalInSeconds() {
		return wifiCheckIntervalInSeconds;
	}

	public String getHomeWifiSSID() {
		return homeWifiSSID;
	}
	
	public WifiInfo getCurrentWifiInfo() {
		if (wifiManager.isWifiEnabled()) { 
    		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if ((wifiInfo != null) && (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED)) {
            	return wifiInfo;
            } else {
            	return null;
            }
		} else {
			return null;
		}
	}

}
