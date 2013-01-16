package com.rubika.aotalk.util;

import com.rubika.aotalk.TheLeet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Logging {
	public static void log(String tag, String message) {
		if (TheLeet.getContext() != null) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(TheLeet.getContext());
			
			if (settings.getBoolean("enableDebug", false)) {
				Log.d(tag, "--------------------\n" + message + "\n--------------------");
			}
		}
	}

	public static void toast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
