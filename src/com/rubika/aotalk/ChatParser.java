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
	
    private String getTime() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        return "<b>[" + dateFormat.format(date) + "]</b>";
    }
}
