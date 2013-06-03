package com.rubika.aotalk.recipebook;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.rubika.aotalk.Preferences;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;
import com.viewpagerindicator.TitlePageIndicator;

public class RecipeBook extends SherlockFragmentActivity implements ViewPager.OnPageChangeListener {
	protected static final String APP_TAG = "--> The Leet :: RecipeBook";

	private static Context context;
	public String searchId = null;
	public String searchText = null;
	public boolean isTablet = false;
	public static ViewPager fragmentPager;
	private static TitlePageIndicator titleIndicator;
	private static List<SherlockListFragment> fragments;
	private static SharedPreferences settings;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//super.setTheme(R.style.Theme_AOTalkTheme_Light);
        
        setContentView(R.layout.recipebook);
        
        context = this;
        settings = PreferenceManager.getDefaultSharedPreferences(this);
       
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
        	searchId = extras.getString("id");
        	searchText = extras.getString("text");
        	
        	try {
        		if (searchText != null) {
        			searchText = URLEncoder.encode(searchText, "UTF-8").replace("+", "%20");
        		}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
        	
        	Logging.log(APP_TAG, "got extras id: " + searchId);
        	Logging.log(APP_TAG, "got extras text: " + searchText);
        }
        
        if (getIntent().getData() != null) {
	        if(getIntent().getData().toString().startsWith("aorb://")) {
	        	searchId = getIntent().getData().toString().replace("aorb://", "");
	        	Logging.log(APP_TAG, "got intent id: " + searchId);
	        }
        }
        
        if (searchText != null || searchId != null) {
        	setTitle(getString(R.string.search_results));
        }
       
        final ActionBar bar = getSupportActionBar();
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
                
		fragments = new Vector<SherlockListFragment>();
        fragments.add(FragmentActivityRecipes.DataListFragment.newInstance(this));
        
        FragmentAdapter fragmentAdapter = new FragmentAdapter(super.getSupportFragmentManager(), fragments);

        fragmentPager = (ViewPager) findViewById(R.id.fragmentpager);
        fragmentPager.setAdapter(fragmentAdapter);
        fragmentPager.setOnPageChangeListener(this);
        fragmentPager.setPageMargin(0);

        titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
        titleIndicator.setViewPager(fragmentPager);
        
        setTitleIndicator();
        if (findViewById(R.id.datafragment) != null) {
        	isTablet = true;
        }
	}
	
	public void loadFragment(Intent intent, int type) {
    	FragmentManager fm = getSupportFragmentManager();
    	FragmentTransaction ft = fm.beginTransaction();
    	
    	switch(type) {
    	case 1:
    		ft.replace(R.id.datafragment, ActivityRecipe.DataFragment.newInstance(
    				this,
	    			intent.getExtras().getString("id")
	    		));
    		break;
    	}
    	
    	ft.commit();
	}
	
	public void unloadFragment() {
    	FragmentManager fm = getSupportFragmentManager();
    	FragmentTransaction ft = fm.beginTransaction();
    	
    	if (fm.findFragmentById(0) != null) {
    		ft.remove(fm.findFragmentById(0));
    	} else {
    		fm.popBackStack();
    	}
    	
    	ft.commit();
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
    
    private static void setTitleIndicator() {
        if (settings.getBoolean("hideTitles", false)) {
        	titleIndicator.setVisibility(View.GONE);
        } else {
        	titleIndicator.setVisibility(View.VISIBLE);
        }
    }
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
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
        return RecipeBook.context;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
    }
}
