package com.rubika.aotalk.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jakewharton.notificationcompat2.NotificationCompat2;
import com.rubika.aotalk.AOTalk;
import com.rubika.aotalk.database.DatabaseHandler;
import com.rubika.aotalk.item.Account;
import com.rubika.aotalk.item.Channel;
import com.rubika.aotalk.item.ChatMessage;
import com.rubika.aotalk.item.Friend;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.R;
import com.spoledge.aacdecoder.AACPlayer;
import com.spoledge.aacdecoder.PlayerCallback;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import ao.chat.ChatClient;
import ao.misc.Convert;
import ao.misc.NameFormat;
import ao.protocol.CharacterInfo;
import ao.protocol.Client;
import ao.protocol.ClientListener;
import ao.protocol.ClientStateException;
import ao.protocol.packets.Packet;
import ao.protocol.packets.bi.ChannelMessagePacket;
import ao.protocol.packets.bi.FriendUpdatePacket;
import ao.protocol.packets.bi.PrivateChannelInvitePacket;
import ao.protocol.packets.bi.PrivateChannelKickPacket;
import ao.protocol.packets.bi.PrivateChannelMessagePacket;
import ao.protocol.packets.bi.PrivateMessagePacket;
import ao.protocol.packets.toclient.BroadcastMessagePacket;
import ao.protocol.packets.toclient.ChannelUpdatePacket;
import ao.protocol.packets.toclient.CharacterListPacket;
import ao.protocol.packets.toclient.LoginErrorPacket;
import ao.protocol.packets.toclient.PrivateChannelCharacterJoinPacket;
import ao.protocol.packets.toclient.PrivateChannelCharacterLeavePacket;
import ao.protocol.packets.toclient.SystemMessagePacket;
import ao.protocol.packets.toclient.VicinityMessagePacket;

public class ClientService extends Service {
	protected static final String APP_TAG  = "--> AnarchyTalk::ClientService";

	private int NOTIFICATION = 0;
	private NotificationManager notificationManager;
	private NotificationCompat2.Builder notificationBuilder;
	
	private CharacterInfo currentCharacter;
	public String currentTargetChannel = "";
	public String currentTargetCharacter = "";
	public String currentShowChannel = ServiceTools.CHANNEL_MAIN;
	private int notificationCounter;
	public ChatClient chatClient;
	private Account currentAccount;
	private SharedPreferences settings;
	public SharedPreferences.Editor editor;
	public boolean manualLogin = false;
	public boolean accountFailed = false;
	
	private String PLAYURL = "";

	private boolean isPlaying = false;
	private boolean retryOnFailure = true;
	private boolean stoppedByCall = false;
	
	private TelephonyManager phoneManager;
	private PhoneStateListener phoneListener;
	
	private AACPlayer aacPlayer;
	private AudioManager audioManager;
	
	private String currentTrack = null;
	
	public List<String> channelsMuted = new ArrayList<String>();
	
	private Messenger messenger = new Messenger(new IncomingHandler(this));

	public ArrayList<Messenger> clients = new ArrayList<Messenger>();
	private DatabaseHandler databaseHandler;
	
	private Handler AOVoiceHandler = new Handler();
	private long AOVoiceUpdateTime = 60000;
	private boolean AOVoiceIsUpdating = false;
	
	public List<Channel> channelList = new ArrayList<Channel>();
	public List<Channel> privateList = new ArrayList<Channel>();
	public List<Channel> invitationList = new ArrayList<Channel>();
	private List<Friend> friendList = new ArrayList<Friend>();
	
	private Context context;
	
