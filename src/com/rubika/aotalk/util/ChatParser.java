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
import java.util.Locale;

public class ChatParser {
	public final static int MESSAGE_TYPE_SYSTEM		 = 0;
	public final static int MESSAGE_TYPE_PRIVATE	 = 1;
	public final static int MESSAGE_TYPE_CLIENT		 = 2;
	public final static int MESSAGE_TYPE_GROUP		 = 3;
	public final static int MESSAGE_TYPE_PLAIN		 = 4;
	//public final static int MESSAGE_TYPE_ORG		 = 5;
	public final static int MESSAGE_TYPE_PG			 = 6;
	public final static int MESSAGE_TYPE_FRIEND		 = 7;
	
	public static String parse(String message, int type) {
		String output = "";
		
		if(type != MESSAGE_TYPE_PLAIN) {
			output += getFormattedTime() + " ";
		}
		
		output += message.replace("\n", "<br />");
		
		return output;
	}
	
    public static String getFormattedTime() {
    	return getFormattedTimeFromLong(new Date().getTime());
    }
	
    public static String getFormattedTimeFromLong(long datetime) {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date date = new Date(datetime);
        return "<b><font color=#ffffff>[" + dateFormat.format(date) + "]</font></b>";
    }
}
