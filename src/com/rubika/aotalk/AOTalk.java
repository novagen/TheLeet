/*
 * AOTalk.java
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

import ao.misc.AONameFormat;
import ao.protocol.AOBot;
import ao.protocol.AODimensionAddress;
import ao.protocol.packets.bi.AOPrivateGroupInvitePacket;
import ao.protocol.packets.in.AOCharListPacket;

public class AOTalk extends Activity {
	protected static final String APPTAG = "--> AOTalk";

	private BroadcastReceiver messageReceiver    = new AOBotMessageReceiver();
	private BroadcastReceiver connectionReceiver = new AOBotConnectionReceiver();

	private ServiceConnection conn;
	private AOBotService bot;
	private ChatParser chat;
	
	private String PASSWORD  = "";
	private String USERNAME  = "";
	private boolean SAVEPREF = false;
	private boolean FULLSCRN = false;
	
	private final String CHANNEL_MSG = "Private Message";
	private final String CHANNEL_FRIEND = "Friend";
	private String CHATCHANNEL = "";
	private String MESSAGETO   = "";
	private String LASTMESSAGE = "";
	
	private Button channelbutton;
	private EditText msginput;
	private Context context;
	private ProgressDialog loader;
	
	private List<String> predefinedText;
	private List<String> groupDisable;
	//private List<String> watchEnable;
	private List<ChatMessage> messages;

	private ListView messagelist;
	private ChatMessageAdapter msgadapter;
	
	private boolean welcome = true;
	
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
			
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
        
        //Load values that are saved from last time the app was used
        SAVEPREF = settings.getBoolean("savepref", SAVEPREF);
        USERNAME = settings.getString("username", USERNAME);
        PASSWORD = settings.getString("password", PASSWORD);
        CHATCHANNEL = settings.getString("chatchannel", CHATCHANNEL);
        MESSAGETO = settings.getString("messageto", MESSAGETO);
        FULLSCRN = settings.getBoolean("fullscreen", FULLSCRN);
        
        /* Not in use as listview bugs in fullscreen
        if(AOTalk.this.FULLSCRN) {
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);	
        } else {
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);	
        }
        */
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        context = this;
        
        //Predefined text string, user can chose between them when long pressing input field
        predefinedText = new ArrayList<String>();
        predefinedText.add("!afk ");
        predefinedText.add("!online");
        predefinedText.add("!join");
        predefinedText.add("!leave");
        predefinedText.add("!items ");

        chat = new ChatParser();
        
        groupDisable = new ArrayList<String>();
        //watchEnable = new ArrayList<String>();
        messages = new ArrayList<ChatMessage>();
        
        messagelist = (ListView)findViewById(R.id.messagelist);
        messagelist.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        
        //Disable automatic pop up of keyboard at launch
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        msgadapter = new ChatMessageAdapter(this, messages);
        messagelist.setAdapter(msgadapter);
        messagelist.setFocusable(true);
        messagelist.setFocusableInTouchMode(true);
        messagelist.setItemsCanFocus(true);
        
        messagelist.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
		    	ChatMessage message = messages.get(pos);
		    	String tmpToCharacter  = "";
		    	String tmpToChannel    = "";
		    	String tmpCharacter    = "";
		    	String tmpChannel      = "";
		    	String tmpLeaveChannel = "";
		    	
		     	int countArrayAlts = 0;
		    	
		    	if(message.getChannel() != null) {
			     	if(message.getChannel().startsWith(AOBotService.PRIVATE_GROUP_PREFIX)) {
			     		countArrayAlts++;
			     	}
		    		countArrayAlts++;
		    	}
		     	
		     	if(message.getCharacter() != null) {
		     		countArrayAlts++;
		    	}
		     	
		     	CharSequence tmpOptions[] = new CharSequence[countArrayAlts];
		     	
		     	int countMenuAlts = 0;

		    	if(message.getChannel() != null && !AOTalk.this.bot.getGroupIgnoreList().contains(message.getChannel())) {
			     	if(message.getChannel().startsWith(AOBotService.PRIVATE_GROUP_PREFIX)) {
			     		tmpLeaveChannel = "Leave " + message.getChannel();
			     		tmpOptions[countMenuAlts] = tmpLeaveChannel;
			    		tmpChannel = message.getChannel();
			     		countMenuAlts++;
			     	}
			     	
			     	tmpToChannel = AOTalk.this.getString(R.string.group_message_to) + " " + message.getChannel();
		    		tmpOptions[countMenuAlts] = tmpToChannel;
		    		tmpChannel = message.getChannel();
		    		countMenuAlts++;
		    	}
		     	
		     	if(message.getCharacter() != null) {
		    		tmpToCharacter = AOTalk.this.getString(R.string.private_message_to) + " " + message.getCharacter();
		    		tmpOptions[countMenuAlts] = tmpToCharacter;
		    		tmpCharacter = message.getCharacter();
		    		countMenuAlts++;
		    	}
		    	
		    	if(countMenuAlts > 0) {
			    	final CharSequence options[] = tmpOptions;
			    	final String toCharacter = tmpToCharacter;
			    	final String toChannel = tmpToChannel;
			    	final String character = tmpCharacter;
			    	final String channel = tmpChannel;
			    	final String leaveChannel = tmpLeaveChannel;
			   	
			    	AlertDialog.Builder builder = new AlertDialog.Builder(AOTalk.this);
			    	builder.setTitle("Message options");
			    	builder.setItems(options, new DialogInterface.OnClickListener() {
			    	    public void onClick(DialogInterface dialog, int item) {
			    	    	if(options[item].toString().equals(leaveChannel)) {
			    	    		AOTalk.this.bot.leaveGroup(channel.replace(AOBotService.PRIVATE_GROUP_PREFIX, ""));
			    	    		AOTalk.this.CHATCHANNEL = "";
			    	    		AOTalk.this.MESSAGETO = "";
			    	    		Log.d(APPTAG, "Leaving group...");
			    	    		setButtonText();
			    	    	} else if(options[item].toString().equals(toCharacter)) {
			    	    		AOTalk.this.CHATCHANNEL = AOTalk.this.CHANNEL_MSG;
			    	    		AOTalk.this.MESSAGETO = character;
			    	    		setButtonText();
			    	    	} else if(options[item].toString().equals(toChannel)) {
			    	    		AOTalk.this.CHATCHANNEL = channel;
			    	    		setButtonText();
			    	    	}
			    	    }
			    	});
			    	
			    	AlertDialog optionlist = builder.create();
			    	optionlist.show();
			    	
			    	return true;
		    	} else {
		    		return false;
		    	}
			}
        });
        
        messages.add(new ChatMessage(
        		new Date().getTime(),
        		chat.parse("<br /><b>" + getString(R.string.welcome) + "</b>" + getString(R.string.about),
        		ChatParser.TYPE_PLAIN_MESSAGE), 
        		null, 
        		null
        ));
               
        channelbutton = (Button) findViewById(R.id.msgchannel);
        channelbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setChannel();
			}
		});
        
        setButtonText();
		
        msginput = (EditText) findViewById(R.id.msginput);
        msginput.setOnKeyListener(new OnKeyListener() {
			@Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					if(AOTalk.this.bot.getState() == ao.protocol.AOBot.State.LOGGED_IN && msginput.getText().toString().length() > 0) {				
                		//Send message
                		if(CHATCHANNEL.equals(CHANNEL_MSG)) {
                			AOTalk.this.bot.sendTell(AOTalk.this.MESSAGETO, msginput.getText().toString(), true, true);
                			getMessages();
                			
							Log.d(APPTAG, "Sent private message to " + MESSAGETO + ": " + msginput.getText().toString());
                		} else { //Send to group
                			if(CHATCHANNEL.startsWith(AOBotService.PRIVATE_GROUP_PREFIX)) {
                				AOTalk.this.bot.sendPGMsg(AOTalk.this.CHATCHANNEL.replace(AOBotService.PRIVATE_GROUP_PREFIX, ""), msginput.getText().toString());
                				Log.d(APPTAG, "Sent private group message to " + 
                						CHATCHANNEL.replace(AOBotService.PRIVATE_GROUP_PREFIX, "") + 
                						": " + msginput.getText().toString()
                				);
                				
                			} else {
                				AOTalk.this.bot.sendGMsg(AOTalk.this.CHATCHANNEL, msginput.getText().toString());
                				Log.d(APPTAG, "Sent group message to " + CHATCHANNEL + ": " + msginput.getText().toString());
                			}
                		}
                		
                		AOTalk.this.LASTMESSAGE = AOTalk.this.msginput.getText().toString();
                		AOTalk.this.msginput.setText("");

	                	return true;
					} else {
						Log.d(APPTAG, "Not logged in or no message, can't send message");
                	}
                }
				
                return false;
            }
        });
        
        //Long click on input lets user select from last message and predefined texts
        msginput.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				showPredefinedText();
				return true;
			}
		});
        
        //Connect to the bot service
        attachToService();
    }
    
    
    //Change the text on the channel button
    private void setButtonText() {
		if(AOTalk.this.CHATCHANNEL != "") {
			if(AOTalk.this.CHATCHANNEL.equals(AOTalk.this.CHANNEL_MSG)) {
				if(!AOTalk.this.MESSAGETO.equals("")) {
					AOTalk.this.channelbutton.setText(
						AOTalk.this.getString(R.string.tell) + ": " + AONameFormat.format(AOTalk.this.MESSAGETO)
					);
				} else {
					AOTalk.this.channelbutton.setText(AOTalk.this.getString(R.string.select_channel));
				}
	    	} else {
	    		AOTalk.this.channelbutton.setText(CHATCHANNEL);
	    	}
		} else {
			AOTalk.this.channelbutton.setText(AOTalk.this.getString(R.string.select_channel));
		}   	
    }

    
    //Displays a list of predefined text (and the last message user made) when long pressing the input field
    private void showPredefinedText() {
    	CharSequence tempTexts[] = null;
    	int adder = 0;
    	
    	if(predefinedText != null) {
 	    	if((!predefinedText.contains(AOTalk.this.LASTMESSAGE) && (!AOTalk.this.LASTMESSAGE.equals("")))) {
	    		adder = 1;
	    	} 
 	    	
 	    	tempTexts = new CharSequence[predefinedText.size() + adder];
 	    	
	    	for(int i = 0; i < predefinedText.size() + adder; i++) {
	    		if(i == 0 && adder > 0) {
	    			tempTexts[i] = AOTalk.this.LASTMESSAGE;
	    		} else {
	    			tempTexts[i] = predefinedText.get(i - adder);
	    		}
	    	}
    	}
     	
    	final CharSequence[] texts = tempTexts;

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(AOTalk.this.getString(R.string.select_message));
    	builder.setItems(texts, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	    	AOTalk.this.msginput.setText(texts[item]);
    	    	
    	    	//Move cursor to the end of the text
    	    	Editable etext = AOTalk.this.msginput.getText();
    	    	int position = etext.length();
    	    	Selection.setSelection(etext, position);
    	    	
    	    	//Force soft keyboard to show after selecting a predefined text
    	    	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	    	mgr.showSoftInput(AOTalk.this.msginput, InputMethodManager.SHOW_IMPLICIT);
    	    }
    	});
    	
    	AlertDialog textlist = builder.create();
    	textlist.show();
    }

    
    //Show a pop up when a new invitation is received
    private void handleInvitation() {
    	final AOPrivateGroupInvitePacket invitation = AOTalk.this.bot.getInvitation();
    	
    	AlertDialog joinGroupDialog = new AlertDialog.Builder(AOTalk.this).create();
    	joinGroupDialog.setTitle(AOTalk.this.bot.getCharTable().getName(invitation.getGroupID()));
    	joinGroupDialog.setMessage(AOTalk.this.getString(R.string.join_group));
    		            
    	joinGroupDialog.setButton(AOTalk.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		    	AOTalk.this.bot.acceptInvitation(invitation.getGroupID());
				return;
			} 
		});
		
    	joinGroupDialog.setButton2(AOTalk.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				AOTalk.this.bot.rejectInvitation(invitation.getGroupID());
				return;
			}
		}); 
    	
    	joinGroupDialog.show();   	
    }
    
    //Load last messages from the bot service
    private void getMessages() {
    	if(AOTalk.this.bot.getMessagesSize() > 0) {
    		if(AOTalk.this.welcome) {
    			AOTalk.this.messages.clear();
    			AOTalk.this.welcome = false;
    		}
    		
    		List<ChatMessage> temp = AOTalk.this.bot.getLastMessages(AOTalk.this.messages.size());
    		
    		if(temp != null) {
	    		for(int i = 0; i < temp.size(); i++) {
	    			AOTalk.this.messages.add(temp.get(i));
	    			AOTalk.this.msgadapter.notifyDataSetChanged();
	    		}
    		}
    	}
    }
    
    
    //Let user select server during the connection
	private void setServer() {
    	final CharSequence servers[] = {"Atlantean", "Rimor", "TestLive"};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(AOTalk.this.getString(R.string.select_server));
    	builder.setItems(servers, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	    	loader = new ProgressDialog(context);
		    	loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		    	loader.setTitle(getResources().getString(R.string.connecting));
				loader.setMessage(getResources().getString(R.string.please_wait));
				loader.show();
    	    	
    	    	if(servers[item].toString().equals("Atlantean")) {
    	    		new Thread() {
    		            public void run() {
    		            	AOTalk.this.bot.setServer(AODimensionAddress.RK1);
    		        	}
    				}.start();
    	    	} else if(servers[item].toString().equals("Rimor")) {
    	    		new Thread() {
    		            public void run() {
    		            	AOTalk.this.bot.setServer(AODimensionAddress.RK2);
    		        	}
    				}.start();
    	    	} else {
    	    		new Thread() {
    		            public void run() {
    		            	AOTalk.this.bot.setServer(AODimensionAddress.TEST);
    		        	}
    				}.start();
    	    	}
    	    }
    	});
    	
    	AlertDialog serverlist = builder.create();
    	serverlist.show();
    }
    
	
	//Lets the user select character during connection
	private void setCharacter() {
    	final AOCharListPacket charpacket = bot.getCharPacket();

    	if(charpacket != null) {
	    	CharSequence names[] = new CharSequence[charpacket.getNumCharacters()];
	    	
    		for(int i = 0; i < charpacket.getNumCharacters(); i++) {
    			names[i] = charpacket.getCharacter(i).getName();
	    	}

	    	final CharSequence[] charlist = names;
	
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setTitle(AOTalk.this.getString(R.string.select_character));
	    	builder.setItems(charlist, new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int item) {
	    	    	AOTalk.this.bot.setCharacter(charpacket.findCharacter(AONameFormat.format(charlist[item].toString())));
	    	    }
	    	});
	    	
	    	builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					AOTalk.this.bot.disconnect();
				}}
	    	);
	    	
	    	AlertDialog characters = builder.create();	    	
	    	characters.show();
    	} else {
    		Log.d(APPTAG, "Character packet is NULL");
    	}
    }
    
	
    private void setChannel() {
    	CharSequence[] tempChannels = null;
    	List<String> groupList = AOTalk.this.bot.getGroupList();
    	int add = 0;
    	int chn = 0;
    	int cnt = 0;
    	
    	if(AOTalk.this.bot.getState().equals(AOBot.State.LOGGED_IN)) {
    		add++;
    	}
    	
    	if(!AOTalk.this.bot.getOnlineFriends().isEmpty()) {
    		add++;
    	}
    	
    	for(int i = 0; i < groupList.size(); i++) {
    		if(!AOTalk.this.bot.getGroupIgnoreList().contains(groupList.get(i))) {
    			chn++;
    		}
    	}
    	    	
    	if(groupList != null) {
    		tempChannels = new CharSequence[chn + add];
    		
	    	for(int i = 0; i < (groupList.size() + add); i++) {
	    		if(i == 0 && add > 0) {
	    			tempChannels[cnt] = AOTalk.this.CHANNEL_MSG;
	    			cnt++;
	    		} else if(i == 1 && add == 2) {
	    			tempChannels[cnt] = AOTalk.this.CHANNEL_FRIEND;
	    			cnt++;
	    		} else {
	    			if(!AOTalk.this.bot.getGroupIgnoreList().contains(groupList.get(i - add))) {
		    			tempChannels[cnt] = groupList.get(i - add);
		    			cnt++;
	        		}
	    		}
	    	} 
    	}
     	
    	final CharSequence[] channellist = tempChannels;

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(AOTalk.this.getString(R.string.select_channel));
    	
    	builder.setItems(channellist, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
         	    AOTalk.this.CHATCHANNEL = channellist[item].toString();
         	    
         	    if(channellist[item].toString().equals(AOTalk.this.CHANNEL_MSG)) {
     	        	LayoutInflater inflater = (LayoutInflater)AOTalk.this.getSystemService(LAYOUT_INFLATER_SERVICE);
    	            final View layout = inflater.inflate(R.layout.sendto,(ViewGroup) findViewById(R.layout.sendto));
    	            
    	        	Builder builder = new AlertDialog.Builder(context);
    	        	builder.setTitle(getResources().getString(R.string.send_to_title));
    	        	builder.setView(layout);
    	        	
    	    		EditText TargetEditText = (EditText) layout.findViewById(R.id.targetname);
    	    		TargetEditText.setText(AOTalk.this.MESSAGETO);
    	    		
    	    		//Select text, for easier removal
    	    		TargetEditText.selectAll();
    	    		
    	    		//Force soft keyboard to show after selecting a predefined text
        	    	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        	    	mgr.showSoftInput(AOTalk.this.msginput, InputMethodManager.SHOW_IMPLICIT);
    	        	
    	        	builder.setPositiveButton(AOTalk.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
    	    			public void onClick(DialogInterface dialog, int which) {
    	    				EditText TargetEditText = (EditText) layout.findViewById(R.id.targetname);
    	    				AOTalk.this.MESSAGETO = TargetEditText.getText().toString();
    	    				
    	    				setButtonText();
    	    				return;
    	    			}
    	    		});
    	        	
    	        	AlertDialog targetbox = builder.create();
    	        	targetbox.show();    	    		
    	    	} else if(channellist[item].toString().equals(AOTalk.this.CHANNEL_FRIEND)) {
    	    		showFriends();
    	    	} else {
    	    		setButtonText();
    	    	}
    	    }
    	});
    	
    	AlertDialog channels = builder.create();
    	channels.show();
    }
    
    
    private void setAccount() {
    	LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.login,(ViewGroup) findViewById(R.layout.login));
    	Builder builder = new AlertDialog.Builder(context);
    	builder.setTitle(getResources().getString(R.string.login_title));
    	builder.setView(layout);
    	
		EditText UserEditText = (EditText) layout.findViewById(R.id.username);
		EditText PassEditText = (EditText) layout.findViewById(R.id.password);
		CheckBox SavePrefs    = (CheckBox) layout.findViewById(R.id.savepassword);
		
		UserEditText.setText(USERNAME);
    	PassEditText.setText(PASSWORD);
    	SavePrefs.setChecked(SAVEPREF);
    	
    	builder.setPositiveButton(AOTalk.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {  	                											
				EditText UserEditText = (EditText) layout.findViewById(R.id.username);
				EditText PassEditText = (EditText) layout.findViewById(R.id.password);
				CheckBox SavePrefs    = (CheckBox) layout.findViewById(R.id.savepassword);
				
				AOTalk.this.USERNAME = UserEditText.getText().toString();
				AOTalk.this.PASSWORD = PassEditText.getText().toString();
				AOTalk.this.SAVEPREF = SavePrefs.isChecked();
				
				loader = new ProgressDialog(context);
		    	loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		    	loader.setTitle(getResources().getString(R.string.connecting));
				loader.setMessage(getResources().getString(R.string.please_wait));
				loader.show();
				
				new Thread() {
		            public void run() {
		            	AOTalk.this.bot.setAccount(AOTalk.this.USERNAME, AOTalk.this.PASSWORD);
		        	}
				}.start();
				
				return;
			} 
		});
		
    	builder.setNegativeButton(AOTalk.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(AOTalk.this.bot.getState() != AOBot.State.DISCONNECTED) {
					AOTalk.this.bot.disconnect();
				}
				return;
			}
		}); 
    	
    	builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if(AOTalk.this.bot.getState() != AOBot.State.DISCONNECTED) {
					AOTalk.this.bot.disconnect();
				}
				return;
			}
    	});
    	
    	AlertDialog loginbox = builder.create();
    	loginbox.show();
	}
    
    
    private void clearLog() {
    	AlertDialog clearDialog = new AlertDialog.Builder(AOTalk.this).create();
    	
    	clearDialog.setTitle(AOTalk.this.getString(R.string.clear_chat_log));
    	clearDialog.setMessage(getResources().getString(R.string.want_to_clear));
    	clearDialog.setIcon(R.drawable.icon_clear);
    		            
    	clearDialog.setButton(AOTalk.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				AOTalk.this.messages.clear();
				AOTalk.this.bot.clearLog();
				
				AOTalk.this.messagelist.post(new Runnable() {
					@Override
					public void run() {
						AOTalk.this.msgadapter.notifyDataSetChanged();
					}
				});
				
				return;
			} 
		});
		
    	clearDialog.setButton2(AOTalk.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}); 
    	
    	clearDialog.show();
    }
    
    
    private void showFriends() {   	
    	final List<Friend> friendList = AOTalk.this.bot.getOnlineFriends();
    	
    	CharSequence[] tempList = null;
    	
    	if(friendList != null) {
    		tempList = new CharSequence[friendList.size()];
    		
	    	for(int i = 0; i < friendList.size(); i++) {
	    		tempList[i] = friendList.get(i).getName();
	    	}
    	}
     	
    	final CharSequence[] flist = tempList;

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(AOTalk.this.getString(R.string.friends_online));
    	
    	builder.setItems(flist, new DialogInterface.OnClickListener() {
    	    @Override
			public void onClick(DialogInterface dialog, int which) {
				AOTalk.this.CHATCHANNEL = AOTalk.this.CHANNEL_MSG;
				AOTalk.this.MESSAGETO = flist[which].toString();
				setButtonText();
			}
    	});
    	
    	AlertDialog settingsbox = builder.create();
    	settingsbox.show();	
    }
    
    
    @Override
    public void onResume() {
    	super.onResume();
    	
        //Load values that are saved from last time the app was used
        SAVEPREF = settings.getBoolean("savepref", SAVEPREF);
        USERNAME = settings.getString("username", USERNAME);
        PASSWORD = settings.getString("password", PASSWORD);
        CHATCHANNEL = settings.getString("chatchannel", CHATCHANNEL);
        MESSAGETO = settings.getString("messageto", MESSAGETO);
        FULLSCRN = settings.getBoolean("fullscreen", FULLSCRN);
        
        /* Not in use as listview bugs in fullscreen
        if(AOTalk.this.FULLSCRN) {
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);	
        } else {
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);	
        }
        */
        
        String temp1[] = settings.getString("disabledchannels", "").split(",");
        for(int i = 0; i < temp1.length; i++) {
        	groupDisable.add(temp1[i]);
        }
        
        /*
        String temp2[] = settings.getString("watchchannels", "").split(",");
        for(int i = 0; i < temp2.length; i++) {
        	watchEnable.add(temp2[i]);
        }
        */
    
        attachToService();
    }
    
    
    @Override
    public void onRestart() {
    	super.onRestart();
    }
    
    
    @Override
    public void onPause() {
        super.onPause();
        
        if(AOTalk.this.bot != null) {
	        if(settings.getBoolean("autoafk", false)) {
	        	AOTalk.this.bot.setAFK(true);
	        }
        }
        
        savePreferences();
	}
    
    
    @Override
    public void onStop() {
    	super.onStop();
   	
    	savePreferences();
    	unregisterReceivers();
    }
    
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
    
    private void savePreferences() {	
		editor.putBoolean("savepref", SAVEPREF);
		editor.putString("chatchannel", CHATCHANNEL);
		editor.putString("messageto", MESSAGETO);
		editor.putBoolean("fullscreen", FULLSCRN);
		
		String disabledChannels = "";
		List<String> dc = AOTalk.this.bot.getGroupDisableList();
		
		for(int i = 0; i < dc.size(); i++) {
			disabledChannels += dc.get(i);
			if(i > 0 && i < dc.size() - 1) {
				disabledChannels += ",";
			}
		}
		
		editor.putString("disabledchannels", disabledChannels);
		
		String watchChannels = "";
		List<String> wc = AOTalk.this.bot.getWatchChannels();
		
		for(int i = 0; i < wc.size(); i++) {
			watchChannels += wc.get(i);
			if(i > 0 && i < wc.size() - 1) {
				watchChannels += ",";
			}
		}
		
		editor.putString("watchchannels", watchChannels);
		
		if(SAVEPREF) {
			editor.putString("username", USERNAME);
			editor.putString("password", PASSWORD);
		} else {
			editor.putString("username", "");
			editor.putString("password", "");			
		}
		
		editor.commit();
    }
	
    
    private void registerReceivers() {
    	this.registerReceiver(messageReceiver, new IntentFilter(AOBotService.INFO_MESSAGE));    	
	    this.registerReceiver(connectionReceiver, new IntentFilter(AOBotService.INFO_CONNECTION));    	
    }
    
    
    private void unregisterReceivers() {
    	this.unregisterReceiver(messageReceiver);
    	this.unregisterReceiver(connectionReceiver);
    }
    
    
	private class AOBotConnectionReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	String value = intent.getStringExtra(AOBotService.EXTRA_CONNECTION);
	    	
	    	//Service wants server
	    	if(value.equals(AOBotService.CON_SERVER)) {
	    		setServer();
	    	}
	    	
	    	//Service wants account information
	    	if(value.equals(AOBotService.CON_ACCOUNT)) {
		    	if(AOTalk.this.loader != null) {
		    		AOTalk.this.loader.dismiss();
		    		AOTalk.this.loader = null;
		    	}
	    		
	    		setAccount();
	    	}
	    	
	    	//Service wants character
	    	if(value.equals(AOBotService.CON_CHARACTER)) {
	    		setCharacter();
	    	}
	    	
	    	//Service failed to log in
	    	if(value.equals(AOBotService.CON_LFAILURE)) {
		    	AOTalk.this.bot.appendToLog(
		    		chat.parse(AOTalk.this.getString(R.string.could_not_log_in), ChatParser.TYPE_CLIENT_MESSAGE),
		    		null,
		    		null,
		    		ChatParser.TYPE_CLIENT_MESSAGE
		    	);
		    }
	    	
	    	//Service failed to connect
	    	if(value.equals(AOBotService.CON_CFAILURE)) {
		    	AOTalk.this.bot.appendToLog(
		    		chat.parse(AOTalk.this.getString(R.string.could_not_connect), ChatParser.TYPE_CLIENT_MESSAGE),
		    		null,
		    		null,
		    		ChatParser.TYPE_CLIENT_MESSAGE
		    	);

		    	if(AOTalk.this.loader != null) {
		    		AOTalk.this.loader.dismiss();
		    		AOTalk.this.loader = null;
		    	}
	    	}
	    	
	    	//Service is connected
	    	if(value.equals(AOBotService.CON_CONNECTED)) {
	    		AOTalk.this.bot.appendToLog(
	    			chat.parse(AOTalk.this.getString(R.string.connected), ChatParser.TYPE_CLIENT_MESSAGE),
	    			null,
	    			null,
	    			ChatParser.TYPE_CLIENT_MESSAGE
	    		);

		    	if(AOTalk.this.loader != null) {
		    		AOTalk.this.loader.dismiss();
		    		AOTalk.this.loader = null;
		    	}
	    	}
	    	
	    	//Service is disconnected
	    	if(value.equals(AOBotService.CON_DISCONNECTED)) {    	
		    	if(AOTalk.this.loader != null) {
	    			AOTalk.this.loader.dismiss();
	    			AOTalk.this.loader = null;
	    		}
	    	}
	    	
	    	//Service got a channel invite
	    	if(value.equals(AOBotService.CON_INVITE)) {
		    	handleInvitation();
	    	}
	    	
	    	getMessages();
	    	Log.d(APPTAG, "AOBotConnectionReceiver received message");
	    }
	}
	
	
	private class AOBotMessageReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	getMessages();
	    	Log.d(APPTAG, "AOBotMessageReceiver received message");
	    }
	}
	
	
	private void attachToService() {
		Intent serviceIntent = new Intent(this, AOBotService.class);
	    
	    conn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				AOTalk.this.bot = ((AOBotService.ListenBinder) service).getService();
				
				AOTalk.this.bot.setDisabledGroups(groupDisable);
				//AOTalk.this.bot.setEnabledWatchGroups(watchEnable);
				
				getMessages();
				
				AOTalk.this.messagelist.post(new Runnable() {
					@Override
					public void run() {
						AOTalk.this.msgadapter.notifyDataSetChanged();
			    		AOTalk.this.messagelist.setSelection(AOTalk.this.messages.size()-1);
					}
				});
				
		        if(settings.getBoolean("autoafk", false)) {
		        	if(AOTalk.this.bot.getAFK()) {
		        		AOTalk.this.bot.setAFK(false);
		        	}
		        }
			}
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				AOTalk.this.bot = null;
			}
	    };

	    this.getApplicationContext().startService(serviceIntent);
	    this.getApplicationContext().bindService(serviceIntent, conn, 0);
	    
	    registerReceivers();
	}
    
    
    private void showSettings() {
    	Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
    
	
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.mainmenu, menu);
		
        return true;
    }
    
    
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem afkButton = (MenuItem) menu.findItem(R.id.afk);
        MenuItem conButton = (MenuItem) menu.findItem(R.id.connect);
        
		if (afkButton != null) {
	    	if(AOTalk.this.bot != null) {
				if(AOTalk.this.bot.getAFK()) {
		    		afkButton.setIcon(R.drawable.icon_afk_on);
				} else {
					afkButton.setIcon(R.drawable.icon_afk_off);
				}
	    	}
		}
		
		if(AOTalk.this.bot != null) {
			if(AOTalk.this.bot.getState() != AOBot.State.DISCONNECTED) {
				conButton.setIcon(R.drawable.icon_disconnect);
				conButton.setTitle(getString(R.string.disconnect));
			} else {
				conButton.setIcon(R.drawable.icon_connect);
				conButton.setTitle(getString(R.string.connect));
			}
		}
		
		return true;
    }

    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	        case R.id.connect:
	        	if(AOTalk.this.bot != null) {
		        	if(AOTalk.this.bot.getState() == AOBot.State.DISCONNECTED) {
			        	AOTalk.this.bot.connect();
			        	AOTalk.this.messages.clear();
		        	} else {
		        		AOTalk.this.bot.disconnect();
		        	}
	        	}
	            return true;
	        case R.id.clear:
	        	clearLog();
	        	return true;
	        case R.id.settings:
	        	showSettings();
	        	return true;
	        case R.id.afk:
	        	if(AOTalk.this.bot != null) {
	        		if(AOTalk.this.bot.getAFK()) {
		        		AOTalk.this.bot.setAFK(false);
	        		} else {
		        		AOTalk.this.bot.setAFK(true);
	        		}
	        	}
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
}
