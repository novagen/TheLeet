/*
 * Watch.java
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Watch {
	protected static final String APPTAG = "--> AOTalk::Watch";
	private Intent watchBroadcast;
	private SharedPreferences settings;
	private Context context;
	
	public static final String CHN_PRIVATE = "Private messages";
	public static final String CHN_SYSTEM  = "System messages";
	public static final String CHN_OTHERS  = "Other channels";

	
	public Watch(Context ct) {
		settings = PreferenceManager.getDefaultSharedPreferences(ct);
		context = ct;
		watchBroadcast = new Intent();
	}
	
	/**
	 * Sends message to the watch
	 * @param message1
	 * @param message2
	 * @param channel
	 */
	public void pushText(String character, String channel, String message, boolean vibrate) {
		if(settings.getBoolean("enablewatch", false)) {			
			String msg1 = "";
			String msg2 = "";
			String srcs = null;
			
			ChatParser cp = new ChatParser();
			msg1 = cp.getTime().replaceAll("\\<[^>]*>","").replace("[","").replace("]","");
			
			if(vibrate) {
				watchBroadcast.setAction("com.smartmadsoft.openwatch.action.VIBRATE");
			} else {
				watchBroadcast.setAction("com.smartmadsoft.openwatch.action.TEXT");
			}
			
			if(character != null) {
				srcs = character;
			}
			
			if(channel != null) {
				srcs = channel;
			}
			
			if(srcs == null) {
				srcs = "System";
			}
			
			msg1 = msg1 + " " + srcs;
			
			watchBroadcast.putExtra("line1", msg1);
		
			if(message != null) {
				message = message.replaceAll("\\<[^>]*>","");
				
				if(message.contains("]: ")) {
					msg2 = message.substring(message.indexOf("]: ") + 3);
				} else {
					msg2 = message.substring(message.indexOf("] ") + 2);
				}
				
				watchBroadcast.putExtra("line2", msg2);
			}
			
			context.getApplicationContext().sendBroadcast(watchBroadcast);
		}
	}
}
