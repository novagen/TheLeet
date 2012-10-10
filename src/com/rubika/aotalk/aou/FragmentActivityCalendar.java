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

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentActivityCalendar extends SherlockFragmentActivity {
	private static final String APP_TAG = "--> AnarchyTalk::FragmentActivityCalendar";

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


    public static class DataEntry {
    	private String title;
    	private String desc;
    	private String time;
    	private String server;
    	private String topic;
    	
    	public DataEntry(String title, String desc, String time, String server, String topic) {
    		this.title = title;
    		this.desc = desc;
    		this.time = time;
    		this.server = server;
    		this.topic = topic;
    	}

    	public String getTopic() {
    		return topic;
    	}
    	
        public String getLabel() {
            return title;
        }
        
        public String getDescription() {
        	return desc;
        }
        
        public String getTime() {
        	return time;
        }
        
        public String getServer() {
        	return server;
        }
        
        @Override public String toString() {
            return desc;
        }
   }
    
    /**
     * Perform alphabetical comparison of application entry objects.
     */
    public static final Comparator<DataEntry> ALPHA_COMPARATOR = new Comparator<DataEntry>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(DataEntry object1, DataEntry object2) {
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
            if (densityChanged || (configChanges&(ActivityInfo.CONFIG_LOCALE | ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
                mLastDensity = res.getDisplayMetrics().densityDpi;
                return true;
            }
            return false;
        }
    }


    public static class ListLoader extends AsyncTaskLoader<List<DataEntry>> {
        List<DataEntry> dataList;
        //PackageIntentReceiver mPackageObserver;
        
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
      
        public ListLoader(Context context) {
            super(context);
        }
    	
        @Override public List<DataEntry> loadInBackground() {
	        List<DataEntry> items = new ArrayList<DataEntry>();
        	
        	String xml = null;
            Document doc = null;
        	 
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(AOU.CALENDAR_URL);
     
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
            } catch (UnsupportedEncodingException e) {
				Logging.log(APP_TAG, e.getMessage());
            } catch (ClientProtocolException e) {
				Logging.log(APP_TAG, e.getMessage());
            } catch (IOException e) {
				Logging.log(APP_TAG, e.getMessage());
            }
            
            if (xml != null) {
	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            try {
	                DocumentBuilder db = dbf.newDocumentBuilder();
	     
	                InputSource is = new InputSource();
	                is.setCharacterStream(new StringReader(xml));
	                doc = db.parse(is); 
	            } catch (ParserConfigurationException e) {
	                Logging.log(APP_TAG, e.getMessage());
	                return null;
	            } catch (SAXException e) {
	                Logging.log(APP_TAG, e.getMessage());
	                return null;
	            } catch (IOException e) {
	                Logging.log(APP_TAG, e.getMessage());
	                return null;
	            }
            }
            
            if (doc != null) {
	            NodeList nl = doc.getElementsByTagName("event");
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	            
	            for (int i = 0; i < nl.getLength(); i++) {
	                Element e = (Element) nl.item(i);
	                
	                String date_from = e.getAttribute("date_from");
	                //String date_to   = e.getAttribute("date_to");
	                
	                if (!date_from.equals("")) {
		                //if (Long.parseLong(date_from) > System.currentTimeMillis() || Long.parseLong(date_to) > System.currentTimeMillis()) {
		                	date_from = df.format(new Date(Long.parseLong(date_from) * 1000));
		                
			                String server = e.getAttribute("dimension");
			                if (server.equals("0")) {
			                	server = "Global";
			                } else if (server.equals("1")) {
			                	server = "Atlantean";
			                } else if (server.equals("2")) {
			                	server = "Rimor";
			                }
			                			                
			                String title = Html.fromHtml(getValue(e, "subject").replace("<![", "").replace("]]>", "").trim()).toString();
			                String desc  = Html.fromHtml(getValue(e, "message").replace("<![", "").replace("]]>", "").replace("\n", "<br />").trim()).toString();
			                String topic = e.getAttribute("topic_id");
			                
			                DataEntry entry = new DataEntry(title, desc, date_from, server, topic);
			                items.add(entry);
		                //}
	                }
	            }
            }
			
			return items;
        }
        
        private String getValue(Element item, String str) {
        	NodeList n = item.getElementsByTagName(str);
        	return getElementValue(n.item(0));
        }

        private final String getElementValue( Node elem ) {
        	Node child;

        	if (elem != null) {
        		if (elem.hasChildNodes()) {
        			for (child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
        				if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
        					return child.getNodeValue();
        				}
        			}
        		}
        	}

        	return "";
        }
        
        @Override public void deliverResult(List<DataEntry> news) {
            if (isReset()) {
                // An async query came in while the loader is stopped.  We
                // don't need the result.
                if (news != null) {
                    onReleaseResources(news);
                }
            }
            List<DataEntry> oldNews = news;
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
        @Override public void onCanceled(List<DataEntry> news) {
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
        protected void onReleaseResources(List<DataEntry> news) {
            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
	}

    public static class ListAdapter extends ArrayAdapter<DataEntry> {
        private final LayoutInflater mInflater;
        private boolean animationEnabled;

        public ListAdapter(Context context, boolean enableAnimation) {
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            animationEnabled = enableAnimation;
        }

        public void setData(List<DataEntry> data) {
            clear();
            if (data != null) {
                for (DataEntry entry : data) {
                	add(entry);
                }
            }
        }

        /**
         * Populate new items in the list.
         */
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.list_item_calendar, parent, false);
            }

            final DataEntry item = getItem(position);
            
            ((TextView) convertView.findViewById(R.id.text)).setText(item.getLabel());
            ((TextView) convertView.findViewById(R.id.date)).setText(item.getTime());
            ((TextView) convertView.findViewById(R.id.server)).setText(item.getServer());

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

    public static class DataListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<DataEntry>> {

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
            setEmptyText(getString(R.string.no_calendar));

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new ListAdapter(getActivity(), PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext()).getBoolean("enableAnimations", true));
            setListAdapter(mAdapter);

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
			Intent intent = new Intent();
			intent.setClass(mAdapter.getContext(), ActivityCalendar.class);
			intent.putExtra("title", mAdapter.getItem(position).getLabel());
			intent.putExtra("date", mAdapter.getItem(position).getTime());
			intent.putExtra("server", mAdapter.getItem(position).getServer());
			intent.putExtra("text", mAdapter.getItem(position).getDescription());
			intent.putExtra("link", "http://www.ao-universe.com/forum/viewtopic.php?t=" + mAdapter.getItem(position).getTopic());
			
			mAdapter.getContext().startActivity(intent);
        }

        @Override public Loader<List<DataEntry>> onCreateLoader(int id, Bundle args) {
            // This is called when a new Loader needs to be created.  This
            // sample only has one Loader with no arguments, so it is simple.
            return new ListLoader(getActivity());
        }

        @Override public void onLoadFinished(Loader<List<DataEntry>> loader, List<DataEntry> data) {
            // Set the new data in the adapter.
            mAdapter.setData(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        @Override public void onLoaderReset(Loader<List<DataEntry>> loader) {
            // Clear the data in the adapter.
            mAdapter.setData(null);
        }
    }

}
