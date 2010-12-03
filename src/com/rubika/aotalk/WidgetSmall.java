/*
 * WidgetSmall.java
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

	        String iconPrefix = WidgetSmallConfig.loadIconPref(context, appWidgetId);
            updateWidget(context, widgetMngr, appWidgetId, iconPrefix);
        }
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(APPTAG, "onReceive");
        
		super.onReceive(context, intent);
	}
	
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.d(APPTAG, "onDeleted");
		
		super.onDeleted(context, appWidgetIds);
	}
	
	static void updateWidget(Context context, AppWidgetManager widgetManager, int widgetId, String iconPrefix) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_small);
        String text = WidgetSmallConfig.loadIconPref(context, widgetId);

        Intent appIntent = new Intent(context, AOTalk.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);

        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        
        if(text.equals("Default")) {
        	views.setImageViewResource(R.id.widget_button, R.drawable.widget_button_aotalk);
        } else if(text.equals("Atrox")) {
        	views.setImageViewResource(R.id.widget_button, R.drawable.widget_button_atrox);
        } else if(text.equals("Clan")) {
        	views.setImageViewResource(R.id.widget_button, R.drawable.widget_button_clan);
        } else if(text.equals("ICC")) {
        	views.setImageViewResource(R.id.widget_button, R.drawable.widget_button_icc);
        } else if(text.equals("Omni")) {
        	views.setImageViewResource(R.id.widget_button, R.drawable.widget_button_omni);
        } else {
        	views.setImageViewResource(R.id.widget_button, R.drawable.widget_button_aotalk);
        }
        
        // Tell the widget manager
        widgetManager.updateAppWidget(widgetId, views);
    }
}
