package com.rubika.aotalk.aou;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ActivityCalendar extends SherlockFragmentActivity {
	protected static final String APP_TAG = "--> The Leet :: ActivityCalendar";
    private static Tracker tracker;
	
	public ActivityCalendar() {
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//super.setTheme(R.style.Theme_AOTalkTheme_Light);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        
        FrameLayout frame = new FrameLayout(this);
        setContentView(frame, lp);
        
        final ActionBar bar = getSupportActionBar();
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        
        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, DataFragment.newInstance(
            	null,
	    		intent.getExtras().getString("title"),
				intent.getExtras().getString("text"),
				intent.getExtras().getString("date"),
				intent.getExtras().getString("link")
            )).commit();
        }
	}

    @Override
    protected void onResume() {
    	super.onResume();
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
	

    public static class DataFragment extends SherlockFragment {
    	private Bundle extras;
        private static AOU aou;

        public DataFragment() {
        }
        
        public static DataFragment newInstance(AOU parent, String title, String text, String date, String link) {
        	DataFragment f = new DataFragment();
        	
        	Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("text", text);
            args.putString("date", date);
            args.putString("link", link);
            f.setArguments(args);
        	
            aou = parent;
        	
            return f;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            
            extras = this.getArguments();
            this.getActivity().setTitle(extras.getString("title"));

            setHasOptionsMenu(true);
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (container == null) {
                return null;
            }

            //setContentView(R.layout.activity_calendar);
            View layout = inflater.inflate(R.layout.activity_calendar, container, false);
            extras = this.getArguments();
            
            EasyTracker.getInstance().setContext( this.getActivity().getApplicationContext());
            tracker = EasyTracker.getTracker();
    		tracker.sendEvent("AOU", "Calendar", extras.getString("title"), 0L);
            
            TextView date = (TextView) layout.findViewById(R.id.date);
            TextView text = (TextView) layout.findViewById(R.id.text);
            
            if (extras.getString("date").equals("")) {
            	date.setVisibility(View.GONE);
            } else {
            	date.setText(extras.getString("date"));
            }
            
            if (extras.getString("text").equals("")) {
            	text.setVisibility(View.GONE);
            } else {
            	text.setText(extras.getString("text"));
            }

            ((Button) layout.findViewById(R.id.close)).setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				if (AOU.isTablet && aou != null) {
    					aou.unloadFragment();
    				} else {
    					getActivity().finish();
    				}
    			}
    		});

            ((Button) layout.findViewById(R.id.forum)).setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				Uri uri = Uri.parse(extras.getString("link"));
    				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    				startActivity(intent);
    			}
    		});

            return layout;
        }
    }
}
