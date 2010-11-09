package com.rubika.aotalk;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

public class ChatParser {
	protected static final int MSG_CONNECTED     = 0x101;
	protected static final int MSG_FAILURE       = 0x102;
	protected static final int MSG_STARTED       = 0x103;
	protected static final int MSG_CHARLIST      = 0x104;
	protected static final int MSG_TELL          = 0x105;
	protected static final int MSG_GROUP         = 0x106;
	protected static final int MSG_NOTICE        = 0x107;
	protected static final int MSG_DISCONNECTED  = 0x108;
	protected static final int MSG_AUTHENTICATED = 0x109;
	protected static final int MSG_LOGGED_IN     = 0x110;
	
	@SuppressWarnings("unused")
	private TextView logtext;
	
	public ChatParser(TextView view) {
		this.logtext = view;
	}
	
	public Spanned parse(String message) {
		return Html.fromHtml("<br />" + getTime() + " " + message.replace("\n", "<br />"));
	}
	
    private String getTime() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        return "<b>[" + dateFormat.format(date) + "]</b>";
    }
}
