package com.rubika.aotalk.util;

import com.rubika.aotalk.AOTalk;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Logging {
	public static void log(String tag, String message) {
		if (AOTalk.LOGGING_ENABLED) {
			Log.d(tag, "--------------------\n" + message + "\n--------------------");
		}
	}

	public static void toast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
