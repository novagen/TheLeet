package com.rubika.aotalk.towerwars;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.R;

public class Towerwars extends SherlockFragmentActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayHomeAsUpEnabled(true);
        
		bar.addTab(bar.newTab()
		        .setText("Recent")
		        .setTabListener(new TabListener<FragmentActivityAttacks.DataListFragment>(
		                this, "recent", FragmentActivityAttacks.DataListFragment.class)));
                
        bar.addTab(bar.newTab()
                .setText("Sites")
                .setTabListener(new TabListener<FragmentActivitySites.DataListFragment>(
                        this, "sites", FragmentActivitySites.DataListFragment.class)));
        
        /*
		bar.addTab(bar.newTab()
		        .setText("Graphs")
		        .setTabListener(new TabListener<FragmentActivityGraphs.DataListFragment>(
		                this, "graphs", FragmentActivityGraphs.DataListFragment.class)));
		*/
	}
	
	@Override
	protected void onPause() {
		System.gc();
		super.onPause();
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
