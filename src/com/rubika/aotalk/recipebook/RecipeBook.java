package com.rubika.aotalk.recipebook;

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

public class RecipeBook extends SherlockFragmentActivity {
	protected static final String APP_TAG = "--> AOTalk::RecipeBook";
	public static final String RECIPES_CATEGORIES_URL = "http://aodevnet.com/recipes/api/cats/format/xml/bot/aotalk";
	public static final String RECIPES_CATEGORY_URL  = "http://aodevnet.com/recipes/api/catlist/format/xml/id/%s/bot/aotalk";
	public static final String RECIPES_INFO_URL    = "http://aodevnet.com/recipes/api/show/id/%s/format/xml/bot/aotalk";
	public static final String RECIPES_SEARCH_URL  = "http://aodevnet.com/recipes/api/search/kw/%s/format/xml/bot/aotalk";
	public static final String RECIPES_BY_ITEM_URL = "http://aodevnet.com/recipes/api/byitem/id/%s/format/xml/bot/aotalk";

	private static Context context;
	public String searchId = null;
	public String searchText = null;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = this;
        
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
        
        if (searchText != null || searchId != null) {
        	setTitle(getString(R.string.search_results));
        }
        
        if (getIntent().getData() != null) {
	        if(getIntent().getData().toString().startsWith("aorb://")) {
	        	searchId = getIntent().getData().toString().replace("aorb://", "");
	        	Logging.log(APP_TAG, "got intent id: " + searchId);
	        }
        }
       
        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.abbg));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SherlockListFragment mFragment = (SherlockListFragment) SherlockFragment.instantiate(this, FragmentActivityRecipes.DataListFragment.class.getName(), null);
                
        ft.add(android.R.id.content, mFragment, "browse");
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
        return RecipeBook.context;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
    }
}
