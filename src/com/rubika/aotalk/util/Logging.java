package com.rubika.aotalk.util;

import com.rubika.aotalk.AOTalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Logging {
	public static void log(String tag, String message) {
		if (AOTalk.getContext() != null) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(AOTalk.getContext());
			
			if (settings.getBoolean("enableDebug", false)) {
				Log.d(tag, "--------------------\n" + message + "\n--------------------");
			}
		}
	}

	public static void toast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
