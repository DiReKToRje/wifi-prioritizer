package com.gryglicki.android.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class WiFiPrioritizerActivity extends Activity {
	
	/* Shared Data */
	WiFiPrioritizerApplication application;
	
	/* Managers */
	WifiManager wifiManager;
	BroadcastReceiver wifiStatusReceiver;
	IntentFilter wifiStatusFilter;
	
	/* Views */
	TextView connectedNetwork;
	TextView serviceStatus;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        application = (WiFiPrioritizerApplication) getApplication();
        
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiStatusFilter = new IntentFilter();
        wifiStatusFilter.addAction("android.net.wifi.STATE_CHANGE");
        wifiStatusFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        wifiStatusReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateConnectedNetworkLabel();
			}
		};
		
		connectedNetwork = (TextView) findViewById(R.id.connectedNetwork);
		serviceStatus = (TextView) findViewById(R.id.serviceStatus);
		
		updateConnectedNetworkLabel();
		updateServiceStatusLabel();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	registerReceiver(wifiStatusReceiver, wifiStatusFilter);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	unregisterReceiver(wifiStatusReceiver);
    }
    
    
    private void updateConnectedNetworkLabel() {
    	if (wifiManager.isWifiEnabled()) {
    		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if ((wifiInfo == null) || (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED)) {
            	connectedNetwork.setText(getString(R.string.no_connected_network));
            } else {
            	StringBuilder sb = new StringBuilder();
            	sb.append(getString(R.string.connected_network));
            	sb.append("SSID: ").append(wifiInfo.getSSID());
            	sb.append(", signal: ").append(wifiInfo.getRssi());
            	connectedNetwork.setText(sb);
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
    		case R.id.menu_prefsActivity:
    			startActivity(new Intent(this, PrefsActivity.class));
    			break;
    		case R.id.menu_serviceStart:
    			if (! application.isPrioritizerServiceRunning()) {
    				startService(new Intent(this, WiFiPrioritizerService.class));
        			updateServiceStatusLabel();	
    			} else {
    				showError(R.string.error_service_running);
    			}
    			break;
    		case R.id.menu_serviceStop:
    			if (application.isPrioritizerServiceRunning()) {
    				stopService(new Intent(this, WiFiPrioritizerService.class));
        			updateServiceStatusLabel();	
    			}
    			break;
    	}
    	return true;
    }
    
    
    private void showError(int resMsgId) {
    	Toast.makeText(this, getString(resMsgId), Toast.LENGTH_SHORT).show();
    }
    
}