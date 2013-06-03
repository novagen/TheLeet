package com.rubika.aotalk;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

public class DashClockExtender extends DashClockExtension {
	private static DashClockExtender dashClockExtender = null;
	
	public static DashClockExtender getInstance() {
		if (dashClockExtender == null) {
			dashClockExtender = new DashClockExtender();
		}
		
		return dashClockExtender;
	}
	
	@Override
	protected void onInitialize(boolean isReconnect) {
		super.onInitialize(isReconnect);
		setUpdateWhenScreenOn(true);
		
		dashClockExtender = this;
	}

	public void changeMessage() {
		onUpdateData(UPDATE_REASON_CONTENT_CHANGED);
	}

	@Override
	protected void onUpdateData(int reason) {
		publishUpdateExtensionData();
	}


	/**
	 * publishUpdata
	 */
	private void publishUpdateExtensionData() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(AOTalk.getContext());
		
		if (!settings.getString("lastCharacterName", "").equals(""))	{	
			PackageManager pm = getPackageManager();
			Intent intent = pm.getLaunchIntentForPackage("com.rubika.aotalk");
	
			ExtensionData data = new ExtensionData()
				.visible(true)
				.icon(R.drawable.outleet)
				.status("")
				.expandedTitle(settings.getString("lastCharacterName", ""));
	
			if (intent != null) {
				data.clickIntent(intent);
			}
	
			publishUpdate(data);
		} else {
			publishUpdate(null);
		}
	}
}