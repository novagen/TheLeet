package com.rubika.aotalk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import ao.misc.NameFormat;
import ao.protocol.CharacterInfo;
import ao.protocol.DimensionAddress;
import ao.protocol.packets.toclient.CharacterListPacket;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.rubika.aotalk.adapter.ChannelAdapter;
import com.rubika.aotalk.adapter.CharacterAdapter;
import com.rubika.aotalk.adapter.ChatMessageAdapter;
import com.rubika.aotalk.adapter.FriendAdapter;
import com.rubika.aotalk.adapter.GridAdapter;
import com.rubika.aotalk.aou.AOU;
import com.rubika.aotalk.database.DatabaseHandler;
import com.rubika.aotalk.item.Account;
import com.rubika.aotalk.item.Channel;
import com.rubika.aotalk.item.Character;
import com.rubika.aotalk.item.ChatMessage;
import com.rubika.aotalk.item.Friend;
import com.rubika.aotalk.item.Tool;
import com.rubika.aotalk.map.Map;
import com.rubika.aotalk.market.Market;
import com.rubika.aotalk.recipebook.RecipeBook;
import com.rubika.aotalk.service.ClientService;
import com.rubika.aotalk.service.ServiceTools;
import com.rubika.aotalk.towerwars.Towerwars;
import com.rubika.aotalk.util.Logging;
import com.viewpagerindicator.TitlePageIndicator;

public class AOTalk extends SherlockFragmentActivity implements ViewPager.OnPageChangeListener, OnPreferenceChangeListener {
	private static final String APP_TAG = "--> AnarchyTalk";
	
	public Messenger service = null;
	private static Context context;
	private boolean serviceIsBound = false;	
	private ProgressDialog loader;
	private static DatabaseHandler databaseHandler;
	private ChatMessageAdapter messageAdapter;
	private ChannelAdapter channelAdapter;
	public GridAdapter gridAdapter;
	private FriendAdapter friendAdapter;
	private int selectedAccountToManage = 0;
	private List<SherlockFragment> fragments;
	
	private static ImageButton play;
	public boolean isPlaying = false;

	private String currentTargetChannel = "";
	private String currentTargetCharacter = "";
	private int currentUserID = 0;
	private int currentServerID = 0;
	private String currentCharacterName = "";
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private String currentShowChannel = ServiceTools.CHANNEL_MAIN;
	private ActionBar actionBar;
	private ViewPager fragmentPager;
	private TitlePageIndicator titleIndicator;
	
	private List<Channel> channelList = new ArrayList<Channel>();
	private List<Channel> privateList = new ArrayList<Channel>();
	private List<Friend> friendList = new ArrayList<Friend>();
	
	public String currentTrack = null;
			
	public final Messenger serviceMessenger = new Messenger(new ServiceHandler());

	class ServiceHandler extends Handler {
	    @SuppressWarnings("unchecked")
		@Override
	    public void handleMessage(Message message) {
	    	switch (message.what) {
	            case ServiceTools.MESSAGE_LOGIN_ERROR:
	            	Logging.toast(context, getString(R.string.login_error));
	                break;
	            case ServiceTools.MESSAGE_CHANNEL:
	            	channelList = (List<Channel>)message.obj;
	            	
	    	        updateInputHint();
	    	        updateChannelList();
	    	        updateConnectionButton();
	    	        
	            	break;
	            case ServiceTools.MESSAGE_FRIEND:
	            	friendList = (List<Friend>)message.obj;
	            	
	    	        updateFriendList();
	    	        updateMessages();
	    	        
	                break;
	            case ServiceTools.MESSAGE_IS_CONNECTED:
	    	        setDisconnect();
	    	        
	                break;
	            case ServiceTools.MESSAGE_IS_DISCONNECTED:
	    	        setAccount();
	    	        
	                break;
	            case ServiceTools.MESSAGE_UPDATE:
	            	updateMessages();
	            	
	                break;
	            case ServiceTools.MESSAGE_REGISTERED:
	    			currentUserID = message.arg1;
	    			currentServerID = message.arg2;

	    			getMessages();
	    			
	    			List<Object> registerData = (ArrayList<Object>) message.obj;
	    	        
	    			friendList = (List<Friend>)registerData.get(0);
	    	        channelList = (List<Channel>)registerData.get(1);
	    	        privateList = (List<Channel>)registerData.get(8);
	    			currentTargetChannel = (String)registerData.get(2);
	    			currentTargetCharacter = (String)registerData.get(3);
	    			currentCharacterName = (String)registerData.get(4);
	    			currentShowChannel = (String)registerData.get(5);
	    			isPlaying = (Boolean)registerData.get(6);
	    	        
	    	        updateFriendList();
	    	        updateChannelList();
	    	        updateInputHint();
	    	        updateConnectionButton();
	    	        handleInvitations((List<Channel>)registerData.get(7));
	    	        
	    	        currentTrack = (String) registerData.get(9);
	            	updatePlayerTrack();
	    	        
	    	        if (channelList.size() > 0 && settings.getBoolean("showChatWhenOnline", true)) {
						fragmentPager.setCurrentItem(1);
	    	        }
	    	        
	                break;
	            case ServiceTools.MESSAGE_STARTED:
	    	        hideLoader();
	    	        
					if (settings.getBoolean("showChatWhenOnline", true)) {
						fragmentPager.setCurrentItem(1);
					}
					
	    			currentUserID = message.arg1;
	    			currentServerID = message.arg2;
	    			
	    			List<Object> startedData = (ArrayList<Object>) message.obj;
	    			
	    			currentTargetChannel = (String)startedData.get(0);
	    			currentTargetCharacter = (String)startedData.get(1);
	    			currentCharacterName = (String)startedData.get(2);
	    			currentShowChannel = (String)startedData.get(3);
	    			
	    	        updateInputHint();
	    			getMessages();
	    			
	    	        break;
	            case ServiceTools.MESSAGE_CHARACTERS:
	    	        hideLoader();
	            	setCharacter((CharacterListPacket) message.obj);
	            	
	            	break;
	            case ServiceTools.MESSAGE_CONNECTION_ERROR:
	    	        hideLoader();
	    	        
	    	        friendList.clear();
	    	        channelList.clear();
	    	        
	    	        ((EditText) findViewById(R.id.input)).setHint(getString(R.string.disconnected));
	    	        
	            	updateMessages();
	    	        updateChannelList();
	    	        updateFriendList();
	    	        updateConnectionButton();
	    	        
	            	Logging.toast(context, getString(R.string.connection_error));
	    	        
	                break;
	            case ServiceTools.MESSAGE_CLIENT_ERROR:
	    	        hideLoader();
	            	Logging.log(APP_TAG, "Client error");
	    	        
	                break;
	            case ServiceTools.MESSAGE_WHOIS:
	    	        List<String> whoisData = (ArrayList<String>) message.obj;
	    	        showWhoIs(whoisData.get(1), whoisData.get(0));
	    	        
	                break;
	            case ServiceTools.MESSAGE_DISCONNECTED:
	            	hideLoader();
	            	
	    	        friendList.clear();
	    	        channelList.clear();
	    	        
	    	        ((EditText) findViewById(R.id.input)).setHint(getString(R.string.disconnected));
	    	        
	            	updateMessages();
	    	        updateChannelList();
	    	        updateFriendList();
	    	        updateConnectionButton();
	            	
	            	if (message.arg1 == 0 && settings.getBoolean("clearMessagesOnDisconnect", true)) {
	            		databaseHandler.deleteAllPostsForUser(currentUserID);
	            	}
	            	
	                break;
	            case ServiceTools.MESSAGE_PLAYER_ERROR:
	            	Logging.log(APP_TAG, "Player error");
	            	
	                break;
	            case ServiceTools.MESSAGE_PLAYER_STARTED:
	            	isPlaying = true;
	            	updatePlayer();

	                break;
	            case ServiceTools.MESSAGE_PLAYER_STOPPED:
	            	isPlaying = false;
	            	currentTrack = null;
	            	updatePlayer();

	                break;
	            case ServiceTools.MESSAGE_PLAYER_TRACK:
	            	currentTrack = (String) message.obj;
	            	updatePlayerTrack();
	            	
	            	break;
	            case ServiceTools.MESSAGE_PRIVATE_CHANNEL_INVITATION:
	            	handleInvitations((List<Channel>) message.obj);
	            	
	            	break;
	            case ServiceTools.MESSAGE_PRIVATE_CHANNEL:
	            	privateList = (List<Channel>) message.obj;
	            	updateChannelList();
	            	
	            	break;
	            default:
	                super.handleMessage(message);
	        }
	    }
	}
		
