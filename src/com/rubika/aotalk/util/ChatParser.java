/*
 * ChatParser.java
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

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatParser {
	public final static int TYPE_SYSTEM_MESSAGE  = 0;
	public final static int TYPE_PRIVATE_MESSAGE = 1;
	public final static int TYPE_CLIENT_MESSAGE  = 2;
	public final static int TYPE_GROUP_MESSAGE   = 3;
	public final static int TYPE_PLAIN_MESSAGE   = 4;
	public final static int TYPE_ORG_MESSAGE     = 5;
	public final static int TYPE_PG_MESSAGE      = 6;
	public final static int TYPE_FRIEND_MESSAGE  = 7;
	
	public static String parse(String message, int type) {
		String output = "";
		
		if(type != TYPE_PLAIN_MESSAGE) {
			output += "<font color=#ffffff>" + getFormattedTime() + "</font> ";
		}
		
		output += message.replace("\n", "<br />");
		
		return output;
	}
	
    public static String getFormattedTime() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        return "<b><font color=#ffffff>[" + dateFormat.format(date) + "]</font></b>";
    }
	
    public static String getFormattedTimeFromLong(long datetime) {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date(datetime);
        return "<b>[" + dateFormat.format(date) + "]</b>";
    }
}
