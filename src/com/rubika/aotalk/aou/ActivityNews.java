package com.rubika.aotalk.aou;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.Spanned;
import android.text.Html.ImageGetter;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ActivityNews extends SherlockFragmentActivity {
	private static final String APP_TAG = "--> The Leet :: ActivityNews";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//super.setTheme(R.style.Theme_AOTalkTheme_Light);
        
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        
        FrameLayout frame = new FrameLayout(this);
        setContentView(frame, lp);
        
        final ActionBar bar = getSupportActionBar();
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        
        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, DataFragment.newInstance(
            	null,
	    		intent.getExtras().getString("title"),
				intent.getExtras().getString("text"),
				intent.getExtras().getString("date"),
				intent.getExtras().getString("link")
            )).commit();
        }
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
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
 
	public static class DataFragment extends SherlockFragment {
		private TextView text;
	    private static Tracker tracker;
    	private Bundle extras;
    	private Activity activity;
        private static AOU aou;

        public DataFragment() {
        }
        
        public static DataFragment newInstance(AOU parent, String title, String text, String date, String link) {
        	DataFragment f = new DataFragment();
        	
        	Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("text", text);
            args.putString("date", date);
            args.putString("link", link);
            f.setArguments(args);
        	
            aou = parent;

            return f;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            
            activity = this.getActivity();
            extras = this.getArguments();
            this.getActivity().setTitle(extras.getString("title"));

            setHasOptionsMenu(true);
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (container == null) {
                return null;
            }

            View layout = inflater.inflate(R.layout.activity_news, container, false);
            activity = this.getActivity();
            extras = this.getArguments();
            
            EasyTracker.getInstance().setContext( this.getActivity().getApplicationContext());
            tracker = EasyTracker.getTracker();
    		tracker.sendEvent("AOU", "News", extras.getString("title"), 0L);
            
            TextView date = (TextView) layout.findViewById(R.id.date);
            
            text = (TextView) layout.findViewById(R.id.text);
            text.setMovementMethod(LinkMovementMethod.getInstance());
            
            if (extras.getString("date").equals("")) {
            	date.setVisibility(View.GONE);
            } else {
            	date.setText(extras.getString("date"));
            }
            
            ((Button) layout.findViewById(R.id.close)).setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				if (AOU.isTablet && aou != null) {
    					aou.unloadFragment();
    				} else {
    					getActivity().finish();
    				}
    			}
    		});

            ((Button) layout.findViewById(R.id.forum)).setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				Uri uri = Uri.parse(extras.getString("link"));
    				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    				startActivity(intent);
    			}
    		});

        	new NewsData().execute();

            return layout;
        }
        
    	private void postNewsData(Spanned newsText) {
    		text.setText(newsText);
    	}
    	
    	private class NewsData extends AsyncTask<URL, Integer, Long> {
    		private Spanned newsText;
    		private ProgressDialog loader;
    		
    		protected void onProgressUpdate(Integer... progress) {
    		}
    	
    		protected void onPostExecute(Long result) {
    			postNewsData(newsText);
    			if (loader != null) {
    				loader.dismiss();
    				loader = null;
    			}
    		}
    		
    		protected void onPreExecute() {
                loader = new ProgressDialog(activity);
                loader.setMessage("Loading..");
                loader.setCancelable(false);
                loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loader.show();
    		}

    		@Override
    		protected Long doInBackground(URL... arg0) {
    	        if (extras.getString("text").equals("")) {
    	        	newsText = Html.fromHtml("");
    	        } else {
    	        	newsText = Html.fromHtml(extras.getString("text"), getImage, null);
    	        }
    	        
    			return null;
    		}
    		
    	    private ImageGetter getImage = new ImageGetter() {
    	    	@Override
    	    	public Drawable getDrawable(String source) {                  
    	    		Drawable d = null;
    	    		try {
    	    			String path = "";
    	    			
    	    			if (source.contains("http")) {
    	    				path = source;
    	    			} else {
    	    				path = "http://www.ao-universe.com/" + source;
    	    			}
    	    			
    	    			InputStream src = imageFetch(path);
    	    			d = Drawable.createFromStream(src, "src");
    	    			if(d != null){
    	    				d.setBounds(0,0,d.getIntrinsicWidth(), d.getIntrinsicHeight());
    	    			}
    	    		} catch (MalformedURLException e) {
    					Logging.log(APP_TAG, e.getMessage());
    	    		} catch (IOException e) {
    					Logging.log(APP_TAG, e.getMessage());
    	    		}

    	    		return d;
    	    	}

    	    };
    	    
    	    public InputStream imageFetch(String source) throws MalformedURLException,IOException {
    	    	URL url = new URL(source);
    	    	Object o = url.getContent();
    	    	InputStream content = (InputStream)o;

    	    	return content;
    	    }
    	}
    }
}

