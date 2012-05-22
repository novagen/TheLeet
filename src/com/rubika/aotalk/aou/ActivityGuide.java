package com.rubika.aotalk.aou;

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
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityGuide extends SherlockActivity {
	protected static final String APP_TAG = "--> AOTalk::ActivityGuide";
	
	private TextView text;
	private TextView title;
	private TextView faction;
	private TextView classes;
	private TextView level;
	private String id;
	private ProgressDialog mProgressDialog;
	
	protected static final String HTML_START = 
		"<html><head></head><style type=\"text/css\">" +
		"body { background-color:transparent; color:#ffffff; font-size:0.9em; padding:0; margin:0; }" +
		"a { color:#9191ff; }" +
		"</style><body>";
	protected static final String HTML_END   = "</body></html>";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_guide);

        final ActionBar bar = getSupportActionBar();
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        
        if (extras != null) {
	        id = extras.getString("id");
        }
       
        if (getIntent().getData() != null) {
	        if(getIntent().getData().toString().startsWith("guideref://")) {
	        	id = getIntent().getData().getPathSegments().get(0);
	        }
        }
        
        Logging.log(APP_TAG, "Guide ID " + id);
        
        text = (TextView) findViewById(R.id.text);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        
        title = (TextView) findViewById(R.id.title);
        faction = (TextView) findViewById(R.id.faction);
        classes = (TextView) findViewById(R.id.classes);
        level = (TextView) findViewById(R.id.level);
       
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
        
        new GuideData().execute();
	}
	
	private void postGuideData(String guideTitle, Spanned guideText, String guideFaction, String guideClasses, String guideLevel, final String guideLink) {
		if (guideTitle != null) {
			setTitle(guideTitle);
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
		
		if (guideText != null) {
			text.setText(guideText);
			Logging.log(APP_TAG, guideText.toString());
		} else {
			Toast.makeText(this, R.string.data_load_failed, Toast.LENGTH_LONG);
		}
		

        ((Button) findViewById(R.id.forum)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse(guideLink);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
    private ImageGetter getImage = new ImageGetter() {
    	@Override
    	public Drawable getDrawable(String source) {                  
    		Drawable d = null;
    		try {
    			String path = "";
    			
    			if (source.startsWith("http")) {
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
        
        private InputStream imageFetch(String source) throws MalformedURLException, IOException {
        	URL url = new URL(source);
        	Object o = url.getContent();
        	InputStream content = (InputStream)o;
        	return content;
        }
    };
    
	private class GuideData extends AsyncTask<URL, Integer, Long> {
		private Spanned guideText;
		private String guideTitle;
		private String guideFaction;
		private String guideClasses;
		private String guideLevel;
		private String guideLink;
		
		protected void onProgressUpdate(Integer... progress) {
		}
	
		protected void onPostExecute(Long result) {
			postGuideData(guideTitle, guideText, guideFaction, guideClasses, guideLevel, guideLink);
		}
		
		protected void onPreExecute() {
		}

		@Override
		protected Long doInBackground(URL... arg0) {
        	String xml = null;
            Document doc = null;

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(String.format(AOU.GUIDES_INFO_URL, id));
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
            	NodeList nl = doc.getElementsByTagName("content");
	            
	            for (int i = nl.getLength() - 1; i >= 0; i--) {
	                Logging.log(APP_TAG, "Content " + i);
	            	Element e = (Element) nl.item(i);
	                
	                String guideHtml = getValue(e, "text").replace("<![", "").replace("]]>", "").trim();
	                guideTitle = getValue(e, "name");
	                	
	                Logging.log(APP_TAG, "Replaceing guide links");
	                Pattern pattern = Pattern.compile("\\[url=index.php\\?id=(.*?)&pid=(.*?)](.*?)\\[/url]");
	                Matcher matcher = pattern.matcher(guideHtml);
	                guideHtml = matcher.replaceAll("<a href=\"guideref://$1/$2\">$3</a>");

	                Logging.log(APP_TAG, "Converting BBCode");
	                guideHtml = BBCodeConverter.convertString(guideHtml);
	                Logging.log(APP_TAG, "Converting Item references");
	                guideHtml = XyphosData.insertData(guideHtml);

	                if (guideHtml == null) {
	                	guideHtml = getString(R.string.unable_to_load_guide);
	                }
	                
                	guideText = Html.fromHtml(guideHtml, getImage, null);
	                guideFaction = getValue(e, "faction");
	                guideClasses = getValue(e, "class");
	                guideLevel = getValue(e, "level");
	                guideLink = String.format("http://www.ao-universe.com/index.php?id=14&pid=%s", id);
	                
	                Logging.log(APP_TAG, "All done");
	            }
            }

			return null;
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
}
