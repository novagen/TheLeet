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
package com.rubika.aotalk.util;

import com.rubika.aotalk.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.text.Html;

public class WidgetController {
	protected static final String APP_TAG = "--> The Leet :: WidgetController";
	
	public static final String BROADCAST  = "com.rubika.aotalk.UPDATE_WIDGET";
	public static final String BROADCAST_SMALL  = "com.rubika.aotalk.UPDATE_WIDGET_SMALL";
	public static final String BROADCAST_LARGE  = "com.rubika.aotalk.UPDATE_WIDGET_LARGE";
	public static final String CLEAR_BROADCAST  = "com.rubika.aotalk.UPDATE_WIDGET_CLEAR";
	public static final String WALLPAPER  = "com.rubika.aotalk.WALLPAPER";
		
	/**
	 * Set message on the classic widgets
	 * @param message
	 * @param type
	 * @param context
	 */
	public static void setText(String message, int type, Context context) {
		Logging.log(APP_TAG, "Sending normal broadcast");
		
		//Handle text colors
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        
        int COLOR_APP = settings.getInt("color_app", Statics.COLOR_ORG_APP);
        int COLOR_SYS = settings.getInt("color_system", Statics.COLOR_ORG_SYS);
        int COLOR_PRV = settings.getInt("color_prv", Statics.COLOR_ORG_PRV);
        int COLOR_GRP = settings.getInt("color_group", Statics.COLOR_ORG_GRP);
    	
        int color = COLOR_GRP;
        
		switch(type) {
			case ChatParser.MESSAGE_TYPE_SYSTEM:
				color = COLOR_SYS;
				break;
			case ChatParser.MESSAGE_TYPE_PRIVATE:
				color = COLOR_PRV;
				break;
			case ChatParser.MESSAGE_TYPE_CLIENT:
				color = COLOR_APP;
				break;
			case ChatParser.MESSAGE_TYPE_GROUP:
				color = COLOR_GRP;
				break;
			default:
				color = COLOR_GRP;
				break;
		}
		
		Intent broadcast = new Intent(BROADCAST);
		broadcast.putExtra("color", color);
		broadcast.putExtra("text", message);
		context.getApplicationContext().sendBroadcast(broadcast);
		
		broadcast = new Intent(BROADCAST_SMALL);
		broadcast.putExtra("color", color);
		broadcast.putExtra("text", message);
		context.getApplicationContext().sendBroadcast(broadcast);
		
		broadcast = new Intent(BROADCAST_LARGE);
		broadcast.putExtra("color", color);
		broadcast.putExtra("text", message);
		context.getApplicationContext().sendBroadcast(broadcast);
		
		broadcast = new Intent(WALLPAPER);
		broadcast.putExtra("wallpaper", ":!:" + type + ":!:" + message);
	    context.getApplicationContext().sendBroadcast(broadcast);
	}
	
	/**
	 * Set message on the clear widget
	 * @param message
	 * @param user
	 * @param channel
	 * @param context
	 */
	public static void setClearText(String message, String user, String channel, Context context) {
		Logging.log(APP_TAG, "Sending clear broadcast");
		
	    String target = "";
	    String time   = "";
	    String text   = "";

	    if(message.equals(ChatParser.parse(context.getString(R.string.disconnected), ChatParser.MESSAGE_TYPE_CLIENT))) {
	    	target = context.getString(R.string.app_name);
	    	try {
	    	    time = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
	    	} catch (NameNotFoundException e) {
	    		time = "";
	    	}
	    	
	    	text   = context.getString(R.string.widget_clear).replace("\n", "<br />");
	    } else {
		    if(channel != null && !channel.equals(Statics.CHANNEL_PM)) {
		    	target  = channel;
		    	if (channel.equals(Statics.CHANNEL_SYSTEM)) {
		    		target = "System";
		    	}
		    	text = message.replaceAll("\\[[^]]*]","").replaceAll("\\<font[^>]*>","").trim();
		    } else {
		    	if(user != null) {
		    		target = user;
		    	} else {
		    		target = "System";
		    	}
		    	text = message.replaceAll("\\[[^]]*]","").replaceAll("\\<font[^>]*>","").trim().replace("from :", "");
		    	text = Html.fromHtml(text).toString();
		    }
		    
		    time = ChatParser.getFormattedTime().replaceAll("\\<[^>]*>","").replace("[","").replace("]","");
	    }
	    
		Intent broadcast = new Intent(CLEAR_BROADCAST);
		broadcast.putExtra("text", text);
		broadcast.putExtra("target", target);
		broadcast.putExtra("time", time);
		
		context.getApplicationContext().sendBroadcast(broadcast);  
	}
}
