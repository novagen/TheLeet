package com.rubika.aotalk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

public class Gridstream extends SherlockFragmentActivity {
	private static final String APP_TAG = "--> The Leet :: Gridstream";
	private static Activity activity;
	private static Context context;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        context = this;
        
        EasyTracker.getInstance().setContext(this);

        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
        	GridstreamFragment list = new GridstreamFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
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
    
    public static class GridstreamFragment extends SherlockFragment {
    	private static WebView webView;
    	
    	public GridstreamFragment() {
        }

        @Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }
        
        @SuppressLint("NewApi")
		@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	View fragmentLayout = inflater.inflate(R.layout.alert_gsp, container, false);
        	
			webView = (WebView) fragmentLayout.findViewById(R.id.gspcalendar);
			webView.setBackgroundColor(0);
	        
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				fragmentLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
			
			WebViewClient webClient = new WebViewClient() {
				@Override
	            public boolean shouldOverrideUrlLoading(WebView view, String url) {
					Logging.log(APP_TAG, url);
					
					if(url.startsWith("http://www.gridstream.org/")) {
						try {
							new GridstreamPage().execute(new URL(url));
						} catch (MalformedURLException e) {
							Logging.log(APP_TAG, e.getMessage());
						}
						return true;
					} else {
						return false;
					}
				}
			};
			
			webView.setWebViewClient(webClient);
			showGridstreamData("");
			
			try {
				new GridstreamCalendar().execute(new URL("http://gridstream.org/gsp-nextgen/subpages/news.aspx"));
			} catch (MalformedURLException e) {
				Logging.log(APP_TAG, e.getMessage());
			}
			
			Button close = (Button)fragmentLayout.findViewById(R.id.close);
			close.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.finish();
				}
			});
			
			return fragmentLayout;
        }
    
		private static Handler gspHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String gspData = (String) msg.obj;	
				showGridstreamData(gspData);
			}
		};
		
		public class GridstreamCalendar extends AsyncTask<URL, Integer, Long> {
	        private ProgressDialog loader;
	        
			@Override    
	        protected void onPreExecute() {
				loader = new ProgressDialog(context);
		    	loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				loader.setMessage(getString(R.string.loading_data) + getString(R.string.dots));
				loader.show();
	        }

	        @Override 
			protected void onPostExecute(Long result) {
		        if (loader != null) {
		        	loader.dismiss();
		        }
		    }
	        
	        @Override
	        protected void onProgressUpdate(Integer... progress) {
	        }
	        
			@Override
			protected Long doInBackground(URL... urls) {
				HttpClient httpClient = new DefaultHttpClient();
			    HttpContext localContext = new BasicHttpContext();
			    HttpGet httpGet = new HttpGet(urls[0].toString());
			    HttpResponse response = null;
			    BufferedReader reader = null;
			    
				try {
					response = httpClient.execute(httpGet, localContext);
				} catch (ClientProtocolException e) {
					Logging.log(APP_TAG, e.getMessage());
				} catch (IOException e) {
					Logging.log(APP_TAG, e.getMessage());
				}
				
			    String html = "";
			     
			    if (response != null) {
					try {
						reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					} catch (IllegalStateException e) {
						Logging.log(APP_TAG, e.getMessage());
					} catch (IOException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
			    }
			    
			    if (reader != null) {
				    String line = null;
				    try {
						while ((line = reader.readLine()) != null){
							html += line + "\n";
						}
					} catch (IOException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
			    }
		    
				if (!html.equals("")) {
		        	Pattern pattern = Pattern.compile("<b class=\"leftbarheadlines\">Events today:</b><br>(.*?)</td>", Pattern.DOTALL);
		            Matcher matcher = pattern.matcher(html);
		            
		            String resultHtml = "";

		            while(matcher.find()) {
		            	resultHtml = "<b class=\"leftbarheadlines\">Events today:</b><br>" + matcher.group(1);
		            }
		            
		            resultHtml = resultHtml.replace("src=\"/", "src=\"http://www.gridstream.org/");
		            resultHtml = resultHtml.replace("href=\"/", "href=\"http://www.gridstream.org/");
		            							
					Message msg = Message.obtain();
					msg.obj = resultHtml;
					
					gspHandler.sendMessage(msg);
					Logging.log(APP_TAG, resultHtml);
				}
				
				return 0L;
			}
	    }
	    
	    public class GridstreamPage extends AsyncTask<URL, Integer, Long> {
	        private ProgressDialog loader;
	        
	    	@Override    
	        protected void onPreExecute() {
				loader = new ProgressDialog(context);
		    	loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				loader.setMessage(getString(R.string.loading_data) + getString(R.string.dots));
				loader.show();
	        }

	        @Override 
			protected void onPostExecute(Long result) {
		        if (loader != null) {
		        	loader.dismiss();
		        }
		    }
	        
	        @Override
	        protected void onProgressUpdate(Integer... progress) {
	        }
	        
			@Override
			protected Long doInBackground(URL... urls) {
			    HttpClient httpClient = new DefaultHttpClient();
			    HttpContext localContext = new BasicHttpContext();
			    HttpGet httpGet = new HttpGet(urls[0].toString());
			    HttpResponse response = null;
			    BufferedReader reader = null;
			    
				try {
					response = httpClient.execute(httpGet, localContext);
				} catch (ClientProtocolException e) {
					Logging.log(APP_TAG, e.getMessage());
				} catch (IOException e) {
					Logging.log(APP_TAG, e.getMessage());
				}
				
			    String html = "";

			    if (response != null) {
					try {
						reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					} catch (IllegalStateException e) {
						Logging.log(APP_TAG, e.getMessage());
					} catch (IOException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
			    }
			    
			    if (reader != null) {
				    String line = null;
				    try {
						while ((line = reader.readLine()) != null){
							html += line + "\n";
						}
					} catch (IOException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
			    }
		    
				if (!html.equals("")) {
		        	Pattern pattern = Pattern.compile("<td width=\"100%\">(.*?)</td>", Pattern.DOTALL);
		            Matcher matcher = pattern.matcher(html);
		            
		            String resultHtml = "";

		            while(matcher.find()) {
		            	resultHtml = matcher.group(1);
		            }
		            
		            resultHtml = resultHtml.replace("src=\"/", "src=\"http://www.gridstream.org/");
		            resultHtml = resultHtml.replace("href=\"/", "href=\"http://www.gridstream.org/");
		            							            
					Message msg = Message.obtain();
					msg.obj = resultHtml;
					
					gspHandler.sendMessage(msg);
					Logging.log(APP_TAG, resultHtml);
				}
				
				return 0L;
			}
	    }
	
		private static void showGridstreamData(String resultHtml) {
			Logging.log(APP_TAG, resultHtml);
			webView.loadData(Uri.encode(Statics.GSP_HTML_START + resultHtml + Statics.HTML_END), "text/html", "UTF-8");
		}
    }
}