	private synchronized void handleInvitations(List<Channel> invitations) {
		for (final Channel channel : invitations) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.join_channel));
			builder.setMessage(String.format(getString(R.string.you_were_invited_to_channel), channel.getName().replace(ServiceTools.PREFIX_PRIVATE_GROUP, "")));
			
			builder.setPositiveButton(getString(R.string.ok), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
			        Message msg = Message.obtain(null, ServiceTools.MESSAGE_PRIVATE_CHANNEL_JOIN);
			        msg.replyTo = serviceMessenger;
			        msg.obj = channel;
			        
			        try {
						service.send(msg);
					} catch (RemoteException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
				}
			});

			builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
			        Message msg = Message.obtain(null, ServiceTools.MESSAGE_PRIVATE_CHANNEL_DENY);
			        msg.replyTo = serviceMessenger;
			        msg.obj = channel;
			        
			        try {
						service.send(msg);
					} catch (RemoteException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
				}
			});

			builder.create().show();
		}
	}
	
	private void updatePlayer() {
		if (isPlaying) {
        	Logging.log(APP_TAG, "Player started");
        	play.setImageResource(R.drawable.ic_menu_stop);
		} else {
        	Logging.log(APP_TAG, "Player stopped");
        	play.setImageResource(R.drawable.ic_menu_play);			
		}
	}
	
	private void updatePlayerTrack() {
    	FragmentTools fragment = (FragmentTools)fragments.get(0);

    	if (fragment != null && currentTrack != null) {
    		fragment.updateTitle(currentTrack);
    	}
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder ibinder) {
	        service = new Messenger(ibinder);

	        try {
	            Message message = Message.obtain(null, ServiceTools.MESSAGE_CLIENT_REGISTER);
	            message.replyTo = serviceMessenger;
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
		currentTargetChannel = settings.getString("lastUsedChannel", currentTargetChannel);
		currentTargetCharacter = settings.getString("lastUsedCharacter", currentTargetCharacter);

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
	                msg.replyTo = serviceMessenger;
	                service.send(msg);
	            } catch (RemoteException e) {
					Logging.log(APP_TAG, e.getMessage());
	            }
	        }

	        unbindService(serviceConnection);
	        serviceIsBound = false;
	    }
		
		editor.putString("lastUsedChannel", currentTargetChannel);
		editor.putString("lastUsedCharacter", currentTargetCharacter);
		editor.commit();
	}
	
	private void sendMessage(String tell) {
		Logging.log(APP_TAG, "sendMessage\ncurrentTargetChannel: " + currentTargetChannel + "\ncurrentTargetCharacter: " + currentTargetCharacter);
		ChatMessage chatMessage = null;
		
		List<Channel> tempList = new ArrayList<Channel>();
		tempList.addAll(channelList);
		tempList.addAll(privateList);
		
		if (currentTargetChannel.length() > 0) {
			for (Channel channel : tempList) {
	        	if (channel.getName().equals(currentTargetChannel)) {
		        	chatMessage = new ChatMessage(System.currentTimeMillis(), tell, "", channel.getName(), 0, 0);
		        	break;
	        	}
	        }
		} else if (currentTargetCharacter.length() > 0) {
        	chatMessage = new ChatMessage(System.currentTimeMillis(), tell, currentTargetCharacter, "", 0, 0);			
		}
        
		if (chatMessage != null) {
			Logging.log(APP_TAG, "Sending message to service");
			try {
	            Message message = Message.obtain(null, ServiceTools.MESSAGE_SEND);
	            message.arg1 = 1;
	            message.obj = chatMessage;
	            message.replyTo = serviceMessenger;
	            
	            service.send(message);
	        } catch (RemoteException e) {
				Logging.log(APP_TAG, e.getMessage());
	        }
		}
	}
	
	private void whoIs(final String name, final int server, final boolean manual) {
		setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
		
        if (settings.getBoolean("whoisFromWeb", true) || manual) {
			new Thread(new Runnable() { 
	            public void run(){
					Message msg = Message.obtain();
					msg.what = 0;
					
					List<String> whoisData = ServiceTools.getUserData(context, name, server);
	
					if (whoisData == null) {
						whoisData = new ArrayList<String>();
						whoisData.add("");
						whoisData.add("");
						whoisData.add(name);
					}
	
					msg.obj = whoisData;
					msg.arg1 = manual? 1 : 0;
					whoIsHandler.sendMessage(msg);
				}
			}).start();
        } else {
			ChatMessage chatMessage = new ChatMessage(
					System.currentTimeMillis(),
					String.format(ServiceTools.WHOIS_MESSAGE, name),
					ServiceTools.BOTNAME,
					ServiceTools.CHANNEL_PM,
					0,
					0
				);

			Message message = Message.obtain();
			message.what = ServiceTools.MESSAGE_SEND;
			message.arg1 = 0;
			message.obj = chatMessage;
			
			try {
				service.send(message);
			} catch (RemoteException e) {
				Logging.log(APP_TAG, e.getMessage());
			}
        	
        }
	}
	
	private Handler whoIsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			@SuppressWarnings("unchecked")
			List<String> whoisData = (ArrayList<String>) msg.obj;
			
			if (whoisData.get(0).equals("")) {
				if (settings.getBoolean("whoisFallbackToBot", true) && channelList.size() > 0 && msg.arg1 != 1) {
					ChatMessage chatMessage = new ChatMessage(
							System.currentTimeMillis(),
							String.format(ServiceTools.WHOIS_MESSAGE, whoisData.get(2)),
							ServiceTools.BOTNAME,
							ServiceTools.CHANNEL_PM,
							0,
							0
						);
	
					Message message = Message.obtain();
					message.what = ServiceTools.MESSAGE_SEND;
					message.arg1 = 0;
					message.obj = chatMessage;
					
					try {
						service.send(message);
					} catch (RemoteException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
				} else {
					showWhoIs(getString(R.string.no_char_data), getString(R.string.no_char_title));
				}
			} else {
				showWhoIs(whoisData.get(1), whoisData.get(0));
			}
		}
	};
	
	private void showWhoIs(String message, String name) {
		setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
		
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.alert_whois, null);

		if (name.equals("")) {
			name = getString(R.string.no_char_title);
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setTitle(Html.fromHtml(name));
		builder.setPositiveButton(
			getString(R.string.ok),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			}
		);

		final AlertDialog dialog = builder.create();

		final WebView webView = (WebView) layout.findViewById(R.id.whois);
		webView.setBackgroundColor(Color.parseColor("#000000"));
		webView.loadData(Uri.encode(ServiceTools.HTML_START
			+ message
			+ ServiceTools.HTML_END), "text/html", "UTF-8"
		);
		
		WebViewClient webClient = new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				dialog.show();
			}
		};
		
		webView.setWebViewClient(webClient);
	}
	
	private void manualWhoIs() {
        if (channelList.size() <= 0 && !settings.getBoolean("whoisFromWeb", true)) {
        	Logging.toast(context, getString(R.string.not_connected));
        } else {
			LayoutInflater factory = LayoutInflater.from(context);
	        View view = factory.inflate(R.layout.alert_lookup, null);
	        final EditText username = (EditText) view.findViewById(R.id.username);
	        
	        AlertDialog.Builder builder = new AlertDialog.Builder(context);
	        builder.setTitle(R.string.whois);
	        builder.setView(view);
	        
	        final Spinner spinner = (Spinner) view.findViewById(R.id.server);
	        
	        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
					int server = 0;
					
	            	switch(spinner.getSelectedItemPosition()) {
					case 0:
						server = DimensionAddress.RK1.getID();
						break;
					case 1:
						server = DimensionAddress.RK2.getID();
						break;
					default:
						server = DimensionAddress.RK1.getID();
	            	}
	            	
	            	if (username.getText().toString().length() > 0) {
	            		whoIs(NameFormat.format(username.getText().toString()), server, true);
	            	}
	            }
	        });
	        
	        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            }
	        });
	        
	        builder.create();
	        builder.show();
        }
	}
	
	private void updateConnectionButton() {
		if (channelList.size() > 1) {
			gridAdapter.getItem(0).setName(getString(R.string.disconnect));
		} else {
			gridAdapter.getItem(0).setName(getString(R.string.connect));
		}
		
		gridAdapter.notifyDataSetChanged();

	}
	
	private synchronized void updateChannelList() {
		List<Channel> tempList = new ArrayList<Channel>();

		tempList.addAll(channelList);
		Collections.sort(tempList, new Channel.CustomComparator());
		
		List<Channel> privList = new ArrayList<Channel>();
		privList.addAll(privateList);
		
		Collections.sort(privList, new Channel.CustomComparator());
		tempList.addAll(privList);
		
		String lastShowChannel = currentShowChannel;
		
		channelAdapter.clear();
		channelAdapter.add(new Channel("AnarchyTalk", 0, true, false));
		
		if (tempList.size() > 0) {
			channelAdapter.add(new Channel("Private messages", 0, true, false));
		}
		
		for (Channel channel : tempList) {
			if (channel.getEnabled() && !channel.getMuted()) {
				channelAdapter.add(channel);
			}
		}
	
		channelAdapter.notifyDataSetChanged();
		
		int navigationItem = 0;
		int navigationCount = 2;
		
		for (Channel channel : tempList) {
			Logging.log(APP_TAG, "Found channel "  + channel.getName() + ", want channel "  + lastShowChannel);
			if (lastShowChannel.equals(channel.getName())) {
				navigationItem = navigationCount;
				break;
			}
			
			navigationCount++;
		}
		
		if (lastShowChannel.equals(ServiceTools.CHANNEL_MAIN)) {
			navigationItem = 0;
		}
		
		if (lastShowChannel.equals(ServiceTools.CHANNEL_PM) && tempList.size() > 0) {
			navigationItem = 1;
		}
		
		Logging.log(APP_TAG, "Setting channel to " + navigationItem);
		actionBar.setSelectedNavigationItem(navigationItem);
	}
	
	private synchronized void updateFriendList() {
		Logging.log(APP_TAG, "updateFriendList");
		
		List<Friend> tempList = new ArrayList<Friend>();
		tempList.addAll(friendList);
		
		if (settings.getBoolean("showOnlyOnline", false)) {
			for (int i = tempList.size() - 1; i >= 0; i--) {
				if (!tempList.get(i).isOnline()) {
					tempList.remove(i);
				}
			}
		}
		
		Collections.sort(tempList, new Friend.CustomComparator());
		
		friendAdapter.clear();
		
		for (Friend friend : tempList) {
			friendAdapter.add(friend);
		}
		
		friendAdapter.notifyDataSetChanged();
	}

	private void updateInputHint() {
	    boolean hintIsSet = false;
	    
	    List<Channel> tempList = new ArrayList<Channel>();
	    tempList.addAll(channelList);
	    tempList.addAll(privateList);
	    
	    Logging.log(APP_TAG, "currentTargetChannel: '" + currentTargetChannel + "', currentTargetCharacter: '" + currentTargetCharacter + "'" + ", listsize: " + tempList.size());
	    
		if (currentTargetChannel.equals("") && currentTargetCharacter.equals("") && tempList.size() > 0) {
	    	((EditText) findViewById(R.id.input)).setHint(getString(R.string.select_channel));
		} else if (tempList.size() == 0) {
        	((EditText) findViewById(R.id.input)).setHint(getString(R.string.disconnected));
		} else {
			if (!currentTargetChannel.equals("")) {
				if (tempList.size() > 0) {
		        	for (Channel channel : tempList) {
		        		if (channel.getName().equals(currentTargetChannel)) {
		    				((EditText) findViewById(R.id.input)).setHint(channel.getName());
		    				hintIsSet = true;
		    				break;
		        		}
		        	}
		        	
		        	if (!hintIsSet) {
		            	((EditText) findViewById(R.id.input)).setHint(getString(R.string.select_channel));
		            }
		        }
			}
			
			if (!currentTargetCharacter.equals("")) {
				((EditText) findViewById(R.id.input)).setHint(String.format(getString(R.string.tell), currentTargetCharacter));
			}
	    }
	}

	private synchronized void getMessages() {
    	if (currentUserID != 0) {
	    	
	    	List<ChatMessage> newMessages = databaseHandler.getAllPostsForUser(
	    			currentUserID, 
	    			currentServerID, 
	    			currentShowChannel
	    		);
	    	
		    messageAdapter.clear();
	    	
	    	for (ChatMessage message : newMessages) {
	    		message.showAnimation(false);
	    		messageAdapter.add(message);
	    	}
	    	
	    	messageAdapter.notifyDataSetChanged();
    	}
	}
	
	private synchronized void updateMessages() {
		if (currentUserID != 0 && messageAdapter.getCount() > 0) {
    		List<ChatMessage> newMessages = databaseHandler.getNewPostsForUser(
	    			currentUserID, 
	    			messageAdapter.getItem(messageAdapter.getCount() - 1).getId(), 
	    			currentServerID, 
	    			currentShowChannel
	    		);
	    	
	    	for (ChatMessage message : newMessages) {
	    		//message.showAnimation(false);
	    		messageAdapter.add(message);
	    	}
	    	
	    	if (newMessages.size() > 0) {
	    		messageAdapter.notifyDataSetChanged();
	    	}
    	}
    	
    	if (currentUserID != 0 && messageAdapter.getCount() == 0) {
    		getMessages();
    	}
 	}
	
	private void setAccount() {
		final List<Account> accounts = new DatabaseHandler(context).getAllAccounts();
		int numberOfAccounts = 0;
		
		if (accounts != null) {
			numberOfAccounts = accounts.size();
		}
		
		final CharSequence[] listItems = new CharSequence[numberOfAccounts + 1];
		
		listItems[0] = getString(R.string.add_account);
		
		for (int i = 0; i < numberOfAccounts; i++) {
			String serverName = "";
			if (accounts.get(i).getServer() == DimensionAddress.RK1) {
				serverName = "RK1";
			}
			if (accounts.get(i).getServer() == DimensionAddress.RK2) {
				serverName = "RK2";
			}
			if (accounts.get(i).getServer() == DimensionAddress.TEST) {
				serverName = "Test";
			}
			listItems[i + 1] = serverName + " - " + accounts.get(i).getUsername();
		}
	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.select_account));
		builder.setItems(
			listItems,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, final int item) {
					if (item > 0) {
						showLoader(getString(R.string.connecting));
						final Account account = accounts.get(item - 1);
						
						new Thread() {
							public void run() {
				                Message msg = Message.obtain(null, ServiceTools.MESSAGE_CONNECT);
				                msg.replyTo = serviceMessenger;
				                msg.obj = account;
				                
				                try {
									service.send(msg);
								} catch (RemoteException e) {
									Logging.log(APP_TAG, e.getMessage());
								}
							}
						}.start();
					} else {
						newAccount();
					}
				}
			}
		);
		
		if (numberOfAccounts > 0) {
			builder.setNeutralButton(getString(R.string.manage), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					manageAccount();
				}
			});
		}
	
		builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
	
		builder.create().show();
	}

	private void manageAccount() {
		final List<Account> accounts = new DatabaseHandler(context).getAllAccounts();
		int numberOfAccounts = 0;
		
		if (accounts != null) {
			numberOfAccounts = accounts.size();
		}
		
		final CharSequence[] listItems = new CharSequence[numberOfAccounts];
		
		for (int i = 0; i < numberOfAccounts; i++) {
			String serverName = "";
			if (accounts.get(i).getServer() == DimensionAddress.RK1) {
				serverName = "RK1";
			}
			if (accounts.get(i).getServer() == DimensionAddress.RK2) {
				serverName = "RK2";
			}
			if (accounts.get(i).getServer() == DimensionAddress.TEST) {
				serverName = "Test";
			}
			listItems[i] = serverName + " - " + accounts.get(i).getUsername();
		}
	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.select_account));
		builder.setSingleChoiceItems(listItems, 0, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	selectedAccountToManage = which;
	        }
	    });
		
		builder.setNeutralButton(getString(R.string.delete), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(getString(R.string.delete_account));
				builder.setMessage(getString(R.string.confirm_delete_account));
				
				builder.setPositiveButton(getString(R.string.ok), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						databaseHandler.deleteAccount(accounts.get(selectedAccountToManage));
						manageAccount();
					}
				});
	
				builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						manageAccount();
					}
				});
	
				builder.create().show();
	
			}
		});
	
		builder.setNegativeButton(getString(R.string.edit), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				editAccount(accounts.get(selectedAccountToManage));
			}
		});
	
		builder.setPositiveButton(getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setAccount();
			}
		});
	
		builder.create().show();
	}

	private void newAccount() {
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		
		final View layout = inflater.inflate(R.layout.alert_account, null);
		final Spinner spinner = (Spinner) layout.findViewById(R.id.server);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(getResources().getString(R.string.login_title));
		builder.setView(layout);

		builder.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (
							((EditText) layout.findViewById(R.id.username)).getText().toString().length() > 0
							&&
							((EditText) layout.findViewById(R.id.password)).getText().toString().length() > 0
						) {
							showLoader(getString(R.string.connecting));
	
							new Thread() {
								public void run() {
									DimensionAddress server;
	
									switch(spinner.getSelectedItemPosition()) {
										case 0:
											server = DimensionAddress.RK1;
											break;
										case 1:
											server = DimensionAddress.RK2;
											break;
										case 2:
											server = DimensionAddress.TEST;
											break;
										default:
											server = DimensionAddress.RK1;
									}
									Account account = new Account(
											((EditText) layout.findViewById(R.id.username)).getText().toString(),
											((EditText) layout.findViewById(R.id.password)).getText().toString(),
											server,
											false,
											0
									);
									
									if (((CheckBox) layout.findViewById(R.id.savepassword)).isChecked()) {
										new DatabaseHandler(context).addAccount(account);
									}
									
					                Message msg = Message.obtain(null, ServiceTools.MESSAGE_CONNECT);
					                msg.replyTo = serviceMessenger;
					                msg.obj = account;
					                
					                try {
										service.send(msg);
									} catch (RemoteException e) {
										Logging.log(APP_TAG, e.getMessage());
									}
								}
							}.start();
						} else {
							Logging.toast(context, getString(R.string.u_and_p_required));
						}
					}
				});

		builder.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				setAccount();
			}
		});
		
		builder.create().show();
	}
	
	private void editAccount(final Account account) {
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		
		final View layout = inflater.inflate(R.layout.alert_account, null);
		
		((CheckBox) layout.findViewById(R.id.savepassword)).setVisibility(View.GONE);
		
		((EditText) layout.findViewById(R.id.username)).setText(account.getUsername());
		((EditText) layout.findViewById(R.id.password)).setText(account.getPassword());
		
		int spinnerSelection = 0;
		if (account.getServer() == DimensionAddress.RK1) {
			spinnerSelection = 0;
		}
		if (account.getServer() == DimensionAddress.RK2) {
			spinnerSelection = 1;
		}
		if (account.getServer() == DimensionAddress.TEST) {
			spinnerSelection = 2;
		}

		((Spinner) layout.findViewById(R.id.server)).setSelection(spinnerSelection);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(getResources().getString(R.string.login_title));
		builder.setView(layout);

		builder.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (
							((EditText) layout.findViewById(R.id.username)).getText().toString().length() > 0
							&&
							((EditText) layout.findViewById(R.id.password)).getText().toString().length() > 0
						) {
							DimensionAddress server;
							
							switch(((Spinner) layout.findViewById(R.id.server)).getSelectedItemPosition()) {
								case 0:
									server = DimensionAddress.RK1;
									break;
								case 1:
									server = DimensionAddress.RK2;
									break;
								case 2:
									server = DimensionAddress.TEST;
									break;
								default:
									server = DimensionAddress.RK1;
							}
							
							account.setUsername(((EditText) layout.findViewById(R.id.username)).getText().toString());
							account.setPassword(((EditText) layout.findViewById(R.id.password)).getText().toString());
							account.setServer(server);
							
							new DatabaseHandler(context).updateAccount(account);
							manageAccount();
						} else {
							Logging.toast(context, getString(R.string.u_and_p_required));
						}
					}
				});

		builder.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				manageAccount();
			}
		});
		
		builder.create().show();
	}
	
	private void setDisconnect() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.disconnect));
		builder.setMessage(getString(R.string.confirm_disconnect));
		
		builder.setPositiveButton(getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
                Message msg = Message.obtain(null, ServiceTools.MESSAGE_DISCONNECT);
                msg.replyTo = serviceMessenger;
                
                try {
					service.send(msg);
				} catch (RemoteException e) {
					Logging.log(APP_TAG, e.getMessage());
				}
			}
		});

		builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		builder.create().show();
	}
	
	private void setCharacter(final CharacterListPacket charpacket) {
		final List<Character> listItems = new ArrayList<Character>();

		if (charpacket != null) {
			for (int i = 0; i < charpacket.getNumCharacters(); i++) {
				listItems.add(new Character(charpacket.getCharacter(i).getName(), 0, 0));
			}
			
			if (settings.getBoolean("sortLoginCharacters", false)) {
				Collections.sort(listItems, new Character.CustomComparator());
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.select_character));
			builder.setAdapter(
				new CharacterAdapter(this, R.layout.list_item, listItems),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, final int item) {
						showLoader(getString(R.string.connecting));

						final CharacterInfo character = charpacket.getCharacter(item);

						new Thread() {
							public void run() {
				                Message msg = Message.obtain(null, ServiceTools.MESSAGE_CHARACTER);
				                msg.replyTo = serviceMessenger;
				                msg.obj = character;
				                
				                try {
									service.send(msg);
								} catch (RemoteException e) {
									Logging.log(APP_TAG, e.getMessage());
								}
							}
						}.start();

						return;
					}
				}
			);
			
			builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
	                Message msg = Message.obtain(null, ServiceTools.MESSAGE_DISCONNECT);
	                msg.replyTo = serviceMessenger;
	                
	                try {
						service.send(msg);
					} catch (RemoteException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
				}
			});

			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
	                Message msg = Message.obtain(null, ServiceTools.MESSAGE_DISCONNECT);
	                msg.replyTo = serviceMessenger;
	                
	                try {
						service.send(msg);
					} catch (RemoteException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
				}
			});

			builder.create().show();
		}
	}
	
	private void setChannel() {
		if (channelList.size() > 0) {
			final List<Channel> tempList = new ArrayList<Channel>();
			
			for (Channel channel : channelList) {
				if (channel.getEnabled() && !channel.getMuted()) {
					tempList.add(channel);
				}
			}
			
			Collections.sort(tempList, new Channel.CustomComparator());
			
			List<Channel> privList = new ArrayList<Channel>();
			privList.addAll(privateList);
			Collections.sort(privList, new Channel.CustomComparator());
			
			tempList.addAll(privList);
			
			int itemSize = 0;
			
			if (!currentShowChannel.equals(ServiceTools.CHANNEL_PM)) {
				itemSize = tempList.size();
			}
			
			final CharSequence[] listItems = new CharSequence[itemSize + 2];
	
			listItems[0] = getString(R.string.select_friend);
			listItems[1] = getString(R.string.enter_name);
			
			for (int i = 0; i < itemSize; i++) {
				listItems[i + 2] = tempList.get(i).getName();
			}
	
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.select_channel));
			builder.setItems(
				listItems,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, final int item) {
						if (item == 0) {
							final List<Friend> tempList = new ArrayList<Friend>();
							tempList.addAll(friendList);
							
							if (settings.getBoolean("showOnlyOnline", false)) {
								for (int i = tempList.size() - 1; i >= 0; i--) {
									if (!tempList.get(i).isOnline()) {
										tempList.remove(i);
									}
								}
							}
							
							Collections.sort(tempList, new Friend.CustomComparator());
							
							AlertDialog.Builder builder = new AlertDialog.Builder(context);
							builder.setTitle(getString(R.string.select_friend));
							builder.setAdapter(new FriendAdapter(context, R.id.friendlist, tempList), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										currentTargetCharacter = tempList.get(which).getName();
										currentTargetChannel = "";
										setServiceTargets();
									}
								}
							);
							
							builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									setChannel();
								}
							});
					
							builder.create().show();
						} else if (item == 1) {
				            LayoutInflater factory = LayoutInflater.from(context);
				            View view = factory.inflate(R.layout.alert_character, null);
				            final EditText username = (EditText) view.findViewById(R.id.username);
				            
				            AlertDialog.Builder builder = new AlertDialog.Builder(context);
			                builder.setTitle(R.string.enter_name);
			                builder.setView(view);
			                
			                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int whichButton) {
			                    	if (username.getText().toString().length() > 0) {
			                    		currentTargetCharacter = NameFormat.format(username.getText().toString());
										currentTargetChannel = "";
										setServiceTargets();
			                    	}
			                    }
			                });
			                
			                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int whichButton) {
			                    	setChannel();
			                    }
			                });
			                
			                builder.create();
			                builder.show();
						} else if (item > 1) {
							currentTargetCharacter = "";
							currentTargetChannel = tempList.get(item - 2).getName();
						}
						
						setServiceTargets();
					}
				}
			);
			
			builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
	
			builder.create().show();
		}
	}
	
	private void setServiceTargets() {
		if (service != null) {
			Message msg = Message.obtain(null, ServiceTools.MESSAGE_SET_CHARACTER);
	        msg.obj = currentTargetCharacter;
	        msg.replyTo = serviceMessenger;
	        
	        try {
				service.send(msg);
			} catch (RemoteException e) {
				Logging.log(APP_TAG, e.getMessage());
			}
			
	        msg = Message.obtain(null, ServiceTools.MESSAGE_SET_CHANNEL);
	        msg.obj = currentTargetChannel;
	        msg.replyTo = serviceMessenger;
	        
	        try {
				service.send(msg);
			} catch (RemoteException e) {
				Logging.log(APP_TAG, e.getMessage());
			}
			
	        msg = Message.obtain(null, ServiceTools.MESSAGE_SET_SHOW);
	        msg.obj = currentShowChannel;
	        msg.replyTo = serviceMessenger;
	        
	        try {
				service.send(msg);
			} catch (RemoteException e) {
				Logging.log(APP_TAG, e.getMessage());
			}
		}
		
		updateInputHint();		
	}
	
	private void showLoader(String message) {
		loader.setMessage(message + getString(R.string.dots));
		loader.show();
	}
	
	private void hideLoader() {
    	if (loader != null) {
    		loader.dismiss();
    	}		
	}
			
	private List<Tool> getToolItems() {
		List<Tool> toolList = new ArrayList<Tool>();
		
		toolList.add(new Tool(getString(R.string.connect), R.drawable.icon_refresh, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Message msg = Message.obtain(null, ServiceTools.MESSAGE_STATUS);
					msg.replyTo = serviceMessenger;
					service.send(msg);
				} catch (RemoteException e) {
					Logging.log(APP_TAG, e.getMessage());
				}
			}
		}));

		toolList.add(new Tool(getString(R.string.whois), R.drawable.icon_search, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				manualWhoIs();
			}
		}));
		
		toolList.add(new Tool(getString(R.string.market_monitor), R.drawable.icon_shopping, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, Market.class);
				startActivity(intent);
			}
		}));
		
		toolList.add(new Tool(getString(R.string.ao_universe), R.drawable.icon_puzzle, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, AOU.class);
				startActivity(intent);
			}
		}));
		toolList.add(new Tool(getString(R.string.recipebook), R.drawable.icon_books, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, RecipeBook.class);
				startActivity(intent);
			}
		}));

		toolList.add(new Tool(getString(R.string.towerwars), R.drawable.icon_chess, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, Towerwars.class);
				startActivity(intent);
			}
		}));
		

		toolList.add(new Tool(getString(R.string.map), R.drawable.icon_globe, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, Map.class);
				startActivity(intent);
			}
		}));

		/*
		toolList.add(new Tool(getString(R.string.media), R.drawable.icon_television, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, Media.class);
				startActivity(intent);
			}
		}));
		*/
		
		toolList.add(new Tool(getString(R.string.preferences), R.drawable.icon_process, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, Preferences.class);
				startActivity(intent);
			}
		}));
		
		return toolList;
	}
	
	public static Context getContext() {
		return context;
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
        
        if (settings.getBoolean("firstRun", true)) {
        	editor.clear();
        	editor.putBoolean("firstRun", false);
        	editor.commit();
        }
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.main);
        setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
		
		friendAdapter = new FriendAdapter(this, R.id.friendlist, friendList);

		channelAdapter = new ChannelAdapter(this, R.id.messagelist, channelList);
		channelAdapter.add(new Channel(getString(R.string.app_name), 0, true, false));
		
		actionBar = getSupportActionBar();
		
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);		
		actionBar.setListNavigationCallbacks(channelAdapter, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition , long itemId) {
				messageAdapter.clear();
				
				if (currentShowChannel == ServiceTools.CHANNEL_MAIN) {
					editor.putString("lastUsedChannel", currentTargetChannel);
					editor.putString("lastUsedCharacter", currentTargetCharacter);
					editor.commit();
				}
				
				if (itemPosition == 0) {
					currentTargetChannel = settings.getString("lastUsedChannel", "");
					currentTargetCharacter = settings.getString("lastUsedCharacter", "");
					currentShowChannel = ServiceTools.CHANNEL_MAIN;
				} else if (itemPosition == 1) {
					currentTargetChannel = "";
					currentTargetCharacter = settings.getString("lastUsedCharacter", "");
					currentShowChannel = ServiceTools.CHANNEL_PM;
				} else {
					List<Channel> tempList = new ArrayList<Channel>();
					tempList.addAll(channelList);
					tempList.addAll(privateList);
					
					for (Channel channel : tempList) {
						if (channel.getName().equals(channelAdapter.getItem(itemPosition).getName())) {
							currentTargetChannel = channelAdapter.getItem(itemPosition).getName();
							currentTargetCharacter = "";
							currentShowChannel = channelAdapter.getItem(itemPosition).getName();
							
							break;
						}
					}
				}
				
				if (itemPosition > 1) {
					((ImageButton) findViewById(R.id.channel)).setVisibility(View.GONE);
				} else {
					((ImageButton) findViewById(R.id.channel)).setVisibility(View.VISIBLE);
				}
				
				setServiceTargets();
				updateMessages();
				
				return true;
			}
		});
		
        context = this;
        
		loader = new ProgressDialog(context);
		loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		loader.setCancelable(false);

        databaseHandler = new DatabaseHandler(this);
        messageAdapter = new ChatMessageAdapter(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<ChatMessage>(), settings.getBoolean("enableAnimations", true));
		
        OnItemLongClickListener chatFragmentClickListener = new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final ChatMessage message = messageAdapter.getItem(arg2);
				
				if (!message.getChannel().equals(ServiceTools.CHANNEL_SYSTEM) && message.getCharacter() != null && message.getCharacter().length() > 0) {
					int diff = 0;
					
					if (message.getCharacter().equals(currentCharacterName)) {
						diff++;
					}
					
					if (message.getChannel().equals(ServiceTools.CHANNEL_PM) || message.getChannel().equals(ServiceTools.CHANNEL_MAIN) || message.getChannel().equals(ServiceTools.CHANNEL_FRIEND)) {
						diff++;
					}
					
					final CharSequence[] listItems = new CharSequence[3 - diff];
					
					int itemCharacter = -1;
					int itemChannel = -1;
					int itemWhois = -1;
					int currentItem = 0;
					
					if (!message.getCharacter().equals(currentCharacterName)) {
						listItems[currentItem] = "Message to " + message.getCharacter();
						itemCharacter = currentItem;
						currentItem++;
					}
					
					Logging.log(APP_TAG, "Channel is " + message.getChannel());
					
					if (!message.getChannel().equals(ServiceTools.CHANNEL_PM) && !message.getChannel().equals(ServiceTools.CHANNEL_MAIN) && !message.getChannel().equals(ServiceTools.CHANNEL_FRIEND)) {
						listItems[currentItem] = "Message to " + message.getChannel();
						itemChannel = currentItem;
						currentItem++;
					}
					
					listItems[currentItem] = String.format(getString(R.string.who_is), message.getCharacter());
					itemWhois = currentItem;
					
					final int fItemCharacter = itemCharacter;
					final int fItemChannel = itemChannel;
					final int fItemWhois = itemWhois;

					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle(getString(R.string.select_action));
					builder.setItems(
						listItems,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, final int item) {
								if (item == fItemCharacter && fItemCharacter >= 0) {
									currentTargetCharacter = message.getCharacter();
									currentTargetChannel = "";
									
									setServiceTargets();
								}
								
								if (item == fItemChannel && fItemChannel >= 0) {
									currentTargetCharacter = "";
									
									List<Channel> tempList = new ArrayList<Channel>();
									tempList.addAll(channelList);
									tempList.addAll(privateList);
									
									for (Channel channel : tempList) {
										if (channel.getName().toLowerCase().equals(message.getChannel().toLowerCase())) {
											currentTargetChannel = channel.getName();
											break;
										}
									}
									
									setServiceTargets();
								}
								
								if (item == fItemWhois && fItemWhois >= 0) {
									whoIs(message.getCharacter(), currentServerID, false);								
								}
							}
						}
					);

					builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
				
					builder.create().show();
					
					return true;
				} else {
					return false;
				}
			}
		};
		
        OnItemLongClickListener friendFragmentClickListener = new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final Friend friend = friendAdapter.getItem(arg2);
				final CharSequence[] listItems = new CharSequence[3];
				
				listItems[0] = getString(R.string.send_private_message);
				listItems[1] = String.format(getString(R.string.who_is), friend.getName());
				listItems[2] = getString(R.string.remove_friend);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(friend.getName());
				builder.setItems(
					listItems,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, final int item) {
							if (item == 0) {
								editor.putString("lastUsedChannel", "");
								editor.putString("lastUsedCharacter", friend.getName());
								editor.commit();
								
								currentTargetCharacter = friend.getName();
								currentTargetChannel = "";

								actionBar.setSelectedNavigationItem(0);
								fragmentPager.setCurrentItem(1);
																
								setServiceTargets();
								
								return;
							}
							
							if (item == 1) {
								whoIs(friend.getName(), currentServerID, false);								
							}
							
							if (item == 2) {
				    	    	final String fname = friend.getName();
				    	    	
				    	    	AlertDialog acceptRemoveDialog = new AlertDialog.Builder(context).create();
				    	    	
				    	    	acceptRemoveDialog.setTitle(String.format(getString(R.string.remove_friend_title), fname));
				    	    	acceptRemoveDialog.setMessage(String.format(getString(R.string.remove_friend_confirm), fname));
				    	    		            
				    	    	acceptRemoveDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
				    				public void onClick(DialogInterface dialog, int which) {
				    			    	Message msg = Message.obtain(null, ServiceTools.MESSAGE_FRIEND_REMOVE);
				    			        msg.replyTo = serviceMessenger;
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

								return;
							}
						}
					}
				);
				
				builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		
				builder.create().show();

				return true;
			}
		};
		
		TextWatcher friendFragmentTextListener = new TextWatcher() {
			@Override
			public void afterTextChanged(Editable e) {
				List<Friend> tempList = new ArrayList<Friend>();
				tempList.addAll(friendList);
				
				if (settings.getBoolean("showOnlyOnline", false)) {
					for (int i = tempList.size() - 1; i >= 0; i--) {
						if (!tempList.get(i).isOnline()) {
							tempList.remove(i);
						}
					}
				}
				
				Collections.sort(tempList, new Friend.CustomComparator());
				
				friendAdapter.clear();

				for (Friend friend : tempList) {
					if (friend.getName().toLowerCase().startsWith(e.toString().toLowerCase()) || e.toString().equals("")) {
						friendAdapter.add(friend);
					}
				}
				
				friendAdapter.notifyDataSetChanged();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		};
		
        View.OnClickListener inputChannelClick = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setChannel();
			}
		};
		
		OnKeyListener inputTextClick = new OnKeyListener() {
			@Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
		        	if (((EditText) v).getText().toString().length() > 0) {
						sendMessage(((EditText) v).getText().toString());
						((EditText) v).setText("");
		        	}
	            	return true;
				}
				
				return false;
			}
        };
        
		gridAdapter = new GridAdapter(this, R.id.grid, getToolItems());

		fragments = new Vector<SherlockFragment>();
        fragments.add(FragmentTools.newInstance((AOTalk)this, gridAdapter));
        fragments.add(FragmentChat.newInstance(messageAdapter, chatFragmentClickListener, inputChannelClick, inputTextClick));
        fragments.add(FragmentFriends.newInstance((AOTalk)this, friendAdapter, friendFragmentClickListener, friendFragmentTextListener));
        
        FragmentAdapter fragmentAdapter = new FragmentAdapter(super.getSupportFragmentManager(), fragments);

        fragmentPager = (ViewPager) findViewById(R.id.fragmentpager);
        fragmentPager.setAdapter(fragmentAdapter);
        fragmentPager.setOnPageChangeListener(this);
        
        titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
        titleIndicator.setViewPager(fragmentPager);
        
        if (settings.getBoolean("hideTitles", false)) {
        	titleIndicator.setVisibility(View.GONE);
        } else {
        	titleIndicator.setVisibility(View.VISIBLE);
        }
                
        bindService();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
    	super.onResume();
        
        if (settings.getBoolean("hideTitles", false)) {
        	titleIndicator.setVisibility(View.GONE);
        } else {
        	titleIndicator.setVisibility(View.VISIBLE);
        }

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
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.connect) {
			try {
				Message msg = Message.obtain(null, ServiceTools.MESSAGE_STATUS);
				msg.replyTo = serviceMessenger;
				service.send(msg);
			} catch (RemoteException e) {
				Logging.log(APP_TAG, e.getMessage());
			}
			return true;
		} else if (item.getItemId() == R.id.lookup) {
			manualWhoIs();
			return true;
		} else if (item.getItemId() == R.id.market) {
			Intent intent = new Intent(context, Market.class);
			startActivity(intent);
			return true;
		} else if (item.getItemId() == R.id.aou) {
			Intent intent = new Intent(context, AOU.class);
			startActivity(intent);
			return true;
		} else if (item.getItemId() == R.id.aorb) {
			Intent intent = new Intent(context, RecipeBook.class);
			startActivity(intent);
			return true;
		} else if (item.getItemId() == R.id.preferences) {
			Intent intent = new Intent(context, Preferences.class);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
	}

	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		Logging.log(APP_TAG, "onPreferenceChange");
		
		if (arg0.getKey().equals("showOnlyOnline")) {
			updateFriendList();
		}
		
		return false;
	}
}