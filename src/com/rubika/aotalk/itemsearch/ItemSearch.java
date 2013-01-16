package com.rubika.aotalk.itemsearch;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.Preferences;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;

public class ItemSearch extends SherlockFragmentActivity {
	protected static final String APP_TAG = "--> AOTalk::ItemSearch";
	public static final String SEARCH_URL = "http://cidb.botsharp.net/?bot=aotalk&output=xml&version=1.2&search=%s";

	private static Context context;
	public String searchString = null;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = this;
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
        	searchString = extras.getString("name");
        	
        	try {
				searchString = URLEncoder.encode(searchString, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
        	
        	Logging.log(APP_TAG, "got extras name: " + searchString);
        }
        
        if (getIntent().getData() != null) {
	        if(getIntent().getData().toString().startsWith("sitem://")) {
	        	searchString = getIntent().getData().toString().replace("sitem://", "");
	        	
	        	try {
					searchString = URLEncoder.encode(searchString, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
	        	
	        	Logging.log(APP_TAG, "got intent name: " + searchString);
	        }
        }
       
        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.abbg));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SherlockListFragment mFragment = (SherlockListFragment) SherlockFragment.instantiate(this, FragmentActivityItemsearch.DataListFragment.class.getName(), null);
                
        ft.add(android.R.id.content, mFragment, "search");
        ft.commit();
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_aorb, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
			case R.id.preferences:
				Intent intent = new Intent(this, Preferences.class);
				startActivity(intent);
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public static Context getAppContext() {
        return ItemSearch.context;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
    }
}