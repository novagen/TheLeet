package com.rubika.aotalk.recipebook;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.rubika.aotalk.Information;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

public class ActivityRecipe extends SherlockFragmentActivity {
	protected static final String APP_TAG = "--> The Leet :: ActivityRecipe";
	private String id = null;

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
        Bundle extras = intent.getExtras();
        
        if (extras != null) {
	        id = extras.getString("id");
        }
        
        if (getIntent().getData() != null) {
	        if(getIntent().getData().toString().startsWith("aorb://")) {
	        	id = getIntent().getData().toString().replace("aorb://", "");
	        }
        }
        
        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, DataFragment.newInstance(
            	null,
	    		id
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
		getSupportMenuInflater().inflate(R.menu.menu_aou, menu);
    	return true;
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
    	private Bundle extras;
        private static RecipeBook fragmentHolder;
    	private Activity activity;
    	private WebView info;
    	private String id = null;

        public DataFragment() {
        }
        
        public static DataFragment newInstance(RecipeBook parent, String id) {
        	DataFragment f = new DataFragment();
        	
        	Bundle args = new Bundle();
            args.putString("id", id);
            f.setArguments(args);
        	
            fragmentHolder = parent;
        	
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

            activity = this.getActivity();
            extras = this.getArguments();
            
            id = extras.getString("id");

            View layout = inflater.inflate(R.layout.activity_recipe, container, false);
            extras = this.getArguments();
            
            info = (WebView) layout.findViewById(R.id.web);
            info.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            info.setBackgroundColor(0);
            info.setVisibility(View.INVISIBLE);
            
    		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
    			layout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    		}
           
            info.setWebViewClient(new WebViewClient() {  
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
    	    		Intent intent;
    	    		
                	if (url.startsWith("aorb://")) {
    					intent = new Intent(activity.getApplicationContext(), ActivityRecipe.class);
    					intent.putExtra("id", url.replace("aorb://", ""));
    					intent.setData(Uri.parse(url));
    					startActivity(intent);
    	            } else {
    					intent = new Intent(activity.getApplicationContext(), Information.class);
    					intent.setData(Uri.parse(url));
    					startActivity(intent);
    	    		}
                	
    				return true;
               }
            });
            
            
            ((Button) layout.findViewById(R.id.close)).setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				if (fragmentHolder != null && fragmentHolder.isTablet) {
    					fragmentHolder.unloadFragment();
    				} else {
    					getActivity().finish();
    				}
    			}
    		});
           
            new RecipeData().execute();

            return layout;
        }
        
    	private void postRecipeData(String recipeTitle, String recipeText) {
            Logging.log(APP_TAG, recipeText);

            if (recipeTitle != null) {
    			activity.setTitle(recipeTitle);
    		}
    	
    		String text = "";
    		
        	if(recipeText != null && recipeText.length() > 0) {
            	text = Statics.HTML_START + recipeText + Statics.HTML_END;
            } else {
            	text = Statics.HTML_START + getString(R.string.no_data).replace("\n", "<br />") + Statics.HTML_END;
            }
        	
        	info.loadData(Uri.encode(text), "text/html", "UTF-8");
        	info.setVisibility(View.VISIBLE);
    	}
    	
    	private class RecipeData extends AsyncTask<URL, Integer, Long> {
    		private ProgressDialog loader;
    		private String recipeText;
    		private String recipeTitle;
    		
    		protected void onProgressUpdate(Integer... progress) {
    		}
    	
    		protected void onPostExecute(Long result) {
    			postRecipeData(recipeTitle, recipeText);
    			
    			if (loader != null) {
    				loader.dismiss();
    				loader = null;
    			}
    		}
    		
    		protected void onPreExecute() {
    	        loader = new ProgressDialog(activity);
    	        loader.setMessage(getString(R.string.loading_data) + getString(R.string.dots));
    	        loader.setCancelable(true);
    	        loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	        loader.show();
    		}

    		@Override
    		protected Long doInBackground(URL... arg0) {
            	String xml = null;
                Document doc = null;
                
                try {
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(String.format(Statics.RECIPES_INFO_URL, id));
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    xml = EntityUtils.toString(httpEntity);
                    Logging.log(APP_TAG, String.format(Statics.RECIPES_INFO_URL, id) + "\r\n" + xml);
                } catch (UnsupportedEncodingException e) {
    				Logging.log(APP_TAG, e.getMessage());
                } catch (ClientProtocolException e) {
    				Logging.log(APP_TAG, e.getMessage());
                } catch (IOException e) {
    				Logging.log(APP_TAG, e.getMessage());
                }
                
                if (xml != null) {
                    Logging.log(APP_TAG, "Fixed\n\r\n\r" + xml);

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
                	NodeList nl = doc.getElementsByTagName("xml");
    	            
    	            for (int i = nl.getLength() - 1; i >= 0; i--) {
    	            	Element e = (Element) nl.item(i);
    	            	
    	            	recipeTitle = getValue(e, "recipe_name");
    			        
    			        if (getValue(e, "recipe_text") != null) {
    	                	recipeText = RecipeParser.parse(getValue(e, "recipe_text"));
    			        } else {
    		                recipeText = "";
    			        }
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
