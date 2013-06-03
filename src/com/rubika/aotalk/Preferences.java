package com.rubika.aotalk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.rubika.aotalk.adapter.FriendAdapter;
import com.rubika.aotalk.item.Channel;
import com.rubika.aotalk.item.Friend;
import com.rubika.aotalk.item.MonitorSite;
import com.rubika.aotalk.item.RKNAccount;
import com.rubika.aotalk.market.Market;
import com.rubika.aotalk.purchase.IabHelper;
import com.rubika.aotalk.purchase.IabResult;
import com.rubika.aotalk.purchase.Inventory;
import com.rubika.aotalk.purchase.Purchase;
import com.rubika.aotalk.service.ClientService;
import com.rubika.aotalk.ui.colorpicker.ColorPickerPreference;
import com.rubika.aotalk.util.ImageCache;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.RKNet;
import com.rubika.aotalk.util.Statics;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
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
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.RingtonePreference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import ao.misc.NameFormat;

public class Preferences extends SherlockPreferenceActivity  {
	private static final String APP_TAG = "--> The Leet :: Preferences";
	
	private Context context;
	private Activity activity;
	private Messenger service = null;
	private boolean serviceIsBound = false;	
	private PreferenceManager prefManager;
    private static Tracker tracker;
    
    private IabHelper mHelper;
    private String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwddN1kOq2TzDa3Wi1Mfc1WXP94wRjZWk0tbVCKdm+FH1kRHNEnY98/vbuOY/wWgSPQPqpO2FEyb5f70Qjoe+t0eE+jhLUX1v/8WeRDicy+kX5YAMKKBPMFAGm4FZMdbKPZBA6wh9FvDieBLJjYafyVJgiJ1QVglRFBAtQ8EmNvcYX/LCeII1b/bIyM4DjcEacl1/WP/Z6l9/Jr3egOlGe0bLiyhaKuAsEbOSSfiL/rkxQ4yqOVeAUIl4pJi+8W4DcUo+4IL1d/uxPkgoNaS7ofRtlVTZl3mBI1+ZUPUh1F2M0a090peJ8yDW4mc2DHVZ3Au8BDDutB+4L516lCqkRwIDAQAB";
    
    private static final int RC_REQUEST = 10001;
    private static final String SKU_SMALL = "small_donation";
    private static final String SKU_MEDIUM = "medium_donation";
    private static final String SKU_LARGE = "large_donation";
	
	private static List<Friend> friendList = new ArrayList<Friend>();
	private static List<Channel> channelList = new ArrayList<Channel>();
	
	private PreferenceScreen prefScreen;
	private PreferenceCategory prefCat;
    
    private String smallPrice = "";
    private String mediumPrice = "";
    private String largePrice = "";
	
	final static Messenger messenger = new Messenger(new IncomingHandler());
	
	private AccountManager accountManager;
	private Account[] accounts;
		
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
	
	@SuppressWarnings("deprecation")
	@Override 
	protected void onCreate(Bundle savedInstanceState) { 
	    super.onCreate(savedInstanceState); 

	    context = this;
	    activity = this;
	    
        getListView().setScrollingCacheEnabled(false);
        	    
        EasyTracker.getInstance().setContext(this);
        tracker = EasyTracker.getTracker();
	    
        accountManager = AccountManager.get(context);
	    
    	loader = new ProgressDialog(context);
		loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		loader.setCancelable(false);
		
		preload = new ProgressDialog(context);
		preload.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		preload.setCancelable(false);
	    
        final ActionBar bar = getSupportActionBar();
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);

        mHelper = new IabHelper(activity, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
            	Logging.log(APP_TAG, "Setup finished.");

                if (!result.isSuccess()) {
                	Logging.log(APP_TAG, "Problem setting up in-app billing: " + result);
                    return;
                }

                Logging.log(APP_TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });

