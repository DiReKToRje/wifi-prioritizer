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

package com.gryglicki.android.wifiprioritizer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WiFiPrioritizerActiveService extends Service {

	/* Shared data */
	private WiFiPrioritizerApplication application;
	
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
			super(WiFiPrioritizerActiveService.class.getName()+"-"+PrioritizerThread.class.getName());
		}
		
		@Override
		public void run() {
			WiFiPrioritizerActiveService service = WiFiPrioritizerActiveService.this;
			while (service.runFlag) {
				try {
					application.checkReconnect();
					
					Thread.sleep(application.getCheckIntervalInSeconds() * 1000);
				} catch (InterruptedException ie) {
					service.runFlag = false;
				}
			}

		}

		
	}

}
