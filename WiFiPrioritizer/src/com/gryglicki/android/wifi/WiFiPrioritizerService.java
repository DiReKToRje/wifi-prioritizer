package com.gryglicki.android.wifi;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class WiFiPrioritizerService extends Service {

	/* Shared data */
	private WiFiPrioritizerApplication application;
	private SharedPreferences prefs;
	
	/* Managers */
	WifiManager wifiManager;
	
	private volatile boolean runFlag = false;
	private PrioritizerThread prioritizerThread;
	
	
	/* We don't use it here */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	/* Creating service */
	@Override
	public void onCreate() {
		super.onCreate();
		prioritizerThread = new PrioritizerThread();
		application = (WiFiPrioritizerApplication) getApplication();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	}
	
	/* startService() */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (! runFlag) {
			runFlag = true;
			prioritizerThread.start();
			application.setPrioritizerServiceRunning(runFlag);
		}
		return START_STICKY;
	}
	
	/* stopService() */
	@Override
	public void onDestroy() {
		super.onDestroy();
		runFlag = false;
		
		prioritizerThread.interrupt();
		prioritizerThread = null;
		
		application.setPrioritizerServiceRunning(runFlag);
	}

	
	
	/* Working Thread class */
	private class PrioritizerThread extends Thread {
		public PrioritizerThread() {
			super(WiFiPrioritizerService.class.getName()+"-"+PrioritizerThread.class.getName());
		}
		
		@Override
		public void run() {
			WiFiPrioritizerService service = WiFiPrioritizerService.this;
			while (service.runFlag) {
				try {
					application.checkReconnect();
					
					Log.d("TEST", "Service.trying to reconnect");
					Thread.sleep(application.getCheckInterval() * 1000);
				} catch (InterruptedException ie) {
					service.runFlag = false;
				}
			}

		}

		
	}

}
