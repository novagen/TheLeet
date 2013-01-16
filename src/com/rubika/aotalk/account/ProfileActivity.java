package com.rubika.aotalk.account;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.R;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends SherlockActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_profile);

        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.abbg));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
		
		if (getIntent().getData() != null) {
			//Cursor cursor = managedQuery(getIntent().getData(), null, null, null, null);
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
