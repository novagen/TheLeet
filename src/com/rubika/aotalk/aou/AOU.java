package com.rubika.aotalk.aou;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.Preferences;
import com.rubika.aotalk.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

public class AOU extends SherlockFragmentActivity {
	protected static final String APPTAG = "--> AOTalk::AOU";
	public static final String NEWS_URL    	      = "http://www.ao-universe.com/files/_xml/news_1_3.xml";
	public static final String CALENDAR_URL 	  = "http://www.ao-universe.com/files/_xml/calendar_1_38_bot.xml";
	public static final String GUIDES_FOLDERS_URL = "http://www.ao-universe.com/mobile/parser.php?mode=list&bot=aotalk&output=html";
	public static final String GUIDES_FOLDER_URL  = "http://www.ao-universe.com/mobile/parser.php?mode=list&id=%s&bot=aotalk&output=html";
	public static final String GUIDES_INFO_URL    = "http://www.ao-universe.com/mobile/parser.php?mode=view&id=%s&bot=aotalk&output=html";
	public static final String GUIDES_SEARCH_URL  = "http://www.ao-universe.com/mobile/parser.php?mode=search&search=%s&bot=aotalk&output=html";
	private static Context context;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = this;
        
        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayHomeAsUpEnabled(true);
        
        bar.addTab(bar.newTab()
                .setText("News")
                .setTabListener(new TabListener<FragmentActivityNews.DataListFragment>(
                        this, "news", FragmentActivityNews.DataListFragment.class)));
        
        bar.addTab(bar.newTab()
                .setText("Guides")
                .setTabListener(new TabListener<FragmentActivityGuides.DataListFragment>(
                        this, "guides", FragmentActivityGuides.DataListFragment.class)));
        
        bar.addTab(bar.newTab()
                .setText("Calendar")
                .setTabListener(new TabListener<FragmentActivityCalendar.DataListFragment>(
                        this, "calendar", FragmentActivityCalendar.DataListFragment.class)));

        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
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
			case R.id.preferences:
				Intent intent = new Intent(this, Preferences.class);
				startActivity(intent);
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public static Context getAppContext() {
        return AOU.context;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
    }

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
}