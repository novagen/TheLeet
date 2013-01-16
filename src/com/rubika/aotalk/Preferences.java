package com.rubika.aotalk;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.adapter.FriendAdapter;
import com.rubika.aotalk.item.Channel;
import com.rubika.aotalk.item.Friend;
import com.rubika.aotalk.item.TowerSite;
import com.rubika.aotalk.market.Market;
import com.rubika.aotalk.service.ClientService;
import com.rubika.aotalk.ui.colorpicker.ColorPickerPreference;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.RingtoneManager;
import android.os.AsyncTask;
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
	private static final String APP_TAG = "--> The Leet ::Preferences";
	
	private Context context;
	private Messenger service = null;
	private boolean serviceIsBound = false;	
	private PreferenceManager prefManager;
	
	private static List<Friend> friendList = new ArrayList<Friend>();
	private static List<Channel> channelList = new ArrayList<Channel>();
	
	final static Messenger messenger = new Messenger(new IncomingHandler());
	
	/*
	private CheckBoxPreference checkboxPrefWhoisWeb;
	private CheckBoxPreference checkboxPrefWhoisFallback;
	*/
	
	static class IncomingHandler extends Handler {
		@SuppressWarnings("unchecked")
		@Override
	    public void handleMessage(Message message) {
	    	switch (message.what) {
            case Statics.MESSAGE_FRIEND:
            	friendList = (List<Friend>)message.obj;
    	        
                break;
            case Statics.MESSAGE_REGISTERED:
    			List<Object> registerData = (ArrayList<Object>) message.obj;
    			friendList = (List<Friend>)registerData.get(0);
    			channelList = (List<Channel>)registerData.get(1);

    			break;
            default:
                super.handleMessage(message);
	    	}
	    }
	}
	
	private AccountManager accountManager;
	private Account[] accounts;
	
	@SuppressWarnings("deprecation")
	@Override 
	protected void onCreate(Bundle savedInstanceState) { 
	    super.onCreate(savedInstanceState); 

	    context = this;
	    
        accountManager = AccountManager.get(context);
	    
    	loader = new ProgressDialog(context);
		loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		loader.setCancelable(false);
	    
        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.abbg));
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
        checkboxShowChatWhenOnline.setSummaryOn(R.string.show_chat_when_online_info_on);
        checkboxShowChatWhenOnline.setSummaryOff(R.string.show_chat_when_online_info_off);
        checkboxShowChatWhenOnline.setDefaultValue(true);
        root.addPreference(checkboxShowChatWhenOnline);
        
        //Hide titles
	    CheckBoxPreference checkboxHideTitles = new CheckBoxPreference(this);
	    checkboxHideTitles.setKey("hideTitles");
	    checkboxHideTitles.setTitle(getString(R.string.hide_titles));
	    checkboxHideTitles.setSummaryOn(R.string.hide_titles_info_on);
	    checkboxHideTitles.setSummaryOff(R.string.hide_titles_info_off);
	    checkboxHideTitles.setDefaultValue(false);
        root.addPreference(checkboxHideTitles);

        CheckBoxPreference checkboxEnableAnimations = new CheckBoxPreference(this);
        checkboxEnableAnimations.setKey("enableAnimations");
        checkboxEnableAnimations.setTitle(getString(R.string.enable_animations));
        checkboxEnableAnimations.setSummaryOn(R.string.enable_animations_info_on);
        checkboxEnableAnimations.setSummaryOff(R.string.enable_animations_info_off);
        checkboxEnableAnimations.setDefaultValue(true);
        root.addPreference(checkboxEnableAnimations);

        CheckBoxPreference checkboxEnableFaces = new CheckBoxPreference(this);
        checkboxEnableFaces.setKey("enableFaces");
        checkboxEnableFaces.setTitle(getString(R.string.enable_faces));
        checkboxEnableFaces.setSummaryOn(R.string.enable_faces_info_on);
        checkboxEnableFaces.setSummaryOff(R.string.enable_faces_info_off);
        checkboxEnableFaces.setDefaultValue(true);
        root.addPreference(checkboxEnableFaces);

        CheckBoxPreference checkboxEnableTimestamp = new CheckBoxPreference(this);
        checkboxEnableTimestamp.setKey("showTimestamp");
        checkboxEnableTimestamp.setTitle(getString(R.string.show_timestamps));
        checkboxEnableTimestamp.setSummaryOn(R.string.show_timestamps_info_on);
        checkboxEnableTimestamp.setSummaryOff(R.string.show_timestamps_info_off);
        checkboxEnableTimestamp.setDefaultValue(true);
        root.addPreference(checkboxEnableTimestamp);
	    
        //Colors
        prefScreen = prefManager.createPreferenceScreen(this);
        prefScreen.setTitle(getString(R.string.colors));
        prefScreen.setSummary(getString(R.string.colors_info));
        
        ColorPickerPreference colorPref1 = new ColorPickerPreference(this);
        colorPref1.setKey("color_app");
        colorPref1.setTitle(getString(R.string.color_app));
        colorPref1.setSummary(getString(R.string.color_app_info));
        colorPref1.setAlphaSliderEnabled(false);
        colorPref1.setDefaultValue(Statics.COLOR_ORG_APP);
	    prefScreen.addPreference(colorPref1);
	            
	    ColorPickerPreference colorPref2 = new ColorPickerPreference(this);
	    colorPref2.setKey("color_system");
	    colorPref2.setTitle(getString(R.string.color_sys));
	    colorPref2.setSummary(getString(R.string.color_sys_info));
	    colorPref2.setAlphaSliderEnabled(false);
        colorPref2.setDefaultValue(Statics.COLOR_ORG_SYS);
        prefScreen.addPreference(colorPref2);
	    
        ColorPickerPreference colorPref3 = new ColorPickerPreference(this);
        colorPref3.setKey("color_prv");
        colorPref3.setTitle(getString(R.string.color_prv));
        colorPref3.setSummary(getString(R.string.color_prv_info));
        colorPref3.setAlphaSliderEnabled(false);
        colorPref3.setDefaultValue(Statics.COLOR_ORG_PRV);
	    prefScreen.addPreference(colorPref3);
	    
	    ColorPickerPreference colorPref4 = new ColorPickerPreference(this);
	    colorPref4.setKey("color_group");
	    colorPref4.setTitle(getString(R.string.color_grp));
	    colorPref4.setSummary(getString(R.string.color_grp_info));
	    colorPref4.setAlphaSliderEnabled(false);
        colorPref4.setDefaultValue(Statics.COLOR_ORG_GRP);
	    prefScreen.addPreference(colorPref4);
	    
	    ColorPickerPreference colorPref5 = new ColorPickerPreference(this);
	    colorPref5.setKey("color_frn");
	    colorPref5.setTitle(getString(R.string.color_frn));
	    colorPref5.setSummary(getString(R.string.color_frn_info));
	    colorPref5.setAlphaSliderEnabled(false);
	    colorPref5.setDefaultValue(Statics.COLOR_ORG_FRN);
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
	    
        CheckBoxPreference checkboxSortLoginCharacters = new CheckBoxPreference(this);
        checkboxSortLoginCharacters.setKey("sortLoginCharacters");
        checkboxSortLoginCharacters.setTitle(getString(R.string.sort_login_characters));
        checkboxSortLoginCharacters.setSummaryOn(R.string.sort_login_characters_info_on);
        checkboxSortLoginCharacters.setSummaryOff(R.string.sort_login_characters_info_off);
        checkboxSortLoginCharacters.setDefaultValue(false);
        root.addPreference(checkboxSortLoginCharacters);
	    
	    
	    // Connection settings
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.connection));
        root.addPreference(prefCat);

        ListPreference server = new ListPreference(this);
		server.setKey("server");
        server.setTitle(getString(R.string.server));
        server.setSummary(getString(R.string.server_info));
        server.setEntries(R.array.server);
        server.setEntryValues(R.array.server_values);
        server.setDefaultValue("1");
        root.addPreference(server);
        
	    //Automatic reconnect
        CheckBoxPreference checkboxPrefReconnect = new CheckBoxPreference(this);
        checkboxPrefReconnect.setKey("autoReconnect");
        checkboxPrefReconnect.setTitle(getString(R.string.automatic_reconnect));
        checkboxPrefReconnect.setSummaryOn(R.string.automatic_reconnect_info_on);
        checkboxPrefReconnect.setSummaryOff(R.string.automatic_reconnect_info_off);
        checkboxPrefReconnect.setDefaultValue(true);
        root.addPreference(checkboxPrefReconnect);
        
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
        
        /*
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
		*/

        // Notifications
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.notifications));
        root.addPreference(prefCat);

        CheckBoxPreference checkboxPrefNotification = new CheckBoxPreference(this);
        checkboxPrefNotification.setKey("notificationEnabled");
        checkboxPrefNotification.setTitle(getString(R.string.enable_notifications));
        checkboxPrefNotification.setSummaryOn(R.string.enable_notifications_info_on);
        checkboxPrefNotification.setSummaryOff(R.string.enable_notifications_info_off);
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
        
	    /*
	    prefScreen = prefManager.createPreferenceScreen(this);
	    prefScreen.setKey("towernotifications");
	    prefScreen.setTitle(getString(R.string.tower_attack_notifications));
	    prefScreen.setSummary(getString(R.string.tower_attack_notifications_info));
	    prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				if (android.os.Build.VERSION.SDK_INT > 7) {
					accounts = accountManager.getAccountsByType(context.getString(R.string.account_type));
	
					if (accounts.length > 0) {
			    		new GetTowersites().execute();
			        } else {
			        	accountManager.addAccount(context.getString(R.string.account_type), null, null, null, Preferences.this, null, null);
			        }
				} else {
					Logging.toast(context, getString(R.string.unsupported_version));
				}
		        
				return false;
			}
	    });
	    root.addPreference(prefScreen); 
		*/
        
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
        checkboxOnlineOnly.setSummaryOn(R.string.show_only_online_info_on);
        checkboxOnlineOnly.setSummaryOff(R.string.show_only_online_info_off);
        checkboxOnlineOnly.setDefaultValue(false);
        root.addPreference(checkboxOnlineOnly);

	    
	    // Market settings
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.market_monitor));
        root.addPreference(prefCat);
        
        CheckBoxPreference checkboxMarketAutoUpdate = new CheckBoxPreference(this);
        checkboxMarketAutoUpdate.setKey("marketautoupdate");
        checkboxMarketAutoUpdate.setTitle(getString(R.string.market_autoupdate));
        checkboxMarketAutoUpdate.setSummaryOn(R.string.market_autoupdate_info_on);
        checkboxMarketAutoUpdate.setSummaryOff(R.string.market_autoupdate_info_off);
        checkboxMarketAutoUpdate.setDefaultValue(true);
        root.addPreference(checkboxMarketAutoUpdate);
        
        EditTextPreference interval = new EditTextPreference(this);
        interval.setKey("marketinterval");
        interval.setTitle(getString(R.string.market_interval));
        interval.setSummary(getString(R.string.market_interval_info));
        interval.setDefaultValue(Market.MARKET_INTERVAL);
        root.addPreference(interval);
        
        
	    // Other settings
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.other_settings));
        root.addPreference(prefCat);

        CheckBoxPreference checkboxKeepScreenOn = new CheckBoxPreference(this);
        checkboxKeepScreenOn.setKey("keepScreenOn");
        checkboxKeepScreenOn.setTitle(getString(R.string.keep_screen_on));
        checkboxKeepScreenOn.setSummaryOn(R.string.keep_screen_on_info_on);
        checkboxKeepScreenOn.setSummaryOff(R.string.keep_screen_on_info_off);
        checkboxKeepScreenOn.setDefaultValue(false);
        root.addPreference(checkboxKeepScreenOn);

        CheckBoxPreference checkboxDebugOutput = new CheckBoxPreference(this);
        checkboxDebugOutput.setKey("enableDebug");
        checkboxDebugOutput.setTitle(getString(R.string.enable_debug_output));
        checkboxDebugOutput.setSummaryOn(R.string.enable_debug_output_info_on);
        checkboxDebugOutput.setSummaryOff(R.string.enable_debug_output_info_off);
        checkboxDebugOutput.setDefaultValue(false);
        root.addPreference(checkboxDebugOutput);

        
	    return root;
	}
	
	private ProgressDialog loader;
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder ibinder) {
	        service = new Messenger(ibinder);

	        try {
	            Message message = Message.obtain(null, Statics.MESSAGE_CLIENT_REGISTER);
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
	                Message msg = Message.obtain(null, Statics.MESSAGE_CLIENT_UNREGISTER);
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
				
				editor.putInt("color_app", Statics.COLOR_ORG_APP);
				editor.putInt("color_system", Statics.COLOR_ORG_SYS);
				editor.putInt("color_tell", Statics.COLOR_ORG_PRV);
				editor.putInt("color_group", Statics.COLOR_ORG_GRP);
				editor.putInt("color_org", Statics.COLOR_ORG_ORG);
				editor.putInt("color_frn", Statics.COLOR_ORG_FRN);
				
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
						Message msg = Message.obtain(null, Statics.MESSAGE_FRIEND_ADD);
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
	    	
			.setAdapter(new FriendAdapter(context, R.id.friendlist, tempList, false), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
	    	    	final String fname = NameFormat.format(tempList.get(which).getName().toString());
	    	    	
	    	    	AlertDialog acceptRemoveDialog = new AlertDialog.Builder(context).create();
	    	    	
	    	    	acceptRemoveDialog.setTitle(String.format(getString(R.string.remove_friend_title), fname));
	    	    	acceptRemoveDialog.setMessage(String.format(getString(R.string.remove_friend_confirm), fname));
	    	    		            
	    	    	acceptRemoveDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int which) {
	    			    	Message msg = Message.obtain(null, Statics.MESSAGE_FRIEND_REMOVE);
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
	
    private List<TowerSite> towerSites = new ArrayList<TowerSite>();

    public class GetTowersites extends AsyncTask<Void, Void, String> {
        @Override    
        protected void onPreExecute() {
    		loader.setMessage("Loading tower sites..");
    		loader.show();
        }

        @Override 
		protected void onPostExecute(String result) {
	    	 handleTowersites();
	     }

		@Override
		protected String doInBackground(Void... params) {
	    	/** TODO
	    	 * Load sites from own server instead of Demoders.
	    	 * Include boolean to show if site is selected for current account
	    	 */
			towerSites.clear();
	    	
			HttpClient httpclient;
	    	HttpGet httpget;
	        
	    	HttpResponse response;
	    	HttpEntity entity;
	    	InputStream is;
	    	BufferedReader reader;
	    	StringBuilder sb;
	    	String line;
	    	String resultData;
	    	
	    	JSONArray jArray;
	    	JSONObject json_data;
	    	
	    	try{
	    		httpclient = new DefaultHttpClient();
		        httpget = new HttpGet(String.format(Statics.TOWER_WARS_SITES, PreferenceManager.getDefaultSharedPreferences(context).getString("server", "1")));
		        	        
		        response = httpclient.execute(httpget);
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

	    	try{
	    		if(resultData != null) {
		    		if((!resultData.startsWith("null"))) {
		    			jArray = new JSONArray(resultData);
		    				    			
		    	        for(int i = 0; i < jArray.length(); i++){
		    	        	json_data = jArray.getJSONObject(i);
		    	        	
		    	        	towerSites.add(new TowerSite(
		                		json_data.getInt("site_id"),
		                		json_data.getInt("zone_id"),
		                		json_data.getString("zone_name"),
		                		json_data.getString("faction_name"),
		                		json_data.getString("site_name"),
		                		json_data.getInt("site_minlvl"),
		                		json_data.getInt("site_maxlvl"),
		                		json_data.getLong("lastresult"),
		                		false,
		                		0,
		                		0
			                ));

		    	        }
		    		}
	    		}
	    	} catch(JSONException e){
	    		Logging.log(APP_TAG, "Error parsing data " + e.toString());
	    	}
	    	
	    	Collections.sort(towerSites, new TowerSite.SitenameComparator());
	        
			return null;
		}
	};
	
	private void handleTowersites() {
    	if (loader != null) {
    		loader.dismiss();
    	}
		
    	if (towerSites != null && towerSites.size() > 0) {
	    	final CharSequence[] sites = new CharSequence[towerSites.size()];
	        final boolean[] values = new boolean[towerSites.size()];
	        
	        for (int i = 0; i < towerSites.size(); i++) {
	        	sites[i] = towerSites.get(i).getSitename();
	        	values[i] = towerSites.get(i).getNotify();
		        Logging.log(APP_TAG, "Adding row: " + towerSites.get(i).getSitename());
	        }
	
	        Logging.log(APP_TAG, "Creating alert");
	        
	        new AlertDialog.Builder(this)
	        .setTitle("Select sites")
	        
	        .setMultiChoiceItems(sites, values, new OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					Logging.log(APP_TAG, "marking " + towerSites.get(which).getSitename());
					towerSites.get(which).setNotify(isChecked);
				}
			})
	        
	        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                new SaveTowersites().execute();
	            }
	        })
	        
	        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            }
	        })
	        
	        .create().show();
    	} else {
    		Logging.toast(this, "Unable to load sites");
    	}
	}
	
	public class SaveTowersites extends AsyncTask<Void, Void, String> {
        @Override    
        protected void onPreExecute() {
    		loader.setMessage("Saving tower sites..");
    		loader.show();
        }
        
        @Override 
		protected void onPostExecute(String result) {
	    	 saveTowersites();
	     }

		@Override
		protected String doInBackground(Void... params) {
			/** TODO
			 * Save selected sites
			 */
			JSONObject json = new JSONObject();
			
			if (accounts.length > 0) {
				try {
					json.put("gcmid", AOTalk.getGCMRegistrationId());
					json.put("userid", accounts[0].name);
					json.put("pword", accountManager.getPassword(accounts[0]));
					
					JSONObject jsonsites = new JSONObject();
					
					for (TowerSite s : towerSites) {
						if (s.getNotify()) {
							jsonsites.put("siteid", String.valueOf(s.getId()));
						}
					}
					
					json.put("sites", jsonsites);
				} catch (JSONException e) {
					Logging.log(APP_TAG, e.getMessage());
				}
			}
			
			try {
				Logging.log(APP_TAG, json.toString(1));
			} catch (JSONException e) {
				Logging.log(APP_TAG, e.getMessage());
			}

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public void saveTowersites() {
		if (loader != null) {
			loader.dismiss();
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
			    	Message msg = Message.obtain(null, Statics.MESSAGE_MUTED_CHANNELS);
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
