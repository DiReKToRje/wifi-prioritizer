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

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class WiFiPrioritizerMainActivity extends Activity {
	
	private static final int HELP_DIALOG = 0;
	
	/* Shared Data */
	WiFiPrioritizerApplication application;
	
	/* Managers */
	WifiManager wifiManager;
	BroadcastReceiver broadcastReceiver;
	IntentFilter broadcastFilter;
	
	/* Views */
	TextView homeNetwork;
	TextView connectedNetwork;
	TextView serviceStatus;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        application = (WiFiPrioritizerApplication) getApplication();
        
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        broadcastFilter = new IntentFilter();
        broadcastFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); //Connected or Disconnected from Wi-Fi network
        broadcastFilter.addAction(WifiManager.RSSI_CHANGED_ACTION); //Change in signal strength
        broadcastFilter.addAction(WiFiPrioritizerApplication.ACTION_SERVICE_STATUS_CHANGED); //Service changed

        broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())
					|| WifiManager.RSSI_CHANGED_ACTION.equals(intent.getAction())) {
					updateConnectedNetworkLabel();
				} else if (WiFiPrioritizerApplication.ACTION_SERVICE_STATUS_CHANGED.equals(intent.getAction())) {
					updateServiceStatusLabel();
				}
			}
		};
		
		homeNetwork = (TextView) findViewById(R.id.homeNetwork);
		connectedNetwork = (TextView) findViewById(R.id.connectedNetwork);
		serviceStatus = (TextView) findViewById(R.id.serviceStatus);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	registerReceiver(broadcastReceiver, broadcastFilter);
    	updateServiceStatusLabel();
    	updateDefaultNetworkLabel();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	unregisterReceiver(broadcastReceiver);
    }
    
    private void updateDefaultNetworkLabel() {
    	homeNetwork.setText(application.getHomeWifiSSID() != null ? application.getHomeWifiSSID() : application.getString(R.string.no_home_network));
    }
    
    private void updateConnectedNetworkLabel() {
    	if (wifiManager.isWifiEnabled()) {
    		WifiInfo wifiInfo = application.getCurrentWifiInfo();
            if (wifiInfo != null) {
            	StringBuilder sb = new StringBuilder();
            	sb.append(wifiInfo.getSSID()).append(" (").append(wifiInfo.getRssi()).append("dBm)");
            	connectedNetwork.setText(sb);
            } else {
            	connectedNetwork.setText(getString(R.string.no_connected_network));
            }
    	} else {
    		connectedNetwork.setText(getString(R.string.wifi_disabled));
    	}
        connectedNetwork.invalidate();
    }
    
    private void updateServiceStatusLabel() {
    	if (application.isPrioritizerServiceRunning()) {
    		serviceStatus.setText(getString(R.string.service_running));
    	} else {
    		serviceStatus.setText(getString(R.string.servcie_stopped));
    	}
        serviceStatus.invalidate();
    }
    
    
    
    /* Reconnect button onClick */
    public void reconnect(View view) {
    	application.checkReconnect();
    }
    
    
    
    
    /* Menu callbacks */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater menuInflater = getMenuInflater();
    	menuInflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_serviceStart:
    			if (! application.isPrioritizerServiceRunning()) {
    				startService(new Intent(this, WiFiPrioritizerActiveService.class));
    			} else {
    				showError(R.string.error_service_running);
    			}
    			break;
    		case R.id.menu_serviceStop:
    			if (application.isPrioritizerServiceRunning()) {
    				stopService(new Intent(this, WiFiPrioritizerActiveService.class));
    			}
    			break;
    		case R.id.menu_prefsActivity:
    			startActivity(new Intent(this, WiFiPrioritizerPrefsActivity.class));
    			break;
    		case R.id.menu_helpDialog:
    			showDialog(HELP_DIALOG);
    			break;
    	}
    	return true;
    }
    
    
    private void showError(int resMsgId) {
    	Toast.makeText(this, getString(resMsgId), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected Dialog onCreateDialog(int dialogId) {
    	Dialog dialog;
    	switch (dialogId) {
    		case HELP_DIALOG:
    			dialog = new Dialog(this);
    			dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
    			dialog.setContentView(R.layout.help_dialog);
    			dialog.setTitle(getString(R.string.helpDialog_title));
    			dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_menu_help);
    			break;
    		default:
    			dialog = super.onCreateDialog(dialogId);
    	}
    	return dialog;
    }
    
}
