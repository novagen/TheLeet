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

package com.rubika.aotalk.recipebook;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
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
import android.graphics.drawable.Drawable;
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

/**
 * Demonstration of the implementation of a custom Loader.
 */
public class FragmentActivityRecipes extends SherlockFragmentActivity {
	private static final String APP_TAG = "--> AnarchyTalk::LoaderRecipes";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
        	DataListFragment list = new DataListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }
    
    public static class DataEntry {
    	private ListLoader loader;
    	private String desc;
    	private String id;
    	private int type;
    	private int icon;
    	
    	public DataEntry(ListLoader loader, String desc, String id, int icon, int type) {
    		this.loader = loader;
    		this.desc = desc;
    		this.id = id;
    		this.icon = icon;
    		this.type = type;
    	}

        public String getLabel() {
            return desc;
        }
        
        public Drawable getIcon() {
        	return loader.getContext().getResources().getDrawable(icon);
        }
        
        public String getID() {
        	return id;
        }
        
        public ListLoader getLoader() {
        	return loader;
        }
        
        public int getType() {
        	return type;
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
            if (densityChanged || (configChanges&(ActivityInfo.CONFIG_LOCALE
                    |ActivityInfo.CONFIG_UI_MODE|ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
                mLastDensity = res.getDisplayMetrics().densityDpi;
                return true;
            }
            return false;
        }
    }


    public static class ListLoader extends AsyncTaskLoader<List<DataEntry>> {
        List<DataEntry> dataList;
        ListAdapter adapter;
        Document folders = null;
       
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
      
        public ListLoader(Context context, ListAdapter adapter) {
            super(context);
            this.adapter = adapter;
        }
    	
        @Override public List<DataEntry> loadInBackground() {
	        List<DataEntry> items = new ArrayList<DataEntry>();
        	String xml = null;
            Document doc = null;
            
	        if (folders == null && adapter.getSearchId() == null) {
            	try {
	                DefaultHttpClient httpClient = new DefaultHttpClient();
	                HttpGet httpGet = new HttpGet(RecipeBook.RECIPES_CATEGORIES_URL);
	     
	                HttpResponse httpResponse = httpClient.execute(httpGet);
	                HttpEntity httpEntity = httpResponse.getEntity();
	                xml = EntityUtils.toString(httpEntity);
	            	Logging.log(APP_TAG, RecipeBook.RECIPES_CATEGORIES_URL + "\r" + xml);
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
		                folders = db.parse(is); 
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
	        }

            try {
                String dataUrl = "";
                
                if (adapter.getSearchId() != null) {
                	dataUrl = String.format(RecipeBook.RECIPES_BY_ITEM_URL, adapter.getSearchId());
    				Logging.log(APP_TAG, "adapter searchid was set");
                } else {
                	dataUrl = String.format(RecipeBook.RECIPES_CATEGORY_URL, adapter.getFolder());
    				Logging.log(APP_TAG, "adapter searchid was NOT set");
                }
                
            	DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(dataUrl);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
            	Logging.log(APP_TAG, dataUrl + "\r" + xml);
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
            
            if (!adapter.getFolder().equals("0")) {
            	items.add(new DataEntry(this, getContext().getString(R.string.back), "0", R.drawable.ic_menu_revert, 2));	            	
            }
                        
            if (folders != null && adapter.getFolder().equals("0")) {
	            NodeList nl = folders.getElementsByTagName("item");
	            
	            for (int i = 0; i < nl.getLength(); i++) {
	                Element e = (Element) nl.item(i);

	                String title = Html.fromHtml(getValue(e, "category_name")).toString();
	                String id = getValue(e, "category_id");
	                
	                DataEntry entry = new DataEntry(this, title, id, R.drawable.ic_menu_archive, 0);
	                items.add(entry);
	            }
            }
            
            if (doc != null) {
            	NodeList nl = doc.getElementsByTagName("item");
            
	            for (int i = 0; i < nl.getLength(); i++) {
	                Element e = (Element) nl.item(i);
	                
	                String title = Html.fromHtml(getValue(e, "recipe_name")).toString();
	                String id = getValue(e, "recipe_id");
	                
		            DataEntry entry = new DataEntry(this, title, id, R.drawable.ic_menu_compass, 1);
		            items.add(entry);
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
        				if (child.getNodeType() == Node.TEXT_NODE) {
        					return child.getNodeValue();
        				}
        			}
        		}
        	}

        	return "";
        }
        
        @Override public void deliverResult(List<DataEntry> news) {
            if (isReset()) {
                if (news != null) {
                    onReleaseResources(news);
                }
            }
            List<DataEntry> oldNews = news;
            dataList = news;

            if (isStarted()) {
                super.deliverResult(news);
            }

            if (oldNews != null) {
                onReleaseResources(oldNews);
            }
        }
        
        @Override protected void onStartLoading() {
            if (dataList != null) {
                deliverResult(dataList);
            }

            boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

            if (takeContentChanged() || dataList == null || configChange) {
                forceLoad();
            }
        }

        /**
         * Handles a request to stop the Loader.
         */
        @Override protected void onStopLoading() {
            cancelLoad();
        }

        /**
         * Handles a request to cancel a load.
         */
        @Override public void onCanceled(List<DataEntry> news) {
            super.onCanceled(news);

            onReleaseResources(news);
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        @Override protected void onReset() {
            super.onReset();

            onStopLoading();

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
        }
	}

    public static class ListAdapter extends ArrayAdapter<DataEntry> {
        private final LayoutInflater mInflater;
        private String folder = "0";
        private String searchId = null;
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
        
        public String getSearchId() {
        	return searchId;
        }
        
        public void setSearchId(String id) {
			Logging.log(APP_TAG, "setSearchId was called with id " + id);
        	searchId = id;
        }

        /**
         * Populate new items in the list.
         */
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.list_item_guide, parent, false);
            }

            DataEntry item = getItem(position);

            ((TextView)convertView.findViewById(R.id.text)).setText(item.getLabel());
            ((TextView)convertView.findViewById(R.id.text)).setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), null, null, null);

            if (animationEnabled) {
                Animation animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, (float)0, Animation.RELATIVE_TO_SELF, (float)0.5); 
	
	            animation.setDuration(200);
				animation.setFillAfter(true);
				
				convertView.setAnimation(animation);
				convertView.startAnimation(animation);
            }
            
            return convertView;
        }
        
        public void setFolder(String folder) {
        	this.folder = folder;
        }
        
        public String getFolder() {
        	return folder;
        }
    }

    public static class DataListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<DataEntry>> {
        ListAdapter mAdapter;
        String mCurFilter;

        @Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            
            setEmptyText(getString(R.string.no_recipes));
            getListView().setFastScrollEnabled(true);

            setHasOptionsMenu(true);

            mAdapter = new ListAdapter(getActivity(), PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext()).getBoolean("enableAnimations", true));
            
            if (((RecipeBook) getActivity()).searchId != null) {
            	mAdapter.setSearchId(((RecipeBook) getActivity()).searchId);
            }
            
            setListAdapter(mAdapter);
            setListShown(false);
            
            getLoaderManager().initLoader(0, null, this);
        }
        
        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	container.setBackgroundResource(R.drawable.fragment_background);
        	return super.onCreateView(inflater, container, savedInstanceState);
        }
        
        @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        }

        @Override public void onListItemClick(ListView l, View v, int position, long id) {
            Logging.log(APP_TAG, "Item clicked: " + mAdapter.getItem(position).getLabel());
            
            if (mAdapter.getItem(position).getType() == 1) {
    			Intent intent = new Intent();
    			intent.setClass(mAdapter.getContext(), ActivityRecipe.class);
    			intent.putExtra("title", mAdapter.getItem(position).getLabel());
    			intent.putExtra("id", mAdapter.getItem(position).getID());
    			
    			mAdapter.getContext().startActivity(intent);	            
            } else {
	            mAdapter.setFolder(mAdapter.getItem(position).getID());
	            
	            setListShown(false);
	            getLoaderManager().restartLoader(0, null, this);
            }
        }

        @Override public Loader<List<DataEntry>> onCreateLoader(int id, Bundle args) {
            return new ListLoader(getActivity(), mAdapter);
        }

        @Override public void onLoadFinished(Loader<List<DataEntry>> loader, List<DataEntry> data) {
            mAdapter.setData(data);

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
