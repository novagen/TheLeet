package com.rubika.aotalk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.adapter.FriendAdapter;
import com.rubika.aotalk.item.Channel;
import com.rubika.aotalk.item.Friend;
import com.rubika.aotalk.service.ClientService;
import com.rubika.aotalk.service.ServiceTools;
import com.rubika.aotalk.ui.colorpicker.ColorPickerPreference;
import com.rubika.aotalk.util.Logging;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import ao.misc.NameFormat;

public class Preferences extends SherlockPreferenceActivity  {
	private static final String APP_TAG = "--> AnarchyTalk::Preferences";
	
	private Context context;
	private Messenger service = null;
	private boolean serviceIsBound = false;	
	private PreferenceManager prefManager;
	private List<Friend> friendList = new ArrayList<Friend>();
	private List<Channel> channelList = new ArrayList<Channel>();
	
	public static int COLOR_ORG_APP = Color.parseColor("#CC99CC");
	public static int COLOR_ORG_SYS = Color.parseColor("#FFCC33");
	public static int COLOR_ORG_PRV = Color.parseColor("#88FF88");
	public static int COLOR_ORG_GRP = Color.parseColor("#FFFFFF");
	public static int COLOR_ORG_ORG = Color.parseColor("#BBBBFF");
	public static int COLOR_ORG_FRN = Color.parseColor("#FFEE55");
	//public static int COLOR_ORG_OTH = Color.parseColor("#FFFFFF");
	
	final Messenger messenger = new Messenger(new IncomingHandler());
	
	private CheckBoxPreference checkboxPrefWhoisWeb;
	private CheckBoxPreference checkboxPrefWhoisFallback;

	class IncomingHandler extends Handler {
		@SuppressWarnings("unchecked")
		@Override
	    public void handleMessage(Message message) {
	    	switch (message.what) {
            case ServiceTools.MESSAGE_FRIEND:
            	friendList = (List<Friend>)message.obj;
    	        
                break;
            case ServiceTools.MESSAGE_REGISTERED:
    			List<Object> registerData = (ArrayList<Object>) message.obj;
    			friendList = (List<Friend>)registerData.get(0);
    			channelList = (List<Channel>)registerData.get(1);

    			break;
            default:
                super.handleMessage(message);
	    	}
	    }
	}
	
	@SuppressWarnings("deprecation")
	@Override 
	protected void onCreate(Bundle savedInstanceState) { 
	    super.onCreate(savedInstanceState); 

	    context = this;
	    
        final ActionBar bar = getSupportActionBar();
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
	            
        this.setPreferenceScreen(createPreferenceHierarchy());
	}
	
	private PreferenceScreen prefScreen;
	private PreferenceCategory prefCat;
	
