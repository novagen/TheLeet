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

package com.rubika.aotalk.towerwars;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.rubika.aotalk.R;
import com.rubika.aotalk.item.TowerSite;
import com.rubika.aotalk.util.Logging;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
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

/**
 * Demonstration of the implementation of a custom Loader.
 */
public class FragmentActivitySites extends SherlockFragmentActivity {
	private static final String APP_TAG = "--> AnarchyTalk::FragmentActivitySites";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            DataListFragment list = new DataListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    
    /**
     * Perform alphabetical comparison of application entry objects.
     */
    public static final Comparator<TowerSite> ALPHA_COMPARATOR = new Comparator<TowerSite>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(TowerSite object1, TowerSite object2) {
            return sCollator.compare(object1.getZone(), object2.getZone());
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

    public static class ListLoader extends AsyncTaskLoader<List<TowerSite>> {
    	private static String url = "http://towerwars.info/m/TowerDistribution.php?d=%s&minlevel=1&maxlevel=300&output=json";
    	private HttpClient httpclient;
    	private HttpGet httpget;
        
    	private HttpResponse response;
    	private HttpEntity entity;
    	private InputStream is;
    	private BufferedReader reader;
    	private StringBuilder sb;
    	private String line;
    	private String resultData;
    	
    	private JSONArray jArray;
    	private JSONObject json_data;
    	
    	private String lastHeader = "";

        private List<TowerSite> dataList;
        
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
        private Context context;
      
        public ListLoader(Context context) {
            super(context);
            this.context = context;
            // Retrieve the package manager for later use; note we don't
            // use 'context' directly but instead the save global application
            // context returned by getContext().
        }
    	
        @Override public List<TowerSite> loadInBackground() {
	        List<TowerSite> items = new ArrayList<TowerSite>();
	        
	    	try{
	    		httpclient = new DefaultHttpClient();
		        httpget = new HttpGet(String.format(url, PreferenceManager.getDefaultSharedPreferences(context).getString("server", "1")));
		        	        
		        response = httpclient.execute(httpget);
		        entity = response.getEntity();
		        is = entity.getContent();
		        
		    	//convert response to string
		    	try{
		    		reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
	    	        sb = new StringBuilder();
	    	        line = null;
	    	        
	    	        while ((line = reader.readLine()) != null) {
	    	        	sb.append(line + "\n");
	    	        }
	    	        
	    	        is.close();
	    	 
	    	        resultData = sb.toString();
		    	} catch(Exception e){
		    	    Logging.log(APP_TAG, "Error converting result " + e.toString());
		    	    resultData = null;
		    	}
	    	} catch(Exception e){
	    		Logging.log(APP_TAG, "Error in http connection " + e.toString());
	    		resultData = null;
	    	}

	    	try{
	    		if(resultData != null) {
		    		if((!resultData.startsWith("null"))) {
		    			jArray = new JSONArray(resultData);
		    				    			
		    	        for(int i = 0; i < jArray.length(); i++){
		    	        	json_data = jArray.getJSONObject(i);
			                
		    	        	if (!json_data.getString("zone_name").equals(lastHeader)) {
		    	        		lastHeader = json_data.getString("zone_name");
		    	        		
			    	        	items.add(new TowerSite(0, 0, json_data.getString("zone_name"), null, null, 0, 0, 0));
		    	        	}
		    	        	
		    	        	items.add(new TowerSite(
		                		json_data.getInt("site_id"),
		                		json_data.getInt("zone_id"),
		                		json_data.getString("zone_name"),
		                		json_data.getString("faction_name"),
		                		json_data.getString("site_name"),
		                		json_data.getInt("site_minlvl"),
		                		json_data.getInt("site_maxlvl"),
		                		json_data.getLong("lastresult")
			                ));

		    	        }
		    		}
	    		}
	    	} catch(JSONException e){
	    		Logging.log(APP_TAG, "Error parsing data " + e.toString());
	    	}
	        
			return items;
        }
        
        @Override public void deliverResult(List<TowerSite> sites) {
            if (isReset()) {
                // An async query came in while the loader is stopped.  We
                // don't need the result.
                if (sites != null) {
                    onReleaseResources(sites);
                }
            }
            
            List<TowerSite> oldSites = sites;
            dataList = sites;

            if (isStarted()) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(sites);
            }

            // At this point we can release the resources associated with
            // 'oldApps' if needed; now that the new result is delivered we
            // know that it is no longer in use.
            if (oldSites != null) {
                onReleaseResources(oldSites);
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
        @Override public void onCanceled(List<TowerSite> Sites) {
            super.onCanceled(Sites);

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources(Sites);
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        @Override protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'Sites'
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
        protected void onReleaseResources(List<TowerSite> Sites) {
            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
	}

    public static class ListAdapter extends ArrayAdapter<TowerSite> /*implements SectionIndexer*/ {
        private final LayoutInflater mInflater;
        private boolean animationEnabled;

        public ListAdapter(Context context, boolean enableAnimation) {
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            animationEnabled = enableAnimation;
            //alphaIndexer = new HashMap<String, Integer>();
        }

        public void setData(List<TowerSite> data) {
            clear();
            if (data != null) {
                for (TowerSite entry : data) {
                	add(entry);
                }
            }
            
            /*
            alphaIndexer = new HashMap<String, Integer>();

            for (int x = 0; x < this.getCount(); x++) {
            	TowerSite s = getItem(x);
 
                String ch =  s.getZone().substring(0, 1);
                ch = ch.toUpperCase();
 
                alphaIndexer.put(ch, x);
            }
            
            Set<String> sectionLetters = alphaIndexer.keySet();
            ArrayList<String> sectionList = new ArrayList<String>(sectionLetters); 
 
            Collections.sort(sectionList);
 
            sections = new String[sectionList.size()];
            sectionList.toArray(sections);
            */
        }

        /**
         * Populate new items in the list.
         */
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.list_item_towersite, parent, false);
            }

            TowerSite item = getItem(position);
            

            if (item.getSitename() == null) {
            	convertView.setBackgroundColor(Color.parseColor("#262626"));
                ((TextView)convertView.findViewById(R.id.text)).setText(item.getZone());
                ((TextView)convertView.findViewById(R.id.target)).setVisibility(View.GONE);
                ((TextView)convertView.findViewById(R.id.level)).setVisibility(View.GONE);
                ((TextView)convertView.findViewById(R.id.status)).setVisibility(View.GONE);
            } else {
            	convertView.setBackgroundColor(Color.TRANSPARENT);
				((TextView)convertView.findViewById(R.id.text)).setText(item.getSitename());
				((TextView)convertView.findViewById(R.id.target)).setText(item.getFaction());
				((TextView)convertView.findViewById(R.id.target)).setVisibility(View.VISIBLE);
				((TextView)convertView.findViewById(R.id.level)).setText(item.getMinlevel() + " - " + item.getMaxlevel());
				((TextView)convertView.findViewById(R.id.level)).setVisibility(View.VISIBLE);
				((TextView)convertView.findViewById(R.id.status)).setText(getTowersiteText((item.getLastresult() * 1) + 300));
				((TextView)convertView.findViewById(R.id.status)).setVisibility(View.VISIBLE);
            }
            
            String factionColor;

            if (item.getFaction() != null) {
	            if (item.getFaction().equals("Omni")) {
	            	factionColor = "#48b4ff";
	            } else if (item.getFaction().equals("Clan")) {
	            	factionColor = "#f86868";
	            } else if (item.getFaction().equals("Neutral")) {
	            	factionColor = "#f1f16e";
	            } else {
	            	factionColor = "#bbbbbb";
	            }
	            
	            ((TextView)convertView.findViewById(R.id.target)).setTextColor(Color.parseColor(factionColor));
            }

            if (animationEnabled) {
	            Animation animation;
		        
	            if (position %2 == 1) {
	            	animation = new ScaleAnimation(0, 1, 1, 1, Animation.RELATIVE_TO_SELF, (float)0, Animation.RELATIVE_TO_SELF, (float)0.5); 
	            } else {
	            	animation = new ScaleAnimation(0, 1, 1, 1, Animation.RELATIVE_TO_SELF, (float)1, Animation.RELATIVE_TO_SELF, (float)0.5); 
	            }
	
	            animation.setDuration(200);
				animation.setFillAfter(true);
				
				convertView.setAnimation(animation);
				convertView.startAnimation(animation);
            }
            
            return convertView;
        }
        
        public String getTowersiteStatus(long planttime) {
        	String isOpen = null;
        	
        	Calendar timeNow = Calendar.getInstance();
        	
    		int opennow_start;
    		int opennow_end;
    		int timeNow_hours;
    		
    		Calendar opentime_start = Calendar.getInstance();
    		Calendar opentime_end = Calendar.getInstance();
        	
        	opentime_start.setTimeInMillis((planttime - 3600*6) * 1000);
        	opentime_end.setTimeInMillis(planttime * 1000);
        	
        	if (opentime_start.get(Calendar.HOUR_OF_DAY) > 12 && opentime_end.get(Calendar.HOUR_OF_DAY) < 12)
        	{
        		opennow_start = opentime_start.get(Calendar.HOUR_OF_DAY) - 12;
        		opennow_end = opentime_end.get(Calendar.HOUR_OF_DAY) + 12;
        		
        		if (timeNow.get(Calendar.HOUR_OF_DAY) > 12)
        			timeNow_hours = timeNow.get(Calendar.HOUR_OF_DAY) - 12;
        		else 
        			timeNow_hours = timeNow.get(Calendar.HOUR_OF_DAY) + 12;
        	}
        	else
        	{
        		opennow_start = opentime_start.get(Calendar.HOUR_OF_DAY);
        		opennow_end = opentime_end.get(Calendar.HOUR_OF_DAY);
        		timeNow_hours = timeNow.get(Calendar.HOUR_OF_DAY);
        	}
        	
        	if (((timeNow_hours > opennow_start) || (timeNow_hours == opennow_start && timeNow.get(Calendar.MINUTE) >= opentime_end.get(Calendar.MINUTE))) && ((timeNow_hours < opennow_end) || (timeNow_hours == opennow_end && timeNow.get(Calendar.MINUTE) <= opentime_end.get(Calendar.MINUTE))))
        		isOpen = "Hot";
        	else
        		isOpen = "Cold";
        		
        	return isOpen;
        }

        public String getTowersiteText(long planttime) {
    		Calendar opentime_start = Calendar.getInstance();
    		Calendar opentime_end = Calendar.getInstance();
        	
        	opentime_start.setTimeInMillis((planttime - 3600*6) * 1000);
        	opentime_end.setTimeInMillis(planttime * 1000);
        	
        	//Start time
        	String opentime_text = "Open from ";
        	
        	if (opentime_start.get(Calendar.HOUR_OF_DAY) < 10)
        	{
        		opentime_text += "0";
        	}
        		
        	opentime_text += opentime_start.get(Calendar.HOUR_OF_DAY) + ":";
        	
        	if (opentime_start.get(Calendar.MINUTE) < 10)
        		opentime_text += "0";
        	
        	opentime_text += opentime_start.get(Calendar.MINUTE);
        	opentime_text += " to ";
        	
        	//End time
        	if (opentime_end.get(Calendar.HOUR_OF_DAY) < 10)
        	{
        		opentime_text+="0";
        	}
        	
        	opentime_text+=opentime_end.get(Calendar.HOUR_OF_DAY) + ":";
        	
        	if (opentime_end.get(Calendar.MINUTE) < 10)
        		opentime_text+="0";
        	
        	opentime_text+=opentime_end.get(Calendar.MINUTE);
        	
        	return opentime_text;
        }

        /*
        HashMap<String, Integer> alphaIndexer;
        String[] sections;
        
		@Override
		public int getPositionForSection(int section) {
			return alphaIndexer.get(sections[section]);
		}

		@Override
		public int getSectionForPosition(int position) {
			return 1;
		}

		@Override
		public Object[] getSections() {
			return sections;
		}
		*/
    }

    public static class DataListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<TowerSite>> {
        // This is the Adapter being used to display the list's data.
        ListAdapter mAdapter;

        // If non-null, this is the current filter the user has provided.
        String mCurFilter;

        public DataListFragment() {
        }

       @Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            
            // Give some text to display if there is no data.  In a real
            // application this would come from a resource.
            //setEmptyText(getString(R.string.no_Sites));

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new ListAdapter(getActivity(), PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext()).getBoolean("enableAnimations", true));
            setListAdapter(mAdapter);
            
            getListView().setFastScrollEnabled(true);
            getListView().setScrollingCacheEnabled(false);

            // Start out with a progress indicator.
            setListShown(false);

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(0, null, this);
        }
        
        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	container.setBackgroundResource(R.drawable.fragment_background);
        	return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        }

        @Override public void onListItemClick(ListView l, View v, int position, long id) {
			/*
			Intent intent = new Intent();
			intent.setClass(mAdapter.getContext(), ActivitySites.class);
			intent.putExtra("title", mAdapter.getItem(position).getLabel());
			intent.putExtra("date", mAdapter.getItem(position).getDate());
			intent.putExtra("text", mAdapter.getItem(position).getDesc());
			intent.putExtra("link", mAdapter.getItem(position).getLink());
			
			mAdapter.getContext().startActivity(intent);
			*/
        }

        @Override public Loader<List<TowerSite>> onCreateLoader(int id, Bundle args) {
            // This is called when a new Loader needs to be created.  This
            // sample only has one Loader with no arguments, so it is simple.
            return new ListLoader(getActivity());
        }

        @Override public void onLoadFinished(Loader<List<TowerSite>> loader, List<TowerSite> data) {
            // Set the new data in the adapter.
            mAdapter.setData(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        @Override public void onLoaderReset(Loader<List<TowerSite>> loader) {
            // Clear the data in the adapter.
            mAdapter.setData(null);
        }
    }

}
