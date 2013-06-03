package com.rubika.aotalk.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.rubika.aotalk.AOTalk;
import com.rubika.aotalk.database.DatabaseHandler;
import com.rubika.aotalk.item.Account;
import com.rubika.aotalk.item.Channel;
import com.rubika.aotalk.item.ChatMessage;
import com.rubika.aotalk.item.Friend;
import com.rubika.aotalk.music.AudioFocusHelper;
import com.rubika.aotalk.music.MusicFocusable;
import com.rubika.aotalk.music.MusicIntentReceiver;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;
import com.rubika.aotalk.R;
import com.spoledge.aacdecoder.AACPlayer;
import com.spoledge.aacdecoder.PlayerCallback;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import ao.chat.ChatClient;
import ao.misc.Convert;
import ao.misc.NameFormat;
import ao.protocol.CharacterInfo;
import ao.protocol.Client;
import ao.protocol.Client.ClientState;
import ao.protocol.ClientListener;
import ao.protocol.ClientStateException;
import ao.protocol.DimensionAddress;
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

public class ClientService extends Service implements MusicFocusable {
	protected static final String APP_TAG = "--> The Leet :: ClientService";

	public static int NOTIFICATION = 1;
	public static int NOTIFICATION_TW = 2;
	public static int NOTIFICATION_OP = 3;
	private static NotificationManager notificationManager;
	private static NotificationCompat.Builder notificationBuilder;

	public static final String ACTION_TOGGLE_PLAYBACK = "com.rubika.aotalk.action.TOGGLE_PLAYBACK";
	public static final String ACTION_PLAY = "com.rubika.aotalk.action.PLAY";
	public static final String ACTION_STOP = "com.rubika.aotalk.action.STOP";

	private CharacterInfo currentCharacter;
	public String currentTargetChannel = "";
	public String currentTargetCharacter = "";
	public String currentShowChannel = Statics.CHANNEL_MAIN;
	private int notificationCounter;
	public ChatClient chatClient;
	private Account currentAccount;
	private SharedPreferences settings;
	public SharedPreferences.Editor editor;
	public boolean manualLogin = false;
	public boolean accountFailed = false;
	private int reconnectAttempts = 0;
	private long lastReconnectAttempt = 0;

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

	private Messenger messenger = new Messenger(new MessageHandler(this));

	public ArrayList<Messenger> clients = new ArrayList<Messenger>();
	// public static DatabaseHandler DatabaseHandler.getInstance(context);

	private Handler AOVoiceHandler = new Handler();
	private long AOVoiceUpdateTime = 60000;
	private boolean AOVoiceIsUpdating = false;

	public List<Channel> channelList = new ArrayList<Channel>();
	public List<Channel> privateList = new ArrayList<Channel>();
	public List<Channel> invitationList = new ArrayList<Channel>();
	private List<Friend> friendList = new ArrayList<Friend>();

	private static Context context;
	private Tracker tracker;
	private MediaPlayer mediaPlayer;
	private RemoteControlClientCompat remoteControlClient;
	private ComponentName mediaButtonReceiverComponent;

	private static Handler faceHandler = new FaceHandler();

	@Override
	public IBinder onBind(Intent arg0) {
		return messenger.getBinder();
	}

