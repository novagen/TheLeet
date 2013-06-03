/*
 * Market.java
 *
 *************************************************************************
 * Copyright 2010 Christofer Engel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rubika.aotalk.market;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.rubika.aotalk.Preferences;
import com.rubika.aotalk.R;
import com.rubika.aotalk.adapter.MarketMessageAdapter;
import com.rubika.aotalk.item.MarketMessage;
import com.rubika.aotalk.util.ChatParser;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.RKNet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class Market extends SherlockActivity {
	protected static final String APP_TAG = "--> The Leet :: Market";
	
	private ListView marketlist;
	private List<MarketMessage> marketposts;
	private MarketMessageAdapter msgadapter;
	private long lastfetch = 0;
	private SharedPreferences settings;
	private TextView status;
	
	public static String MARKET_INTERVAL = "10";
	private static boolean UPDATE = true;
	private String resultData;
	
	private String limit;
	private String url;
	
	private HttpClient httpclient;
	private HttpPost httppost;
    
	private HttpResponse response;
	private HttpEntity entity;
	private InputStream is;
	private BufferedReader reader;
	private StringBuilder sb;
	private String line;
	
	private JSONArray json_array;
	private JSONObject json_data;
	
    private Handler handler = new Handler();
    private static Tracker tracker;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//super.setTheme(R.style.Theme_AOTalkTheme_Light);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        
        setContentView(R.layout.activity_market);

        final ActionBar bar = getSupportActionBar();
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);

        status = (TextView) findViewById(R.id.status);
                
        marketposts = new ArrayList<MarketMessage>();
        
        marketlist = (ListView)findViewById(R.id.market);
        marketlist.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        marketlist.setDividerHeight(0);

        msgadapter = new MarketMessageAdapter(this, marketposts, settings.getBoolean("enableAnimations", true));

        marketlist.setAdapter(msgadapter);
        marketlist.setFocusable(true);
        marketlist.setFocusableInTouchMode(true);
        marketlist.setItemsCanFocus(true);
        
        marketlist.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
	    		return false;
			}
        });
        
        EasyTracker.getInstance().setContext(this);
        tracker = EasyTracker.getTracker();

        handler.post(update);
	}
	
	private class Update extends AsyncTask<Void, Void, String> {
	     protected String doInBackground(Void... str) {
	    	 handler.post(setLoading);
	    	 
	    	 return getMarketData();
	     }

	     protected void onPostExecute(String result) {
	    	 resultData = result;
	    	 handler.post(outputResult);
    	 
    		if(settings.getBoolean("marketautoupdate", UPDATE)) {
    			handler.removeCallbacks(update);
    			handler.postDelayed(update, (Integer.parseInt(settings.getString("marketinterval", MARKET_INTERVAL).trim()) * 1000));
    		}
	     }
	}
	
    private Runnable update = new Runnable() {
    	public void run() {
    		new Update().execute();
    	}

    };
        
	final Runnable outputResult = new Runnable() {
        public void run() {
           	updateResultsInUi();
        }
    };
	
	private Runnable setError = new Runnable() {
		public void run() {
			status.setText("Error!");
		}
	};
	
	private Runnable setLoading = new Runnable() {
		public void run() {
			setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
			status.setText("Loading...");
		}
	};
	
	private Runnable setDone = new Runnable() {
		public void run() {
			String statustext = getString(R.string.auto_update) + ": ";
			setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
						
			if(settings.getBoolean("marketautoupdate", UPDATE)) {
				statustext += getString(R.string.on);
			} else {
				statustext += getString(R.string.off);
			}
			
			status.setText(statustext);
		}
	};

    private String getMarketData() {    	
        long loadTime = System.currentTimeMillis();
        
    	limit = "";
    	if(lastfetch == 0) {
    		limit = "&limit=50";
    	}
    	
    	url = String.format(
    			Locale.getDefault(), 
    			RKNet.RKNET_MARKET_PATH, 
    			lastfetch,
    			limit
    		);
    	
    	try{
    		httpclient = new DefaultHttpClient();
	        httppost = new HttpPost(url);
	        	        
	        response = httpclient.execute(httppost);
	        entity = response.getEntity();
	        is = entity.getContent();
	        
	    	try{
	    		reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
    	        sb = new StringBuilder();
    	        line = null;
    	        
    	        while ((line = reader.readLine()) != null) {
    	        	sb.append(line + "\n");
    	        }
    	        
    	        is.close();
    	        
            	tracker.sendTiming("Loading", System.currentTimeMillis() - loadTime, "Market", null);
    	 
    	        return sb.toString();
	    	} catch(Exception e){
	    	    Logging.log(APP_TAG, "Error converting result " + e.toString());
	    	    return null;
	    	}
    	} catch(Exception e){
    		Logging.log(APP_TAG, "Error in http connection " + e.toString());
	        return null;
    	}
    }
    
    private void updateResultsInUi() {
    	try{
    		if(resultData != null) {
	    		if((!resultData.startsWith("null"))) {
	                boolean showAnimation = true;
	                
	    			if(lastfetch == 0) {
	    				showAnimation = false;
	                }

	    			
	    			json_array = new JSONArray(resultData);
	    				    			
	    	        for(int i = json_array.length() - 1; i >= 0; i--){
	    	        	json_data = json_array.getJSONObject(i);
		                
	    	        	if (i == 0) {
		                	lastfetch = json_data.getLong("time");
	    	        	}
	    	        	
	    	        	int side = 0;
	    	        	
		                if(json_data.getInt("omni") == 1) {
		                	side = 1;
		                }
		                
		                if(json_data.getInt("clan") == 1) {
		                	side = 2;
		                }
		                
		                if(json_data.getInt("neut") == 1) {
		                	side = 3;
		                }
		                
		                MarketMessage message = new MarketMessage(
		                		json_data.getLong("time"),
		                		ChatParser.parse(json_data.getString("message"), ChatParser.MESSAGE_TYPE_PLAIN), 
		                		json_data.getString("player"), 
		                		side
			                );
		                
		                message.showAnimation(showAnimation);
		                marketposts.add(0, message);
	    	        }
	    		}
    		} else {
    			handler.post(setError);
    		}
    	} catch(JSONException e){
    		Logging.log(APP_TAG, "Error parsing data " + e.toString());
    	}
    	
    	msgadapter.notifyDataSetChanged();
    	handler.post(setDone);
    }
   
    @Override
    protected void onResume() {
    	super.onResume();

    	handler.post(setDone);
		
    	if(settings.getBoolean("marketautoupdate", UPDATE)) {
	        handler.removeCallbacks(update);
			handler.post(update);
    	}
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
    	handler.removeCallbacks(update);
	}
        
    @Override
    protected void onDestroy() {
    	super.onDestroy();
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
        getSupportMenuInflater().inflate(R.menu.menu_market, menu);
		
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == android.R.id.home) {
	        finish();
	        return true;
    	} else if (item.getItemId() == R.id.update) {
			handler.removeCallbacks(update);
    		handler.post(update);
			return true;
		} else if (item.getItemId() == R.id.preferences) {
			Intent intent = new Intent(this, Preferences.class);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }
}
