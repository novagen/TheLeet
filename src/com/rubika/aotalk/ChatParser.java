package com.rubika.aotalk;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.Html;
import android.text.Spanned;

public class ChatParser {
	public String parse(String message) {
		return "\n" + getTime() + " " + removeTags(message);
	}
	
    public String getTime() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        return "[" + dateFormat.format(date) + "]";
    }
    
	private Spanned removeTags(String message) {
		//return message.replaceAll("\\<.*?\\>", "");
		return Html.fromHtml(message);
	}
}
