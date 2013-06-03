package com.rubika.aotalk.itemsearch;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.rubika.aotalk.AOTalk;
import com.rubika.aotalk.R;
import com.rubika.aotalk.TheLeet;
import com.rubika.aotalk.item.Item;
import com.rubika.aotalk.util.ImageCache;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentActivityItemsearch extends SherlockFragmentActivity {
	private static final String APP_TAG = "--> The Leet :: FragmentActivityItemSearch";
    private static Tracker tracker;

	public FragmentActivityItemsearch() {
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//super.setTheme(R.style.Theme_AOTalkTheme_Light);
        
        EasyTracker.getInstance().setContext(this);
        tracker = EasyTracker.getTracker();

        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
            DataListFragment list = new DataListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

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


    public static class ListLoader extends AsyncTaskLoader<List<Item>> {
        List<Item> dataList;
        ListAdapter adapter;
        
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
      
        public ListLoader(Context context, ListAdapter adapter) {
            super(context);
            this.adapter = adapter;
        }
    	
        @Override public List<Item> loadInBackground() {
	        List<Item> items = new ArrayList<Item>();
	        long loadTime = System.currentTimeMillis();
        	
        	String xml = null;
            Document doc = null;
            
            String searchString = adapter.getSearchString();
            if (searchString == null) {
            	searchString = "";
            }
            
            String url = String.format(Statics.CIDB_SEARCH_URL, searchString);
        	 
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
     
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
                Logging.log(APP_TAG, url + "\n\r" + xml);
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
	            NodeList nl = doc.getElementsByTagName("item");
	            
	            for (int i = 0; i < nl.getLength(); i++) {
	                Element e = (Element) nl.item(i);
	                
	                if(items.size() > 0) {
		                if (items.get(items.size() - 1).getName().equals(e.getAttribute("name"))) {
		                	items.get(items.size() - 1).setId(e.getAttribute("highid"));
		                } else {
			                items.add(new Item(e.getAttribute("name"), e.getAttribute("highid"), e.getAttribute("icon")));
		                }
		            } else {
		                items.add(new Item(e.getAttribute("name"), e.getAttribute("highid"), e.getAttribute("icon")));
		            }
	            }
            }
            
            for (Item item : items) {
            	item.setIconBitmap(ImageCache.getImage(TheLeet.getContext(), item.getIcon(), Statics.ICON_PATH, ImageCache.getCacheDirectory(TheLeet.getContext().getPackageName(), "icons"), CompressFormat.PNG));
            }

            if (items != null && items.size() > 0) {
            	if (tracker == null) {
            		EasyTracker.getInstance().setContext(AOTalk.getContext());
                    tracker = EasyTracker.getTracker();
            	}
            	
            	tracker.sendTiming("Loading", System.currentTimeMillis() - loadTime, "Item search", null);
            }
            
            return items;
        }
        
        @Override public void deliverResult(List<Item> news) {
            if (isReset()) {
                if (news != null) {
                    onReleaseResources(news);
                }
            }
            List<Item> oldNews = news;
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
        @Override public void onCanceled(List<Item> news) {
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
        protected void onReleaseResources(List<Item> news) {
        }
	}

    public static class ListAdapter extends ArrayAdapter<Item> {
        private final LayoutInflater mInflater;
        private boolean animationEnabled;
        private String searchString = null;

        public ListAdapter(Context context, boolean enableAnimation) {
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            animationEnabled = enableAnimation;
        }

        public void setData(List<Item> data) {
            clear();
            if (data != null) {
                for (Item entry : data) {
                	add(entry);
                }
            }
        }
        
        public String getSearchString() {
        	return searchString;
        }
        
        public void setSearchString(String search) {
			Logging.log(APP_TAG, "setSearchId was called with id " + search);
        	searchString = search;
        }

        /**
         * Populate new items in the list.
         */
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.list_item_item, parent, false);
            }

            final Item item = getItem(position);
            
            ((TextView) convertView.findViewById(R.id.text)).setText(item.getName());
            ((ImageView) convertView.findViewById(R.id.icon)).setImageBitmap(item.getIconBitmap());

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

    public static class DataListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<Item>> {
        ListAdapter mAdapter;

        String mCurFilter;
        
        public DataListFragment() {
        }

        @Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setEmptyText(getString(R.string.no_items));

            setHasOptionsMenu(true);

            mAdapter = new ListAdapter(getActivity(), PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext()).getBoolean("enableAnimations", true));
            
            if (((ItemSearch) getActivity()).searchString != null) {
            	mAdapter.setSearchString(((ItemSearch) getActivity()).searchString);
            }

            setListAdapter(mAdapter);

            getListView().setScrollingCacheEnabled(false);
            getListView().setDividerHeight(0);

            setListShown(false);

            getLoaderManager().initLoader(0, null, this);
        }
        
        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	container.setBackgroundResource(0);
        	return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        }

        @Override public void onListItemClick(ListView l, View v, int position, long id) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("itemref://" + mAdapter.getItem(position).getId() + "/0/0"));
			startActivity(intent);
        }

        @Override public Loader<List<Item>> onCreateLoader(int id, Bundle args) {
            return new ListLoader(getActivity(), mAdapter);
        }

        @Override public void onLoadFinished(Loader<List<Item>> loader, List<Item> data) {
            mAdapter.setData(data);

            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        @Override public void onLoaderReset(Loader<List<Item>> loader) {
            mAdapter.setData(null);
        }
    }
}
