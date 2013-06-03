package com.rubika.aotalk.account;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends SherlockActivity {
	protected static final String APP_TAG   = "--> The Leet :: ProfileActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//super.setTheme(R.style.Theme_AOTalkTheme_Light);

		setContentView(R.layout.account_profile);

        final ActionBar bar = getSupportActionBar();
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
		
		if (getIntent().getData() != null) {
			Cursor cursor = getContentResolver().query(getIntent().getData(), null, null, null, null);
			
			if (cursor.moveToNext()) {
				String name = cursor.getString(cursor.getColumnIndex("DATA1"));
				TextView tv = (TextView) findViewById(R.id.profiletext);
				tv.setText(name);
			}
			
			cursor.close();
		} else {
			finish();
		}
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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