	@SuppressWarnings("deprecation")
	private PreferenceScreen createPreferenceHierarchy() {        
		prefManager = getPreferenceManager();
		PreferenceScreen root = prefManager.createPreferenceScreen(this);
        
	    prefScreen = prefManager.createPreferenceScreen(this);
	    prefScreen.setKey("about_button");
    	try {
    	    prefScreen.setTitle(getString(R.string.app_name) + " " + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
    	} catch (NameNotFoundException e) {
    	    prefScreen.setTitle(getString(R.string.app_name));
    	}
	    prefScreen.setSummary(getString(R.string.about_aotalk));
	    prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent(context, Help.class);
				startActivity(intent);

				return false;
			}
	    });
	    root.addPreference(prefScreen);
	    
	    
	    // General settings
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.general));
        root.addPreference(prefCat);
        
        //Switch to chat when online
	    CheckBoxPreference checkboxShowChatWhenOnline = new CheckBoxPreference(this);
        checkboxShowChatWhenOnline.setKey("showChatWhenOnline");
        checkboxShowChatWhenOnline.setTitle(getString(R.string.show_chat_when_online));
        checkboxShowChatWhenOnline.setSummary(getString(R.string.show_chat_when_online_info));
        checkboxShowChatWhenOnline.setDefaultValue(true);
        root.addPreference(checkboxShowChatWhenOnline);
        
        //Hide titles
	    CheckBoxPreference checkboxHideTitles = new CheckBoxPreference(this);
	    checkboxHideTitles.setKey("hideTitles");
	    checkboxHideTitles.setTitle(getString(R.string.hide_titles));
	    checkboxHideTitles.setSummary(getString(R.string.hide_titles_info));
	    checkboxHideTitles.setDefaultValue(false);
        root.addPreference(checkboxHideTitles);
       
        //Manage hidden channels
        prefScreen = prefManager.createPreferenceScreen(this);
        prefScreen.setKey("disabledchannels_button");
        prefScreen.setTitle(getString(R.string.mute_channels));
        prefScreen.setSummary(getString(R.string.select_channels_to_mute));
        prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				handleChannels();
				return false;
			}
	    });
	    root.addPreference(prefScreen);
	    
        //Colors
        prefScreen = prefManager.createPreferenceScreen(this);
        prefScreen.setTitle(getString(R.string.colors));
        prefScreen.setSummary(getString(R.string.colors_info));
        
        ColorPickerPreference colorPref1 = new ColorPickerPreference(this);
        colorPref1.setKey("color_app");
        colorPref1.setTitle(getString(R.string.color_app));
        colorPref1.setSummary(getString(R.string.color_app_info));
        colorPref1.setAlphaSliderEnabled(false);
        colorPref1.setDefaultValue(COLOR_ORG_APP);
	    prefScreen.addPreference(colorPref1);
	            
	    ColorPickerPreference colorPref2 = new ColorPickerPreference(this);
	    colorPref2.setKey("color_system");
	    colorPref2.setTitle(getString(R.string.color_sys));
	    colorPref2.setSummary(getString(R.string.color_sys_info));
	    colorPref2.setAlphaSliderEnabled(false);
        colorPref2.setDefaultValue(COLOR_ORG_SYS);
        prefScreen.addPreference(colorPref2);
	    
        ColorPickerPreference colorPref3 = new ColorPickerPreference(this);
        colorPref3.setKey("color_prv");
        colorPref3.setTitle(getString(R.string.color_prv));
        colorPref3.setSummary(getString(R.string.color_prv_info));
        colorPref3.setAlphaSliderEnabled(false);
        colorPref3.setDefaultValue(COLOR_ORG_PRV);
	    prefScreen.addPreference(colorPref3);
	    
	    ColorPickerPreference colorPref4 = new ColorPickerPreference(this);
	    colorPref4.setKey("color_group");
	    colorPref4.setTitle(getString(R.string.color_grp));
	    colorPref4.setSummary(getString(R.string.color_grp_info));
	    colorPref4.setAlphaSliderEnabled(false);
        colorPref4.setDefaultValue(COLOR_ORG_GRP);
	    prefScreen.addPreference(colorPref4);
	    
	    ColorPickerPreference colorPref5 = new ColorPickerPreference(this);
	    colorPref5.setKey("color_frn");
	    colorPref5.setTitle(getString(R.string.color_frn));
	    colorPref5.setSummary(getString(R.string.color_frn_info));
	    colorPref5.setAlphaSliderEnabled(false);
	    colorPref5.setDefaultValue(COLOR_ORG_FRN);
	    prefScreen.addPreference(colorPref5);
	    
	    /*
	    ColorPickerPreference colorPref6 = new ColorPickerPreference(this);
	    colorPref6.setKey("color_other");
	    colorPref6.setTitle(getString(R.string.color_oth));
	    colorPref6.setSummary(getString(R.string.color_oth_info));
	    colorPref6.setAlphaSliderEnabled(false);
	    colorPref6.setDefaultValue(COLOR_ORG_OTH);
	    prefScreen.addPreference(colorPref6);
	    */
	    
	    PreferenceScreen resetColors = prefManager.createPreferenceScreen(this);
	    resetColors.setKey("reset_colors");
	    resetColors.setTitle(getString(R.string.resetcolors));
	    resetColors.setSummary(getString(R.string.resetcolors_info));
	    resetColors.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				resetColors();
				return false;
			}
	    });
	    prefScreen.addPreference(resetColors);
	    
	    root.addPreference(prefScreen);
	    
	    
	    // Connection settings
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.connection));
        root.addPreference(prefCat);
        
	    //Automatic reconnect
        CheckBoxPreference checkboxPrefReconnect = new CheckBoxPreference(this);
        checkboxPrefReconnect.setKey("autoReconnect");
        checkboxPrefReconnect.setTitle(getString(R.string.automatic_reconnect));
        checkboxPrefReconnect.setSummary(getString(R.string.automatic_reconnect_info));
        checkboxPrefReconnect.setDefaultValue(true);
        root.addPreference(checkboxPrefReconnect);
        
	    //Do lookups on web page
        checkboxPrefWhoisWeb = new CheckBoxPreference(this);
        checkboxPrefWhoisWeb.setKey("whoisFromWeb");
        checkboxPrefWhoisWeb.setTitle(getString(R.string.whois_from_web));
        checkboxPrefWhoisWeb.setSummary(getString(R.string.whois_from_web_info));
        checkboxPrefWhoisWeb.setDefaultValue(true);
        root.addPreference(checkboxPrefWhoisWeb);

        //Fall back to bot lookup
        checkboxPrefWhoisFallback = new CheckBoxPreference(this);
        checkboxPrefWhoisFallback.setKey("whoisFallbackToBot");
        checkboxPrefWhoisFallback.setTitle(getString(R.string.whois_fallback));
        checkboxPrefWhoisFallback.setSummary(getString(R.string.whois_fallback_info));
        checkboxPrefWhoisFallback.setDefaultValue(true);
        root.addPreference(checkboxPrefWhoisFallback);


        // Notifications
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.notifications));
        root.addPreference(prefCat);

        CheckBoxPreference checkboxPrefNotification = new CheckBoxPreference(this);
        checkboxPrefNotification.setKey("notificationEnabled");
        checkboxPrefNotification.setTitle(getString(R.string.enable_notifications));
        checkboxPrefNotification.setSummary(getString(R.string.enable_notifications_info));
        checkboxPrefNotification.setDefaultValue(true);
        root.addPreference(checkboxPrefNotification);

        RingtonePreference ringPreference = new RingtonePreference(this);
        ringPreference.setKey("notificationSound");
        ringPreference.setTitle(getString(R.string.notification_sound));
        ringPreference.setSummary(getString(R.string.notification_sound_info));
        ringPreference.setRingtoneType(RingtoneManager.TYPE_NOTIFICATION);
        ringPreference.setDefaultValue(Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        ringPreference.setShowDefault(true);
        ringPreference.setShowSilent(true);
	    root.addPreference(ringPreference); 

        
	    // Friends
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.friends));
        root.addPreference(prefCat);

        prefScreen = prefManager.createPreferenceScreen(this);
        prefScreen.setKey("addfriend_button");
        prefScreen.setTitle(getString(R.string.add_friend));
        prefScreen.setSummary(getString(R.string.add_friend_to_buddy_list));
        prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				addFriend();
				return false;
			}
	    });
	    root.addPreference(prefScreen); 
        
	    prefScreen = prefManager.createPreferenceScreen(this);
	    prefScreen.setKey("addfriend_button");
	    prefScreen.setTitle(getString(R.string.remove_friend));
	    prefScreen.setSummary(getString(R.string.remove_friend_from_buddy_list));
	    prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				removeFriend();
				return false;
			}
	    });
	    root.addPreference(prefScreen); 

        CheckBoxPreference checkboxOnlineOnly = new CheckBoxPreference(this);
        checkboxOnlineOnly.setKey("showOnlyOnline");
        checkboxOnlineOnly.setTitle(getString(R.string.show_only_online));
        checkboxOnlineOnly.setSummary(getString(R.string.show_only_online_info));
        checkboxOnlineOnly.setDefaultValue(false);
        root.addPreference(checkboxOnlineOnly);

	    
	    // Market settings
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.market_monitor));
        root.addPreference(prefCat);

        ListPreference server = new ListPreference(this);
        server.setKey("marketserver");
        server.setTitle(getString(R.string.market_server));
        server.setSummary(getString(R.string.market_server_info));
        server.setEntries(R.array.marketservers);
        server.setEntryValues(R.array.marketservers_values);
        server.setDefaultValue("1");
        root.addPreference(server);
        
        EditTextPreference interval = new EditTextPreference(this);
        interval.setKey("marketinterval");
        interval.setTitle(getString(R.string.market_interval));
        interval.setSummary(getString(R.string.market_interval_info));
        interval.setDefaultValue("5");
        root.addPreference(interval);
        
        CheckBoxPreference autoupdate = new CheckBoxPreference(this);
        autoupdate.setKey("marketautoupdate");
        autoupdate.setTitle(getString(R.string.market_autoupdate));
        autoupdate.setSummary(getString(R.string.market_autoupdate_info));
        autoupdate.setDefaultValue(true);
        root.addPreference(autoupdate);
        
        
	    return root;
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
	
	private void resetColors() {
		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.resetcolors)
        .setMessage(R.string.resetcolors_text)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = settings.edit();
				
				editor.putInt("color_app", COLOR_ORG_APP);
				editor.putInt("color_system", COLOR_ORG_SYS);
				editor.putInt("color_tell", COLOR_ORG_PRV);
				editor.putInt("color_group", COLOR_ORG_GRP);
				editor.putInt("color_org", COLOR_ORG_ORG);
				editor.putInt("color_frn", COLOR_ORG_FRN);
				
				editor.commit();
				finish();
            }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
	}
	
	private void addFriend() {
		if (channelList.size() > 0) {
			LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
	        final View layout = inflater.inflate(R.layout.alert_character, null);
	        
	    	new AlertDialog.Builder(this)
	    	.setTitle(getString(R.string.add_friend))
	    	.setView(layout)
	
	    	.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					EditText name = (EditText) layout.findViewById(R.id.username);
					
			    	if (name.getText().toString().length() > 0) {
						Message msg = Message.obtain(null, ServiceTools.MESSAGE_FRIEND_ADD);
				        msg.replyTo = messenger;
				        msg.obj = NameFormat.format(name.getText().toString().trim());
				        
				        try {
							service.send(msg);
						} catch (RemoteException e) {
							Logging.log(APP_TAG, e.getMessage());
						}
			    	}
					return;
				} 
			})
			
	    	.setNegativeButton(getString(R.string.cancel), null)
	    	.create().show();
    	} else {
    		Logging.toast(context, getString(R.string.not_connected));
    	}
	}
	
	private void removeFriend() {
		if (channelList.size() > 0) {
			final List<Friend> tempList = new ArrayList<Friend>();
			tempList.addAll(friendList);
			
			Collections.sort(tempList, new Friend.CustomComparator());
			
	    	new AlertDialog.Builder(this)
	    	.setTitle(getString(R.string.remove_friend))
	    	
			.setAdapter(new FriendAdapter(context, R.id.friendlist, tempList), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
	    	    	final String fname = NameFormat.format(tempList.get(which).getName().toString());
	    	    	
	    	    	AlertDialog acceptRemoveDialog = new AlertDialog.Builder(context).create();
	    	    	
	    	    	acceptRemoveDialog.setTitle(String.format(getString(R.string.remove_friend_title), fname));
	    	    	acceptRemoveDialog.setMessage(String.format(getString(R.string.remove_friend_confirm), fname));
	    	    		            
	    	    	acceptRemoveDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int which) {
	    			    	Message msg = Message.obtain(null, ServiceTools.MESSAGE_FRIEND_REMOVE);
	    			        msg.replyTo = messenger;
	    			        msg.obj = fname;
	    			        
	    			        try {
	    						service.send(msg);
	    					} catch (RemoteException e) {
	    						Logging.log(APP_TAG, e.getMessage());
	    					}
	
	    					return;
	    				} 
	    			});
	    			
	    	    	acceptRemoveDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int which) {
	    					return;
	    				}
	    			});
	    	    	
	    	    	acceptRemoveDialog.show();
				}
	    	})
	    	
	    	.setNegativeButton(this.getString(R.string.cancel), null)
	    	.create().show();
    	} else {
    		Logging.toast(context, getString(R.string.not_connected));
    	}
	}
		
    private void handleChannels() {   	
    	if (channelList.size() > 0) {
	    	final List<Channel> tempList = new ArrayList<Channel>();
	    	tempList.addAll(channelList);
	    	
	    	Collections.sort(tempList, new Channel.CustomComparator());
	        
	        final CharSequence[] channels = new CharSequence[tempList.size()];
	        final boolean[] values = new boolean[tempList.size()];
	        
	        for (int i = 0; i < tempList.size(); i++) {
	        	channels[i] = tempList.get(i).getName();
	        	values[i] = tempList.get(i).getMuted();
	        }
	
	    	new AlertDialog.Builder(this)
	        .setTitle(R.string.disable_channels)
	        
	        .setMultiChoiceItems(channels, values, new OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					Channel channel = tempList.get(which);
					channel.setMuted(isChecked);
					tempList.set(which, channel);
				}
			})
	        
	        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
			    	Message msg = Message.obtain(null, ServiceTools.MESSAGE_MUTED_CHANNELS);
			        msg.replyTo = messenger;
			        msg.obj = tempList;
			        
			        try {
						service.send(msg);
					} catch (RemoteException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
	            }
	        })
	        
	        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                /* User clicked No so do some stuff */
	            }
	        })
	        
	        .create().show();
    	} else {
    		Logging.toast(context, getString(R.string.not_connected));
    	}
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
