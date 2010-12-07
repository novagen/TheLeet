/*
 * WidgetController.java
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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.Html;
import android.widget.RemoteViews;

public class WidgetController {
	private AppWidgetManager manager;
	
	/**
	 * Set text of the widgets
	 * @param message
	 * @param context
	 */
	public void setText(String message, Context context) {
        manager = AppWidgetManager.getInstance(context);
        
        //Small widget
        RemoteViews smallWidgetViews = new RemoteViews(context.getPackageName(), R.layout.widget_small);
        smallWidgetViews.setTextViewText(R.id.widget_text, Html.fromHtml(message));
        ComponentName smallWidget = new ComponentName(context, WidgetSmall.class);
        manager.updateAppWidget(smallWidget, smallWidgetViews);
        
        //Large widget
        RemoteViews largeWidgetViews = new RemoteViews(context.getPackageName(), R.layout.widget_large);
        largeWidgetViews.setTextViewText(R.id.widget_text, Html.fromHtml(message));
        ComponentName largeWidget = new ComponentName(context, WidgetLarge.class);
        manager.updateAppWidget(largeWidget, largeWidgetViews);
	}
}
