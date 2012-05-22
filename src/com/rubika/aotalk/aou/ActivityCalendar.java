package com.rubika.aotalk.aou;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ActivityCalendar extends SherlockActivity {
	private Bundle extras;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar);

        final ActionBar bar = getSupportActionBar();
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        extras = intent.getExtras();

        setTitle(extras.getString("title"));
        
        TextView date   = (TextView) findViewById(R.id.date);
        TextView server = (TextView) findViewById(R.id.server);
        TextView text   = (TextView) findViewById(R.id.text);
        
        if (extras.getString("date").equals("")) {
        	date.setVisibility(View.GONE);
        } else {
        	date.setText(extras.getString("date"));
        }
        
        if (extras.getString("server").equals("")) {
        	server.setVisibility(View.GONE);
        } else {
        	server.setText(extras.getString("server"));
        }
        
        if (extras.getString("text").equals("")) {
        	text.setVisibility(View.GONE);
        } else {
        	text.setText(extras.getString("text"));
        }

        ((Button) findViewById(R.id.close)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

        ((Button) findViewById(R.id.forum)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse(extras.getString("link"));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
	}

    @Override
    protected void onResume() {
    	super.onResume();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_aou, menu);
    	return true;
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
