/*
 * Information.java
 *
 *************************************************************************
 * Copyright 2010 Christofer Engel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rubika.aotalk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.item.ChatMessage;
import com.rubika.aotalk.recipebook.RecipeBook;
import com.rubika.aotalk.service.ClientService;
import com.rubika.aotalk.service.ServiceTools;
import com.rubika.aotalk.util.ItemRef;
import com.rubika.aotalk.util.Logging;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class Information extends SherlockActivity {
	protected static final String APP_TAG    = "--> AnarchyTalk::ShowInfo";
	protected static final String CMD_START = "/start";
	protected static final String CMD_TELL  = "/tell";
	protected static final String CMD_CC    = "/cc";
	
	protected static final String CC_ADD = "addbuddy";
	protected static final String CC_REM = "rembuddy";
		
	private String chatcmd;
	private String target;
	private String method;
	private String message;
	private String resultData;
	private String openurl;
	private String text;
	
	private Intent intent;
	
	private Pattern pattern;
    private Matcher matcher;
	
	private ProgressDialog loader;
	private WebView info;
	
	final Handler resultHandler = new Handler();
	
	final Runnable outputResult = new Runnable() {
        public void run() {
           	updateResultsInUi();
        }
    };
    
    private void updateResultsInUi() {
        text = "";
        
    	if(resultData != null && resultData.length() > 0) {
        	text = ServiceTools.HTML_START + resultData + ServiceTools.HTML_END;
        } else {
        	text = ServiceTools.HTML_START + getString(R.string.no_data).replace("\n", "<br />") + ServiceTools.HTML_END;
        }
            	
    	info.loadData(Uri.encode(text), "text/html", "UTF-8");
    	info.setVisibility(View.VISIBLE);
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
    	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
                
        final ActionBar bar = getSupportActionBar();
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        bindService();
        
        info = (WebView) findViewById(R.id.web);
        info.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        info.setBackgroundColor(0);
        info.setVisibility(View.INVISIBLE);
        
        info.setWebViewClient(new WebViewClient() {  
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    		//Intercept chatcmd links
	    		if(url.startsWith("chatcmd://")) {
					pattern = Pattern.compile("chatcmd://(.*?) .*");
					matcher = pattern.matcher(Uri.decode(url));
			        
					while(matcher.find()) {
			        	chatcmd = matcher.group(1).trim();
			        }
	    	        
	    	        if(chatcmd.equals(CMD_START)) {
						pattern = Pattern.compile("chatcmd://(.*?) (.*)");
						matcher = pattern.matcher(Uri.decode(url));
				        
						while(matcher.find()) {
							openurl = matcher.group(2).trim();
				        }

						intent = new Intent(Intent.ACTION_VIEW, Uri.parse(openurl));
	    	        	startActivity(intent);
	    	        	
	    	        	finish();
	    	        } else if(chatcmd.equals(CMD_TELL)) {
	    	        	pattern = Pattern.compile("chatcmd://(.*?) (.*?) (.*)");
						matcher = pattern.matcher(Uri.decode(url));
				        
						while(matcher.find()) {
				        	target = matcher.group(2).trim();
				        	message = matcher.group(3).trim();
				        }
	    	        	
	    	        	if(target != null && message != null) {
	    	        		ChatMessage chatMessage = new ChatMessage(System.currentTimeMillis(), message, target, "", 0, 0);
	    	        		
	    	        		Message message = Message.obtain(null, ServiceTools.MESSAGE_SEND);
	    		            message.arg1 = 1;
	    		            message.obj = chatMessage;
	    		            message.replyTo = messenger;
	    		            
	    		            try {
								service.send(message);
							} catch (RemoteException e) {
								Logging.log(APP_TAG, e.getMessage());
							}
							
		    	        	finish();
						}
	    	        } else if(chatcmd.equals(CMD_CC)) {
	    	        	pattern = Pattern.compile("chatcmd://(.*?) (.*?) (.*)");
						matcher = pattern.matcher(Uri.decode(url));
				        
						while(matcher.find()) {
							method = matcher.group(2).trim();
							target = matcher.group(3).trim();
				        }
	    	        	
						if(target != null && method != null) {
							Message msg = Message.obtain();
							
							if(method.equals(CC_ADD)) {
						    	msg.what = ServiceTools.MESSAGE_FRIEND_ADD;
							}
							
							if(method.equals(CC_REM)) {
						    	msg.what = ServiceTools.MESSAGE_FRIEND_REMOVE;
							}
							
					        msg.replyTo = messenger;
					        msg.obj = target;
					        
					        try {
								service.send(msg);
							} catch (RemoteException e) {
								Logging.log(APP_TAG, e.getMessage());
							}

							finish();
						}
	    	        } else {
	    		        info.loadData(ServiceTools.HTML_START + getString(R.string.chatcmd_not_implemented) + "<br />'" + chatcmd + "'" + ServiceTools.HTML_END, "text/html", "UTF-8");
	    		    	info.setVisibility(View.VISIBLE);
	    	        }
	    		} else if (url.startsWith("aorbid://")) {
					intent = new Intent(Information.this, RecipeBook.class);
					intent.putExtra("id", url.replace("aorbid://", ""));
					intent.setData(Uri.parse(url));
					startActivity(intent);
	            } else {
					intent = new Intent(Information.this, Information.class);
					intent.setData(Uri.parse(url));
					startActivity(intent);
	    		}
				
				return true;
            }
        });
        
        
        //Show text data in webview
        if(
        	getIntent().getData().toString().startsWith("text://") || 
        	getIntent().getData().toString().startsWith("charref://")
        ) {
	        text = getIntent().getData().toString().trim().replace("\n", "<br />").replaceFirst("text://", "");
	        
	        if(text.startsWith("<br />")) {
	        	text = text.replaceFirst("<br />", "");
	        }
	        
	        text = Uri.decode(text);
	        
	        //Use icons from rubi-ka.com
	        pattern = Pattern.compile("<img src=\'?rdb://([0-9]*?)\'?>");
	        matcher = pattern.matcher(text);
	        
	        while(matcher.find()) {
	        	text = text.replace(
		        	"<img src=rdb://" + matcher.group(1) + ">", 
		        	"<img src=\"http://109.74.0.178/icon/" + matcher.group(1) + ".gif\" class=\"icon\">"
	        	);
	        	
	        	text = text.replace(
			        "<img src='rdb://" + matcher.group(1) + "'>", 
			        "<img src=\"http://109.74.0.178/icon/" + matcher.group(1) + ".gif\" class=\"icon\">"
		        );	
	        }
	        
	        //Remove UI_GFX, don't know how to match them to a file.
	        pattern = Pattern.compile("<img src=\'?tdb://(.*?)\'?>");
	        matcher = pattern.matcher(text);
	        
	        while(matcher.find()) {
	        	text = text.replace(
	        		"<img src=tdb://" + matcher.group(1) + ">", ""
	        	);
	        }
	        
	        //text = text.replace("--------------------------------------------------------------", "------------------------------------------------------------");
	        text = text.replace("--------------------------------------------------------------", "<hr />");
	        
	        Logging.log(APP_TAG, text);
	        
	        info.loadData(ServiceTools.HTML_START + text + ServiceTools.HTML_END, "text/html", "UTF-8");
	    	info.setVisibility(View.VISIBLE);
        }
        
        
        //Load item information
        if(getIntent().getData().toString().startsWith("itemref://")) {
        	String values[] = Uri.decode(getIntent().getData().toString()).replace("itemref://", "").trim().split("/");
        	
        	final String lowid  = values[0];
        	final String itemql = values[2];
        	final ItemRef iref = new ItemRef();
        	
        	loader = new ProgressDialog(this);
	    	loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			loader.setMessage(getString(R.string.loading_data) + getString(R.string.dots));
			loader.show();
			
			new Thread() {
	            public void run() {
	            	resultData = iref.getData(lowid, itemql);
	                resultHandler.post(outputResult);
			        
			        loader.dismiss();
	        	}
			}.start();
        }
        
		if(getIntent().getData().toString().startsWith("user://")) {
	        info.loadData(ServiceTools.HTML_START + getString(R.string.chatcmd_not_implemented) + "<br />'user://'" + ServiceTools.HTML_END, "text/html", "UTF-8");	    			
	    	info.setVisibility(View.VISIBLE);
		}

        ((Button) findViewById(R.id.close)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	bindService();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
        unbindService();
    }
    
    private boolean serviceIsBound = false;
	private Messenger service = null;
	final Messenger messenger = new Messenger(new IncomingHandler());
	
	static class IncomingHandler extends Handler {
		@Override
	    public void handleMessage(Message message) {
	    	switch (message.what) {
	            case ServiceTools.MESSAGE_REGISTERED:
	                break;
	            default:
	                super.handleMessage(message);
	        }
	    }
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder ibinder) {
	        service = new Messenger(ibinder);

	        try {
	            Message message = Message.obtain(null, ServiceTools.MESSAGE_CLIENT_REGISTER);
	            message.replyTo = messenger;
	            service.send(message);
	        } catch (RemoteException e) {
				Logging.log(APP_TAG, e.getMessage());
	        }
	    }

	    public void onServiceDisconnected(ComponentName className) {
	    	service = null;
	    }
	};
    
	private void bindService() {
		if (!serviceIsBound) {
	    	Intent serviceIntent = new Intent(this, ClientService.class);
			bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		    serviceIsBound = true;
		    
		    startService(serviceIntent);
	    }
	}

	private void unbindService() {
		if (serviceIsBound) {
	        if (service != null) {
	            try {
	                Message msg = Message.obtain(null, ServiceTools.MESSAGE_CLIENT_UNREGISTER);
	                msg.replyTo = messenger;
	                service.send(msg);
	            } catch (RemoteException e) {
					Logging.log(APP_TAG, e.getMessage());
	            }
	        }

	        unbindService(serviceConnection);
	        serviceIsBound = false;
	    }
	}
}
