package com.rubika.aotalk;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent; 
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.google.android.gcm.GCMBaseIntentService;
import com.rubika.aotalk.R;
import com.rubika.aotalk.item.RKNAccount;
import com.rubika.aotalk.map.Map;
import com.rubika.aotalk.service.ClientService;
import com.rubika.aotalk.towerwars.Towerwars;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.RKNet;


public class GCMIntentService extends GCMBaseIntentService {
	private static final String APP_TAG = "--> The Leet :: GCMIntentService";
	private long[] pattern = { 0, 200, 500, 100, 100, 300 };
	private String registrationId = "";
	private Context context;
	
	@Override
	protected void onMessage(Context context, Intent intent) {
		int requestID = (int) System.currentTimeMillis();
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        Logging.log(TAG, "Message received");
		Logging.log(TAG, intent.getExtras().getString("message") + ", " + intent.getExtras().getString("zone") + ", " + intent.getExtras().getString("x") + ", " + intent.getExtras().getString("y"));
		
		if (intent.getExtras().getString("zone") != null && !intent.getExtras().getString("zone").equals("")) {
			if (settings.getBoolean("towerNotificationEnabled", true)) {
				Intent i = new Intent(context, Map.class);
				i.putExtra("name", intent.getExtras().getString("message"));
				i.putExtra("zone", intent.getExtras().getString("zone"));
				i.putExtra("x", Integer.parseInt(intent.getExtras().getString("x")));
				i.putExtra("y", Integer.parseInt(intent.getExtras().getString("y")));
				
				PendingIntent towerIntent = PendingIntent.getActivity(getApplicationContext(), 1, new Intent(context, Towerwars.class), Intent.FLAG_ACTIVITY_CLEAR_TOP);
				PendingIntent leetIntent = PendingIntent.getActivity(getApplicationContext(), 2, new Intent(context, AOTalk.class), Intent.FLAG_ACTIVITY_CLEAR_TOP);
				PendingIntent mapIntent = PendingIntent.getActivity(getApplicationContext(), requestID, i, PendingIntent.FLAG_UPDATE_CURRENT);
		
				NotificationCompat.Builder builder = new Builder(this);
				builder.setSmallIcon(R.drawable.ic_towerwars);
				builder.setLargeIcon(((BitmapDrawable)getResources().getDrawable(R.drawable.ic_notification_old)).getBitmap());
				builder.setTicker(intent.getExtras().getString("tickerText"));
				builder.setContentTitle(intent.getExtras().getString("contentTitle"));
				builder.setContentText(intent.getExtras().getString("message"));
				builder.setAutoCancel(true);
				if (settings.getBoolean("towerNotificationLed", true)) {
					builder.setLights(0xffff0000, 300, 300);
				}
				if (settings.getBoolean("towerNotificationVibrateEnabled", true)) {
					builder.setVibrate(pattern);
				} else {
					builder.setVibrate(null);
				}
				builder.setSound(Uri.parse(settings.getString("towerNotificationSound","android.resource://com.rubika.aotalk/raw/rollerrat")));
				builder.setContentIntent(towerIntent);
				builder.setOngoing(false);
				builder.addAction(R.drawable.icon_bightness, "The Leet", leetIntent);
				builder.addAction(R.drawable.icon_location, "Show on map", mapIntent);
				builder.setUsesChronometer(true);
				builder.setPriority(NotificationCompat.PRIORITY_HIGH);
				
				NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				manager.notify(ClientService.NOTIFICATION_TW, builder.build());
			}
		} else {
			NotificationCompat.Builder builder = new Builder(this);
			builder.setTicker(intent.getExtras().getString("tickerText"));
			builder.setContentTitle(intent.getExtras().getString("contentTitle"));
			builder.setContentText(intent.getExtras().getString("message"));
			builder.setAutoCancel(true);
			builder.setOngoing(false);
			builder.setLights(0xffffff00, 200, 500);
			
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(ClientService.NOTIFICATION_TW, builder.build());
		}
	}

	@Override
	protected void onError(Context context, String errorId) {
		Logging.log(TAG, "onError: " + errorId);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Logging.log(TAG, "onRegistered: " + registrationId);
		
		this.registrationId = registrationId;
		this.context = context;

		new AddRegistrationID().execute();
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Logging.log(TAG, "onUnregistered: " + registrationId);
		
		this.registrationId = registrationId;
		this.context = context;
		
		new RemoveRegistrationID().execute();
	}
	
    public class RemoveRegistrationID extends AsyncTask<Void, Void, String> {
        @Override    
        protected void onPreExecute() {
        }

        @Override 
		protected void onPostExecute(String result) {
	     }