	private Handler faceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bitmap face = (Bitmap) msg.obj;
			setNotificationFace(face);
		}
	};
	
	@Override
	public IBinder onBind(Intent arg0) {
		return messenger.getBinder();
	}

	private static class IncomingHandler extends Handler {
		private final WeakReference<ClientService> clientService;
		
		public IncomingHandler(ClientService s) {
			clientService = new WeakReference<ClientService>(s);
		}
		
		@SuppressWarnings("unchecked")
		@Override
        public void handleMessage(Message message) {
        	if (message != null) {
	        	switch (message.what) {
		            case ServiceTools.MESSAGE_PLAYER_PLAY:
		            	clientService.get().play();
		            	break;
		            case ServiceTools.MESSAGE_PLAYER_STOP:
		            	clientService.get().stop();
		            	break;
		            case ServiceTools.MESSAGE_PRIVATE_CHANNEL_JOIN:
		            	Channel invitationJoin = (Channel) message.obj;
		            	clientService.get().privateList.add(invitationJoin);
		            	
		            	int removeThis = -1;
		            	for (int i = 0; i < clientService.get().invitationList.size(); i++) {
		            		if (clientService.get().invitationList.get(i).getID() == invitationJoin.getID()) {
		            			removeThis = i;
		            		}
		            	}
		            	
		            	if (removeThis >= 0) {
		            		clientService.get().invitationList.remove(removeThis);
		            	}
		            	
						try {
							clientService.get().chatClient.acceptInvite(invitationJoin.getID());
						} catch (IOException e) {
							Logging.log(APP_TAG, e.getMessage());
						} catch (ClientStateException e) {
							Logging.log(APP_TAG, e.getMessage());
						}

						Message joined = Message.obtain(null, ServiceTools.MESSAGE_PRIVATE_CHANNEL);
						joined.obj = clientService.get().privateList;
		            	
						clientService.get().message(joined);

						break;
		            case ServiceTools.MESSAGE_PRIVATE_CHANNEL_DENY:
		            	Channel invitationLeave = (Channel) message.obj;
		            	clientService.get().privateList.remove(invitationLeave);

		            	removeThis = -1;
		            	for (int i = 0; i < clientService.get().invitationList.size(); i++) {
		            		if (clientService.get().invitationList.get(i).getID() == invitationLeave.getID()) {
		            			removeThis = i;
		            		}
		            	}
		            	
		            	if (removeThis >= 0) {
		            		clientService.get().invitationList.remove(removeThis);
		            	}
		            	
						try {
							clientService.get().chatClient.denyInvite(invitationLeave.getID());
						} catch (IOException e) {
							Logging.log(APP_TAG, e.getMessage());
						} catch (ClientStateException e) {
							Logging.log(APP_TAG, e.getMessage());
						}
						
						break;
		            case ServiceTools.MESSAGE_CLIENT_REGISTER:
		            	clientService.get().clientRegistered(message);
		            	
		            	Logging.log(APP_TAG, "Clients registered: " + clientService.get().clients.size());
		            	
		            	if (clientService.get().clients.size() > 0) {
		            		clientService.get().AOVoiceHandler.removeCallbacks(clientService.get().AOVoiceUpdateTask);
		            		clientService.get().AOVoiceHandler.post(clientService.get().AOVoiceUpdateTask);
		            	} else {
		            		clientService.get().AOVoiceHandler.removeCallbacks(clientService.get().AOVoiceUpdateTask);
		            	}
		            	
		                break;
		            case ServiceTools.MESSAGE_CLIENT_UNREGISTER:
		            	clientService.get().clients.remove(message.replyTo);
		            	
		            	if (clientService.get().clients.size() == 0) {
		            		clientService.get().AOVoiceHandler.removeCallbacks(clientService.get().AOVoiceUpdateTask);
		            	}
		            	
		            	break;
	                case ServiceTools.MESSAGE_CONNECT:
	                	clientService.get().editor.putInt("lastAccount", ((Account) message.obj).getID());
	                	clientService.get().editor.commit();

	                	clientService.get().accountFailed = false;
	                	clientService.get().manualLogin = true;
	                	
	                	clientService.get().connect((Account) message.obj);
	                	
	                	break;
	                case ServiceTools.MESSAGE_SET_CHANNEL:
	                	clientService.get().currentTargetChannel = (String)message.obj;
	                	
	                	clientService.get().editor.putString("currentChannel", clientService.get().currentTargetChannel);
	                	clientService.get().editor.commit();
	                	
	                	break;
	                case ServiceTools.MESSAGE_SET_SHOW:
	                	clientService.get().currentShowChannel = (String)message.obj;
	                	
	                	break;
	                case ServiceTools.MESSAGE_SET_CHARACTER:
	                	if (clientService.get().chatClient != null && message.obj != null) {
	                		clientService.get().currentTargetCharacter = (String) message.obj;
		                	
	                		clientService.get().editor.putString("currentCharacter", clientService.get().currentTargetCharacter);
	                		clientService.get().editor.commit();
	                	}
	                	
	                	break;
	                case ServiceTools.MESSAGE_SEND:
	                	clientService.get().sendMessage((ChatMessage) message.obj, message.arg1);
	                	break;
	                case ServiceTools.MESSAGE_STATUS:
	                	if (clientService.get().chatClient.getState() == Client.ClientState.LOGGED_IN) {
	                		clientService.get().message(Message.obtain(null, ServiceTools.MESSAGE_IS_CONNECTED, 0, 0));
	                	} else {
	                		clientService.get().message(Message.obtain(null, ServiceTools.MESSAGE_IS_DISCONNECTED, 0, 0));
	                	}
	                	
	                	break;
	                case ServiceTools.MESSAGE_DISCONNECT:
	                	clientService.get().editor.putBoolean("reconnect", false);
	                	clientService.get().editor.putInt("lastAccount", 0);
	                	clientService.get().editor.commit();
	                	
	                	clientService.get().manualLogin = true;
	                	
	                	if (clientService.get().chatClient.getState() != Client.ClientState.DISCONNECTED) {
	                		try {
	                			clientService.get().chatClient.disconnect();
							} catch (IOException e) {
								Logging.log(APP_TAG, e.getMessage());
							}
	                	}
	                	
	                    break;
	                case ServiceTools.MESSAGE_CHARACTER:
	                	if (clientService.get().settings.getBoolean("autoReconnect", true)) {
	                		clientService.get().editor.putBoolean("reconnect", true);
	                	} else {
	                		clientService.get().editor.putBoolean("reconnect", false);
	                	}
	                	
	                	clientService.get().editor.commit();

	                	if (message.obj != null) {
	                		clientService.get().login((CharacterInfo) message.obj);
	                	}
	                	
	                    break;
	                case ServiceTools.MESSAGE_FRIEND_ADD:
	                	String nameAdd = (String) message.obj;
	                	
	                	if (!nameAdd.equals("")) {
	                		clientService.get().addFriend(nameAdd);
	                	}
	                	
	                	break;
	                case ServiceTools.MESSAGE_FRIEND_REMOVE:
	                	String nameRemove = (String) message.obj;
	                	
	                	if (!nameRemove.equals("")) {
	                		clientService.get().removeFriend(nameRemove);
	                	}
	                	
	                	break;
	                case ServiceTools.MESSAGE_MUTED_CHANNELS:
	                	clientService.get().channelList = (List<Channel>) message.obj;
	                	clientService.get().channelsMuted.clear();
	                	
	                	String muted = "";
	                	
	                	for (Channel channel : clientService.get().channelList) {
	                		if (channel.getMuted()) {
	                			clientService.get().channelsMuted.add(channel.getName());
	                			if (muted.length() > 0) {
	                				muted += ",";
	                			}
	                			
	                			muted += channel.getName();
	                		}
	                	}
	                	
	                	clientService.get().editor.putString("mutedChannels", muted);
	                	clientService.get().editor.commit();
	                	
						Message msg = Message.obtain(null, ServiceTools.MESSAGE_CHANNEL);
						msg.obj = clientService.get().channelList;

						clientService.get().message(msg);
	                	
	                	break;
	                default:
	                    super.handleMessage(message);
	            }
        	}
        }
    }

	@Override
	public void onCreate() {
		Logging.log(APP_TAG, "onCreate");

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
        
        currentTargetChannel = settings.getString("currentChannel", "");
        currentTargetCharacter = settings.getString("currentCharacter", "");
        context = this.getApplicationContext();

		Intent notificationIntent = new Intent(this, AOTalk.class);
	    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

	    notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat2.Builder(this);
        
        notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0));
        notificationBuilder.setContentTitle(getString(R.string.app_name));
        notificationBuilder.setContentText("Service running");
        notificationBuilder.setTicker("Service started");
        notificationBuilder.setOngoing(true);
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        notificationBuilder.setNumber(0);
        notificationBuilder.setLargeIcon(((BitmapDrawable)getResources().getDrawable(R.drawable.ic_notification)).getBitmap());
        
        notificationManager.cancel(NOTIFICATION);
        
        databaseHandler = new DatabaseHandler(this);
        
    	String muted  = settings.getString("mutedChannels", "");
    	if (muted.length() > 0) {
    		String[] mutedChannels = muted.split(",");
    		
    		for (int i = 0; i < mutedChannels.length; i++) {
    			channelsMuted.add(mutedChannels[i]);
    		}
    	}
        
        chatClient = new ChatClient();
        chatClient.addListener(new ClientListener() {
			@Override
			public void connected(Client bot) {
				Logging.log(APP_TAG, "Connected");

				authenticate();
			}

			@Override
			public void authenticated(Client bot) {
				Logging.log(APP_TAG, "Authenticated");
			}

			@Override
			public void loggedIn(Client bot) {
				Logging.log(APP_TAG, "Logged in");
				
            	manualLogin = false;
				chatClient.start();
			}

			@Override
			public void started(Client bot) {
				Logging.log(APP_TAG, "Started");
		        startForeground(NOTIFICATION, notificationBuilder.build());

		        setNotification(
						String.format(getString(R.string.character_is_online), currentCharacter.getName()),
						String.format(getString(R.string.character_logged_in), currentCharacter.getName()),
						true,
						false
					);
		        
				new Thread(new Runnable() { 
		            public void run(){
		        	    Message msg = Message.obtain();
						msg.what = 0;
						msg.obj = ServiceTools.getUserImage(String.format(ServiceTools.BASE_CHAR_URL, currentAccount.getServer().getID(), currentCharacter.getName()), context);
						
						faceHandler.sendMessage(msg);
					}
				}).start();

				List<Object> returnData = new ArrayList<Object>();
				
				returnData.add(currentTargetChannel);
				returnData.add(currentTargetCharacter);
				returnData.add(currentCharacter.getName());
				returnData.add(currentShowChannel);
				
				Message msg = Message.obtain();
				msg.what = ServiceTools.MESSAGE_STARTED;
				msg.arg1 = currentCharacter.getID();
                
                if (currentAccount != null) {
                	msg.arg2 = currentAccount.getServer().getID();
                } else {
                	msg.arg2 = 0;
                }

                msg.obj = returnData;
            	message(msg);
            	
            	editor.putInt("lastCharacter", currentCharacter.getID());
            	editor.commit();
			}
			
			@Override
			public void disconnected(Client bot) {
				Logging.log(APP_TAG, "Disconnected");
								
				if (currentCharacter != null) {
					setNotification(
							getString(R.string.disconnected),
							getString(R.string.disconnected),
							false,
							false
						);
				
					databaseHandler.addPost(
							"Disconnected",
							ServiceTools.CHANNEL_APPLICATION,
							ServiceTools.CHANNEL_APPLICATION,
							currentCharacter.getID(),
							currentAccount.getServer().getID()
						);
				}
				
				message(Message.obtain(null, ServiceTools.MESSAGE_DISCONNECTED, (settings.getBoolean("reconnect", true)? 1 : 0), 0));
				
				friendList.clear();
				channelList.clear();
				invitationList.clear();
				privateList.clear();
				
				Logging.log(APP_TAG, "accountFailed: " + accountFailed);
				
				if (!settings.getBoolean("reconnect", true) || accountFailed) {
					Logging.log(APP_TAG, "Skipping automatic reconnect");
					currentAccount = null;
					currentCharacter = null;
				} else {
					Logging.log(APP_TAG, "Automatic reconnect");
                	accountFailed = false;
					connect(currentAccount);
				}

			    stopForeground(true);
            }

			@Override
			public void exception(Client bot, Exception e) {
				Logging.log(APP_TAG, e.getMessage());
				message(Message.obtain(null, ServiceTools.MESSAGE_CLIENT_ERROR, 0, 0));
			}

			@Override
			public void packet(Client bot, Packet packet) {
				Message msg = null;
				
				if (packet == null) {
					return;
				}

				//Character list packet
				if(packet.getType() == CharacterListPacket.TYPE) {
	                CharacterInfo[] cl = ((CharacterListPacket) packet).getCharacters();
					
	                if (settings.getBoolean("reconnect", true) && !manualLogin) {
	                	if (currentCharacter != null) {
    						login(currentCharacter);
	                	} else {
	                		if (settings.getInt("lastCharacter", 0) != 0) {
	                			CharacterInfo cu = null;
	                			
	                			for (CharacterInfo c : cl) {
		    	                	Logging.log(APP_TAG, "user: " + c.getName() + ", status: " + c.getOnline());

		    	                	if (c.getID() == settings.getInt("lastCharacter", 0) /*&& !ServiceTools.intToBoolean(c.getOnline())*/) {
		                				cu = c;
		                			}
		                		}
	                			
	                			
	                			if (cu != null) {
	                				login(cu);
	                			}
	                		}
	                	}
	                	
	                	manualLogin = false;
	                } else {
						msg = Message.obtain(null, ServiceTools.MESSAGE_CHARACTERS);
		                msg.obj = (CharacterListPacket) packet;
	                }
				}
				
				//Log in failed
				if(packet.getType() == LoginErrorPacket.TYPE) {					
	                msg = Message.obtain(null, ServiceTools.MESSAGE_LOGIN_ERROR);
	                accountFailed = true;
				}

				//Private message
				if(packet.getType() == PrivateMessagePacket.TYPE && packet.getDirection() == Packet.Direction.TO_CLIENT) {
					if(chatClient.getCharTable().getName(((PrivateMessagePacket)packet).getCharID()).equals(NameFormat.format(ServiceTools.BOTNAME)) 
							&& ((PrivateMessagePacket)packet).getMessage().contains("::AOTalk::")) {
						String whoisName = "";	
						String whoisText = ((PrivateMessagePacket)packet).getMessage()
								.replace("::AOTalk::", "")
								.replace(ServiceTools.WHOIS_START, "")
								.replace(ServiceTools.WHOIS_END, "")
								.replace("\n", "<br />")
								.trim();
							
						if(whoisText.startsWith("<br />")) {
							whoisText = whoisText.replaceFirst("<br />", "");
						}
						
						Pattern pattern = Pattern.compile("Name:</font> (.*?)<br />");
						Matcher matcher = pattern.matcher(whoisText);
				        
				        while(matcher.find()) {
				        	whoisText = whoisText.replace(
					        	"Name:</font> " + matcher.group(1) + "<br />", 
					        	""
				        	);
				        	
				        	whoisName = matcher.group(1).trim();
				        }
						
						List<String> whoisData = new ArrayList<String>();
						whoisData.add(whoisName);
						whoisData.add(whoisText);

						Message whois = Message.obtain();
						whois.what = ServiceTools.MESSAGE_WHOIS;
						whois.obj = whoisData;
						
				        message(whois);
					} else {
						databaseHandler.addPost(
								((PrivateMessagePacket)packet).display(chatClient.getCharTable(), chatClient.getGroupTable()),
								chatClient.getCharTable().getName(((PrivateMessagePacket)packet).getCharID()),
								ServiceTools.CHANNEL_PM,
								currentCharacter.getID(),
								currentAccount.getServer().getID()
							);
						
						if (clients.size() == 0 && settings.getBoolean("notificationEnabled", true)) {
							notificationCounter++;
							setNotification(
									null,
									chatClient.getCharTable().getName(((PrivateMessagePacket)packet).getCharID())
										+ ": " 
										+ Html.fromHtml(((PrivateMessagePacket)packet).getMessage()).toString(),
									true,
									true
								);
						}

						message(Message.obtain(null, ServiceTools.MESSAGE_UPDATE, 0, 0));
					}
				}
				
				//Chat group message
				if(packet.getType() == ChannelMessagePacket.TYPE && packet.getDirection() == Packet.Direction.TO_CLIENT) {
					if (!channelsMuted.contains(chatClient.getGroupTable().getName(((ChannelMessagePacket)packet).getGroupID()))) {
						databaseHandler.addPost(
								((ChannelMessagePacket)packet).display(chatClient.getCharTable(), chatClient.getGroupTable()),
								chatClient.getCharTable().getName(((ChannelMessagePacket)packet).getCharID()),
								chatClient.getGroupTable().getName(((ChannelMessagePacket)packet).getGroupID()),
								currentCharacter.getID(),
								currentAccount.getServer().getID()
							);
						
						message(Message.obtain(null, ServiceTools.MESSAGE_UPDATE, 0, 0));
					}
				}
				
				//System message
				if(packet.getType() == SystemMessagePacket.TYPE && packet.getDirection() == Packet.Direction.TO_CLIENT) {
					// Got offline message
					if(((SystemMessagePacket)packet).getMsgType().equals("a460d92")) {
						databaseHandler.addPost(
								String.format(getString(R.string.offline_message_from), NameFormat.format(chatClient.getCharTable().getName(((SystemMessagePacket)packet).getCharID()))),
								ServiceTools.CHANNEL_SYSTEM,
								ServiceTools.CHANNEL_SYSTEM,
								currentCharacter.getID(),
								currentAccount.getServer().getID()
							);
					}
					
					//Offline message, message buffered
					if(((SystemMessagePacket)packet).getMsgType().equals("9740ff4")) {
						databaseHandler.addPost(
								String.format(getString(R.string.user_offline_message_buffered),NameFormat.format(chatClient.getCharTable().getName(((SystemMessagePacket)packet).getCharID()))),
								ServiceTools.CHANNEL_SYSTEM,
								ServiceTools.CHANNEL_SYSTEM,
								currentCharacter.getID(),
								currentAccount.getServer().getID()
							);
					}
					
					//Offline message, message buffered
					if(((SystemMessagePacket)packet).getMsgType().equals("340e245")) {
						databaseHandler.addPost(
								getString(R.string.message_could_not_be_sent),
								ServiceTools.CHANNEL_SYSTEM,
								ServiceTools.CHANNEL_SYSTEM,
								currentCharacter.getID(),
								currentAccount.getServer().getID()
							);
					}
					
					message(Message.obtain(null, ServiceTools.MESSAGE_UPDATE, 0, 0));
				}
				
				//Broadcast message
				if(packet.getType() == BroadcastMessagePacket.TYPE && packet.getDirection() == Packet.Direction.TO_CLIENT) {
					databaseHandler.addPost(
							((BroadcastMessagePacket)packet).display(chatClient.getCharTable(), chatClient.getGroupTable()),
							ServiceTools.CHANNEL_SYSTEM,
							ServiceTools.CHANNEL_SYSTEM,
							currentCharacter.getID(),
							currentAccount.getServer().getID()
						);
					
					message(Message.obtain(null, ServiceTools.MESSAGE_UPDATE, 0, 0));
				}
				
				//Vicinity notice
				if(packet.getType() == VicinityMessagePacket.TYPE) {
					databaseHandler.addPost(
							((VicinityMessagePacket)packet).display(chatClient.getCharTable(), chatClient.getGroupTable()),
							ServiceTools.CHANNEL_SYSTEM,
							ServiceTools.CHANNEL_SYSTEM,
							currentCharacter.getID(),
							currentAccount.getServer().getID()
						);
					
					message(Message.obtain(null, ServiceTools.MESSAGE_UPDATE, 0, 0));
				}
				
				//Friend update
				if(packet.getType() == FriendUpdatePacket.TYPE) {
	                if (!((FriendUpdatePacket) packet).isFriend()) {
						boolean removeFriend = false;
						int removeFriendID = 0;
						int friendCounter = 0;
						
	                	for (Friend friend : friendList) {
							if (friend.getID() == ((FriendUpdatePacket) packet).getCharID()) {
								removeFriend = true;
								removeFriendID = friendCounter;
								break;
							}
							friendCounter++;
						}
	                	
	                	if (removeFriend) {
	                		friendList.remove(removeFriendID);
	                	}
	                }
	                
					if (((FriendUpdatePacket) packet).isFriend()) {
						boolean addFriend = true;
						
						for (Friend friend : friendList) {
							if (friend.getID() == ((FriendUpdatePacket) packet).getCharID()) {
								if (friend.isOnline() != ((FriendUpdatePacket) packet).isOnline()) {
									databaseHandler.addPost(
											((FriendUpdatePacket) packet).display(chatClient.getCharTable(), chatClient.getGroupTable()),
											chatClient.getCharTable().getName(((FriendUpdatePacket) packet).getCharID()),
											ServiceTools.CHANNEL_FRIEND,
											currentCharacter.getID(),
											currentAccount.getServer().getID()
										);							
								}
								
								friend.setOnline(((FriendUpdatePacket) packet).isOnline());
								addFriend = false;
							}
						}
						
						if (addFriend) {
							friendList.add(new Friend(
									chatClient.getCharTable().getName(((FriendUpdatePacket) packet).getCharID()),
									((FriendUpdatePacket) packet).getCharID(),
									((FriendUpdatePacket) packet).isOnline()
								));
						}
						
						if (addFriend && ((FriendUpdatePacket) packet).isOnline()) {
							databaseHandler.addPost(
									((FriendUpdatePacket) packet).display(chatClient.getCharTable(), chatClient.getGroupTable()) ,
									chatClient.getCharTable().getName(((FriendUpdatePacket) packet).getCharID()),
									ServiceTools.CHANNEL_FRIEND,
									currentCharacter.getID(),
									currentAccount.getServer().getID()
								);
						}
						
						msg = Message.obtain(null, ServiceTools.MESSAGE_FRIEND);
						msg.obj = friendList;
	                }
					
            		AOVoiceHandler.removeCallbacks(AOVoiceUpdateTask);
            		AOVoiceHandler.post(AOVoiceUpdateTask);
				}
				
				//Group announcement
				if(packet.getType() == ChannelUpdatePacket.TYPE) {
					boolean addChannel = true;
					
					for (Channel channel : channelList) {
						if(channel.getName().equals(((ChannelUpdatePacket)packet).getGroupName())) {
							addChannel = false;
						}
					}
					
					if (addChannel) {
						boolean muted = false;
						boolean enabled = true;
						
						if (channelsMuted.contains(((ChannelUpdatePacket)packet).getGroupName())) {
							muted = true;
						}
						
						if (ServiceTools.channelsDisabled.contains(((ChannelUpdatePacket)packet).getGroupName())) {
							enabled = false;
						}
						
						channelList.add(new Channel(((ChannelUpdatePacket)packet).getGroupName(), Convert.byteToInt(((ChannelUpdatePacket)packet).getGroupID()), enabled, muted));
						
						msg = Message.obtain(null, ServiceTools.MESSAGE_CHANNEL);
						msg.obj = channelList;
					}
				}
				
				//Private group invitation
				if(packet.getType() == PrivateChannelInvitePacket.TYPE) {
					Logging.log(APP_TAG, "Got invitation");
					boolean addChannel = true;
					
					for (Channel channel : invitationList) {
						if (channel.getID() == ((PrivateChannelInvitePacket) packet).getGroupID()) {
							addChannel = false;
						}
					}
					
					if (addChannel) {
						invitationList.add(new Channel(
								ServiceTools.PREFIX_PRIVATE_GROUP + chatClient.getCharTable().getName(((PrivateChannelInvitePacket) packet).getGroupID()),
							((PrivateChannelInvitePacket) packet).getGroupID(),
							true,
							false
						));
					}
					
					if (clients.size() == 0 && settings.getBoolean("notificationEnabled", true)) {
						notificationCounter++;
						setNotification(
								null,
								String.format(getString(R.string.you_were_invited, chatClient.getCharTable().getName(((PrivateChannelInvitePacket) packet).getGroupID()))),
								true,
								true
							);
					}
					
					msg = Message.obtain(null, ServiceTools.MESSAGE_PRIVATE_CHANNEL_INVITATION);
					msg.obj = invitationList;
				}
				
				//Private group join
				if(packet.getType() == PrivateChannelCharacterJoinPacket.TYPE) {
					databaseHandler.addPost(
							String.format(
									getString(R.string.joined_channel), 
									chatClient.getCharTable().getName(((PrivateChannelCharacterJoinPacket)packet).getGroupID()), 
									chatClient.getCharTable().getName(((PrivateChannelCharacterJoinPacket)packet).getCharID())
							),
							chatClient.getCharTable().getName(((PrivateChannelCharacterJoinPacket)packet).getCharID()),
							ServiceTools.PREFIX_PRIVATE_GROUP + chatClient.getCharTable().getName(((PrivateChannelCharacterJoinPacket)packet).getGroupID()),
							currentCharacter.getID(),
							currentAccount.getServer().getID()
						);
					
					message(Message.obtain(null, ServiceTools.MESSAGE_UPDATE, 0, 0));
				}
				
				//Private group kick
				if(packet.getType() == PrivateChannelKickPacket.TYPE) {
					databaseHandler.addPost(
							String.format(getString(R.string.kicked_from_channel), chatClient.getCharTable().getName(((PrivateChannelKickPacket)packet).getGroupID())),
							chatClient.getCharTable().getName(((PrivateChannelKickPacket)packet).getGroupID()),
							ServiceTools.PREFIX_PRIVATE_GROUP + chatClient.getCharTable().getName(((PrivateChannelKickPacket)packet).getGroupID()),
							currentCharacter.getID(),
							currentAccount.getServer().getID()
						);
					
					int kickedFrom = -1;
					for (int i = 0; i < privateList.size(); i++) {
						if (privateList.get(i).getID() == ((PrivateChannelKickPacket)packet).getGroupID()) {
							kickedFrom = i;
						}
					}
					
					if (kickedFrom >= 0) {
						privateList.remove(kickedFrom);
					}
					
					Message message = Message.obtain();
					message.what = ServiceTools.MESSAGE_PRIVATE_CHANNEL;
					message.obj = privateList;
					
					message(message);
					message(Message.obtain(null, ServiceTools.MESSAGE_UPDATE, 0, 0));
				}
				
				//Private group leave
				if(packet.getType() == PrivateChannelCharacterLeavePacket.TYPE) {
					databaseHandler.addPost(
							String.format(
									getString(R.string.left_channel), 
									chatClient.getCharTable().getName(((PrivateChannelCharacterLeavePacket)packet).getGroupID()), 
									chatClient.getCharTable().getName(((PrivateChannelCharacterLeavePacket)packet).getCharID())
							),
							chatClient.getCharTable().getName(((PrivateChannelCharacterLeavePacket)packet).getCharID()),
							ServiceTools.PREFIX_PRIVATE_GROUP + chatClient.getCharTable().getName(((PrivateChannelCharacterLeavePacket)packet).getGroupID()),
							currentCharacter.getID(),
							currentAccount.getServer().getID()
						);
					
					message(Message.obtain(null, ServiceTools.MESSAGE_UPDATE, 0, 0));
				}
				
				//Private group message
				if(packet.getType() == PrivateChannelMessagePacket.TYPE) {
					databaseHandler.addPost(
							((PrivateChannelMessagePacket)packet).display(chatClient.getCharTable(), chatClient.getGroupTable()),
							chatClient.getCharTable().getName(((PrivateChannelMessagePacket)packet).getCharID()),
							ServiceTools.PREFIX_PRIVATE_GROUP + chatClient.getCharTable().getName(((PrivateChannelMessagePacket)packet).getGroupID()),
							currentCharacter.getID(),
							currentAccount.getServer().getID()
						);
					
					message(Message.obtain(null, ServiceTools.MESSAGE_UPDATE, 0, 0));
				}
				
            	// Send message if one exists
				if (msg != null) {
            		message(msg);
            	}
			}
		});
        
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		aacPlayer = new AACPlayer();
        aacPlayer.setAudioBufferCapacityMs(AACPlayer.DEFAULT_AUDIO_BUFFER_CAPACITY_MS);
        aacPlayer.setDecodeBufferCapacityMs(AACPlayer.DEFAULT_DECODE_BUFFER_CAPACITY_MS);
        
		PlayerCallback playerCallback = new PlayerCallback() {
			@Override
		    public void playerStarted() {
		        isPlaying = true;
				setNotificationPlaying(true);
		    }
		    
			@Override
		    public void playerPCMFeedBuffer(boolean isPlaying, int bufSizeMs, int bufCapacityMs) {
		    }
		    
			@Override
		    public void playerStopped(int perf) {
				audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
				currentTrack = null;
		        isPlaying = false;
				setNotificationPlaying(false);
		    }
		    
			@Override
		    public void playerException(Throwable t) {
		        if (retryOnFailure) {
		        	aacPlayer.stop();
					play();
		        }
		    }

			@Override
			public void playerMetadata(String key, String value) {
				Logging.log(APP_TAG, key + " : " + value);
				
				if (key != null) {
					if (key.equals("StreamTitle")) {
						currentTrack = value;
						
						Message message = Message.obtain(null, ServiceTools.MESSAGE_PLAYER_TRACK, 0, 0);
						message.obj = value;
						message(message);
					}
				}
			}
		};
		
		aacPlayer.setPlayerCallback(playerCallback);
		phoneManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
		// Create a PhoneStateListener to watch for offhook and idle events
		phoneListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
				case TelephonyManager.CALL_STATE_OFFHOOK:
				case TelephonyManager.CALL_STATE_RINGING:
					// phone going offhook or ringing, pause the player
					if (true) {
						audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
						stoppedByCall = true;
					}
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					// phone idle
					if (stoppedByCall) {
						audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
						stoppedByCall = false;
					}
					break;
				}
			}
		};
		
		phoneManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
		
        if (settings.getBoolean("reconnect", true) && !accountFailed) {
        	Account account = databaseHandler.getAccount(settings.getInt("lastAccount", 0));
        	connect(account);
        }
	}
	
	@Override
	public void onDestroy() {
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
	
	public void clientRegistered(Message message) {
		clients.add(message.replyTo);
		
	    Message msg = Message.obtain();
	    
		if (chatClient != null) {
	    	if (chatClient.getState() != Client.ClientState.DISCONNECTED) {
	        	if (currentCharacter != null) {
	        		msg.arg1 = currentCharacter.getID();
	        	} else {
	        		msg.arg1 = 0;
	        	}
	        	
	            notificationCounter = 0;
				setNotification(
						null,
						null,
						true,
						true
					);
	    	}
		}
	    
	    if (currentAccount != null) {
	    	msg.arg2 = currentAccount.getServer().getID();
	    } else {
	    	msg.arg2 = 0;
	    }
	    
	    msg.what = ServiceTools.MESSAGE_REGISTERED;
	    msg.setTarget(null);
	    
	    List<Object> registerData = new ArrayList<Object>();
	    registerData.add(friendList);
	    registerData.add(channelList);
	    registerData.add(currentTargetChannel);
	    registerData.add(currentTargetCharacter);
	
	    if (currentCharacter != null) {
	    	registerData.add(currentCharacter.getName());
	    } else {
	    	registerData.add("");
	    }
	    
	    registerData.add(currentShowChannel);
	    registerData.add(isPlaying);
	    registerData.add(invitationList);
	    registerData.add(privateList);
	    registerData.add(currentTrack);
	
	    msg.obj = registerData;
	    
		Logging.log(APP_TAG, "Channels: " + channelList.size());
	    ClientService.this.message(msg);		
	}

	public boolean sendMessage(ChatMessage message, int show) {
		Logging.log(APP_TAG, "sendMessage\nChannel: " + message.getChannel() + "\nCharacter: " + message.getCharacter());
		
		if (!message.getCharacter().equals("")) {
			try {
				Logging.log(APP_TAG, "Sending tell");
				chatClient.sendTell(message.getCharacter(), message.getMessage(), true);
				if (show == 1) {
					databaseHandler.addPost(
							String.format(getString(R.string.message_to), message.getCharacter(), message.getMessage()),
							message.getCharacter(),
							ServiceTools.CHANNEL_PM,
							currentCharacter.getID(),
							chatClient.getDimensionID()
						);
					message(Message.obtain(null, ServiceTools.MESSAGE_UPDATE, 0, 0));
				}
			} catch (IOException e) {
				Logging.log(APP_TAG, e.getMessage());
				return false;
			}
		}
		
		if (!message.getChannel().equals("")) {
			try {
				if (message.getChannel().startsWith(ServiceTools.PREFIX_PRIVATE_GROUP)) {
					Logging.log(APP_TAG, "Sending to private group");
					chatClient.sendPrivateChannelMessage(message.getChannel().replace(ServiceTools.PREFIX_PRIVATE_GROUP, ""), message.getMessage());
				} else {
					Logging.log(APP_TAG, "Sending to public group");
					chatClient.sendChannelMessage(message.getChannel(), message.getMessage());
				}
			} catch (IOException e) {
				Logging.log(APP_TAG, e.getMessage());
				return false;
			}
		}
		
		return true;
	}
	
	private long lastNotificationSound = 0;
	private long timeBetweenNotificationSounds = 2000;
	
	private void setNotification(String message, String ticker, boolean persistent, boolean update) {
		Logging.log(APP_TAG, "Notifications: " + notificationCounter);
		
		if (!update) {
			notificationBuilder.setContentText(message);
		    notificationBuilder.setOngoing(persistent);
		    notificationBuilder.setWhen(System.currentTimeMillis());
	    }
	    
	    notificationBuilder.setNumber(notificationCounter);
	    notificationBuilder.setTicker(ticker);
	    
	    if (ticker != null && update && settings.getString("notificationSound", null) != null && System.currentTimeMillis() - lastNotificationSound > timeBetweenNotificationSounds) {
	    	notificationBuilder.setSound(Uri.parse(settings.getString("notificationSound", null)));
	    	lastNotificationSound = System.currentTimeMillis();
	    } else {
	    	notificationBuilder.setSound(null);
	    }
			    
	    notificationManager.notify(NOTIFICATION, notificationBuilder.build());
		
		if (!persistent && !isPlaying) {
			notificationManager.cancel(NOTIFICATION);
		}
		
		setNotificationPlaying(isPlaying);
	}

	private void setNotificationFace(Bitmap face) {
		if (face != null) {
		    notificationBuilder.setLargeIcon(face);
		    notificationManager.notify(NOTIFICATION, notificationBuilder.build());
		} else {
		    Logging.log(APP_TAG, "Character image is NULL");
		}
	}
	
	private void setNotificationPlaying(boolean isplaying) {
		if (isplaying) {
	    	notificationBuilder.setSmallIcon(R.drawable.ic_gridstream);
	    	
	    	if (chatClient.getState() == Client.ClientState.DISCONNECTED) {
	    		notificationBuilder.setLargeIcon(((BitmapDrawable)getResources().getDrawable(R.drawable.ic_notification)).getBitmap());
	    		notificationBuilder.setContentText("GridStream is playing");
	    		notificationBuilder.setContentTitle(getString(R.string.app_name));
	    		notificationBuilder.setTicker(null);
	    	}
	    } else {
	    	notificationBuilder.setSmallIcon(R.drawable.ic_notification);
	    }
	    
	    notificationManager.notify(NOTIFICATION, notificationBuilder.build());
	    
	    if (chatClient.getState() == Client.ClientState.DISCONNECTED && !isPlaying) {
	    	notificationManager.cancel(NOTIFICATION);
	    }
	}
	
	public boolean message(Message message) {
		if (message != null) {
			for (int  i = clients.size() - 1; i >= 0; i--) {
	            try {
	                clients.get(i).send(message);
	            } catch (RemoteException ex) {
	                clients.remove(i);
	            }
	        }
			
			return true;
		}
		
		return false;
	}
	
	private class UpdateAOVoice extends AsyncTask<Void, Void, String> {
		protected String doInBackground(Void... str) {
			AOVoiceIsUpdating = true;
			updateAOVoiceData();
	    	 
			return null;
		}

		protected void onPostExecute(String result) {
			Message msg = Message.obtain(null, ServiceTools.MESSAGE_FRIEND);
			msg.obj = friendList;
			message(msg);
	    	 
	    	AOVoiceIsUpdating = false;
		}
	}

	private void updateAOVoiceData() {
		HttpClient httpclient;
		HttpPost httppost;
		HttpResponse response;
		HttpEntity entity;
		InputStream is;
		BufferedReader reader;
		StringBuilder sb;
		String line;
		String resultData = null;
		JSONArray jArray;
		JSONObject json_data;

		try {
    		httpclient = new DefaultHttpClient();
	        httppost = new HttpPost("http://api.aospeak.com/online/" + chatClient.getDimensionID() +  "/");
	        	        
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
    	 
    			Logging.log(APP_TAG, sb.toString());

    	        resultData = sb.toString();
	    	} catch(Exception e){
	    	    Logging.log(APP_TAG, e.toString());
	    	}
    	} catch(Exception e){
    		Logging.log(APP_TAG, e.toString());
    	}
		
    	try{
    		if(resultData != null) {
	    		if((!resultData.startsWith("null"))) {
	    			jArray = new JSONArray(resultData);
	    			
    	        	try {
		    			for(Friend f : friendList) {
	    	        		f.setAOSpeakStatus(false);
	    	        	}
    	        	} catch (ConcurrentModificationException e) {
    	        		Logging.log(APP_TAG, e.getMessage());
    	        	}
    	        		    				    			
	    	        for(int i = jArray.length() - 1; i >= 0; i--){
	    	        	json_data = jArray.getJSONObject(i);
		                
	    	        	Logging.log(APP_TAG, json_data.getString("name"));
	    	        	
	    	        	try {
		    	        	for(Friend f : friendList) {
		    	        		if (f.getName().toLowerCase().equals(json_data.getString("name").toLowerCase())) {
		    	    	        	Logging.log(APP_TAG, json_data.getString("name") + " is online at AOSpeak");
		    	        			f.setAOSpeakStatus(true);
		    	        		}
		    	        	}
	    	        	} catch (ConcurrentModificationException e) {
	    	        		Logging.log(APP_TAG, e.getMessage());
	    	        	}
	    	        }
	    		}
    		}
    	} catch(JSONException e){
    		Logging.log(APP_TAG, e.toString());
    	}
	}
		
	private Runnable AOVoiceUpdateTask = new Runnable() {
		public void run() {
			Logging.log(APP_TAG, "Running AOVoiceUpdate");
			
			if (chatClient.getState() == Client.ClientState.LOGGED_IN && !AOVoiceIsUpdating) {
		    	Logging.log(APP_TAG, "User is logged in, fetching AOVoice data and updating friends");
		    	new UpdateAOVoice().execute();
			} else {
		    	Logging.log(APP_TAG, "User is NOT logged in or update already running, no need to fetch data");
			}

			if (clients.size() > 0) {
				AOVoiceHandler.postDelayed(this, AOVoiceUpdateTime);
			} else {
				AOVoiceHandler.removeCallbacks(this);
			}
		}
	};

	public void connect(Account acc) {
		if (acc != null) {
			currentAccount = acc;
			
			new Thread(new Runnable() { 
	            public void run(){
	        		if (settings.getBoolean("reconnect", true)) {
	        			try {
	                        synchronized (this) {
	                            long startTime = System.currentTimeMillis();
	                            long timeout = 60000;
	                            
	                        	while(!ServiceTools.isOnline(context)) {
	                            	wait(500);
	                            	
	                            	if (System.currentTimeMillis() - startTime > timeout) {
	                            		Logging.log(APP_TAG, String.format(getString(R.string.reconnect_timeout), (timeout / 1000)));
	                            		break;
	                            	}
	                            }
	                        }
	                    } catch (InterruptedException e) {
							Logging.log(APP_TAG, e.getMessage());
	                    }
	        		}
	            	
	            	try {
	        			if (currentAccount != null) {
	        				Logging.log(APP_TAG, "Connecting");
	        				chatClient.connect(currentAccount.getServer());
	        			}
	        		} catch (IOException e) {
	                	message(Message.obtain(null, ServiceTools.MESSAGE_CONNECTION_ERROR, 0, 0));
	                	
	                	accountFailed = true;
						Logging.log(APP_TAG, e.getMessage());
						Logging.log(APP_TAG, "connection failed");
	        		}
	            }
			}).start();
		}
	}
	
	private void authenticate() {
		if (chatClient.getState() == Client.ClientState.CONNECTED) {
			try {
				if (currentAccount != null) {
					chatClient.authenticate(currentAccount.getUsername(), currentAccount.getPassword());
				}
			} catch (IOException e) {
            	message(Message.obtain(null, ServiceTools.MESSAGE_CONNECTION_ERROR, 0, 0));

            	accountFailed = true;
				Logging.log(APP_TAG, e.getMessage());
				Logging.log(APP_TAG, "authentication failed");
			}
		}
	}
	
	public void login(final CharacterInfo character) {
		if (character != null) {
			currentCharacter = character;
			
			new Thread(new Runnable() { 
	            public void run(){
					try {
						Logging.log(APP_TAG, "Logging in");
						chatClient.login(character);
					} catch (IOException e) {
	                	message(Message.obtain(null, ServiceTools.MESSAGE_CONNECTION_ERROR, 0, 0));

	                	accountFailed = true;
						Logging.log(APP_TAG, e.getMessage());
						Logging.log(APP_TAG, "login failed");
					}
				}
			}).start();
		}
	}

	public boolean addFriend(final String name) {
		if (chatClient.getState() != Client.ClientState.DISCONNECTED) {
			if (NameFormat.format(currentCharacter.getName()).equals(NameFormat.format(name))) {
				Logging.toast(context, getString(R.string.not_add_yourself));
				
				return false;
			}
			
			List<Friend> tempList = friendList;
			boolean addCharacter = true;
			
			for (Friend friend : tempList) {
				if (NameFormat.format(friend.getName()).equals(NameFormat.format(name))) {
					addCharacter = false;
				}
			}
	
			if (addCharacter) {
				try {
					chatClient.addFriend(name, true);
					Logging.toast(context, String.format(getString(R.string.added_to_buddy_list), name));
				} catch (IOException e) {
					Logging.log(APP_TAG, e.getMessage());
				}
				
				return true;
			} else {
				Logging.toast(context, String.format(getString(R.string.already_in_buddy_list), name));				
			}
		} else {
			Logging.toast(context, getString(R.string.disconnected));
		}
		
		return false;
	}

	public boolean removeFriend(final String name) {
		if (chatClient.getState() != Client.ClientState.DISCONNECTED) {
			List<Friend> tempList = new ArrayList<Friend>();
			tempList.addAll(friendList);
			
			boolean removeCharacter = false;
			int friendCounter = 0;
			int removeID = 0;
			
			for (Friend friend : tempList) {
				if (NameFormat.format(friend.getName()).equals(NameFormat.format(name))) {
					removeCharacter = true;
					removeID = friendCounter;
				}
				
				friendCounter++;
			}
			
			final int removeThis = removeID;
			
			if (removeCharacter) {
				try {
					chatClient.deleteFriend(name, true);
					friendList.remove(removeThis);
					
					Logging.toast(context, String.format(getString(R.string.removed_from_buddy_list), name));
					
					Message msg = Message.obtain(null, ServiceTools.MESSAGE_FRIEND);
					msg.obj = friendList;
					
					message(msg);
					
				} catch (IOException e) {
					Logging.log(APP_TAG, e.getMessage());
				}
				
				return true;
			} else {
				Logging.toast(context, String.format(getString(R.string.not_in_buddy_list), name));				
			}
		} else {
			Logging.toast(context, getString(R.string.disconnected));			
		}
		
		return false;
	}

	public void play() {
		aacPlayer.stop();
		
		if (PLAYURL.equals("")) {
	    	try {
	            URL updateURL = new URL("http://community.loudcity.com/stations/gridstream-productions/files/show/gsp.pls");
	            URLConnection conn = updateURL.openConnection();
	            InputStream is = conn.getInputStream();
	            BufferedInputStream bis = new BufferedInputStream(is);
	            ByteArrayBuffer baf = new ByteArrayBuffer(50);
	
	            int current = 0;
	            while((current = bis.read()) != -1){
	                baf.append((byte)current);
	            }
	
	            String html = new String(baf.toByteArray());
	            String[] lines = html.split("\n");
	            
	            for(String s : lines) {
	            	Logging.log(APP_TAG, s);
	            	if(s.trim().startsWith("File1=")) {
	            		PLAYURL = s.trim().replace("File1=", "").trim();
	            	}
	            }
	        } catch (Exception e) {
				Logging.log(APP_TAG, e.getMessage());
	        }
	    }
		
		if (!isPlaying) {
			try {
				aacPlayer.playAsync(PLAYURL, 48);
			} catch (Exception e) {
				Logging.log(APP_TAG, e.getMessage());
	        	Logging.log(APP_TAG, "Player failed to start");
	        	
		        if (retryOnFailure) {
		        	aacPlayer.stop();
		        	Logging.log(APP_TAG, "Player retry");
					play();
		        }
			}
	    }
	}

	public void stop() {
		audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
		aacPlayer.stop();
	}
}