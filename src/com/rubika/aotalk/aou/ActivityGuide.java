package com.rubika.aotalk.aou;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.rubika.aotalk.R;
import com.rubika.aotalk.TheLeet;
import com.rubika.aotalk.util.ImageCache;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityGuide extends SherlockFragmentActivity {
	protected static final String APP_TAG = "--> The Leet :: ActivityGuide";
	private String id;
	private ProgressDialog mProgressDialog;
	
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
        
        if (intent.getExtras() != null && intent.getExtras().getString("id") != null) {
	        id = intent.getExtras().getString("id");
        }
       
        if (intent.getData() != null) {
	        if(intent.getData().toString().startsWith("guideref://")) {
	        	id = intent.getData().toString().replace("guideref://", "").replace("/", "");
	        }
        }

        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, DataFragment.newInstance(
            	null,
	    		intent.getExtras().getString("title"),
				id
            )).commit();
        }
	}

	
    @Override
    protected void onResume() {
    	super.onResume();
    }

    @Override
	protected void onDestroy() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
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
	    private static Tracker tracker;
    	private Bundle extras;
    	private WebView info;
    	private TextView title;
    	private TextView faction;
    	private TextView classes;
    	private TextView level;
    	private Button forum;
    	private Activity activity;
    	private String id;
        private static AOU aou;

        public DataFragment() {
        }
        
        public static DataFragment newInstance(AOU parent, String title, String id) {
        	DataFragment f = new DataFragment();
        	
        	Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("id", id);
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
        
        @SuppressLint("NewApi")
		@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (container == null) {
                return null;
            }

            View layout = inflater.inflate(R.layout.activity_guide, container, false);
            activity = this.getActivity();
            extras = this.getArguments();
            
            id = extras.getString("id");
            
            EasyTracker.getInstance().setContext(this.getActivity().getApplicationContext());
            tracker = EasyTracker.getTracker();
             
    		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
    			layout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    		}
            
            info = (WebView) layout.findViewById(R.id.info);
            info.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            info.setBackgroundColor(0);
            info.setVisibility(View.INVISIBLE);
            info.setWebViewClient(new WebViewClient() {  
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
    	    		if (url.contains("://")) {
    	            	Intent intent;
    	        		intent = new Intent(Intent.ACTION_VIEW);
    	        		intent.setData(Uri.parse(url));
    	        		
    					startActivity(intent);
    	            	
    					return true;
    	    		} else {
    	    			return true;
    	    		}
               }
            });
            
            WebSettings settings = info.getSettings();
            settings.setJavaScriptEnabled(false);
            
            title = (TextView) layout.findViewById(R.id.title);
            faction = (TextView) layout.findViewById(R.id.faction);
            classes = (TextView) layout.findViewById(R.id.classes);
            level = (TextView) layout.findViewById(R.id.level);
                     
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
            
            forum = (Button) layout.findViewById(R.id.forum);
            
            new GuideData().execute();

            return layout;
        }
	
		private void postGuideData(String guideTitle, String guideFaction, String guideClasses, String guideLevel, final String guideLink, String guideLocal, String guideText) {
			if (guideTitle != null) {
				tracker.sendEvent("AOU", "Guide", guideTitle, 0L);
	
				getActivity().setTitle(guideTitle);
	        	title.setText(guideTitle);
			}
			
	        if (guideFaction != null) {
	        	faction.setText(guideFaction);
	        }
	        
	        if (guideClasses != null) {
	        	classes.setText(guideClasses);
	        }
	        
	        if (guideLevel != null) {
	        	level.setText(guideLevel);
	        }
			
			if (guideLocal != null || guideText != null) {
		        if (guideLocal != null) {
					info.loadUrl("file://" + guideLocal);
				} else {
			    	info.loadData(guideText, "text/html", "UTF-8");
				}
				info.setVisibility(View.VISIBLE);
			} else {
				Toast.makeText(getActivity().getApplicationContext(), R.string.data_load_failed, Toast.LENGTH_LONG).show();
			}
	
	        forum.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Uri uri = Uri.parse(guideLink);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				}
			});
		}
	    	
	private class GuideData extends AsyncTask<URL, Integer, Long> {
		private String guideTitle;
		private String guideFaction;
		private String guideClasses;
		private String guideLevel;
		private String guideLink;
		private String guideLocal;
		private String guideText = null;
		private ProgressDialog loader;
		
		protected void onProgressUpdate(Integer... progress) {
		}
	
		protected void onPostExecute(Long result) {
			postGuideData(guideTitle, guideFaction, guideClasses, guideLevel, guideLink, guideLocal, guideText);
			
			if (loader != null) {
				loader.dismiss();
				loader = null;
			}
		}
		
		protected void onPreExecute() {
            loader = new ProgressDialog(activity);
            loader.setMessage("Loading..");
            loader.setCancelable(true);
            loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loader.show();
		}

		@Override
		protected Long doInBackground(URL... arg0) {
	        long loadTime = System.currentTimeMillis();

	        String xml = null;
            String html = null;
            
            Document doc = null;

            Logging.log(APP_TAG, "Starting XML download");

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(String.format(Statics.GUIDES_INFO_URL, id));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
                Logging.log(APP_TAG, String.format(Statics.GUIDES_INFO_URL, id));
                Logging.log(APP_TAG, xml);
            } catch (UnsupportedEncodingException e) {
				Logging.log(APP_TAG, e.getMessage());
            } catch (ClientProtocolException e) {
				Logging.log(APP_TAG, e.getMessage());
            } catch (IOException e) {
				Logging.log(APP_TAG, e.getMessage());
            }
            
            if (xml != null) {
	            xml = xml.replaceAll("\r\n", "").replaceAll("\n", "").replaceAll("\r", "");
	            
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
            	NodeList nl = doc.getElementsByTagName("content");
	            
	            for (int i = nl.getLength() - 1; i >= 0; i--) {
	                Logging.log(APP_TAG, "Content " + i);
	            	Element e = (Element) nl.item(i);
	                
	                guideTitle = getValue(e, "name");
	            	
	                Pattern pattern = Pattern.compile("<text>(.*?)</text>");
		            Matcher matcher = pattern.matcher(xml);
		            
		            if (matcher.find()) {
		            	html = matcher.group(1).replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&amp;", "&");
		            }
	                	
	                Logging.log(APP_TAG, "Replaceing guide links");
	                pattern = Pattern.compile("\"index.php.*?&pid=([0-9]*?)\"");
	                
	                if (html != null) {
		                if (pattern != null) {
			                matcher = pattern.matcher(html);
			                html = matcher.replaceAll("\"guideref://$1/\"");
		                }
		            }
	                
	                pattern = Pattern.compile("\"http://www.ao-universe.com/index.php.*?&pid=([0-9]*?)\"");
	                
	                if (html != null) {
		                if (pattern != null) {
			                matcher = pattern.matcher(html);
			                html = matcher.replaceAll("\"guideref://$1/\"");
		                }
		            }
	                
	                Logging.log(APP_TAG, "Replaceing item links");
	                pattern = Pattern.compile("\"http://www.xyphos.com/ao/aodb.php\\?id=(.*?)\"");
	                
	                if (html != null) {
		                if (pattern != null) {
			                matcher = pattern.matcher(html);
			                html = matcher.replaceAll("\"itemref://$1/0/0\"");
		                }
	                }
	                
	                Logging.log(APP_TAG, "Replaceing image paths");
	                html = html.replaceAll("\"./files/_upload", "\"http://www.ao-universe.com/files/_upload");
	                
	                Logging.log(APP_TAG, "Replacing map references");
	                pattern = Pattern.compile(" - ([0-9]*?)\\.[0-9]x([0-9]*?)\\.[0-9]\\)</span>");
	                
	                if (html != null) {
		                if (pattern != null) {
			                matcher = pattern.matcher(html);
			                html = matcher.replaceAll(" - $1x$2)</span>");
		                }
	                }
	                pattern = Pattern.compile("<span class=\"waypoint\" id=\".*?\">(.*?) \\((.*?) - ([0-9]*?)x([0-9]*?)\\)</span>");
	                
	                if (html != null) {
		                if (pattern != null) {
			                matcher = pattern.matcher(html);
			                html = matcher.replaceAll("<a href=\"aomap://$1/$2/$3/$4\">$1 ($2 - $3x$4)</a>");
		                }
	                }
	                
	                html = html.replaceAll(" border=\"0\"", "");
	                
	                Logging.log(APP_TAG, "Removing onclicks");
	                pattern = Pattern.compile("onclick=\"(.*?)\"");
	                
	                if (html != null) {
		                if (pattern != null) {
			                matcher = pattern.matcher(html);
			                html = matcher.replaceAll("");
		                }
	                }
	                
	                html = html.replace("<br>", "<br />");
	                html = html.replace(" < ", " &lt; ");
	                
	                Logging.log(APP_TAG, html);
	                
	                guideFaction = getValue(e, "faction");
	                guideClasses = getValue(e, "class");
	                guideLevel = getValue(e, "level");
	                guideLink = String.format("http://www.ao-universe.com/index.php?id=14&pid=%s", id);

	                File cacheDir = ImageCache.getCacheDirectory(TheLeet.getContext().getPackageName(), "guidedata");
	                if (cacheDir != null) {
		                File cacheFile = new File(cacheDir + File.separator + guideLink.hashCode());
	
		                guideLocal = cacheFile.getAbsolutePath();
	                	
						try {
			    	        Logging.log(APP_TAG, "Saving to: " + cacheFile.getAbsolutePath());
	
			    	        FileOutputStream fOut = new FileOutputStream(cacheFile);
		                	OutputStreamWriter osw = new OutputStreamWriter(fOut);
		                	
		                	osw.write(Statics.GUIDE_HTML_START + html + Statics.HTML_END);
		                	osw.close();
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
	                } else {
		    	        guideText = Statics.GUIDE_HTML_START + html + Statics.HTML_END;
	                	Logging.log(APP_TAG, "Unable to saving guide cache");
	                }
	                
	    	        Logging.log(APP_TAG, "All done");
	            	tracker.sendTiming("Loading", System.currentTimeMillis() - loadTime, "AOU Guide", null);
	            }
            }

			return null;
		}
		
        private final String getValue(Element item, String str) {
        	NodeList n = item.getElementsByTagName(str);
        	return getElementValue(n.item(0));
        }

        private final String getElementValue(Node elem) {
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
	}
	}
}