		@Override
		protected String doInBackground(Void... params) {			
			HttpClient httpclient;
			HttpPost httppost;
	        
	    	HttpResponse response;
	    	HttpEntity entity;
	    	InputStream is;
	    	BufferedReader reader;
	    	StringBuilder sb;
	    	String line;
	    	String resultData;
	    	
			try {
				httpclient = new DefaultHttpClient();
		        httppost = new HttpPost(RKNet.getApiAccountPath(RKNet.RKNET_ACCOUNT_DELKEYS));
	
		        JSONObject j = new JSONObject();
		        
		        j.put("Key", registrationId);
		        			        
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
		    	} catch(Exception e){
		    	    Logging.log(APP_TAG, "Error converting result " + e.toString());
		    	    resultData = null;
		    	}
	    	} catch(Exception e){
	    		Logging.log(APP_TAG, "Error in http connection " + e.toString());
	    		resultData = null;
	    	}
			
			Logging.log(APP_TAG, resultData);
	    	
	    	return null;
		}
    }
    
    public class AddRegistrationID extends AsyncTask<Void, Void, String> {
    	private AccountManager accountManager;
    	private android.accounts.Account[] accounts;
    	private RKNAccount rknetaccount = null;

        @Override    
        protected void onPreExecute() {
        }

        @Override 
		protected void onPostExecute(String result) {
	     }

		@Override
		protected String doInBackground(Void... params) {			
			HttpClient httpclient;
			HttpPost httppost;
	        
	    	HttpResponse response;
	    	HttpEntity entity;
	    	InputStream is;
	    	BufferedReader reader;
	    	StringBuilder sb;
	    	String line;
	    	String resultData;
	    	JSONObject json_data;
	    	
	        accountManager = AccountManager.get(context);
			accounts = accountManager.getAccountsByType(context.getString(R.string.account_type));
	    		    	
			if (accounts.length > 0) {
				try {
		    		httpclient = new DefaultHttpClient();
			        httppost = new HttpPost(RKNet.getApiAccountPath(RKNet.RKNET_ACCOUNT_LOGIN));
	
			        JSONObject j = new JSONObject();
			        
			        j.put("Username", accounts[0].name);
			        j.put("Password", accountManager.getPassword(accounts[0]));
			        			        
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
			    	} catch(Exception e){
			    	    Logging.log(APP_TAG, "Error converting result " + e.toString());
			    	    resultData = null;
			    	}
		    	} catch(Exception e){
		    		Logging.log(APP_TAG, "Error in http connection " + e.toString());
		    		resultData = null;
		    	}
	
		    	try {
		    		if(resultData != null) {
		    			resultData = resultData.substring(0, resultData.lastIndexOf("}")).replace("{\"d\":", "");
			    		
		    			if((!resultData.startsWith("null"))) {
		    				json_data = new JSONObject(resultData);
		    				
		    				rknetaccount = new RKNAccount(
		                		json_data.getInt("Id"),
		                		json_data.getString("Username"),
		                		json_data.getString("Password")
			                );
		    				
		    				JSONArray registrations = json_data.getJSONArray("Registrations");
		    				
		    				boolean isRegistered = false;
		    				
		    				for(int i = 0; i < registrations.length(); i++){
		    					 JSONObject reg = registrations.getJSONObject(i);
		    					 if (reg.getString("Key").equals(AOTalk.getGCMRegistrationId())) {
		    						 isRegistered = true;
		    					 }
		    				}
		    				
		    				if (!isRegistered && registrationId != null && !registrationId.equals("")) {
		    					Logging.log(APP_TAG, "Device not registered");
		    					
		    		    		try{
		    			    		httpclient = new DefaultHttpClient();
		    				        httppost = new HttpPost(RKNet.getApiAccountPath(RKNet.RKNET_ACCOUNT_SETKEYS));
		    		
		    				        JSONObject j = new JSONObject();
		    				        j.put("AccountId", rknetaccount.getAccountId());
		    				        j.put("Key", registrationId);
		    				        j.put("UUID", AOTalk.getDeviceIdentifier());
		    				        
		    				        Logging.log(APP_TAG, j.toString(1));

		    				        httppost.setEntity(new StringEntity(j.toString()));
		    				        httppost.setHeader("Accept", "application/json");
		    				        httppost.setHeader("Content-type", "application/json");
		    				        
		    				        response = httpclient.execute(httppost);
		    				        entity = response.getEntity();
		    				        is = entity.getContent();
		    				        
		    				    	try{
		    				    		reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
		    			    	        sb = new StringBuilder();
		    			    	        line = null;
		    			    	        
		    			    	        while ((line = reader.readLine()) != null) {
		    			    	        	sb.append(line + "\n");
		    			    	        }
		    			    	        
		    			    	        is.close();
		    			    	 
		    			    	        resultData = sb.toString();
		    				    	} catch(Exception e){
		    				    	    Logging.log(APP_TAG, "Error converting result " + e.toString());
		    				    	    resultData = null;
		    				    	}
		    			    	} catch(Exception e){
		    			    		Logging.log(APP_TAG, "Error in http connection " + e.toString());
		    			    		resultData = null;
		    			    	}
		    				} else {
		    					Logging.log(APP_TAG, "Device already registered");
		    				}
			    		}
		    		}
		    	} catch(JSONException e){
		    		Logging.log(APP_TAG, "Error parsing data " + e.toString());
		    	}
			}
			
			return null;
		}
	};
}
