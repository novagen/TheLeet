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
package com.rubika.aotalk;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatParser {
	public final static int TYPE_SYSTEM_MESSAGE  = 0;
	public final static int TYPE_PRIVATE_MESSAGE = 1;
	public final static int TYPE_CLIENT_MESSAGE  = 2;
	public final static int TYPE_GROUP_MESSAGE   = 3;
	public final static int TYPE_PLAIN_MESSAGE   = 4;
	
	public String parse(String message, int type) {
		String output = "";
		
		switch(type) {
			case TYPE_SYSTEM_MESSAGE:
				output += getTime() + "<font color=#FFCC33>";
				break;
			case TYPE_PRIVATE_MESSAGE:
				output += getTime() + "<font color=#CCFFCC>";
				break;
			case TYPE_CLIENT_MESSAGE:
				output += getTime() + "<font color=#CC99CC>";
				break;
			case TYPE_GROUP_MESSAGE:
				output += getTime();
				break;
		}
		
		output += " " + message.replace("\n", "<br />");
		
		switch(type) {
			case TYPE_SYSTEM_MESSAGE:
				output += "</font>";
				break;
			case TYPE_PRIVATE_MESSAGE:
				output += "</font>";
				break;
			case TYPE_CLIENT_MESSAGE:
				output += "</font>";
				break;
			case TYPE_GROUP_MESSAGE:
				break;
		}
		
		return output;
	}
	
    public String getTime() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        return "<b>[" + dateFormat.format(date) + "]</b>";
    }
}
