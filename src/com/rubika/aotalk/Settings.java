package com.rubika.aotalk;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import ao.protocol.AOBot;

public class Settings extends PreferenceActivity {
	private ServiceConnection conn;
	private AOBotService bot;
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) { 
	    super.onCreate(savedInstanceState); 
	    
	    //addPreferencesFromResource(R.xml.preferences);
	    
	    attachToService();
	    
	    PreferenceScreen screen = createPreferenceHierarchy();
	    setPreferenceScreen(screen); 
	} 
	
	private PreferenceScreen createPreferenceHierarchy() {        
	    PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

	    //Account settings
	    PreferenceCategory accountCat = new PreferenceCategory(this);
	    accountCat.setTitle("Account");
        root.addPreference(accountCat);
        
        EditTextPreference username = new EditTextPreference(this);
        username.setDialogTitle("Set username");
        username.setKey("username");
        username.setTitle("Username");
        username.setSummary("Edit your username");
        root.addPreference(username);
        
        EditTextPreference password = new EditTextPreference(this);
        password.setDialogTitle("Set password");
        password.setKey("password");
        password.setTitle("Password");
        password.setSummary("Edit your password");
        password.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addPreference(password);
        
        CheckBoxPreference saveAccount = new CheckBoxPreference(this);
        saveAccount.setKey("savepref");
        saveAccount.setTitle("Save account");
        saveAccount.setSummary("Save your account information");
        root.addPreference(saveAccount);
	    
	    //Channel settings
        PreferenceCategory channelsCat = new PreferenceCategory(this);
        channelsCat.setTitle("Channels");
        root.addPreference(channelsCat);
        
	    PreferenceScreen muteChannels = getPreferenceManager().createPreferenceScreen(this);
	    muteChannels.setKey("disabledchannels_button");
	    muteChannels.setTitle("Mute channels");
	    muteChannels.setSummary("Select the channels you want to mute");
	    muteChannels.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				handleChannels();
				return false;
			}
	    });
	    root.addPreference(muteChannels);
	    
	    //Friend settings
	    PreferenceCategory friendsCat = new PreferenceCategory(this);
	    friendsCat.setTitle("Friends");
        root.addPreference(friendsCat);
        
	    PreferenceScreen addFriend = getPreferenceManager().createPreferenceScreen(this);
	    addFriend.setKey("addfriend_button");
	    addFriend.setTitle("Add friend");
	    addFriend.setSummary("Add a friend to your buddy list");
	    addFriend.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				addFriend();
				return false;
			}
	    });
	    root.addPreference(addFriend); 
        
	    PreferenceScreen removeFriend = getPreferenceManager().createPreferenceScreen(this);
	    removeFriend.setKey("addfriend_button");
	    removeFriend.setTitle("Remove friend");
	    removeFriend.setSummary("Remove a friend from your buddy list");
	    removeFriend.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				removeFriend();
				return false;
			}
	    });
	    root.addPreference(removeFriend); 
	    
	    /* Disabled due to a bug in ListView (it don't update height when keyboard pops up when fullscreen is enabled)
	    //Layout settings
	    PreferenceCategory layoutCat = new PreferenceCategory(this);
	    layoutCat.setTitle("Layout");
        root.addPreference(layoutCat);

        CheckBoxPreference fullscreen = new CheckBoxPreference(this);
        fullscreen.setKey("fullscreen");
        fullscreen.setTitle("Fullscreen");
        fullscreen.setSummary("Enable fullscreen view");
        root.addPreference(fullscreen);
        */
	    
	    return root; 
	}
	
	private void addFriend() {
	   	LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.addfriend,(ViewGroup) findViewById(R.layout.addfriend));
    	Builder builder = new AlertDialog.Builder(Settings.this);
    	builder.setTitle("Add a friend");
    	builder.setView(layout);

    	builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {  	                											
				EditText name = (EditText) layout.findViewById(R.id.friendname);
				if(Settings.this.bot.getState() != AOBot.State.DISCONNECTED) {
					Settings.this.bot.addFriend(name.getText().toString());	
				}
				return;
			} 
		});
		
    	builder.setNegativeButton(Settings.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//Do nothing.
				return;
			}
		}); 
    	
    	AlertDialog addfriendbox = builder.create();
    	addfriendbox.show();
	}
	
	private void removeFriend() {
    	final List<Friend> friendList = Settings.this.bot.getAllFriends();
    	
    	CharSequence[] tempList = null;
    	
    	if(friendList != null) {
    		tempList = new CharSequence[friendList.size()];
    		
	    	for(int i = 0; i < friendList.size(); i++) {
	    		tempList[i] = friendList.get(i).getName();
	    	}
    	}
     	
    	final CharSequence[] flist = tempList;

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Remove friend");
    	
    	builder.setItems(flist, new DialogInterface.OnClickListener() {
    	    @Override
			public void onClick(DialogInterface dialog, int which) {
    	    	final String fname = flist[which].toString();
    	    	
    	    	AlertDialog acceptRemoveDialog = new AlertDialog.Builder(Settings.this).create();
    	    	
    	    	acceptRemoveDialog.setTitle("Remove " + fname);
    	    	acceptRemoveDialog.setMessage(
    	    			"Are you sure you want to remove " + 
    	    			fname + 
    	    			" from your friends list?\n\nThis is will affect your ingame list!"
    	    	);
    	    		            
    	    	acceptRemoveDialog.setButton(Settings.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					Settings.this.bot.removeFriend(fname);
    					return;
    				} 
    			});
    			
    	    	acceptRemoveDialog.setButton2(Settings.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					//Do nothing.
    					return;
    				}
    			}); 
    	    	
    	    	acceptRemoveDialog.show();
			}
    	});
    	
    	builder.setNegativeButton(this.getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Do nothing.
			}
    	});
    	
    	AlertDialog settingsbox = builder.create();
    	settingsbox.show();	
	}
	
    private void handleChannels() {   	
    	final List<String> groupDisable = Settings.this.bot.getDisabledGroups();
    	final List<String> groupList = Settings.this.bot.getGroupList();
    	
    	CharSequence[] tempChannels = null;
    	boolean[] channelStates = null;
    	
    	if(groupList != null) {
    		tempChannels = new CharSequence[groupList.size()];
    		channelStates = new boolean[groupList.size()];
    		
	    	for(int i = 0; i < groupList.size(); i++) {
	    		tempChannels[i] = groupList.get(i);
				if(groupDisable != null ) {
		    		if(groupDisable.contains(groupList.get(i))) {
						channelStates[i] = true;
					} else {
						channelStates[i] = false;
					}
				} else {
					channelStates[i] = false;
				}
	    	} 
    	}
     	
    	final CharSequence[] channellist = tempChannels;

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(Settings.this.getString(R.string.disable_channels));
    	
    	builder.setMultiChoiceItems(channellist, channelStates, new DialogInterface.OnMultiChoiceClickListener() {
    	    @Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if(isChecked) {
					if(!groupDisable.contains(channellist[which].toString())) {
						groupDisable.add(channellist[which].toString());
					}
				} else {
					if(groupDisable.contains(channellist[which].toString())) {
						groupDisable.remove(channellist[which].toString());
					}
				}
				
				Settings.this.bot.setDisabledGroups(groupDisable);
			}
    	});
    	
    	builder.setPositiveButton(Settings.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {  	                						
				Settings.this.bot.setDisabledGroups(groupDisable);
				return;
			} 
		});
    	
    	AlertDialog settingsbox = builder.create();
    	settingsbox.show();	
    }
    
	private void attachToService() {
		Intent serviceIntent = new Intent(this, AOBotService.class);
	    
	    conn = new ServiceConnection() {  	
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Settings.this.bot = ((AOBotService.ListenBinder) service).getService();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Settings.this.bot = null;
			}
	    };

	    this.getApplicationContext().startService(serviceIntent);
	    this.getApplicationContext().bindService(serviceIntent, conn, 0);
	}
}