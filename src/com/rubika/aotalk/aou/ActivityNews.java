package com.rubika.aotalk.aou;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.Html.ImageGetter;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ActivityNews extends SherlockActivity {
	private static final String APP_TAG = "--> The Leet ::ActivityNews";
	
	private TextView text;
	private Bundle extras;
	private ProgressDialog mProgressDialog;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.abbg));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        extras = intent.getExtras();

        setTitle(extras.getString("title"));
        
        TextView date = (TextView) findViewById(R.id.date);
        
        text = (TextView) findViewById(R.id.text);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        
        if (extras.getString("date").equals("")) {
        	date.setVisibility(View.GONE);
        } else {
        	date.setText(extras.getString("date"));
        }
        
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading..");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();

        ((Button) findViewById(R.id.close)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

        ((Button) findViewById(R.id.forum)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse(extras.getString("link"));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});

    	new NewsData().execute();
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
	
	private void postNewsData(Spanned newsText) {
		text.setText(newsText);
    	mProgressDialog.dismiss();
	}
	
	private class NewsData extends AsyncTask<URL, Integer, Long> {
		private Spanned newsText;
		
		protected void onProgressUpdate(Integer... progress) {
		}
	
		protected void onPostExecute(Long result) {
			postNewsData(newsText);
		}
		
		protected void onPreExecute() {
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
	    	// add delay here (see comment at the end)     
	    	return content;
	    }
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*
	    MenuItem share = menu.findItem(R.id.share);
	    
	    shareActionProvider = (ShareActionProvider) share.getActionProvider();
    	shareActionProvider.setShareIntent(shareIntent);
		*/
		//getSupportMenuInflater().inflate(R.menu.menu_aou, menu);
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
}
