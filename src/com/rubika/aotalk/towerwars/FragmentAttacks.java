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

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.rubika.aotalk.R;
import com.rubika.aotalk.item.TowerAttack;
import com.rubika.aotalk.map.Map;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
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
public class FragmentAttacks extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<TowerAttack>> {
	private static final String APP_TAG = "--> The Leet ::FragmentAttacks";
	private ListAdapter mAdapter;

	public static FragmentAttacks newInstance() {
		FragmentAttacks f = new FragmentAttacks();
        return f;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    
    /**
     * Perform alphabetical comparison of application entry objects.
     */
    public static final Comparator<TowerAttack> ALPHA_COMPARATOR = new Comparator<TowerAttack>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(TowerAttack object1, TowerAttack object2) {
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

    public static class ListLoader extends AsyncTaskLoader<List<TowerAttack>> {
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
    	
        private List<TowerAttack> dataList;
        private Context context;
        
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
      
        public ListLoader(Context context) {
            super(context);
            this.context = context;
            // Retrieve the package manager for later use; note we don't
            // use 'context' directly but instead the save global application
            // context returned by getContext().
        }
    	
        @Override public List<TowerAttack> loadInBackground() {
	        List<TowerAttack> items = new ArrayList<TowerAttack>();

	        try{
	    		httpclient = new DefaultHttpClient();
		        httpget = new HttpGet(String.format(Statics.TOWER_WARS_ATTACKS, PreferenceManager.getDefaultSharedPreferences(context).getString("server", "1")));
		        	        
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
		    			jArray = new JSONObject(resultData).getJSONArray("results");
		    				    			
		    	        for(int i = 0; i < jArray.length(); i++){
		    	        	json_data = jArray.getJSONObject(i);
		    	        	
		    	        	items.add(new TowerAttack(
		                		json_data.getInt("site_id"),
		                		json_data.getLong("time") * 1000,
		                		json_data.getString("zone_name"),
		                		json_data.getString("attacker_nickname"),
		                		json_data.getString("attacker_faction"),
		                		json_data.getString("defender_faction"),
		                		json_data.getString("attacker_guildname"),
		                		json_data.getString("defender_guildname"),
		                		json_data.getString("site_name"),
		                		json_data.getInt("site_minlevel"),
		                		json_data.getInt("site_maxlevel"),
		                		json_data.getInt("site_center_x"),
		                		json_data.getInt("site_center_y")
			                ));

		    	        }
		    		}
	    		}
	    	} catch(JSONException e){
	    		Logging.log(APP_TAG, "Error parsing data " + e.toString());
	    	}
	        
			return items;
        }
        
        @Override public void deliverResult(List<TowerAttack> sites) {
            if (isReset()) {
                // An async query came in while the loader is stopped.  We
                // don't need the result.
                if (sites != null) {
                    onReleaseResources(sites);
                }
            }
            
            List<TowerAttack> oldSites = sites;
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
        @Override public void onCanceled(List<TowerAttack> Sites) {
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
        protected void onReleaseResources(List<TowerAttack> Sites) {
            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
	}

    public static class ListAdapter extends ArrayAdapter<TowerAttack> {
        private final LayoutInflater mInflater;
        private boolean animationEnabled;

        public ListAdapter(Context context, boolean enableAnimation) {
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            animationEnabled = enableAnimation;
        }

        public void setData(List<TowerAttack> data) {
            clear();
            if (data != null) {
                for (TowerAttack entry : data) {
                	add(entry);
                }
            }
        }

        /**
         * Populate new items in the list.
         */
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.list_item_towerattack, parent, false);
            }

            TowerAttack item = getItem(position);
            
            String attackerFactionColor;
            String defenderFactionColor;

            if (item.getAttackerFaction().equals("Omni")) {
            	attackerFactionColor = "#48b4ff";
            } else if (item.getAttackerFaction().equals("Clan")) {
            	attackerFactionColor = "#f86868";
            } else if (item.getAttackerFaction().equals("Neutral")) {
            	attackerFactionColor = "#f1f16e";
            } else {
            	attackerFactionColor = "#bbbbbb";
            }
	            
            if (item.getDefenderFaction().equals("Omni")) {
            	defenderFactionColor = "#48b4ff";
            } else if (item.getDefenderFaction().equals("Clan")) {
            	defenderFactionColor = "#f86868";
            } else if (item.getDefenderFaction().equals("Neutral")) {
            	defenderFactionColor = "#f1f16e";
            } else {
            	defenderFactionColor = "#bbbbbb";
            }
	        
            String timestamp = DateUtils.getRelativeTimeSpanString(item.getTimestamp()).toString();
            
            ((TextView)convertView.findViewById(R.id.timestamp)).setText(timestamp);
            ((TextView)convertView.findViewById(R.id.site)).setText(item.getSitename());
            ((TextView)convertView.findViewById(R.id.zone)).setText(item.getZone());

            ((TextView)convertView.findViewById(R.id.attacker_name)).setText(item.getAttacker());
            ((TextView)convertView.findViewById(R.id.attacker_org)).setText(item.getAttackerOrg());
            ((TextView)convertView.findViewById(R.id.attacker_name)).setTextColor(Color.parseColor(attackerFactionColor));
            ((TextView)convertView.findViewById(R.id.attacker_org)).setTextColor(Color.parseColor(attackerFactionColor));
	        
            ((TextView)convertView.findViewById(R.id.defender_org)).setText(item.getDefenderOrg());
            ((TextView)convertView.findViewById(R.id.defender_org)).setTextColor(Color.parseColor(defenderFactionColor));

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
		intent.setClass(mAdapter.getContext(), Map.class);
		
		intent.putExtra("name", mAdapter.getItem(position).getSitename());
		intent.putExtra("zone", mAdapter.getItem(position).getZone());
		intent.putExtra("x", mAdapter.getItem(position).getX());
		intent.putExtra("y", mAdapter.getItem(position).getY());
		
		mAdapter.getContext().startActivity(intent);
    }

    @Override public Loader<List<TowerAttack>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ListLoader(getActivity());
    }

    @Override public void onLoadFinished(Loader<List<TowerAttack>> loader, List<TowerAttack> data) {
        // Set the new data in the adapter.
        mAdapter.setData(data);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override public void onLoaderReset(Loader<List<TowerAttack>> loader) {
        // Clear the data in the adapter.
        mAdapter.setData(null);
    }
}
