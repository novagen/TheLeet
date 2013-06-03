package com.rubika.aotalk.account;

import android.app.Activity;
import android.webkit.JavascriptInterface;

public class WebAppInterface {
	private Activity activity;

	WebAppInterface(Activity a) {
		activity = a;
	}

	@JavascriptInterface
	public void finishThis() {
		activity.finish();
	}
}