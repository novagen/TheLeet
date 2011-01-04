/*
 * ShowInfo.java
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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import ao.misc.AONameFormat;

public class ShowInfo extends Activity {
	protected static final String APPTAG    = "--> AOTalk::ShowInfo";
	protected static final String CMD_START = "/start";
	protected static final String CMD_TELL  = "/tell";
	protected static final String CMD_CC    = "/cc";
	
	protected static final String CC_ADD = "addbuddy";
	protected static final String CC_REM = "rembuddy";

	private ServiceConnection conn;
	private AOBotService bot;
	
	private String chatcmd;
	private String target;
	private String method;
	private String message;
	private String resultData;
	
	private ProgressDialog loader;
	private TextView info;
	
	final Handler resultHandler = new Handler();
	
	final Runnable outputResult = new Runnable() {
        public void run() {
           	updateResultsInUi();
           	Log.d(APPTAG, "Thread finished, outputting result");
        }
    };
    
    private void updateResultsInUi() {
        if(resultData != null) {
        	if(resultData.length() > 0) {
        		ShowInfo.this.info.setText(Html.fromHtml(resultData, imageLoader, null));
        	} else {
        		ShowInfo.this.info.setText(getString(R.string.no_data));
        	}
        } else {
        	ShowInfo.this.info.setText(getString(R.string.error_data));
        	Log.d(APPTAG, "ResultData IS NULL");
        }
    }
    
	static ImageGetter imageLoader = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
        	Cache cache = new Cache();
        	Drawable drawable = cache.getIcon(source, 48, 48);
        	
        	return drawable;
        }
	};
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showinfo);
        
        info = (TextView) findViewById(R.id.showinfo);
        attachToService();
                
        if(
        	getIntent().getData().toString().startsWith("text://") || 
        	getIntent().getData().toString().startsWith("charref://")
        ) {
	        String text = getIntent().getData().toString().replace("\n", "<br />").replaceFirst("text://", "");
	                
	        //Removes all images until cache has been coded, takes too long to load them every time
	        Pattern pattern = Pattern.compile("<img src=\'?rdb://([0-9]*?)\'?>");
	        Matcher matcher = pattern.matcher(text);
	        while(matcher.find()) {
	        	text = text.replace(
		        	"<img src=rdb://" + matcher.group(1) + ">", ""
	        		//"rdb://" + matcher.group(1), ""
	        		//"http://www.rubi-ka.com/image/icon/" + matcher.group(1) + ".gif"
	        	);
	        	text = text.replace(
		        	"<img src='rdb://" + matcher.group(1) + "'>", ""
		        );
	        }
	        
	        pattern = Pattern.compile("<img src=\'?tdb://(.*?)\'?>");
	        matcher = pattern.matcher(text);
	        while(matcher.find()) {
	        	text = text.replace("<img src=tdb://" + matcher.group(1) + ">","");
	        }	        
	        
	        info.setText(Html.fromHtml(text, imageLoader, null));
	        info.setMovementMethod(LinkMovementMethod.getInstance());
        }
        
        if(getIntent().getData().toString().startsWith("chatcmd://")) {
	        String command = getIntent().getData().toString().replace("chatcmd://", "");
	        chatcmd = command.substring(0, command.indexOf(" ")).trim();
	        
	        if(chatcmd.equals(CMD_START)) {
	        	String url = command.replace(chatcmd, "").trim();
	        	Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	        	startActivity(i);
	        	
	        	this.finish();
	        } else if(chatcmd.equals(CMD_TELL)) {
	        	target  = command.replace(chatcmd, "").trim().substring(0, command.trim().indexOf(" ") + 1).trim();
	        	message = command.replace(chatcmd, "").trim().replace(target, "").trim();
	        } else if(chatcmd.equals(CMD_CC)) {
	        	String[] temp = command.replace(chatcmd, "").trim().split(" ");
	        	method = temp[0].trim();
	        	target = temp[1].trim();
	        } else {
		        info.setText("this chatcmd is not implemented yet");
		        info.append("\n'" + chatcmd + "'");
	        }
        }
        
        if(getIntent().getData().toString().startsWith("itemref://")) {
        	String values[] = getIntent().getData().toString().replace("itemref://", "").trim().split("/");
        	
        	final String lowid  = values[0];
        	final String itemql = values[2];
        	
        	final ItemRef iref = new ItemRef();
        	
        	Log.d(APPTAG, "DATA : " + itemql + ", " + lowid);
        	
        	loader = new ProgressDialog(this);
	    	loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    	loader.setTitle(getResources().getString(R.string.loading_data));
			loader.setMessage(getResources().getString(R.string.please_wait));
			loader.show();
			
			new Thread() {
	            public void run() {
	            	ShowInfo.this.resultData = iref.getData(lowid, itemql);
	                resultHandler.post(outputResult);
			        
			        ShowInfo.this.loader.dismiss();
	        	}
			}.start();
        }
	}
    
	private void attachToService() {
		Intent serviceIntent = new Intent(this, AOBotService.class);
	    
	    conn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				ShowInfo.this.bot = ((AOBotService.ListenBinder) service).getService();
				
				if(chatcmd != null) {
					if(chatcmd.equals(CMD_TELL) && target != null && message != null) {
						bot.sendTell(target, message, true, true);
						Log.d(APPTAG, "Sent /tell " + target + " " + message);
						
						finish();
					}
					
					
					if(chatcmd.equals(CMD_CC) && target != null && method != null) {
						Log.d(APPTAG, "CHATCMD : " + chatcmd + ", METHOD : " + method + ", TARGET : " + target);

						if(!AONameFormat.format(target).equals(AONameFormat.format(ShowInfo.this.bot.getCurrentCharacter()))) {
							ChatParser cp = new ChatParser();
							
							if(method.equals(CC_ADD)) {
								//add a friend
								Log.d(APPTAG, "Added a buddy: " + target);
								bot.addFriend(target);
								bot.appendToLog(
									cp.parse(target + " added to your buddy list", ChatParser.TYPE_SYSTEM_MESSAGE),
									null,
									null,
									ChatParser.TYPE_SYSTEM_MESSAGE
								);
							}
							
							if(method.equals(CC_REM)) {
								//remove a friend
								Log.d(APPTAG, "Removed a buddy: " + target);
								bot.removeFriend(target);
								bot.appendToLog(
									cp.parse(target + " removed from your buddy list", ChatParser.TYPE_SYSTEM_MESSAGE),
									null,
									null,
									ChatParser.TYPE_SYSTEM_MESSAGE
								);
							}
						}

						finish();
					}
				}
			}
		
			@Override
			public void onServiceDisconnected(ComponentName name) {
				ShowInfo.this.bot = null;
			}
	    };

	    this.getApplicationContext().startService(serviceIntent);
	    this.getApplicationContext().bindService(serviceIntent, conn, 0);
	}
    
    @Override
    protected void onPause(){
    	super.onPause();
    	finish();
	} 
    
    @Override
    protected void onStop(){
    	super.onStop();
	}
}
