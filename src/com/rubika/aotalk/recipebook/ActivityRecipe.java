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

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.Information;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

public class ActivityRecipe extends SherlockActivity {
	protected static final String APP_TAG = "--> AOTalk::ActivityRecipe";
	private String id = null;
	private ProgressDialog mProgressDialog;
	private WebView info;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recipe);
        
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

        info = (WebView) findViewById(R.id.web);
        info.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        info.setBackgroundColor(0);
        info.setVisibility(View.INVISIBLE);
        
        info.setWebViewClient(new WebViewClient() {  
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    		Intent intent;
	    		
            	if (url.startsWith("aorb://")) {
					intent = new Intent(ActivityRecipe.this, ActivityRecipe.class);
					intent.putExtra("id", url.replace("aorb://", ""));
					intent.setData(Uri.parse(url));
					startActivity(intent);
	            } else {
					intent = new Intent(ActivityRecipe.this, Information.class);
					intent.setData(Uri.parse(url));
					startActivity(intent);
	    		}
            	
				return true;
           }
        });

        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.abbg));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading..");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
        
        ((Button) findViewById(R.id.close)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
       
        new RecipeData().execute();
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
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
	
	private void postRecipeData(String recipeTitle, String recipeText) {
        Logging.log(APP_TAG, recipeText);

        if (recipeTitle != null) {
			setTitle(recipeTitle);
		}
	
		String text = "";
		
    	if(recipeText != null && recipeText.length() > 0) {
        	text = Statics.HTML_START + recipeText + Statics.HTML_END;
        } else {
        	text = Statics.HTML_START + getString(R.string.no_data).replace("\n", "<br />") + Statics.HTML_END;
        }
    	
    	info.loadData(Uri.encode(text), "text/html", "UTF-8");
    	info.setVisibility(View.VISIBLE);
		
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	private class RecipeData extends AsyncTask<URL, Integer, Long> {
		private String recipeText;
		private String recipeTitle;
		
		protected void onProgressUpdate(Integer... progress) {
		}
	
		protected void onPostExecute(Long result) {
			postRecipeData(recipeTitle, recipeText);
		}
		
		protected void onPreExecute() {
		}

		@Override
		protected Long doInBackground(URL... arg0) {
        	String xml = null;
            Document doc = null;
            
            //Pattern pattern;
            //Matcher matcher;
            
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(String.format(RecipeBook.RECIPES_INFO_URL, id));
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
                Logging.log(APP_TAG, String.format(RecipeBook.RECIPES_INFO_URL, id) + "\r\n" + xml);
            } catch (UnsupportedEncodingException e) {
				Logging.log(APP_TAG, e.getMessage());
            } catch (ClientProtocolException e) {
				Logging.log(APP_TAG, e.getMessage());
            } catch (IOException e) {
				Logging.log(APP_TAG, e.getMessage());
            }
            
            if (xml != null) {
                /*
            	pattern = Pattern.compile("<recipe_text>(.*?)</recipe_text>");
	            matcher = pattern.matcher(xml);
	            
	            if (matcher.find()) {
	            	xml = xml.replace(matcher.group(1), matcher.group(1).replaceAll("<","&lt;").replaceAll(">","&gt;"));
	            }
	            */
            	
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
	
	/*
    private ImageGetter getImage = new ImageGetter() {
    	@Override
    	public Drawable getDrawable(String source) {                  
    		if (!iProgressDialog.isShowing()) {
    	        runOnUiThread(new Runnable() {                 
    	        	@Override
    	        	public void run() {
		                if (mProgressDialog != null) {
		                	mProgressDialog.dismiss();
		                	mProgressDialog = null;
		                }
		                
    	        		iProgressDialog.setMax(imageCount);
		    	        iProgressDialog.setProgress(0);
		    	        iProgressDialog.show();
    	        	}
    	        });
    		}
    		
    		Drawable d = null;
    		try {
    			String path = "";
    			
    			if (source.startsWith("http")) {
    				path = source;
    			} else {
    				path = "http://www.ao-universe.com/" + source;
    			}
    			
    			Logging.log(APP_TAG, path);
    			
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
        	
    		if (iProgressDialog != null) {
				iProgressDialog.setProgress(iProgressDialog.getProgress() + 1);
    		}

    		return d;
    	}
        
        private InputStream imageFetch(String source) throws MalformedURLException, IOException {
        	URL url = new URL(source);
        	Object o = url.getContent();
        	InputStream content = (InputStream)o;
        	
        	return content;
        }
    };
    */
}
