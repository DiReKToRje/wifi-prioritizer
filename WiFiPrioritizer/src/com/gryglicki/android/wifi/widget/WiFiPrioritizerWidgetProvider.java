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

package com.gryglicki.android.wifi.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.widget.RemoteViews;

import com.gryglicki.android.wifi.R;
import com.gryglicki.android.wifi.WiFiPrioritizerApplication;

public class WiFiPrioritizerWidgetProvider extends AppWidgetProvider {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())
			|| WiFiPrioritizerApplication.ACTION_PREFERENCES_CHANGED.equals(intent.getAction())
			|| WiFiPrioritizerApplication.ACTION_SERVICE_STATUS_CHANGED.equals(intent.getAction())) {
			updateAllWidgetsByProvider(context, AppWidgetManager.getInstance(context));
		} else {
			super.onReceive(context, intent);	
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		updateAllWidgetsByProvider(context, appWidgetManager);
	}
	
	
	private void updateAllWidgetsByProvider(Context context, AppWidgetManager appWidgetManager) {
		if (context.getApplicationContext() instanceof WiFiPrioritizerApplication) {
			WiFiPrioritizerApplication application = (WiFiPrioritizerApplication) context.getApplicationContext();
		
			String homeWifi = (application.getHomeWifiSSID() != null ? application.getHomeWifiSSID() : application.getString(R.string.no_home_network));
			String currentWifi = "";
			WifiInfo wifiInfo = application.getCurrentWifiInfo();
			if (wifiInfo != null) {
				currentWifi = wifiInfo.getSSID();
			} else {
				currentWifi = application.getString(R.string.no_connected_network);
			}
			
			RemoteViews remoteViews;
			ComponentName wifiPrioritizerWidgetName;
			
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
			wifiPrioritizerWidgetName = new ComponentName(context, WiFiPrioritizerWidgetProvider.class);
			remoteViews.setTextViewText(R.id.widget_homeWifi, homeWifi);
			remoteViews.setTextViewText(R.id.widget_currentWifi, currentWifi);
			if (application.isPrioritizerServiceRunning()) {
				remoteViews.setImageViewResource(R.id.widget_serviceStatusImg, R.drawable.btn_check_buttonless_on);
			} else {
				remoteViews.setImageViewResource(R.id.widget_serviceStatusImg, R.drawable.btn_check_buttonless_off);
			}

//			Intent intent = new Intent(context, WiFiPrioritizerWidgetProvider.class);
//			intent.setAction(WiFiPrioritizerApplication.ACTION_RECONNECT);
//			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//			remoteViews.setOnClickPendingIntent(R.id.widget_homeWifi, pendingIntent);

			appWidgetManager.updateAppWidget(wifiPrioritizerWidgetName, remoteViews);
		}
	}
	
}
