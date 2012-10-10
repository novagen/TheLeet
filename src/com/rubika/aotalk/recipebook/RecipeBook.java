package com.rubika.aotalk.recipebook;

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

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = this;
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
        	searchId = extras.getString("id");
        	Logging.log(APP_TAG, "got extras id: " + searchId);
        }
        
        if (getIntent().getData() != null) {
	        if(getIntent().getData().toString().startsWith("aorbid://")) {
	        	searchId = getIntent().getData().toString().replace("aorbid://", "");
	        	Logging.log(APP_TAG, "got intent id: " + searchId);
	        }
        }
       
        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SherlockListFragment mFragment = (SherlockListFragment) SherlockFragment.instantiate(this, FragmentActivityRecipes.DataListFragment.class.getName(), null);
                
        ft.add(android.R.id.content, mFragment, "browse");
        ft.commit();

        /*
        bar.addTab(bar.newTab()
                .setText("Browse")
                .setTabListener(new TabListener<LoaderRecipes.DataListFragment>(
                        this, "browse", LoaderRecipes.DataListFragment.class)));
        */
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
	
    /*
    public static class TabListener<T extends SherlockListFragment> implements ActionBar.TabListener {
        private final SherlockFragmentActivity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private SherlockListFragment mFragment;

        public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            mFragment = (SherlockListFragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = (SherlockListFragment) SherlockFragment.instantiate(mActivity, mClass.getName(), mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }
    */
}
