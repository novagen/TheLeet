package com.rubika.aotalk.account;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.RKNet;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class RegisterActivity extends SherlockActivity {
	protected static final String APP_TAG   = "--> The Leet :: RegisterActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//super.setTheme(R.style.Theme_AOTalkTheme_Light);

		setContentView(R.layout.account_register);

        final ActionBar bar = getSupportActionBar();
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        WebView browser = (WebView) findViewById(R.id.webbrowser);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        browser.setBackgroundColor(0);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.addJavascriptInterface(new WebAppInterface(this), "TheLeet");
        
        browser.setWebViewClient(new WebViewClient() {
        	@Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
        		return false;
        	}
        	
        	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
        });
        
        browser.loadUrl(RKNet.RKNET_REGISTER_PATH);
	}
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	try {
        	EasyTracker.getInstance().activityStart(this);
    	} catch (IllegalStateException e) {
    		Logging.log(APP_TAG, e.getMessage());
    	}
    }
    
    @Override
    protected void onStop() {
    	super.onStop();

    	try {
            EasyTracker.getInstance().activityStop(this);
    	} catch (IllegalStateException e) {
    		Logging.log(APP_TAG, e.getMessage());
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
