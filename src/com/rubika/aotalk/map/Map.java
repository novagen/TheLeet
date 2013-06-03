package com.rubika.aotalk.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.qozix.mapview.MapView;
import com.qozix.mapview.MapView.MapEventListener;
import com.qozix.mapview.tiles.MapTileDecoder;
import com.qozix.mapview.tiles.MapTileDecoderHttp;
import com.rubika.aotalk.Preferences;
import com.rubika.aotalk.R;
import com.rubika.aotalk.adapter.MapTypeAdapter;
import com.rubika.aotalk.item.MapType;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.RKNet;
import com.rubika.aotalk.util.Statics;

public class Map extends SherlockFragmentActivity {
	private static final String APP_TAG = "--> The Leet :: Map";

	private Context context;
	private List<MarkerView> rk_markers;
	private List<MarkerView> sl_markers;
	
	private List<MarkerView> rk_markers_from_external;
	private List<MarkerView> sl_markers_from_external;
	
	private MapView mapView;
	private FrameLayout frame;
	private ActionBar bar;
	private MapTileDecoder mapTileDecoder;
	
	private FrameLayout.LayoutParams mapViewLayout = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);;
	
	private Handler RKNLocationHandler = new Handler();
	private long RKNLocationUpdateTime = 2000;
	private boolean RKNLocationIsUpdating = false;
	private boolean DoRKNLocationUpdates = false;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//super.setTheme(R.style.Theme_AOTalkTheme_Light);

		context = this;

		frame = new FrameLayout(this);

		Logging.log(APP_TAG, "Setting content view");
        setContentView(frame);
                
        List<MapType> list = new ArrayList<MapType>();
    	list.add(new MapType("Atlas of Rubi-Ka", 0));
    	list.add(new MapType("Atlas of Shadowlands", 0));
    	
    	MapTypeAdapter mapAdapter = new MapTypeAdapter(this, R.id.messagelist, list); 
    	
    	mapTileDecoder = new MapTileDecoderHttp();
        
        bar = getSupportActionBar();
        
        Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        
        rk_markers = new ArrayList<MarkerView>();
        sl_markers = new ArrayList<MarkerView>();
        
        rk_markers_from_external = new ArrayList<MarkerView>();
        sl_markers_from_external = new ArrayList<MarkerView>();
        
        if (extras != null) {
        	Logging.log(APP_TAG, "Got coordinates from extras");
        	
			Integer[] pos = ZoneTools.getRealPosition(context, extras.getString("zone"), extras.getInt("x"), extras.getInt("y"));
      	
        	Marker marker = new Marker();
        	marker.setTitle(extras.getString("name"));
        	marker.setZone(extras.getString("zone"));
        	marker.setX(pos[0]);
        	marker.setY(pos[1]);
        	marker.setOnRK(pos[2] == 1);
        	
        	if (marker.isOnRK()) 
        	{
        		rk_markers_from_external.add(new MarkerView(createMarkerView(context, marker.getTitle()), marker.getX(), marker.getY()));
        	} 
        	else 
        	{
        		sl_markers_from_external.add(new MarkerView(createMarkerView(context, marker.getTitle()), marker.getX(), marker.getY()));
        	}
        }
        
        if (getIntent().getData() != null) {
	        if(getIntent().getData().toString().startsWith("aomap")) {
	        	Logging.log(APP_TAG, "Got coordinates from intent");
	        	
	        	String values[] = Uri.decode(getIntent().getData().toString()).replace("aomap://", "").trim().split("/");
				Integer[] pos = ZoneTools.getRealPosition(context, values[1], Integer.parseInt(values[2]), Integer.parseInt(values[3]));
	      	
	        	Marker marker = new Marker();
	        	marker.setTitle(values[0]);
	        	marker.setZone(values[1]);
	        	marker.setX(pos[0]);
	        	marker.setY(pos[1]);
	        	marker.setOnRK(pos[2] == 1);
	        	
	        	if (marker.isOnRK()) 
	        	{
	        		rk_markers_from_external.add(new MarkerView(createMarkerView(context, marker.getTitle()), marker.getX(), marker.getY()));
	        	} 
	        	else 
	        	{
	        		sl_markers_from_external.add(new MarkerView(createMarkerView(context, marker.getTitle()), marker.getX(), marker.getY()));
	        	}
	        }
        }
       
		bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(mapAdapter, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition , long itemId) {
				removeMarkers();
				updateMap();
		        return true;
			}
        });
        
        if (rk_markers.size() > 0) 
        {
        	bar.setSelectedNavigationItem(0);
        } 
        else if (sl_markers.size() > 0) 
        {
        	bar.setSelectedNavigationItem(1);
        } 
        else 
        {
        	bar.setSelectedNavigationItem(0);
        }
		
		Logging.log(APP_TAG, "Loading mapview");
		
		initializeMapView(context);
		
		setRKZoomLevels();
		setLowresImage();
				
        frame.addView(mapView, mapViewLayout);
    }
	
	private void setRKZoomLevels() {
        if (mapView != null) {
			mapView.addZoomLevel(7168, 9216, "https://rubi-ka.net/LeetContent/maps/aork/5/map_%row%_%col%.jpg", 1024, 1024);
	        mapView.addZoomLevel(3584, 4608, "https://rubi-ka.net/LeetContent/maps/aork/4/map_%row%_%col%.jpg", 512, 512);
	        mapView.addZoomLevel(1792, 2304, "https://rubi-ka.net/LeetContent/maps/aork/3/map_%row%_%col%.jpg", 256, 256);
	        mapView.addZoomLevel(896, 1152, "https://rubi-ka.net/LeetContent/maps/aork/2/map_%row%_%col%.jpg", 128, 128);
	        mapView.addZoomLevel(448, 576, "https://rubi-ka.net/LeetContent/maps/aork/1/map_%row%_%col%.jpg", 64, 64);
        }
	}
	
	private void setSLZoomLevels() {
        if (mapView != null) {
			mapView.addZoomLevel(6144, 24576, "https://rubi-ka.net/LeetContent/maps/aosl/5/map_%row%_%col%.jpg", 1024, 1024);
	        mapView.addZoomLevel(3072, 12288, "https://rubi-ka.net/LeetContent/maps/aosl/4/map_%row%_%col%.jpg", 512, 512);
	        mapView.addZoomLevel(1536, 6144, "https://rubi-ka.net/LeetContent/maps/aosl/3/map_%row%_%col%.jpg", 256, 256);
	        mapView.addZoomLevel(768, 3072, "https://rubi-ka.net/LeetContent/maps/aosl/2/map_%row%_%col%.jpg", 128, 128);
	        mapView.addZoomLevel(384, 1536, "https://rubi-ka.net/LeetContent/maps/aosl/1/map_%row%_%col%.jpg", 64, 64);
        }
	}
	
	private void updateMap() {
		int itemPosition = bar.getSelectedNavigationIndex();
	    
	    mapView.resetZoomLevels();
				
		if (itemPosition == 0) 
		{
			setRKZoomLevels();
		}
		else if (itemPosition == 1)
		{        
			setSLZoomLevels();
		}
		
		mapView.clear();
		insertMarkers();
		setLowresImage();
		
        mapView.requestRender();
	}
	
	private void setLowresImage() {
		int itemPosition = bar.getSelectedNavigationIndex();
		int currentZoom = mapView.getZoom();
		
		if (mapView != null) {
	        View child = mapView.getChildAt(0);
	        
	        /*
	        if (itemPosition == 0) 
			{
	        	child.setBackgroundResource(R.drawable.rk_lowres_2);				
			}
			else if (itemPosition == 1)
			{        
	        	child.setBackgroundResource(R.drawable.sl_lowres_2);				
			}
	        */
	        
	        if (child != null) {
		        if (itemPosition == 0) 
				{
					switch (currentZoom) {
					case 0: 
			        	child.setBackgroundResource(R.drawable.rk_lowres_1);				
						break;
					case 1: 
			        	child.setBackgroundResource(R.drawable.rk_lowres_2);				
						break;
					case 2: 
			        	child.setBackgroundResource(R.drawable.rk_lowres_3);				
						break;
					case 3: 
			        	child.setBackgroundResource(R.drawable.rk_lowres_4);				
						break;
					case 4: 
			        	child.setBackgroundResource(R.drawable.rk_lowres_4);				
						break;
					default:
			        	child.setBackgroundResource(R.drawable.rk_lowres_4);				
						break;
					}
				}
				else if (itemPosition == 1)
				{        
					switch (currentZoom) {
					case 0: 
			        	child.setBackgroundResource(R.drawable.sl_lowres_1);				
						break;
					case 1: 
			        	child.setBackgroundResource(R.drawable.sl_lowres_2);				
						break;
					case 2: 
			        	child.setBackgroundResource(R.drawable.sl_lowres_3);				
						break;
					case 3: 
			        	child.setBackgroundResource(R.drawable.sl_lowres_4);				
						break;
					case 4: 
			        	child.setBackgroundResource(R.drawable.sl_lowres_4);				
						break;
					default:
			        	child.setBackgroundResource(R.drawable.sl_lowres_4);				
						break;
					}
				}
	        }
		}
		
        System.gc();
	}

	private void updateMarkers() {
    	for(MarkerView marker : rk_markers)
		{
	        mapView.updateMarker(marker.view.getTag(), marker.X, marker.Y);
		}
		
		for(MarkerView marker : sl_markers) 
		{
	        mapView.updateMarker(marker.view.getTag(), marker.X, marker.Y);
		}
		
		for(MarkerView marker : rk_markers_from_external)
		{
	        mapView.updateMarker(marker.view.getTag(), marker.X, marker.Y);
		}
		
		for(MarkerView marker : sl_markers_from_external) 
		{
	        mapView.updateMarker(marker.view.getTag(), marker.X, marker.Y);
		}
	}
	
	private void removeMarkers() {
        if (mapView != null) {
	        mapView.removeAllMarkers();
	        
	        /*
        	for(MarkerView marker : rk_markers)
			{
		        mapView.removeMarker(marker.view);
			}
			
			for(MarkerView marker : sl_markers) 
			{
		        mapView.removeMarker(marker.view);
			}
			
			for(MarkerView marker : rk_markers_from_external)
			{
		        mapView.removeMarker(marker.view);
			}
			
			for(MarkerView marker : sl_markers_from_external) 
			{
		        mapView.removeMarker(marker.view);
			}
			*/
	        
			//mapView.clear();
			//mapView.requestRender();
        }
	}
	
	private void insertMarkers() {
		int itemPosition = bar.getSelectedNavigationIndex();
		
		if (mapView != null) {
		    int totalMarkers = 0;

		    if (itemPosition == 0) 
			{
				for(MarkerView marker : rk_markers)
				{
			        mapView.addMarker(marker.view, marker.X, marker.Y, true);
			        totalMarkers++;
				}
				
				for(MarkerView marker : rk_markers_from_external)
				{
			        mapView.addMarker(marker.view, marker.X, marker.Y, true);
			        totalMarkers++;
				}
				
				if (totalMarkers == 1)
				{
					if (rk_markers.size() > 0) {
						mapView.moveToAndCenter(rk_markers.get(0).X, rk_markers.get(0).Y);
					} else {
						mapView.moveToAndCenter(rk_markers_from_external.get(0).X, rk_markers_from_external.get(0).Y);
					}
				}
			}
			else if (itemPosition == 1)
			{        
				for(MarkerView marker : sl_markers)
				{
			        mapView.addMarker(marker.view, marker.X, marker.Y, true);
			        totalMarkers++;
				}
				
				for(MarkerView marker : sl_markers_from_external)
				{
			        mapView.addMarker(marker.view, marker.X, marker.Y, true);
			        totalMarkers++;
				}
				
				if (totalMarkers == 1)
				{
					if (sl_markers.size() > 0) {
						mapView.moveToAndCenter(sl_markers.get(0).X, sl_markers.get(0).Y);
					} else {
						mapView.moveToAndCenter(sl_markers_from_external.get(0).X, sl_markers_from_external.get(0).Y);
					}
				}
			}
		}
	}
    
	private ImageView createMarkerView(final Context context, final String title) {
    	ImageView imageView = new ImageView(context);
    	imageView.setImageResource(R.drawable.marker);
    	imageView.setTag(title);
    	imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Logging.toast(context, title);
			}
		});
    	
    	return imageView;
	}
	
    private void initializeMapView(final Context context) {
        mapView = new MapView(this);
        mapView.setBackgroundColor(Color.BLACK);
        mapView.setMarkerAnchorPoints(-0.5f, -0.5f);
		mapView.setShouldIntercept(true);
		mapView.setCacheEnabled(true);
		mapView.setTileDecoder(mapTileDecoder);
		//mapView.setScaleToFit(true);
		//mapView.setZoom(1);
		
		mapView.addMapEventListener(new MapEventListener() {
			@Override
			public void onFingerDown(int x, int y) {
			}

			@Override
			public void onFingerUp(int x, int y) {
			}

			@Override
			public void onDrag(int x, int y) {
			}

			@Override
			public void onDoubleTap(int x, int y) {
			}

			@Override
			public void onTap(int x, int y) {
			}

			@Override
			public void onPinch(int x, int y) {
			}

			@Override
			public void onPinchStart(int x, int y) {
			}

			@Override
			public void onPinchComplete(int x, int y) {
			}

			@Override
			public void onFling(int sx, int sy, int dx, int dy) {
			}

			@Override
			public void onFlingComplete(int x, int y) {
			}

			@Override
			public void onScaleChanged(double scale) {
			}

			@Override
			public void onScrollChanged(int x, int y) {
			}

			@Override
			public void onZoomStart(double scale) {
			}

			@Override
			public void onZoomComplete(double scale) {
			}

			@Override
			public void onZoomLevelChanged(int oldZoom, int currentZoom) {
				mapView.clear();
				setLowresImage();				
			}

			@Override
			public void onRenderStart() {
			}

			@Override
			public void onRenderComplete() {
			}
		});
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == android.R.id.home) {
	        finish();
	        return true;
		} else if (item.getItemId() == R.id.preferences) {
			Intent intent = new Intent(this, Preferences.class);
			startActivity(intent);
			return true;
		} else if (item.getItemId() == R.id.aoumarkers) {
			new GetAOUPoints().execute();
			return true;
		} else if (item.getItemId() == R.id.rknmarkers) {
			if (!DoRKNLocationUpdates) {
				RKNLocationHandler.post(RKNLocationUpdateTask);
				DoRKNLocationUpdates = true;
			} else {
				RKNLocationHandler.removeCallbacks(RKNLocationUpdateTask);
				DoRKNLocationUpdates = false;
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }
    
    public class GetRKNPoints extends AsyncTask<Void, Void, String> {
    	private AccountManager accountManager;
    	private Account[] accounts;

    	@Override    
        protected void onPreExecute() {
    		rk_markers.clear();
    		sl_markers.clear();
    		//rk_markers_from_external.clear();
    		//sl_markers_from_external.clear();
        }

        @Override 
		protected void onPostExecute(String result) {
    		removeMarkers();
			insertMarkers();
			//updateMarkers();
		}

		@Override
		protected String doInBackground(Void... params) {			
			accountManager = AccountManager.get(context);
			accounts = accountManager.getAccountsByType(context.getString(R.string.account_type));

			HttpClient httpclient;
			HttpPost httppost;
	        
	    	HttpResponse response;
	    	HttpEntity entity;
	    	InputStream is;
	    	BufferedReader reader;
	    	StringBuilder sb;
	    	String line;
	    	JSONArray jArray;
	    	JSONObject json_data;
	    	String resultData;
	    	
	    	if (accounts != null && accounts.length > 0) {
		    	try {
			        JSONObject j = new JSONObject();
			        
			        j.put("Username", accounts[0].name);
			        j.put("Password", accountManager.getPassword(accounts[0]));
		    	    Logging.log(APP_TAG, "Account: " + j.toString(1));
			        		        			        
		    		httpclient = new DefaultHttpClient();
			        httppost = new HttpPost(RKNet.getApiMapPath(RKNet.RKNET_MAP_ALLCHARS));
		    	    Logging.log(APP_TAG, "Connection: " + RKNet.getApiMapPath(RKNet.RKNET_MAP_ALLCHARS));
			        
			        httppost.setEntity(new StringEntity(j.toString()));
			        httppost.setHeader("Accept", "application/json");
			        httppost.setHeader("Content-type", "application/json");
			        
			        response = httpclient.execute(httppost);
			        entity = response.getEntity();
			        is = entity.getContent();
			        
			    	try {
			    		reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
		    	        sb = new StringBuilder();
		    	        line = null;
		    	        
		    	        while ((line = reader.readLine()) != null) {
		    	        	sb.append(line + "\n");
		    	        }
		    	        
		    	        is.close();
		    	 
		    	        resultData = sb.toString();
			    	} catch(Exception e) {
			    	    Logging.log(APP_TAG, e.toString());
			    	    resultData = null;
			    	}
		    	} catch(Exception e) {
		    		Logging.log(APP_TAG, e.toString());
		    		resultData = null;
		    	}
	    	
		    	try {
		    		if(resultData != null) {
		    			resultData = resultData.substring(0, resultData.lastIndexOf("}")).replace("{\"d\":", "");
			    		Logging.log(APP_TAG, resultData);
			    				    		
		    			if((!resultData.startsWith("null"))) {
			    			jArray = new JSONArray(resultData);
			    				    			
			    	        for(int i = 0; i < jArray.length(); i++){
			    	        	json_data = jArray.getJSONObject(i);
			    	        	
				        		Integer[] pos = ZoneTools.getRealPosition(
				        				context, 
				        				json_data.getInt("Zone"), 
				        				(int) Math.round(Double.parseDouble(json_data.getString("X"))), 
				        				(int) Math.round(Double.parseDouble(json_data.getString("Y")))
				        			);
			    	        	
			    	        	Marker marker = new Marker();
				                marker.setTitle(json_data.getString("NickName"));
				                marker.setZoneId(json_data.getInt("Zone"));
					        	marker.setX(pos[0]);
					        	marker.setY(pos[1]);
					        	marker.setOnRK(pos[2] == 1);
					        	
				                if (pos[2] == 1) {
				            		rk_markers.add(new MarkerView(createMarkerView(context, marker.getTitle()), marker.getX(), marker.getY()));
				                } else {
				            		sl_markers.add(new MarkerView(createMarkerView(context, marker.getTitle()), marker.getX(), marker.getY()));
				                }
			    	        }
			    		}
		    		}
		    	} catch(JSONException e) {
		    		Logging.log(APP_TAG, "Error parsing data " + e.toString());
		    	}
	    	} else {
	    		Logging.log(APP_TAG, "No accounts!");
	    		DoRKNLocationUpdates = false;
	    	}
	    	
			return null;
		}
    }
    
    public class GetAOUPoints extends AsyncTask<Void, Void, String> {
        @Override    
        protected void onPreExecute() {
    		rk_markers.clear();
    		sl_markers.clear();
        }

        @Override 
		protected void onPostExecute(String result) {
    		removeMarkers();
        	insertMarkers();
		}

		@Override
		protected String doInBackground(Void... params) {			
        	String xml = null;
            Document doc = null;
            
        	try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(Statics.GUIDES_LOCATION_URL);
     
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
            	NodeList nl = doc.getElementsByTagName("waypoint");
	            
	            for (int i = nl.getLength() - 1; i >= 0; i--) {
	                Element e = (Element) nl.item(i);
	        		Integer[] pos = ZoneTools.getRealPosition(context, Integer.parseInt(getValue(e, "pfid")), (int) Math.round(Double.parseDouble(getValue(e, "xcoord"))), (int) Math.round(Double.parseDouble(getValue(e, "ycoord"))));
	                
	                Marker marker = new Marker();
	                marker.setTitle(getValue(e, "coordname"));
	                marker.setZoneId(Integer.parseInt(getValue(e, "pfid")));
		        	marker.setX(pos[0]);
		        	marker.setY(pos[1]);
		        	marker.setOnRK(pos[2] == 1);
	                
	                if (pos[2] == 1) {
	            		rk_markers.add(new MarkerView(createMarkerView(context, marker.getTitle()), marker.getX(), marker.getY()));
	                } else {
	            		sl_markers.add(new MarkerView(createMarkerView(context, marker.getTitle()), marker.getX(), marker.getY()));
	                }
	            }
            }
			
			return null;
		}
    }
    
    private static String getValue(Element item, String str) {
    	NodeList n = item.getElementsByTagName(str);
    	return getElementValue(n.item(0));
    }

    private static final String getElementValue( Node elem ) {
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
	
    private Runnable RKNLocationUpdateTask = new Runnable() {
		public void run() {
			if (!RKNLocationIsUpdating) {
				new GetRKNPoints().execute();
			}

			if (DoRKNLocationUpdates) {
				RKNLocationHandler.postDelayed(this, RKNLocationUpdateTime);
			} else {
				RKNLocationHandler.removeCallbacks(this);
			}
		}
	};
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (mapView != null) 
		{
			mapView.clear();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if (mapView != null) 
		{
			mapView.requestRender();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (mapView != null) 
		{
			mapView.destroy();
			mapView = null;
		}
	}
}
