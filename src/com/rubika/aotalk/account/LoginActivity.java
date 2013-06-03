package com.rubika.aotalk.account;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.analytics.tracking.android.EasyTracker;
import com.rubika.aotalk.R;
import com.rubika.aotalk.item.RKNAccount;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.RKNet;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AccountAuthenticatorActivity {
	private static final String APP_TAG = "--> The Leet :: LoginActivity";

	private EditText mUsername;
	private EditText mPassword;
	private Button mLoginButton;
	private static Context context;
	private RKNAccount rknetaccount = null;

	private AccountManager accountManager;
	private Account[] accounts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//super.setTheme(R.style.Theme_AOTalkTheme_Light);
		
        accountManager = AccountManager.get(this);
		accounts = accountManager.getAccountsByType(getString(R.string.account_type));
		
		Logging.log(APP_TAG, "Found " + accounts.length + " accounts");
		
		if (accounts == null || accounts.length == 0) {
			setContentView(R.layout.account_login);
			context = this;
			
			mUsername = (EditText) findViewById(R.id.username);
			mPassword = (EditText) findViewById(R.id.password);
	
			mLoginButton = (Button) findViewById(R.id.login);
			mLoginButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String user = mUsername.getText().toString().trim().toLowerCase(Locale.getDefault());
					String password = mPassword.getText().toString().trim().toLowerCase(Locale.getDefault());
		
					if (user.length() > 0 && password.length() > 0) {
						LoginTask t = new LoginTask(LoginActivity.this);
						t.execute(user, password);
					}
				}
			});
			
			TextView register = (TextView) findViewById(R.id.register);
			register.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(context, RegisterActivity.class);
					startActivity(intent);
				}
			});
		} else {
			setContentView(R.layout.account_max);
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

	private class LoginTask extends AsyncTask<String, Void, Boolean> {
		Context mContext;
		ProgressDialog mDialog;

		LoginTask(Context c) {
			mContext = c;
			mLoginButton.setEnabled(false);

			mDialog = ProgressDialog.show(c, "", getString(R.string.authenticating), true, false);
			mDialog.setCancelable(true);
		}

		@Override
		public Boolean doInBackground(String... params) {
			String user = params[0];
			String pass = params[1];

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

	    	try {
				try {
		    		httpclient = new DefaultHttpClient();
			        httppost = new HttpPost(RKNet.getApiAccountPath(RKNet.RKNET_ACCOUNT_LOGIN));
	
			        JSONObject j = new JSONObject();
			        
			        j.put("Username", user);
			        j.put("Password", pass);
			        			        
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
			    		Logging.log(APP_TAG, resultData);
			    		
		    			if((!resultData.startsWith("null"))) {
		    				json_data = new JSONObject(resultData);
		    				
		    				rknetaccount = new RKNAccount(
		                		json_data.getInt("Id"),
		                		json_data.getString("Username"),
		                		json_data.getString("Password")
			                );
			    		}
		    		}
		    	} catch(JSONException e){
		    		Logging.log(APP_TAG, "Error parsing data " + e.toString());
		    	}
			} catch (Exception e) {
				e.printStackTrace();
			}

	    	if (rknetaccount != null && rknetaccount.getAccountId() > 0) {
				Bundle result = null;
				
				Account account = new Account(user, mContext.getString(R.string.account_type));
				AccountManager am = AccountManager.get(mContext);
				
				if (am.addAccountExplicitly(account, pass, null)) {
					result = new Bundle();
					result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
					result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
					
					setAccountAuthenticatorResult(result);
					
					return true;
				} else {
					return false;
				}
	    	} else {
	    		resultHandler.post(resultRunnable);
	    		return false;
	    	}
		}

		@Override
		public void onPostExecute(Boolean result) {
			mLoginButton.setEnabled(true);
			mDialog.dismiss();
			if (result)
				finish();
		}
	}
	
	private static Handler resultHandler = new Handler();
	private static Runnable resultRunnable = new Runnable(){
	    public void run() 
	    {
    		Logging.toast(context, context.getString(R.string.login_error));
	    }
	};
}
