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

public class WidgetSmall extends AppWidgetProvider {
	private static final String APPTAG = "--> AOTalk::AOTalkWidget";

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

	        String iconPrefix = WidgetConfig.loadIconPref(context, appWidgetId);
            updateAppWidget(context, widgetMngr, appWidgetId, iconPrefix);
        }
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(APPTAG, "onReceive");
        
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
	
	static void updateAppWidget(Context context, AppWidgetManager widgetManager, int widgetId, String iconPrefix) {
        String text = WidgetConfig.loadIconPref(context, widgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_small);

        Intent appIntent = new Intent(context, AOTalk.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);

        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        
        if(text.equals("Default")) {
        	views.setImageViewResource(R.id.widget_button, R.drawable.widget_button_aotalk);
        }

        if(text.equals("Atrox")) {
        	views.setImageViewResource(R.id.widget_button, R.drawable.widget_button_atrox);
        }    
        
        if(text.equals("Clan")) {
        	views.setImageViewResource(R.id.widget_button, R.drawable.widget_button_clan);
        }
        
        if(text.equals("ICC")) {
        	views.setImageViewResource(R.id.widget_button, R.drawable.widget_button_icc);
        }
        
        if(text.equals("Omni")) {
        	views.setImageViewResource(R.id.widget_button, R.drawable.widget_button_omni);
        }
        
        // Tell the widget manager
        widgetManager.updateAppWidget(widgetId, views);
    }
}
