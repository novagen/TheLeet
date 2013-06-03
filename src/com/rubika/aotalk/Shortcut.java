package com.rubika.aotalk;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

public class Shortcut extends SherlockActivity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//super.setTheme(R.style.Theme_AOTalkTheme_Light);
        
		Intent shortcutIntent = new Intent();
		shortcutIntent.setClassName("com.rubika.aotalk", ".AOTalk");
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		shortcutIntent.putExtra("AccountId", 0);

		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "The Leet : [account]");
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher));

		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		sendBroadcast(addIntent);
	}
}