        this.setPreferenceScreen(createPreferenceHierarchy());
	}
	
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        	if (result.isFailure()) {
            	Logging.log(APP_TAG, "Failed to query inventory: " + result);
                return;
            }

            Logging.log(APP_TAG, "Query inventory was successful.");
        }
    };
	
    private boolean verifyDeveloperPayload(Purchase p) {
        return true;
    }

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Logging.log(APP_TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            if (result.isFailure()) {
            	Logging.log(APP_TAG, "Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
            	Logging.log(APP_TAG, "Error purchasing. Authenticity verification failed.");
                return;
            }

            Logging.log(APP_TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_SMALL)) {
                // bought 1/4 tank of gas. So consume it.
                Logging.log(APP_TAG, "Purchase is small.");
            }
            else if (purchase.getSku().equals(SKU_MEDIUM)) {
                Logging.log(APP_TAG, "Purchase is medium.");
            }
            else if (purchase.getSku().equals(SKU_LARGE)) {
                Logging.log(APP_TAG, "Purchase is large.");
            }
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logging.log(APP_TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
        	Logging.log(APP_TAG, "onActivityResult handled by IABUtil.");
        }
    }
   
    private IabHelper.QueryInventoryFinishedListener  mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
	    public void onQueryInventoryFinished(IabResult result, Inventory inventory)   
	    {
	       if (result.isFailure()) {
	          Logging.log(APP_TAG, "Error in QueryInventoryFinishedListener");
	          return;
	        }
	
	        smallPrice = inventory.getSkuDetails(SKU_SMALL).getPrice();
	        mediumPrice = inventory.getSkuDetails(SKU_MEDIUM).getPrice();
	        largePrice = inventory.getSkuDetails(SKU_LARGE).getPrice();
	
	        showDonateList();
	    }
    };
    
    private void showDonateList() {
    	final CharSequence[] donationTypes = new CharSequence[3];
        
    	donationTypes[0] = String.format("Small donation (%s)", smallPrice);
    	donationTypes[1] = String.format("Medium donation (%s)", mediumPrice);
    	donationTypes[2] = String.format("Large donation (%s)", largePrice);
       
        new AlertDialog.Builder(this)
        .setTitle("Donation")
      
        .setItems(donationTypes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String type = null;
				String payload = ""; 
				
				switch(which) {
				case 0:
					type = SKU_SMALL;
					break;
				case 1:
					type = SKU_MEDIUM;
					break;
				case 2:
					type = SKU_LARGE;
					break;
				}
		        
		        if (mHelper.subscriptionsSupported() && type != null) {
		        	mHelper.launchPurchaseFlow(activity, type, RC_REQUEST, mPurchaseFinishedListener, payload);
		        }
			}
		})
        	        
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })

        .create().show();

    }
		
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
	    
	    prefScreen = prefManager.createPreferenceScreen(this);
	    prefScreen.setKey("donate");
    	prefScreen.setTitle(getString(R.string.donate));
	    prefScreen.setSummary(getString(R.string.donate_info));
	    prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				List<String> additionalSkuList = new ArrayList<String>();
				
				additionalSkuList.add(SKU_SMALL);
				additionalSkuList.add(SKU_MEDIUM);
				additionalSkuList.add(SKU_LARGE);
				
				//mHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);
				
				return false;
			}
	    });
	    root.addPreference(prefScreen);
	    
	    // General settings
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.user_interface));
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
	    
        CheckBoxPreference checkboxSortLoginCharacters = new CheckBoxPreference(this);
        checkboxSortLoginCharacters.setKey("sortLoginCharacters");
        checkboxSortLoginCharacters.setTitle(getString(R.string.sort_login_characters));
        checkboxSortLoginCharacters.setSummaryOn(R.string.sort_login_characters_info_on);
        checkboxSortLoginCharacters.setSummaryOff(R.string.sort_login_characters_info_off);
        checkboxSortLoginCharacters.setDefaultValue(false);
        root.addPreference(checkboxSortLoginCharacters);
	    
        CheckBoxPreference checkboxSounds = new CheckBoxPreference(this);
        checkboxSounds.setKey("enableSounds");
        checkboxSounds.setTitle(getString(R.string.enable_sounds));
        checkboxSounds.setSummaryOn(R.string.enable_sounds_info_on);
        checkboxSounds.setSummaryOff(R.string.enable_sounds_info_off);
        checkboxSounds.setDefaultValue(false);
        root.addPreference(checkboxSounds);

        CheckBoxPreference checkboxMusicVibrations = new CheckBoxPreference(this);
        checkboxMusicVibrations.setKey("enableMusicVibrations");
        checkboxMusicVibrations.setTitle(getString(R.string.enable_music_vibrations));
        checkboxMusicVibrations.setSummaryOn(R.string.enable_music_vibrations_info_on);
        checkboxMusicVibrations.setSummaryOff(R.string.enable_music_vibrations_info_off);
        checkboxMusicVibrations.setDefaultValue(false);
        root.addPreference(checkboxMusicVibrations);

        CheckBoxPreference checkboxMusicVisualizer = new CheckBoxPreference(this);
        checkboxMusicVisualizer.setKey("enableMusicVisualizer");
        checkboxMusicVisualizer.setTitle(getString(R.string.enable_music_visualizer));
        checkboxMusicVisualizer.setSummaryOn(R.string.enable_music_visualizer_info_on);
        checkboxMusicVisualizer.setSummaryOff(R.string.enable_music_visualizer_info_off);
        checkboxMusicVisualizer.setDefaultValue(true);
        root.addPreference(checkboxMusicVisualizer);
	    
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
	    
	    ColorPickerPreference colorPref6 = new ColorPickerPreference(this);
	    colorPref6.setKey("color_ocn");
	    colorPref6.setTitle(getString(R.string.color_ocn));
	    colorPref6.setSummary(getString(R.string.color_ocn_info));
	    colorPref6.setAlphaSliderEnabled(false);
	    colorPref6.setDefaultValue(Statics.COLOR_ORG_OCN);
	    prefScreen.addPreference(colorPref6);
	    
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
        checkboxPrefReconnect.setSummaryOn(R.string.automatic_reconnect_info_on);
        checkboxPrefReconnect.setSummaryOff(R.string.automatic_reconnect_info_off);
        checkboxPrefReconnect.setDefaultValue(true);
        root.addPreference(checkboxPrefReconnect);
        
        
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.messages));
        root.addPreference(prefCat);

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

	    CheckBoxPreference checkboxMuteDnet = new CheckBoxPreference(this);
	    checkboxMuteDnet.setKey("muteDnet");
	    checkboxMuteDnet.setTitle(getString(R.string.mute_dnet));
	    checkboxMuteDnet.setSummaryOn(R.string.mute_dnet_info_on);
	    checkboxMuteDnet.setSummaryOff(R.string.mute_dnet_info_off);
	    checkboxMuteDnet.setDefaultValue(false);
        root.addPreference(checkboxMuteDnet);

	    CheckBoxPreference checkboxDnetAsChannel = new CheckBoxPreference(this);
	    checkboxDnetAsChannel.setKey("dnetAsChannel");
	    checkboxDnetAsChannel.setTitle(getString(R.string.dnet_as_channel));
	    checkboxDnetAsChannel.setSummaryOn(R.string.dnet_as_channel_info_on);
	    checkboxDnetAsChannel.setSummaryOff(R.string.dnet_as_channel_info_off);
	    checkboxDnetAsChannel.setDefaultValue(false);
        root.addPreference(checkboxDnetAsChannel);
       
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
        
        /*
        CheckBoxPreference checkboxVibrateNotification = new CheckBoxPreference(this);
        checkboxVibrateNotification.setKey("notificationVibrateEnabled");
        checkboxVibrateNotification.setTitle(getString(R.string.enable_notification_vibration));
        checkboxVibrateNotification.setSummaryOn(R.string.enable_notification_vibration_info_on);
        checkboxVibrateNotification.setSummaryOff(R.string.enable_notification_vibration_info_off);
        checkboxVibrateNotification.setDefaultValue(true);
        root.addPreference(checkboxVibrateNotification);
		*/
        
        RingtonePreference ringPreference = new RingtonePreference(this);
        ringPreference.setKey("notificationSound");
        ringPreference.setTitle(getString(R.string.notification_sound));
        ringPreference.setSummary(getString(R.string.notification_sound_info));
        ringPreference.setRingtoneType(RingtoneManager.TYPE_NOTIFICATION);
        ringPreference.setDefaultValue("android.resource://com.rubika.aotalk/raw/reet" /*Settings.System.DEFAULT_NOTIFICATION_URI.toString()*/);
        ringPreference.setShowDefault(true);
        ringPreference.setShowSilent(true);
	    root.addPreference(ringPreference); 
	    
	    prefCat = new PreferenceCategory(this);
	    prefCat.setTitle(getString(R.string.towerwars));
        root.addPreference(prefCat);
        
        CheckBoxPreference checkboxPrefTowerNotification = new CheckBoxPreference(this);
        checkboxPrefTowerNotification.setKey("towerNotificationEnabled");
        checkboxPrefTowerNotification.setTitle(getString(R.string.enable_notifications));
        checkboxPrefTowerNotification.setSummaryOn(R.string.enable_tower_notifications_info_on);
        checkboxPrefTowerNotification.setSummaryOff(R.string.enable_tower_notifications_info_off);
        checkboxPrefTowerNotification.setDefaultValue(true);
        root.addPreference(checkboxPrefTowerNotification);

        CheckBoxPreference checkboxTowerVibrateNotification = new CheckBoxPreference(this);
        checkboxTowerVibrateNotification.setKey("towerNotificationVibrateEnabled");
        checkboxTowerVibrateNotification.setTitle(getString(R.string.enable_notification_vibration));
        checkboxTowerVibrateNotification.setSummaryOn(R.string.enable_notification_vibration_info_on);
        checkboxTowerVibrateNotification.setSummaryOff(R.string.enable_notification_vibration_info_off);
        checkboxTowerVibrateNotification.setDefaultValue(true);
        root.addPreference(checkboxTowerVibrateNotification);
        
        CheckBoxPreference checkboxPrefTowerLed = new CheckBoxPreference(this);
        checkboxPrefTowerLed.setKey("towerNotificationLed");
        checkboxPrefTowerLed.setTitle(getString(R.string.enable_notification_led));
        checkboxPrefTowerLed.setSummaryOn(R.string.enable_notification_led_info_on);
        checkboxPrefTowerLed.setSummaryOff(R.string.enable_notification_led_info_off);
        checkboxPrefTowerLed.setDefaultValue(true);
        root.addPreference(checkboxPrefTowerLed);

        RingtonePreference towerRingPreference = new RingtonePreference(this);
        towerRingPreference.setKey("towerNotificationSound");
        towerRingPreference.setTitle(getString(R.string.notification_sound));
        towerRingPreference.setSummary(getString(R.string.tower_notification_sound_info));
        towerRingPreference.setRingtoneType(RingtoneManager.TYPE_NOTIFICATION);
        towerRingPreference.setDefaultValue("android.resource://com.rubika.aotalk/raw/rollerrat" /*Settings.System.DEFAULT_NOTIFICATION_URI.toString()*/);
        towerRingPreference.setShowDefault(true);
        towerRingPreference.setShowSilent(true);
	    root.addPreference(towerRingPreference); 
       
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
        
        EditTextPreference orgs = new EditTextPreference(this);
        orgs.setKey("mainorg");
        orgs.setTitle(getString(R.string.main_org));
        orgs.setSummary(getString(R.string.main_org_info));
        orgs.setDefaultValue("");
        root.addPreference(orgs);
        
	    prefScreen = prefManager.createPreferenceScreen(this);
	    prefScreen.setKey("preloadmaps");
	    prefScreen.setTitle(getString(R.string.preload_maps));
	    prefScreen.setSummary(getString(R.string.preload_maps_info));
	    prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(getString(R.string.preload_maps));
				builder.setMessage(getString(R.string.preload_maps_dialog));
				builder.setPositiveButton(getString(R.string.ok), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new PreloadMaps().execute();
					}
				});
				builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				builder.create().show();

				return false;
			}
	    });
	    root.addPreference(prefScreen); 

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
	private ProgressDialog preload;
	
    public class PreloadMaps extends AsyncTask<Void, Void, String> {
        @Override    
        protected void onPreExecute() {
        	preload.setMessage("Downloading maps..");
        	preload.setProgress(0);
        	preload.setMax((65 * 5) + (144 * 5));
        	preload.show();
        }

        @Override 
		protected void onPostExecute(String result) {
        	if (loader != null) {
        		preload.dismiss();
        	}
	     }

		@Override
		protected String doInBackground(Void... params) {		
			File cacheDir = ImageCache.getCacheDirectory(TheLeet.getContext().getPackageName(), "maps");

			String path = "aosl/%d/map_%d_%d.jpg";
			for (int x = 1; x <= 5; x++) {
				for (int y = 0; y < 24; y++) {
					for (int z = 0; z < 6; z++) {
						ImageCache.preloadImage(TheLeet.getContext(), String.format(path, x, y, z), RKNet.RKNET_MAP_BASE_PATH, cacheDir, Bitmap.CompressFormat.JPEG);
						preload.incrementProgressBy(1);
					}
				}
			}
			
			path = "aork/%d/map_%d_%d.jpg";
			for (int x = 1; x <= 5; x++) {
				for (int y = 0; y < 9; y++) {
					for (int z = 0; z < 7; z++) {
						ImageCache.preloadImage(TheLeet.getContext(), String.format(path, x, y, z), RKNet.RKNET_MAP_BASE_PATH, cacheDir, Bitmap.CompressFormat.JPEG);
						preload.incrementProgressBy(1);
					}
				}
			}
						
			return null;
		}
    }
	
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
				editor.putInt("color_prv", Statics.COLOR_ORG_PRV);
				editor.putInt("color_group", Statics.COLOR_ORG_GRP);
				//editor.putInt("color_org", Statics.COLOR_ORG_ORG);
				editor.putInt("color_frn", Statics.COLOR_ORG_FRN);
				editor.putInt("color_ocn", Statics.COLOR_ORG_OCN);
				
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
	
    private List<MonitorSite> towerSites = new ArrayList<MonitorSite>();
	private RKNAccount rknetaccount = null;

    public class GetTowersites extends AsyncTask<Void, Void, String> {
        @Override    
        protected void onPreExecute() {
    		loader.setMessage(getString(R.string.loading_data) + getString(R.string.dots));
    		loader.show();
        }

        @Override 
		protected void onPostExecute(String result) {
	    	 handleTowersites();
	     }

		@Override
		protected String doInBackground(Void... params) {			
	        long loadTime = System.currentTimeMillis();
			towerSites.clear();
	    	
			HttpClient httpclient;
			HttpPost httppost;
	        
	    	HttpResponse response;
	    	HttpEntity entity;
	    	InputStream is;
	    	BufferedReader reader;
	    	StringBuilder sb;
	    	String line;
	    	String resultData;
	    	JSONArray jArray;
	    	JSONObject json_data;
	    		    	
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
		    				
		    				if (!isRegistered && AOTalk.getGCMRegistrationId() != null && !AOTalk.getGCMRegistrationId().equals("")) {
		    					Logging.log(APP_TAG, "Device not registered");
		    					
		    		    		try{
		    			    		httpclient = new DefaultHttpClient();
		    				        httppost = new HttpPost(RKNet.getApiAccountPath(RKNet.RKNET_ACCOUNT_SETKEYS));
		    		
		    				        JSONObject j = new JSONObject();
		    				        j.put("AccountId", rknetaccount.getAccountId());
		    				        j.put("Key", AOTalk.getGCMRegistrationId());
		    				        j.put("UUID", AOTalk.getDeviceIdentifier());
		    				        
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
	    	
	    	if (rknetaccount != null) {
	    		try{
		    		httpclient = new DefaultHttpClient();
			        httppost = new HttpPost(RKNet.getApiAccountPath(RKNet.RKNET_ACCOUNT_GETSITES));
	
			        JSONObject j = new JSONObject();
			        j.put("AccountId", rknetaccount.getAccountId());
			        j.put("Domain", "1");
			        
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
	
		    	try {
		    		if(resultData != null) {
		    			resultData = resultData.substring(0, resultData.lastIndexOf("}")).replace("{\"d\":", "");
			    		
		    			if((!resultData.startsWith("null"))) {
			    			jArray = new JSONArray(resultData);
			    				    			
			    	        for(int i = 0; i < jArray.length(); i++){
			    	        	json_data = jArray.getJSONObject(i);
			    	        	
			    	        	towerSites.add(new MonitorSite(
			                		json_data.getInt("SiteId"),
			                		json_data.getString("Name"),
			                		json_data.getBoolean("Enabled")
				                ));
	
			    	        }
			    		}
		    			
		            	tracker.sendTiming("Loading", System.currentTimeMillis() - loadTime, "Load tracker sites", null);
		    		}
		    	} catch(JSONException e){
		    		Logging.log(APP_TAG, "Error parsing data " + e.toString());
		    	}
	    	}
	    	
	    	Collections.sort(towerSites, new MonitorSite.SitenameComparator());
	        
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
	        	sites[i] = towerSites.get(i).getName();
	        	values[i] = towerSites.get(i).getEnabled();
	        }
	
	        Logging.log(APP_TAG, String.format("Loaded %d sites", sites.length));
	        
	        new AlertDialog.Builder(this)
	        .setTitle(getString(R.string.select_sites))
	        
	        .setMultiChoiceItems(sites, values, new OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					Logging.log(APP_TAG, "marking " + towerSites.get(which).getName());
					towerSites.get(which).setEnabled(isChecked);
				}
			})

	        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                new SaveTowersites().execute();
	            }
	        })
	        
	        .setNeutralButton(R.string.select_all, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	    			for (MonitorSite s : towerSites) {
	    				s.setEnabled(true);
	    			}
	    			
	                new SaveTowersites().execute();
	            }
	        })
	        	        
	        .setNegativeButton(R.string.select_none, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	    			for (MonitorSite s : towerSites) {
	    				s.setEnabled(false);
	    			}
	    			
	                new SaveTowersites().execute();
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
    		loader.setMessage(getString(R.string.saving_sites) + getString(R.string.dots));
    		loader.show();
        }
        
        @Override 
		protected void onPostExecute(String result) {
	    	 saveTowersites();
	     }

		@Override
		protected String doInBackground(Void... params) {
	        long loadTime = System.currentTimeMillis();

	        String sites = "";
			HttpClient httpclient;
			HttpPost httppost;
			
			for (MonitorSite s : towerSites) {
				if (s.getEnabled()) {
					if (sites.length() > 0) {
						sites += ",";
					}
					sites += s.getId();
				}
			}
			
			httpclient = new DefaultHttpClient();
	        httppost = new HttpPost(RKNet.getApiAccountPath(RKNet.RKNET_ACCOUNT_SETSITES));

	        JSONObject j = new JSONObject();
	        
	        try {
				j.put("AccountId", rknetaccount.getAccountId());
		        j.put("Domain", "1");
				j.put("Sites", sites);
		        				
		        httppost.setEntity(new StringEntity(j.toString()));
		        httppost.setHeader("Accept", "application/json");
		        httppost.setHeader("Content-type", "application/json");
		        
		        httpclient.execute(httppost);

            	tracker.sendTiming("Loading", System.currentTimeMillis() - loadTime, "Save tracker sites", null);
			} catch (JSONException e) {
				Logging.log(APP_TAG, e.getMessage());
			} catch (UnsupportedEncodingException e) {
				Logging.log(APP_TAG, e.getMessage());
			} catch (ClientProtocolException e) {
				Logging.log(APP_TAG, e.getMessage());
			} catch (IOException e) {
				Logging.log(APP_TAG, e.getMessage());
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
