/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubika.aotalk.aou;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.mcsoxford.rss.RSSFault;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.rubika.aotalk.R;
import com.rubika.aotalk.item.AouNews;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentNews extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<AouNews>> {
	private static final String APP_TAG = "--> The Leet ::FragmentNews";
	private ListAdapter mAdapter;
	
	public static FragmentNews newInstance() {
		FragmentNews f = new FragmentNews();
        return f;
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    /**
     * Perform alphabetical comparison of application entry objects.
     */
    public static final Comparator<AouNews> ALPHA_COMPARATOR = new Comparator<AouNews>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(AouNews object1, AouNews object2) {
            return sCollator.compare(object1.getLabel(), object2.getLabel());
        }
    };

    /**
     * Helper for determining if the configuration has changed in an interesting
     * way so we need to rebuild the app list.
     */
    public static class InterestingConfigChanges {
        final Configuration mLastConfiguration = new Configuration();
        int mLastDensity;

        boolean applyNewConfig(Resources res) {
            int configChanges = mLastConfiguration.updateFrom(res.getConfiguration());
            boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
            if (densityChanged || (configChanges&(ActivityInfo.CONFIG_LOCALE
                    |ActivityInfo.CONFIG_UI_MODE|ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
                mLastDensity = res.getDisplayMetrics().densityDpi;
                return true;
            }
            return false;
        }
    }

    public static class ListLoader extends AsyncTaskLoader<List<AouNews>> {
        List<AouNews> dataList;
        //PackageIntentReceiver mPackageObserver;
        
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
      
        public ListLoader(Context context) {
            super(context);

            // Retrieve the package manager for later use; note we don't
            // use 'context' directly but instead the save global application
            // context returned by getContext().
        }
    	
        @Override public List<AouNews> loadInBackground() {
	        RSSReader reader = new RSSReader();
	        List<AouNews> items = new ArrayList<AouNews>();
	        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	        
	        try {
				RSSFeed feed = reader.load(Statics.NEWS_URL);
				List<RSSItem> feedItems = feed.getItems();
				
				for(RSSItem i : feedItems) {
					String title = "";
					String target = "";
					
					title = i.getTitle();
					
					if (title.length() > 0) {
						if (title.contains(" - ")) {
							target = title.substring(0, title.indexOf(" - ")).trim();
							title = title.substring(title.indexOf(" - ") + 2, title.length()).trim();
						}
					} else {
						target = "";
						title = "";
					}
					
					items.add(new AouNews(
							title, 
							i.getDescription(), 
							df.format(i.getPubDate()),
							i.getLink().toString(),
							target
					));
				}
			} catch (RSSReaderException e) {
				Logging.log(APP_TAG, e.getMessage());
			} catch (RSSFault e) {
				Logging.log(APP_TAG, e.getMessage());
			}
			
			reader.close();
			reader = null;
			
			return items;
        }
        
        @Override public void deliverResult(List<AouNews> news) {
            if (isReset()) {
                // An async query came in while the loader is stopped.  We
                // don't need the result.
                if (news != null) {
                    onReleaseResources(news);
                }
            }
            List<AouNews> oldNews = news;
            dataList = news;

            if (isStarted()) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(news);
            }

            // At this point we can release the resources associated with
            // 'oldApps' if needed; now that the new result is delivered we
            // know that it is no longer in use.
            if (oldNews != null) {
                onReleaseResources(oldNews);
            }
        }
        
        @Override protected void onStartLoading() {
            if (dataList != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(dataList);
            }

            // Has something interesting in the configuration changed since we
            // last built the app list?
            boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

            if (takeContentChanged() || dataList == null || configChange) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }

        /**
         * Handles a request to stop the Loader.
         */
        @Override protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        /**
         * Handles a request to cancel a load.
         */
        @Override public void onCanceled(List<AouNews> news) {
            super.onCanceled(news);

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources(news);
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        @Override protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'news'
            // if needed.
            if (dataList != null) {
                onReleaseResources(dataList);
                dataList = null;
            }
        }

        /**
         * Helper function to take care of releasing resources associated
         * with an actively loaded data set.
         */
        protected void onReleaseResources(List<AouNews> news) {
            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
	}

    public static class ListAdapter extends ArrayAdapter<AouNews> {
        private final LayoutInflater mInflater;
        private boolean animationEnabled;

        public ListAdapter(Context context, boolean enableAnimation) {
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            animationEnabled = enableAnimation;
        }

        public void setData(List<AouNews> data) {
            clear();
            if (data != null) {
                for (AouNews entry : data) {
                	add(entry);
                }
            }
        }

        /**
         * Populate new items in the list.
         */
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.list_item_news, parent, false);
            }

            AouNews item = getItem(position);
            ((TextView)convertView.findViewById(R.id.text)).setText(item.getLabel());
            ((TextView)convertView.findViewById(R.id.date)).setText(item.getDate());
            ((TextView)convertView.findViewById(R.id.target)).setText(item.getTarget());

            if (animationEnabled) {
                Animation animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, (float)0, Animation.RELATIVE_TO_SELF, (float)0.5); 
	
	            animation.setDuration(200);
				animation.setFillAfter(true);
				
				convertView.setAnimation(animation);
				convertView.startAnimation(animation);
            }
            
            return convertView;
        }
    }


    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        //setEmptyText(getString(R.string.no_news));

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new ListAdapter(getActivity(), PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext()).getBoolean("enableAnimations", true));
        setListAdapter(mAdapter);

        getListView().setScrollingCacheEnabled(false);
        getListView().setDividerHeight(0);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	container.setBackgroundResource(R.drawable.applicationbg);
    	return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    @Override public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent();
		intent.setClass(mAdapter.getContext(), ActivityNews.class);
		intent.putExtra("title", mAdapter.getItem(position).getLabel());
		intent.putExtra("date", mAdapter.getItem(position).getDate());
		intent.putExtra("text", mAdapter.getItem(position).getDesc());
		intent.putExtra("link", mAdapter.getItem(position).getLink());
		
		mAdapter.getContext().startActivity(intent);
    }

    @Override public Loader<List<AouNews>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ListLoader(getActivity());
    }

    @Override public void onLoaderReset(Loader<List<AouNews>> loader) {
        // Clear the data in the adapter.
        mAdapter.setData(null);
    }

	@Override
	public void onLoadFinished(Loader<List<AouNews>> arg0, List<AouNews> arg1) {
        // Set the new data in the adapter.
        mAdapter.setData(arg1);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
	}
}