	static class FaceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Bitmap face = (Bitmap) msg.obj;
			setNotificationFace(face);
		}
	};

	static class MessageHandler extends Handler {
		private final WeakReference<ClientService> clientService;

		public MessageHandler(ClientService s) {
			clientService = new WeakReference<ClientService>(s);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message message) {
			if (message != null) {
				switch (message.what) {
				case Statics.MESSAGE_PLAYER_PLAY:
					clientService.get().play();
					break;
				case Statics.MESSAGE_PLAYER_STOP:
					clientService.get().stop();
					break;
				case Statics.MESSAGE_PRIVATE_CHANNEL_JOIN:
					Channel invitationJoin = (Channel) message.obj;
					clientService.get().privateList.add(invitationJoin);

					int removeThis = -1;
					for (int i = 0; i < clientService.get().invitationList
							.size(); i++) {
						if (clientService.get().invitationList.get(i).getID() == invitationJoin
								.getID()) {
							removeThis = i;
						}
					}

					if (removeThis >= 0) {
						clientService.get().invitationList.remove(removeThis);
					}

					try {
						clientService.get().chatClient
								.acceptInvite(invitationJoin.getID());
					} catch (IOException e) {
						Logging.log(APP_TAG, e.getMessage());
					} catch (ClientStateException e) {
						Logging.log(APP_TAG, e.getMessage());
					}

					Message joined = Message.obtain(null,
							Statics.MESSAGE_PRIVATE_CHANNEL);
					joined.obj = clientService.get().privateList;

					clientService.get().message(joined);

					break;
				case Statics.MESSAGE_PRIVATE_CHANNEL_DENY:
					Channel invitationLeave = (Channel) message.obj;
					clientService.get().privateList.remove(invitationLeave);

					removeThis = -1;
					for (int i = 0; i < clientService.get().invitationList
							.size(); i++) {
						if (clientService.get().invitationList.get(i).getID() == invitationLeave
								.getID()) {
							removeThis = i;
						}
					}

					if (removeThis >= 0) {
						clientService.get().invitationList.remove(removeThis);
					}

					try {
						clientService.get().chatClient
								.denyInvite(invitationLeave.getID());
					} catch (IOException e) {
						Logging.log(APP_TAG, e.getMessage());
					} catch (ClientStateException e) {
						Logging.log(APP_TAG, e.getMessage());
					}

					break;
				case Statics.MESSAGE_CLIENT_REGISTER:
					clientService.get().clientRegistered(message);

					Logging.log(APP_TAG,
							"Clients registered: "
									+ clientService.get().clients.size());

					if (clientService.get().clients.size() > 0) {
						clientService.get().AOVoiceHandler
								.removeCallbacks(clientService.get().AOVoiceUpdateTask);
						clientService.get().AOVoiceHandler.post(clientService
								.get().AOVoiceUpdateTask);
					} else {
						clientService.get().AOVoiceHandler
								.removeCallbacks(clientService.get().AOVoiceUpdateTask);
					}

					break;
				case Statics.MESSAGE_CLIENT_UNREGISTER:
					clientService.get().clients.remove(message.replyTo);

					if (clientService.get().clients.size() == 0) {
						clientService.get().AOVoiceHandler
								.removeCallbacks(clientService.get().AOVoiceUpdateTask);
					}

					break;
				case Statics.MESSAGE_CONNECT:
					clientService.get().editor.putInt("lastAccount",
							((Account) message.obj).getID());
					clientService.get().editor.commit();

					clientService.get().accountFailed = false;
					clientService.get().manualLogin = true;

					clientService.get().connect((Account) message.obj);

					break;
				case Statics.MESSAGE_SET_CHANNEL:
					clientService.get().currentTargetChannel = (String) message.obj;

					clientService.get().editor.putString("currentChannel",
							clientService.get().currentTargetChannel);
					clientService.get().editor.commit();

					break;
				case Statics.MESSAGE_SET_SHOW:
					clientService.get().currentShowChannel = (String) message.obj;

					break;
				case Statics.MESSAGE_SET_CHARACTER:
					if (clientService.get().chatClient != null
							&& message.obj != null) {
						clientService.get().currentTargetCharacter = (String) message.obj;

						clientService.get().editor.putString(
								"currentCharacter",
								clientService.get().currentTargetCharacter);
						clientService.get().editor.commit();
					}

					break;
				case Statics.MESSAGE_SEND:
					clientService.get().sendMessage((ChatMessage) message.obj,
							message.arg1);
					break;
				case Statics.MESSAGE_STATUS:
					if (clientService.get().chatClient.getState() == Client.ClientState.LOGGED_IN) {
						clientService.get().message(
								Message.obtain(null,
										Statics.MESSAGE_IS_CONNECTED, 0, 0));
					} else {
						clientService.get().message(
								Message.obtain(null,
										Statics.MESSAGE_IS_DISCONNECTED, 0, 0));
					}

					break;
				case Statics.MESSAGE_DISCONNECT:
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
				case Statics.MESSAGE_CHARACTER:
					if (clientService.get().settings.getBoolean(
							"autoReconnect", true)) {
						clientService.get().editor
								.putBoolean("reconnect", true);
					} else {
						clientService.get().editor.putBoolean("reconnect",
								false);
					}

					clientService.get().editor.commit();

					if (message.obj != null) {
						clientService.get().login((CharacterInfo) message.obj);
					}

					break;
				case Statics.MESSAGE_FRIEND_ADD:
					String nameAdd = (String) message.obj;

					if (!nameAdd.equals("")) {
						clientService.get().addFriend(nameAdd);
					}

					break;
				case Statics.MESSAGE_FRIEND_REMOVE:
					String nameRemove = (String) message.obj;

					if (!nameRemove.equals("")) {
						clientService.get().removeFriend(nameRemove);
					}

					break;
				case Statics.MESSAGE_MUTED_CHANNELS:
					clientService.get().channelList = (List<Channel>) message.obj;
					clientService.get().channelsMuted.clear();

					String muted = "";

					for (Channel channel : clientService.get().channelList) {
						if (channel.getMuted()) {
							clientService.get().channelsMuted.add(channel
									.getName());
							if (muted.length() > 0) {
								muted += ",";
							}

							muted += channel.getName();
						}
					}

					clientService.get().editor
							.putString("mutedChannels", muted);
					clientService.get().editor.commit();

					Message msg = Message.obtain(null, Statics.MESSAGE_CHANNEL);
					msg.obj = clientService.get().channelList;

					clientService.get().message(msg);

					break;
				default:
					super.handleMessage(message);
				}
			}
		}
	}

	public static Context getContext() {
		return context;
	}

	private void playConnectionSound(int sound) {
		if (settings.getBoolean("enableSounds", false)) {
			Logging.log(APP_TAG, "Playing connection sound");

			mediaPlayer = MediaPlayer.create(context, sound);
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					Logging.log(APP_TAG, "Releaseing MediaPlayer");
					mediaPlayer.release();
				}

			});
			mediaPlayer.start();
		}
	}

	private AudioFocusHelper mAudioFocusHelper = null;
	private AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

	private enum AudioFocus {
		NoFocusNoDuck, // we don't have audio focus, and can't duck
		NoFocusCanDuck, // we don't have focus, but can play at a low volume
						// ("ducking")
		Focused // we have full audio focus
	}

	public enum PlayerState {
		Started, Stopped, Working
	}

	@Override
	public void onCreate() {
		Logging.log(APP_TAG, "onCreate");

		/*
		 * IntentFilter filter = new IntentFilter();
		 * filter.addAction(Statics.BROADCAST_KEY_PLAY);
		 * filter.addAction(Statics.BROADCAST_KEY_STOP);
		 * 
		 * receiver = new BroadcastReceiver() {
		 * 
		 * @Override public void onReceive(Context context, Intent intent) { if
		 * (intent.getAction().equals(Statics.BROADCAST_KEY_PLAY)) { if
		 * (!isPlaying) { play(); } } else if
		 * (intent.getAction().equals(Statics.BROADCAST_KEY_STOP)) { if
		 * (isPlaying) { stop(); } } } };
		 * 
		 * registerReceiver(receiver, filter);
		 */

		settings = PreferenceManager.getDefaultSharedPreferences(this);
		editor = settings.edit();

		EasyTracker.getInstance().setContext(getApplicationContext());
		tracker = EasyTracker.getTracker();

		editor.putString("lastCharacterName", "");
		editor.commit();
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			//DashClockExtender.getInstance().changeMessage();
		}

		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mediaButtonReceiverComponent = new ComponentName(this,
				MusicIntentReceiver.class);

		if (android.os.Build.VERSION.SDK_INT >= 8)
			mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(),
					this);
		else
			mAudioFocus = AudioFocus.Focused; // no focus feature, so we always
												// "have" audio focus

		if (remoteControlClient == null) {
			Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			intent.setComponent(mediaButtonReceiverComponent);
			remoteControlClient = new RemoteControlClientCompat(
					PendingIntent.getBroadcast(this, 0, intent, 0));
			// remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
			// | RemoteControlClient.FLAG_KEY_MEDIA_STOP);
		}

		registerRemoteControl();

		currentTargetChannel = settings.getString("currentChannel", "");
		currentTargetCharacter = settings.getString("currentCharacter", "");
		context = this.getApplicationContext();

		Intent notificationIntent = new Intent(this, AOTalk.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// PendingIntent pendingPlayIntent = PendingIntent.getBroadcast(this, 1,
		// new Intent(Statics.BROADCAST_KEY_PLAY), 0);
		// PendingIntent pendingStopIntent = PendingIntent.getBroadcast(this, 1,
		// new Intent(Statics.BROADCAST_KEY_STOP), 0);

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationBuilder = new Builder(this);

		notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0,
				notificationIntent, 0));
		notificationBuilder.setContentTitle(getString(R.string.app_name));
		notificationBuilder.setContentText(String.format(
				getString(R.string.is_running), getString(R.string.app_name)));
		notificationBuilder.setTicker(null);
		notificationBuilder.setOngoing(true);
		notificationBuilder.setSmallIcon(R.drawable.ic_notification);
		notificationBuilder.setNumber(0);
		notificationBuilder.setLargeIcon(((BitmapDrawable) getResources()
				.getDrawable(R.drawable.leet)).getBitmap());

		// notificationBuilder.setStyle(new
		// NotificationCompat.InboxStyle().setSummaryText(getString(R.string.app_name)
		// + " is running").addLine("test"));
		// notificationBuilder.setStyle(new
		// NotificationCompat.BigTextStyle().bigText(getString(R.string.app_name)));
		// notificationBuilder.setStyle(new
		// NotificationCompat.BigPictureStyle().bigPicture(((BitmapDrawable)getResources().getDrawable(R.drawable.leet)).getBitmap()));
		// notificationBuilder.setPriority(Notification.PRIORITY_LOW);
		// notificationBuilder.addAction(R.drawable.ic_menu_play, "Play",
		// pendingPlayIntent);
		// notificationBuilder.addAction(R.drawable.ic_menu_stop, "Stop",
		// pendingStopIntent);

		notificationManager.cancel(NOTIFICATION);

		// DatabaseHandler.getInstance(context) =
		// DatabaseHandler.getInstance(this);

		String muted = settings.getString("mutedChannels", "");
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

				startForeground(NOTIFICATION, notificationBuilder.build());

				manualLogin = false;
				chatClient.start();

				tracker.sendEvent("Connection", "Connected",
						currentCharacter.getName(), 0L);
			}

			@Override
			public void started(Client bot) {
				Logging.log(APP_TAG, "Started");
				startForeground(NOTIFICATION, notificationBuilder.build());

				setNotification(String.format(
						getString(R.string.character_is_online),
						currentCharacter.getName()), String.format(
						getString(R.string.character_logged_in),
						currentCharacter.getName()), true, false);

				new Thread(new Runnable() {
					public void run() {
						Message msg = Message.obtain();
						msg.what = 0;
						msg.obj = ServiceTools.getUserImage(
								currentCharacter.getName(), context);

						faceHandler.sendMessage(msg);
					}
				}).start();

				List<Object> returnData = new ArrayList<Object>();

				returnData.add(currentTargetChannel);
				returnData.add(currentTargetCharacter);
				returnData.add(currentCharacter.getName());
				returnData.add(currentShowChannel);

				Message msg = Message.obtain();
				msg.what = Statics.MESSAGE_STARTED;
				msg.arg1 = currentCharacter.getID();

				if (currentAccount != null) {
					msg.arg2 = DimensionAddress.RK.getID();
				} else {
					msg.arg2 = 0;
				}

				msg.obj = returnData;
				message(msg);

				editor.putInt("lastCharacter", currentCharacter.getID());
				editor.putString("lastCharacterName", currentCharacter.getName());
				editor.commit();
				
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
					//DashClockExtender.getInstance().changeMessage();
				}

				playConnectionSound(R.raw.grid_in);
			}

			@Override
			public void disconnected(Client bot) {
				Logging.log(APP_TAG, "Disconnected");

				if (currentCharacter != null) {
					setNotification(getString(R.string.disconnected),
							getString(R.string.disconnected), false, false);

					DatabaseHandler.getInstance(context).addPost(
							"Disconnected", Statics.CHANNEL_APPLICATION,
							Statics.CHANNEL_APPLICATION,
							currentCharacter.getID());

					tracker.sendEvent("Connection", "Disconnected",
							currentCharacter.getName(), 0L);
				}

				message(Message.obtain(null, Statics.MESSAGE_DISCONNECTED,
						(settings.getBoolean("reconnect", true) ? 1 : 0), 0));

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
					Logging.log(APP_TAG, "Automatic reconnect (" + reconnectAttempts + ")");
					accountFailed = false;

					if (lastReconnectAttempt + 2500 >= System
							.currentTimeMillis()) {
						reconnectAttempts++;
					} else {
						reconnectAttempts = 0;
					}

					lastReconnectAttempt = System.currentTimeMillis();

					if (reconnectAttempts < 5) {
						connect(currentAccount);
					} else {
						Logging.log(APP_TAG, "Too many automatic reconnects");
						currentAccount = null;
						currentCharacter = null;
					}
				}

				if (!isPlaying) {
					stopForeground(true);
				}
				
				editor.putString("lastCharacterName", "");
				editor.commit();
				
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
					//DashClockExtender.getInstance().changeMessage();
				}

				playConnectionSound(R.raw.grid_out);
			}

			@Override
			public void exception(Client bot, Exception e) {
				Logging.log(APP_TAG, e.getMessage());
				message(Message
						.obtain(null, Statics.MESSAGE_CLIENT_ERROR, 0, 0));
			}

			@Override
			public void packet(Client bot, Packet packet) {
				Message message = null;

				if (packet == null) {
					return;
				}

				// Character list packet
				if (packet.getType() == CharacterListPacket.TYPE) {
					message = handleCharacterListPacket(packet);
				}

				// Log in failed
				if (packet.getType() == LoginErrorPacket.TYPE) {
					message = handleLoginErrorPacket(packet);
				}

				// Private message
				if (packet.getType() == PrivateMessagePacket.TYPE
						&& packet.getDirection() == Packet.Direction.TO_CLIENT) {
					message = handlePrivateMessagePacket(packet);
				}

				// Chat group message
				if (packet.getType() == ChannelMessagePacket.TYPE
						&& packet.getDirection() == Packet.Direction.TO_CLIENT) {
					message = handleChannelMessagePacket(packet);
				}

				// System message
				if (packet.getType() == SystemMessagePacket.TYPE
						&& packet.getDirection() == Packet.Direction.TO_CLIENT) {
					message = handleSystemMessagePacket(packet);
				}

				// Broadcast message
				if (packet.getType() == BroadcastMessagePacket.TYPE
						&& packet.getDirection() == Packet.Direction.TO_CLIENT) {
					message = handleBroadcastMessagePacket(packet);
				}

				// Vicinity notice
				if (packet.getType() == VicinityMessagePacket.TYPE) {
					message = handleVicinityMessagePacket(packet);
				}

				// Friend update
				if (packet.getType() == FriendUpdatePacket.TYPE) {
					message = handleFriendUpdatePacket(packet);
				}

				// Group announcement
				if (packet.getType() == ChannelUpdatePacket.TYPE) {
					message = handleChannelUpdatePacket(packet);
				}

				// Private group invitation
				if (packet.getType() == PrivateChannelInvitePacket.TYPE) {
					message = handlePrivateChannelInvitePacket(packet);
				}

				// Private group join
				if (packet.getType() == PrivateChannelCharacterJoinPacket.TYPE) {
					message = handlePrivateChannelCharacterJoinPacket(packet);
				}

				// Private group kick
				if (packet.getType() == PrivateChannelKickPacket.TYPE) {
					message = handlePrivateChannelKickPacket(packet);
				}

				// Private group leave
				if (packet.getType() == PrivateChannelCharacterLeavePacket.TYPE) {
					message = handlePrivateChannelCharacterLeavePacket(packet);
				}

				// Private group message
				if (packet.getType() == PrivateChannelMessagePacket.TYPE) {
					message = handlePrivateChannelMessagePacket(packet);
				}

				// Send message if one exists
				if (message != null) {
					message(message);
				}
			}
		});

		audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);

		aacPlayer = new AACPlayer();
		aacPlayer
				.setAudioBufferCapacityMs(AACPlayer.DEFAULT_AUDIO_BUFFER_CAPACITY_MS);
		aacPlayer
				.setDecodeBufferCapacityMs(AACPlayer.DEFAULT_DECODE_BUFFER_CAPACITY_MS);

		PlayerCallback playerCallback = new PlayerCallback() {
			@Override
			public void playerStarted() {
				Logging.log(APP_TAG, "playerStarted");

				editor.putBoolean("restartPlayer", true);
				editor.commit();

				// audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);

				isPlaying = true;

				setNotification(getString(R.string.gsp_is_playing), null,
						false, false);
				// setRemoteControlMetadata(((BitmapDrawable)getResources().getDrawable(R.drawable.leet)).getBitmap(),
				// "title", "artist");

				if (chatClient.getState() == ChatClient.ClientState.DISCONNECTED) {
					startForeground(NOTIFICATION, notificationBuilder.build());
				}

				Message message = Message.obtain(null,
						Statics.MESSAGE_PLAYER_STARTED, 0, 0);
				message(message);

				// setRemoteControlPlaying();
			}

			@Override
			public void playerStopped(int perf) {
				Logging.log(APP_TAG, "playerStopped");

				editor.putBoolean("restartPlayer", false);
				editor.commit();

				// audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
				currentTrack = null;
				isPlaying = false;

				setNotification(getString(R.string.gsp_is_playing), null,
						false, false);

				if (chatClient.getState() == ChatClient.ClientState.DISCONNECTED) {
					stopForeground(true);
				}

				Message message = Message.obtain(null,
						Statics.MESSAGE_PLAYER_STOPPED, 0, 0);
				message(message);

				// setRemoteControlStopped();
				giveUpAudioFocus();
			}

			@Override
			public void playerException(Throwable t) {
				Logging.log(APP_TAG, "playerException: " + t.getMessage());

				editor.putBoolean("restartPlayer", false);
				editor.commit();

				if (retryOnFailure) {
					aacPlayer.stop();
					play();
				}
			}

			@Override
			public void playerPCMFeedBuffer(boolean isPlaying, int bufSizeMs,
					int bufCapacityMs) {
			}

			@Override
			public void playerMetadata(String key, String value) {
				if (key != null) {
					if (key.equals("StreamTitle")) {
						currentTrack = value;
						setNotificationPlayerTrack(currentTrack);

						List<Object> playerData = new ArrayList<Object>();
						playerData.add(isPlaying);
						playerData.add(currentTrack);

						Message message = Message.obtain(null,
								Statics.MESSAGE_PLAYER_TRACK, 0, 0);
						message.obj = playerData;
						message(message);
					} else if (key.equals("icy-name")) {
						currentTrack = value;
						setNotificationPlayerTrack(currentTrack);

						List<Object> playerData = new ArrayList<Object>();
						playerData.add(isPlaying);
						playerData.add(currentTrack);

						Message message = Message.obtain(null,
								Statics.MESSAGE_PLAYER_TRACK, 0, 0);
						message.obj = playerData;
						message(message);
					}
				}
			}
		};

		aacPlayer.setPlayerCallback(playerCallback);
		phoneManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		// Create a PhoneStateListener to watch for off hook and idle events
		phoneListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
				case TelephonyManager.CALL_STATE_OFFHOOK:
				case TelephonyManager.CALL_STATE_RINGING:
					// phone going off hook or ringing, pause the player
					if (isPlaying) {
						stop();
						stoppedByCall = true;
					}

					break;
				case TelephonyManager.CALL_STATE_IDLE:
					// phone idle
					if (stoppedByCall) {
						play();
						stoppedByCall = false;
					}
					break;
				}
			}
		};

		phoneManager
				.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

		if (settings.getBoolean("reconnect", true) && !accountFailed) {
			Account account = DatabaseHandler.getInstance(context).getAccount(
					settings.getInt("lastAccount", 0));
			connect(account);
		}

		if (settings.getBoolean("restartPlayer", false)) {
			play();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		giveUpAudioFocus();
		stopForeground(true);
		unregisterRemoteControl();
	}

	private Message handleCharacterListPacket(Packet packet) {
		Logging.log(APP_TAG, "Got CharacterListPacket");

		Message msg = null;
		CharacterInfo[] cl = ((CharacterListPacket) packet).getCharacters();

		if (settings.getBoolean("reconnect", true) && !manualLogin) {
			if (currentCharacter != null) {
				login(currentCharacter);
			} else {
				if (settings.getInt("lastCharacter", 0) != 0) {
					CharacterInfo cu = null;

					for (CharacterInfo c : cl) {
						Logging.log(APP_TAG, "user: " + c.getName()
								+ ", status: " + c.getOnline());

						if (c.getID() == settings.getInt("lastCharacter", 0)) {
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
			msg = Message.obtain(null, Statics.MESSAGE_CHARACTERS);
			msg.obj = (CharacterListPacket) packet;
		}

		return msg;
	}

	private Message handleLoginErrorPacket(Packet packet) {
		Logging.log(APP_TAG, "Got LoginErrorPacket");

		accountFailed = true;
		return Message.obtain(null, Statics.MESSAGE_LOGIN_ERROR);
	}

	private Message handlePrivateMessagePacket(Packet packet) {
		Logging.log(APP_TAG, "Got PrivateMessagePacket");
		Message msg = null;

		boolean skipThis = false;
		boolean treatAsChannel = false;

		String channel = "";
		Pattern pattern;
		Matcher matcher;

		// Check if messages from Dnet and Neutnet should be ignored
		if (settings.getBoolean("muteDnet", false)) {
			pattern = Pattern
					.compile("dnet([0-9].*)", Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(chatClient.getCharTable().getName(
					((PrivateMessagePacket) packet).getCharID()));

			if (matcher.matches() && settings.getBoolean("muteDnet", false)) {
				Logging.log(APP_TAG, "Ignoring message from Dnet");
				skipThis = true;
			}

			pattern = Pattern.compile("neutnet([0-9].*)",
					Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(chatClient.getCharTable().getName(
					((PrivateMessagePacket) packet).getCharID()));

			if (matcher.matches() && settings.getBoolean("muteDnet", false)) {
				Logging.log(APP_TAG, "Ignoring message from Neutnet");
				skipThis = true;
			}
		}

		// Check if messages from Dnet and Neutnet should be treated as channels
		if (settings.getBoolean("dnetAsChannel", false)) {
			pattern = Pattern
					.compile("dnet([0-9].*)", Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(chatClient.getCharTable().getName(
					((PrivateMessagePacket) packet).getCharID()));

			if (matcher.matches()) {
				treatAsChannel = true;
				channel = Statics.CHANNEL_DNET;
			}

			pattern = Pattern.compile("neutnet([0-9].*)",
					Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(chatClient.getCharTable().getName(
					((PrivateMessagePacket) packet).getCharID()));

			if (matcher.matches()) {
				treatAsChannel = true;
				channel = Statics.CHANNEL_NEUTNET;
			}
		}

		if (!skipThis) {
			if (!treatAsChannel) {
				DatabaseHandler.getInstance(context).addPost(
						((PrivateMessagePacket) packet).display(
								chatClient.getCharTable(),
								chatClient.getGroupTable()),
						chatClient.getCharTable().getName(
								((PrivateMessagePacket) packet).getCharID()),
						Statics.CHANNEL_PM, currentCharacter.getID());

				if (clients.size() == 0
						&& settings.getBoolean("notificationEnabled", true)) {
					notificationCounter++;
					setNotification(
							null,
							chatClient.getCharTable()
									.getName(
											((PrivateMessagePacket) packet)
													.getCharID())
									+ ": "
									+ Html.fromHtml(
											((PrivateMessagePacket) packet)
													.getMessage()).toString(),
							true, true);
				}
				msg = Message.obtain(null, Statics.MESSAGE_UPDATE, 0, 0);
			} else {
				Logging.log(APP_TAG, String.format(
						"Treating message as channel (%s)", channel));

				String from = "";
				String message = "";

				pattern = Pattern.compile("^(.*)\\[(.*?)\\]</font>$",
						Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(((PrivateMessagePacket) packet)
						.getMessage());

				if (matcher.matches()) {
					from = Html.fromHtml(matcher.group(2)).toString();

					message = matcher.group(1);
					message = message.replaceAll(
							"^<font color=#(?:[a-zA-Z0-9]{6})>", "");
					message = message.replaceAll("</font>$", "");
					message = "[" + channel + "] " + from + ": " + message;

					if (!channelsMuted.contains(channel)) {
						DatabaseHandler.getInstance(context).addPost(message,
								from, channel, currentCharacter.getID());

						msg = Message
								.obtain(null, Statics.MESSAGE_UPDATE, 0, 0);
					}
				}
			}
		}

		return msg;
	}

	private Message handleChannelMessagePacket(Packet packet) {
		Logging.log(APP_TAG, "Got ChannelMessagePacket");
		Message msg = null;

		if (!channelsMuted.contains(chatClient.getGroupTable().getName(
				((ChannelMessagePacket) packet).getGroupID()))) {
			DatabaseHandler.getInstance(context).addPost(
					((ChannelMessagePacket) packet).display(
							chatClient.getCharTable(),
							chatClient.getGroupTable()),
					chatClient.getCharTable().getName(
							((ChannelMessagePacket) packet).getCharID()),
					chatClient.getGroupTable().getName(
							((ChannelMessagePacket) packet).getGroupID()),
					currentCharacter.getID());

			msg = Message.obtain(null, Statics.MESSAGE_UPDATE, 0, 0);
		}

		return msg;
	}

	private Message handleSystemMessagePacket(Packet packet) {
		Logging.log(APP_TAG, "Got SystemMessagePacket");

		// Got offline message
		if (((SystemMessagePacket) packet).getMsgType().equals("a460d92")) {
			DatabaseHandler.getInstance(context).addPost(
					String.format(getString(R.string.offline_message_from),
							NameFormat.format(chatClient.getCharTable()
									.getName(
											((SystemMessagePacket) packet)
													.getCharID()))),
					Statics.CHANNEL_SYSTEM, Statics.CHANNEL_SYSTEM,
					currentCharacter.getID());
		}

		// Offline message, message buffered
		if (((SystemMessagePacket) packet).getMsgType().equals("9740ff4")) {
			DatabaseHandler.getInstance(context).addPost(
					String.format(
							getString(R.string.user_offline_message_buffered),
							NameFormat.format(chatClient.getCharTable()
									.getName(
											((SystemMessagePacket) packet)
													.getCharID()))),
					Statics.CHANNEL_SYSTEM, Statics.CHANNEL_SYSTEM,
					currentCharacter.getID());
		}

		// Offline message, message buffered
		if (((SystemMessagePacket) packet).getMsgType().equals("340e245")) {
			DatabaseHandler.getInstance(context).addPost(
					getString(R.string.message_could_not_be_sent),
					Statics.CHANNEL_SYSTEM, Statics.CHANNEL_SYSTEM,
					currentCharacter.getID());
		}

		return Message.obtain(null, Statics.MESSAGE_UPDATE, 0, 0);
	}

	private Message handleBroadcastMessagePacket(Packet packet) {
		Logging.log(APP_TAG, "Got BroadcastMessagePacket");

		DatabaseHandler.getInstance(context).addPost(
				((BroadcastMessagePacket) packet).display(
						chatClient.getCharTable(), chatClient.getGroupTable()),
				Statics.CHANNEL_SYSTEM, Statics.CHANNEL_SYSTEM,
				currentCharacter.getID());

		return Message.obtain(null, Statics.MESSAGE_UPDATE, 0, 0);
	}

	private Message handleVicinityMessagePacket(Packet packet) {
		Logging.log(APP_TAG, "Got VicinityMessagePacket");

		DatabaseHandler.getInstance(context).addPost(
				((VicinityMessagePacket) packet).display(
						chatClient.getCharTable(), chatClient.getGroupTable()),
				Statics.CHANNEL_SYSTEM, Statics.CHANNEL_SYSTEM,
				currentCharacter.getID());

		return Message.obtain(null, Statics.MESSAGE_UPDATE, 0, 0);
	}

	private Message handleFriendUpdatePacket(Packet packet) {
		Logging.log(APP_TAG, "Got FriendUpdatePacket");
		Message msg = null;

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
					if (friend.isOnline() != ((FriendUpdatePacket) packet)
							.isOnline()) {
						DatabaseHandler.getInstance(context).addPost(
								((FriendUpdatePacket) packet).display(
										chatClient.getCharTable(),
										chatClient.getGroupTable()),
								chatClient.getCharTable().getName(
										((FriendUpdatePacket) packet)
												.getCharID()),
								Statics.CHANNEL_FRIEND,
								currentCharacter.getID());
					}

					friend.setOnline(((FriendUpdatePacket) packet).isOnline());
					addFriend = false;
				}
			}

			if (addFriend) {
				friendList.add(new Friend(chatClient.getCharTable().getName(
						((FriendUpdatePacket) packet).getCharID()),
						((FriendUpdatePacket) packet).getCharID(),
						((FriendUpdatePacket) packet).isOnline(),
						DatabaseHandler.getInstance(context).getCharacterImage(
								chatClient.getCharTable().getName(
										((FriendUpdatePacket) packet)
												.getCharID()))));
			}

			if (addFriend && ((FriendUpdatePacket) packet).isOnline()) {
				DatabaseHandler.getInstance(context).addPost(
						((FriendUpdatePacket) packet).display(
								chatClient.getCharTable(),
								chatClient.getGroupTable()),
						chatClient.getCharTable().getName(
								((FriendUpdatePacket) packet).getCharID()),
						Statics.CHANNEL_FRIEND, currentCharacter.getID());
			}

			msg = Message.obtain(null, Statics.MESSAGE_FRIEND);
			msg.obj = friendList;
		}

		AOVoiceHandler.removeCallbacks(AOVoiceUpdateTask);
		AOVoiceHandler.postDelayed(AOVoiceUpdateTask, 100);

		return msg;
	}

	private Message handleChannelUpdatePacket(Packet packet) {
		Logging.log(APP_TAG, "Got ChannelUpdatePacket");
		Message msg = null;

		boolean addChannel = true;

		for (Channel channel : channelList) {
			if (channel.getName().equals(
					((ChannelUpdatePacket) packet).getGroupName())) {
				addChannel = false;
			}
		}

		if (addChannel) {
			boolean muted = false;
			boolean enabled = true;

			if (channelsMuted.contains(((ChannelUpdatePacket) packet)
					.getGroupName())) {
				muted = true;
			}

			if (Statics.channelsDisabled
					.contains(((ChannelUpdatePacket) packet).getGroupName())) {
				enabled = false;
			}

			channelList.add(new Channel(((ChannelUpdatePacket) packet)
					.getGroupName(), Convert
					.byteToInt(((ChannelUpdatePacket) packet).getGroupID()),
					enabled, muted));

			msg = Message.obtain(null, Statics.MESSAGE_CHANNEL);
			msg.obj = channelList;
		}

		return msg;
	}

	private Message handlePrivateChannelInvitePacket(Packet packet) {
		Logging.log(APP_TAG, "Got PrivateChannelInvitePacket");
		Message msg = null;

		boolean addChannel = true;

		for (Channel channel : invitationList) {
			if (channel.getID() == ((PrivateChannelInvitePacket) packet)
					.getGroupID()) {
				addChannel = false;
			}
		}

		if (addChannel) {
			invitationList.add(new Channel(
					Statics.PREFIX_PRIVATE_GROUP
							+ chatClient.getCharTable().getName(
									((PrivateChannelInvitePacket) packet)
											.getGroupID()),
					((PrivateChannelInvitePacket) packet).getGroupID(), true,
					false));
		}

		if (clients.size() == 0
				&& settings.getBoolean("notificationEnabled", true)) {
			notificationCounter++;
			setNotification(null,
					String.format(getString(
							R.string.you_were_invited,
							chatClient.getCharTable().getName(
									((PrivateChannelInvitePacket) packet)
											.getGroupID()))), true, true);
		}

		msg = Message.obtain(null, Statics.MESSAGE_PRIVATE_INVITATION);
		msg.obj = invitationList;

		return msg;
	}

	private Message handlePrivateChannelCharacterJoinPacket(Packet packet) {
		Logging.log(APP_TAG, "Got PrivateChannelCharacterJoinPacket");

		DatabaseHandler.getInstance(context).addPost(
				String.format(
						getString(R.string.joined_channel),
						chatClient.getCharTable().getName(
								((PrivateChannelCharacterJoinPacket) packet)
										.getGroupID()),
						chatClient.getCharTable().getName(
								((PrivateChannelCharacterJoinPacket) packet)
										.getCharID())),
				chatClient.getCharTable().getName(
						((PrivateChannelCharacterJoinPacket) packet)
								.getCharID()),
				Statics.PREFIX_PRIVATE_GROUP
						+ chatClient.getCharTable().getName(
								((PrivateChannelCharacterJoinPacket) packet)
										.getGroupID()),
				currentCharacter.getID());

		return Message.obtain(null, Statics.MESSAGE_UPDATE, 0, 0);
	}

	private Message handlePrivateChannelKickPacket(Packet packet) {
		Logging.log(APP_TAG, "Got PrivateChannelKickPacket");

		DatabaseHandler.getInstance(context).addPost(
				String.format(
						getString(R.string.kicked_from_channel),
						chatClient.getCharTable().getName(
								((PrivateChannelKickPacket) packet)
										.getGroupID())),
				chatClient.getCharTable().getName(
						((PrivateChannelKickPacket) packet).getGroupID()),
				Statics.PREFIX_PRIVATE_GROUP
						+ chatClient.getCharTable().getName(
								((PrivateChannelKickPacket) packet)
										.getGroupID()),
				currentCharacter.getID());

		int kickedFrom = -1;
		for (int i = 0; i < privateList.size(); i++) {
			if (privateList.get(i).getID() == ((PrivateChannelKickPacket) packet)
					.getGroupID()) {
				kickedFrom = i;
			}
		}

		if (kickedFrom >= 0) {
			privateList.remove(kickedFrom);
		}

		Message message = Message.obtain();
		message.what = Statics.MESSAGE_PRIVATE_CHANNEL;
		message.obj = privateList;

		message(message);

		return Message.obtain(null, Statics.MESSAGE_UPDATE, 0, 0);
	}

	private Message handlePrivateChannelCharacterLeavePacket(Packet packet) {
		Logging.log(APP_TAG, "Got PrivateChannelCharacterLeavePacket");

		DatabaseHandler.getInstance(context).addPost(
				String.format(
						getString(R.string.left_channel),
						chatClient.getCharTable().getName(
								((PrivateChannelCharacterLeavePacket) packet)
										.getGroupID()),
						chatClient.getCharTable().getName(
								((PrivateChannelCharacterLeavePacket) packet)
										.getCharID())),
				chatClient.getCharTable().getName(
						((PrivateChannelCharacterLeavePacket) packet)
								.getCharID()),
				Statics.PREFIX_PRIVATE_GROUP
						+ chatClient.getCharTable().getName(
								((PrivateChannelCharacterLeavePacket) packet)
										.getGroupID()),
				currentCharacter.getID());

		return Message.obtain(null, Statics.MESSAGE_UPDATE, 0, 0);
	}

	private Message handlePrivateChannelMessagePacket(Packet packet) {
		Logging.log(APP_TAG, "Got PrivateChannelMessagePacket");

		DatabaseHandler.getInstance(context).addPost(
				((PrivateChannelMessagePacket) packet).display(
						chatClient.getCharTable(), chatClient.getGroupTable()),
				chatClient.getCharTable().getName(
						((PrivateChannelMessagePacket) packet).getCharID()),
				Statics.PREFIX_PRIVATE_GROUP
						+ chatClient.getCharTable().getName(
								((PrivateChannelMessagePacket) packet)
										.getGroupID()),
				currentCharacter.getID());

		return Message.obtain(null, Statics.MESSAGE_UPDATE, 0, 0);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();

			if (action != null) {
				Logging.log(APP_TAG, action);

				if (action.equals(ACTION_TOGGLE_PLAYBACK)) {
					togglePlayback();
				} else if (action.equals(ACTION_PLAY)) {
					play();
				} else if (action.equals(ACTION_STOP)) {
					stop();
				}
			}
		}
		return START_STICKY;
	}

	private void registerRemoteControl() {
		RemoteControlHelper.registerRemoteControlClient(audioManager,
				remoteControlClient);
	}

	private void unregisterRemoteControl() {
		RemoteControlHelper.unregisterRemoteControlClient(audioManager,
				remoteControlClient);
	}

	/*
	 * private void setRemoteControlStopped() { if (remoteControlClient != null)
	 * {
	 * remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED
	 * ); } }
	 * 
	 * private void setRemoteControlPlaying() { if (remoteControlClient != null)
	 * {
	 * remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING
	 * ); } }
	 * 
	 * private void setRemoteControlMetadata(Bitmap artwork, String title,
	 * String artist) { MetadataEditorCompat editor =
	 * remoteControlClient.editMetadata(true);
	 * editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, title);
	 * editor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, artist);
	 * editor.putBitmap(RemoteControlClientCompat.MetadataEditorCompat.
	 * METADATA_KEY_ARTWORK, artwork); editor.apply(); }
	 */

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
				setNotification(null, null, true, true);
			}
		}

		msg.what = Statics.MESSAGE_REGISTERED;
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
		registerData.add(chatClient.getState() == ClientState.LOGGED_IN);

		msg.obj = registerData;

		Logging.log(APP_TAG, "Channels: " + channelList.size());
		ClientService.this.message(msg);
	}

	public boolean sendMessage(ChatMessage message, int show) {
		Logging.log(APP_TAG, "sendMessage\nChannel: " + message.getChannel()
				+ "\nCharacter: " + message.getCharacter());

		if (chatClient == null
				|| chatClient.getState() != Client.ClientState.LOGGED_IN) {
			Logging.toast(context, context.getString(R.string.not_connected));
			return false;
		}

		if (!message.getCharacter().equals("")) {
			try {
				Logging.log(APP_TAG, "Sending tell");
				chatClient.sendTell(message.getCharacter(),
						message.getMessage(), true);
				if (show == 1) {
					DatabaseHandler.getInstance(context).addPost(
							String.format(getString(R.string.message_to),
									message.getCharacter(),
									message.getMessage()),
							message.getCharacter(), Statics.CHANNEL_PM,
							currentCharacter.getID());
					message(Message.obtain(null, Statics.MESSAGE_UPDATE, 0, 0));
				}
			} catch (IOException e) {
				Logging.log(APP_TAG, e.getMessage());
				return false;
			}
		}

		if (!message.getChannel().equals("")) {
			try {
				if (message.getChannel().startsWith(
						Statics.PREFIX_PRIVATE_GROUP)) {
					Logging.log(APP_TAG, "Sending to private group");
					chatClient.sendPrivateChannelMessage(message.getChannel()
							.replace(Statics.PREFIX_PRIVATE_GROUP, ""), message
							.getMessage());
				} else {
					Logging.log(APP_TAG, "Sending to public group");
					chatClient.sendChannelMessage(message.getChannel(),
							message.getMessage());
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

	private void setNotification(String message, String ticker,
			boolean persistent, boolean update) {
		if (!update) {
			notificationBuilder.setContentText(message);
			notificationBuilder.setOngoing(persistent);
			notificationBuilder.setWhen(System.currentTimeMillis());
		}

		notificationBuilder.setNumber(notificationCounter);
		notificationBuilder.setTicker(ticker);

		if (ticker != null
				&& update
				&& System.currentTimeMillis() - lastNotificationSound > timeBetweenNotificationSounds) {
			notificationBuilder.setSound(Uri.parse(settings.getString(
					"notificationSound",
					"android.resource://com.rubika.aotalk/raw/reet")));
			lastNotificationSound = System.currentTimeMillis();
		} else {
			notificationBuilder.setSound(null);
		}

		if (isPlaying) {
			notificationBuilder.setOngoing(true);
		}

		setNotificationPlaying();
	}

	private static void setNotificationFace(Bitmap face) {
		if (face != null) {
			notificationBuilder.setLargeIcon(face);
			notificationManager.notify(NOTIFICATION,
					notificationBuilder.build());
		} else {
			Logging.log(APP_TAG, "Character image is NULL");
		}
	}

	private void setNotificationPlayerTrack(String track) {
		notificationBuilder.setContentText(track);
		notificationManager.notify(NOTIFICATION, notificationBuilder.build());
	}

	private void setNotificationPlaying() {
		if (isPlaying) {
			notificationBuilder.setSmallIcon(R.drawable.ic_gridstream);

			if (chatClient.getState() == Client.ClientState.DISCONNECTED) {
				notificationBuilder
						.setLargeIcon(((BitmapDrawable) getResources()
								.getDrawable(R.drawable.gspleet)).getBitmap());
			}

			if (currentTrack != null) {
				notificationBuilder.setContentText(currentTrack);
			}
		} else {
			notificationBuilder.setSmallIcon(R.drawable.ic_notification);

			if (chatClient.getState() == Client.ClientState.DISCONNECTED) {
				notificationBuilder
						.setLargeIcon(((BitmapDrawable) getResources()
								.getDrawable(R.drawable.leet)).getBitmap());
			}

			if (currentCharacter != null) {
				notificationBuilder.setContentText(String.format(
						getString(R.string.character_is_online),
						currentCharacter.getName()));
			} else {
				notificationBuilder.setContentText("Doh");
			}
		}

		notificationManager.notify(NOTIFICATION, notificationBuilder.build());

		if (chatClient.getState() == Client.ClientState.DISCONNECTED
				&& !isPlaying) {
			notificationManager.cancel(NOTIFICATION);
		}
	}

	public boolean message(Message message) {
		if (message != null) {
			for (int i = clients.size() - 1; i >= 0; i--) {
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
			Message msg = Message.obtain(null, Statics.MESSAGE_FRIEND);
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
			httppost = new HttpPost(String.format(Statics.AOSPEAK_API_PATH,
					chatClient.getDimensionID()));

			response = httpclient.execute(httppost);
			entity = response.getEntity();
			is = entity.getContent();

			try {
				reader = new BufferedReader(new InputStreamReader(is,
						"iso-8859-1"), 8);
				sb = new StringBuilder();
				line = null;

				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				is.close();

				Logging.log(APP_TAG, sb.toString());

				resultData = sb.toString();
			} catch (Exception e) {
				Logging.log(APP_TAG, e.toString());
			}
		} catch (Exception e) {
			Logging.log(APP_TAG, e.toString());
		}

		try {
			if (resultData != null) {
				if ((!resultData.startsWith("null"))) {
					jArray = new JSONArray(resultData);

					try {
						for (Friend f : friendList) {
							f.setAOSpeakStatus(false);
						}
					} catch (ConcurrentModificationException e) {
						Logging.log(APP_TAG, e.getMessage());
					}

					for (int i = jArray.length() - 1; i >= 0; i--) {
						json_data = jArray.getJSONObject(i);

						try {
							for (Friend f : friendList) {
								String fixedName = "";

								if (json_data.getString("name").contains(" ")) {
									fixedName = json_data.getString("name")
											.split(" ")[0].trim();
								} else {
									fixedName = json_data.getString("name");
								}

								if (f.getName()
										.toLowerCase(Locale.getDefault())
										.equals(fixedName.toLowerCase(Locale
												.getDefault()))) {
									f.setAOSpeakStatus(true);
								}
							}
						} catch (ConcurrentModificationException e) {
							Logging.log(APP_TAG, e.getMessage());
						}
					}
				}
			}
		} catch (JSONException e) {
			Logging.log(APP_TAG, e.toString());
		}
	}

	private Runnable AOVoiceUpdateTask = new Runnable() {
		public void run() {
			if (chatClient.getState() == Client.ClientState.LOGGED_IN
					&& !AOVoiceIsUpdating) {
				Logging.log(APP_TAG,
						"AOVoiceUpdate - User is logged in, fetching AOVoice data and updating friends");
				new UpdateAOVoice().execute();
			} else {
				Logging.log(
						APP_TAG,
						"AOVoiceUpdate - User is NOT logged in or update already running, no need to fetch data");
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
				public void run() {
					if (settings.getBoolean("reconnect", true)) {
						try {
							synchronized (this) {
								long startTime = System.currentTimeMillis();
								long timeout = 60000;

								while (!ServiceTools.isOnline(context)) {
									wait(500);

									if (System.currentTimeMillis() - startTime > timeout) {
										Logging.log(
												APP_TAG,
												String.format(
														getString(R.string.reconnect_timeout),
														(timeout / 1000)));
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
							if (chatClient.getState() != ChatClient.ClientState.DISCONNECTED) {
								chatClient.disconnect();
							}

							if (chatClient.getState() == ChatClient.ClientState.DISCONNECTED) {
								chatClient.connect(DimensionAddress.RK);
							}
						}
					} catch (IOException e) {
						message(Message.obtain(null, Statics.MESSAGE_CONNECTION_ERROR, 0, 0));
						accountFailed = true;
						
						Logging.log(APP_TAG, e.getMessage());
					} catch (ClientStateException e) {
						message(Message.obtain(null, Statics.MESSAGE_CONNECTION_ERROR, 0, 0));
						accountFailed = true;
						
						Logging.log(APP_TAG, e.getMessage());
					}
				}
			}).start();
		}
	}

	private void authenticate() {
		if (chatClient.getState() == Client.ClientState.CONNECTED) {
			try {
				if (currentAccount != null) {
					chatClient.authenticate(currentAccount.getUsername(),
							currentAccount.getPassword());
				}
			} catch (IOException e) {
				message(Message.obtain(null, Statics.MESSAGE_CONNECTION_ERROR,
						0, 0));

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
				public void run() {
					try {
						Logging.log(APP_TAG, "Logging in");
						chatClient.login(character);
					} catch (IOException e) {
						message(Message.obtain(null,
								Statics.MESSAGE_CONNECTION_ERROR, 0, 0));

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
			if (NameFormat.format(currentCharacter.getName()).equals(
					NameFormat.format(name))) {
				Logging.toast(context, getString(R.string.not_add_yourself));

				return false;
			}

			List<Friend> tempList = friendList;
			boolean addCharacter = true;

			for (Friend friend : tempList) {
				if (NameFormat.format(friend.getName()).equals(
						NameFormat.format(name))) {
					addCharacter = false;
				}
			}

			if (addCharacter) {
				new Thread(new Runnable() {
					public void run() {
						try {
							chatClient.addFriend(name, true);
						} catch (IOException e) {
							Logging.log(APP_TAG, e.getMessage());
						}
					}
				}).start();

				Logging.toast(context, String.format(
						getString(R.string.added_to_buddy_list), name));
				return true;
			} else {
				Logging.toast(context, String.format(
						getString(R.string.already_in_buddy_list), name));
			}
		} else {
			Logging.toast(context, getString(R.string.not_connected));
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
				if (NameFormat.format(friend.getName()).equals(
						NameFormat.format(name))) {
					removeCharacter = true;
					removeID = friendCounter;
				}

				friendCounter++;
			}

			final int removeThis = removeID;

			if (removeCharacter) {
				new Thread(new Runnable() {
					public void run() {
						try {
							chatClient.deleteFriend(name, true);
							friendList.remove(removeThis);

							Message msg = Message.obtain(null,
									Statics.MESSAGE_FRIEND);
							msg.obj = friendList;

							message(msg);

						} catch (IOException e) {
							Logging.log(APP_TAG, e.getMessage());
						}
					}
				}).start();

				Logging.toast(context, String.format(
						getString(R.string.removed_from_buddy_list), name));
				return true;
			} else {
				Logging.toast(context, String.format(
						getString(R.string.not_in_buddy_list), name));
			}
		} else {
			Logging.toast(context, getString(R.string.disconnected));
		}

		return false;
	}

	private void togglePlayback() {
		if (isPlaying) {
			stop();
		} else {
			play();
		}
	}

	public void play() {
		aacPlayer.stop();

		/*
		if (PLAYURL.equals("")) {
			try {
				URL updateURL = new URL(Statics.GSP_PLAYLIST_PATH);
				URLConnection conn = updateURL.openConnection();
				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer baf = new ByteArrayBuffer(50);

				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}

				String html = new String(baf.toByteArray());
				String[] lines = html.split("\n");

				for (String s : lines) {
					Logging.log(APP_TAG, "Line: " + s);
					if (s.trim().startsWith("File1=")) {
						PLAYURL = s.trim().replace("File1=", "").trim();
					}
				}
			} catch (Exception e) {
				Logging.log(APP_TAG, e.getMessage());
			}
		}
		*/
		
		if (PLAYURL.equals("")) {
			PLAYURL = "http://5.152.208.98:8120";
		}

		if (!isPlaying) {
			try {
				tryToGetAudioFocus();
				aacPlayer.playAsync(PLAYURL, 48);
			} catch (Exception e) {
				Logging.log(APP_TAG, e.getMessage());

				if (retryOnFailure) {
					aacPlayer.stop();
					Logging.log(APP_TAG, "Player retry");
					play();
				}
			}
		}
	}

	public void stop() {
		if (isPlaying) {
			// audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
		}
		aacPlayer.stop();
	}

	private void tryToGetAudioFocus() {
		if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
				&& mAudioFocusHelper.requestFocus()) {
			mAudioFocus = AudioFocus.Focused;
		}
	}

	private void giveUpAudioFocus() {
		if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
				&& mAudioFocusHelper.abandonFocus()) {
			mAudioFocus = AudioFocus.NoFocusNoDuck;
		}
	}

	@Override
	public void onGainedAudioFocus() {
		mAudioFocus = AudioFocus.Focused;
	}

	@Override
	public void onLostAudioFocus(boolean canDuck) {
		mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck
				: AudioFocus.NoFocusNoDuck;
	}
}