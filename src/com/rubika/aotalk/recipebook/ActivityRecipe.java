package com.rubika.aotalk.recipebook;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;

public class ActivityRecipe extends SherlockActivity {
	protected static final String APP_TAG = "--> AOTalk::ActivityRecipe";
	private String id;
	private ProgressDialog mProgressDialog;
	private ProgressDialog iProgressDialog;
	private int imageCount = 0;
	private TextView text;

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

        text = (TextView) findViewById(R.id.text);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading..");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
        
        iProgressDialog = new ProgressDialog(this);
        iProgressDialog.setMessage("Loading..");
        iProgressDialog.setCancelable(true);
        iProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        iProgressDialog.setProgress(0);
        
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
	
	private void postRecipeData(String recipeTitle, Spanned recipeText) {
		if (recipeTitle != null) {
			setTitle(recipeTitle);
		}
	
		if (recipeText != null) {
			text.setText(recipeText);
			Logging.log(APP_TAG, recipeText.toString());
		} else {
			Toast.makeText(this, R.string.data_load_failed, Toast.LENGTH_LONG).show();
		}
		
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		
		if (iProgressDialog != null) {
			iProgressDialog.dismiss();
			iProgressDialog = null;
		}
	}
	
	private class RecipeData extends AsyncTask<URL, Integer, Long> {
		private Spanned recipeText;
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

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(String.format(RecipeBook.RECIPES_INFO_URL, id));
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
                Logging.log(APP_TAG, String.format(RecipeBook.RECIPES_INFO_URL, id) + "\r" + xml);
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
            	NodeList nl = doc.getElementsByTagName("xml");
	            
	            for (int i = nl.getLength() - 1; i >= 0; i--) {
	                Logging.log(APP_TAG, "Content " + i);
	            	Element e = (Element) nl.item(i);
	                
	            	String recipeHtml = null;
	            	recipeTitle = getValue(e, "recipe_name");
	            	
	                Pattern pattern = Pattern.compile("<recipe_text>(.*?)</recipe_text>");
		            Matcher matcher = pattern.matcher(xml);
		            
		            if (matcher.find()) {
		            	recipeHtml = RecipeParser.preProcess(matcher.group(1));
		            }
			        
			        if (recipeHtml != null) {
			            pattern = Pattern.compile("#L \"([^/\"]*?)\" \"/tell recipebook rshow (.*?)\"");
				        matcher = pattern.matcher(recipeHtml);
				        
				        while(matcher.find()) {
				        	recipeHtml = recipeHtml.replace(
					        	"#L \"" + matcher.group(1) + "\" \"/tell recipebook rshow " + matcher.group(2) + "\"", 
					        	"<a href=\"aorb://" + matcher.group(2) + "\">" + matcher.group(1) + "</a>"
				        	);
				        }
		                	
				        pattern = Pattern.compile("#L \"([^/\"]*?)\" \"([0-9]*?)\"");
				        matcher = pattern.matcher(recipeHtml);
				        
				        while(matcher.find()) {
				        	recipeHtml = recipeHtml.replace(
					        	"#L \"" + matcher.group(1) + "\" \"" + matcher.group(2) + "\"", 
					        	"<a href=\"itemref://" + matcher.group(2) + "/0/0\">" + matcher.group(1) + "</a>"
				        	);
				        }
				        
				        pattern = Pattern.compile("#L \"([^/\"]*?)\" \"(.*?)\"");
				        matcher = pattern.matcher(recipeHtml);
				        
				        while(matcher.find()) {
				        	recipeHtml = recipeHtml.replace(
					        	"#L \"" + matcher.group(1) + "\" \"" + matcher.group(2) + "\"", 
					        	matcher.group(1)
				        	);
				        }
	
				        pattern = Pattern.compile("<img src=\'?rdb://([0-9]*?)\'?>");
				        matcher = pattern.matcher(recipeHtml);
				        
				        while(matcher.find()) {
				        	recipeHtml = recipeHtml.replace(
					        	"<img src=rdb://" + matcher.group(1) + ">", 
					        	"<img src=\"http://109.74.0.178/icon/" + matcher.group(1) + ".gif\" class=\"icon\">"
				        	);
				        	
				        	recipeHtml = recipeHtml.replace(
						        "<img src='rdb://" + matcher.group(1) + "'>", 
						        "<img src=\"http://109.74.0.178/icon/" + matcher.group(1) + ".gif\" class=\"icon\">"
					        );
				        	
		                	imageCount++;
				        }
		                
		                Logging.log(APP_TAG, recipeHtml);
		                
	                	recipeText = Html.fromHtml(RecipeParser.parse(recipeHtml), getImage, null);
			        } else {
			        	recipeText = Html.fromHtml("");
			        }

	    	        Logging.log(APP_TAG, "All done");
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
}
