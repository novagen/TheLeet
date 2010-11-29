/*
 * AOTalkWidgetSmall.java
 *
 *************************************************************************
 * Copyright 2010 Christofer Engel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rubika.aotalk;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class AOTalkWidgetAtrox extends AppWidgetProvider {
	private static final String APPTAG = "--> AOTalk::AOTalkWidget";
	
	public static String ACTION_WIDGET_LAUNCH   = "ConfigureWidget";
	public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
    private int[] widgetIds;
    private AppWidgetManager widgetMngr;
		
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(APPTAG, "onUpdate");
        
		widgetMngr = appWidgetManager;
		widgetIds = appWidgetIds;
		
		int N = widgetIds.length;

        for (int x = 0; x < N; x++) {
	        int appWidgetId = appWidgetIds[x];
	        
	        Intent appIntent = new Intent(context, AOTalk.class);
	        appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);
	
	        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_atrox);
	        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
	
	        widgetMngr.updateAppWidget(appWidgetId, views);
        }
        
		//super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(APPTAG, "onReceive");
		
		if(widgetIds != null) {
			int N = widgetIds.length;
			
	        for (int x = 0; x < N; x++) {
		        int appWidgetId = widgetIds[x];
		                
		        Intent appIntent = new Intent(context, AOTalk.class);
		        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, appIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_atrox);
		        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
		
		        widgetMngr.updateAppWidget(appWidgetId, views);
	        }
		}
        
		// v1.5 fix that doesn't call onDelete Action
		final String action = intent.getAction();	
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID, 
				AppWidgetManager.INVALID_APPWIDGET_ID
			);
			
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else {
			super.onReceive(context, intent);
		}
	}
	
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.d(APPTAG, "onDeleted");
		
		super.onDeleted(context, appWidgetIds);
	}
}
