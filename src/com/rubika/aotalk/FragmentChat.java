package com.rubika.aotalk;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.rubika.aotalk.item.Channel;
import com.rubika.aotalk.item.ChatMessage;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

@SuppressLint("NewApi")
public class FragmentChat extends SherlockFragment {
	private static final String APP_TAG = "--> The Leet :: FragmentChat";
	private ListView messageList;
	private View fragmentChat;
	
	static FragmentChat newInstance() {
		FragmentChat f = new FragmentChat();
        return f;
    }
	
	public FragmentChat() {
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }	
	/*
	private class ModeCallback implements MultiChoiceModeListener {
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        MenuInflater inflater = getSherlockActivity().getSupportMenuInflater();
	        inflater.inflate(R.menu.menu_messages, menu);
	        mode.setTitle("Select Items");
	        return true;
	    }

	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return true;
	    }

	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	        case R.id.share:
	            Toast.makeText(AOTalk.getContext(), "Shared " + messageList.getCheckedItemCount() + " items", Toast.LENGTH_SHORT).show();
	            mode.finish();
	            break;
	        default:
	            Toast.makeText(messageList.getContext(), "Clicked " + item.getTitle(),
	                    Toast.LENGTH_SHORT).show();
	            break;
	        }
	        return true;
	    }

	    public void onDestroyActionMode(ActionMode mode) {
	    }

	    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
	        final int checkedCount = messageList.getCheckedItemCount();
	        switch (checkedCount) {
	            case 0:
	                mode.setSubtitle(null);
	                break;
	            case 1:
	                mode.setSubtitle("One item selected");
	                break;
	            default:
	                mode.setSubtitle("" + checkedCount + " items selected");
	                break;
	        }
	    }
	}
	*/
	
	public void setHint(String text) {
		if (fragmentChat != null) {
			((EditText) fragmentChat.findViewById(R.id.input)).setHint(text);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logging.log(APP_TAG, "onCreateView");
		
		if (container == null) {
            return null;
        }
				
		fragmentChat = inflater.inflate(R.layout.fragment_chat, container, false);
		
		messageList = (ListView) fragmentChat.findViewById(R.id.messagelist);
		
		//messageList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		//messageList.setMultiChoiceModeListener(new ModeCallback());
		messageList.setAdapter(AOTalk.messageAdapter);
		messageList.setDividerHeight(0);
		messageList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final ChatMessage message = AOTalk.messageAdapter.getItem(arg2);
				
				if (!message.getChannel().equals(Statics.CHANNEL_APPLICATION) && 
						!message.getChannel().equals(Statics.CHANNEL_SYSTEM) && 
						message.getCharacter() != null && 
						message.getCharacter().length() > 0) 
				{
					int diff = 0;
					
					if (message.getCharacter().equals(AOTalk.currentCharacterName)) {
						diff++;
					}
					
					if (message.getChannel().equals(Statics.CHANNEL_PM) || 
							message.getChannel().equals(Statics.CHANNEL_MAIN) || 
							message.getChannel().equals(Statics.CHANNEL_FRIEND) || 
							message.getChannel().equals(Statics.CHANNEL_DNET) || 
							message.getChannel().equals(Statics.CHANNEL_NEUTNET))
					{
						diff++;
					}
					
					final CharSequence[] listItems = new CharSequence[4 - diff];
					
					int itemCharacter = -1;
					int itemChannel = -1;
					int itemWhois = -1;
					int currentItem = 0;
					
					if (!message.getCharacter().equals(AOTalk.currentCharacterName)) {
						listItems[currentItem] = getString(R.string.msg_to) + " " + message.getCharacter();
						itemCharacter = currentItem;
						currentItem++;
					}
					
					Logging.log(APP_TAG, "Channel is " + message.getChannel());
					
					if (!message.getChannel().equals(Statics.CHANNEL_PM) && 
							!message.getChannel().equals(Statics.CHANNEL_MAIN) && 
							!message.getChannel().equals(Statics.CHANNEL_FRIEND) && 
							!message.getChannel().equals(Statics.CHANNEL_DNET) && 
							!message.getChannel().equals(Statics.CHANNEL_NEUTNET)) 
					{
						listItems[currentItem] = getString(R.string.msg_to) + " " + message.getChannel();
						itemChannel = currentItem;
						currentItem++;
					}
					
					listItems[currentItem] = String.format(getString(R.string.who_is), message.getCharacter());
					itemWhois = currentItem;
					
					listItems[++currentItem] = "Copy message text";
					
					
					final int fItemCharacter = itemCharacter;
					final int fItemChannel = itemChannel;
					final int fItemWhois = itemWhois;
					final int fItemCopy = currentItem;

					AlertDialog.Builder builder = new AlertDialog.Builder(AOTalk.getContext());
					builder.setTitle(getString(R.string.select_action));
					builder.setItems(
						listItems,
						new DialogInterface.OnClickListener() {
							@SuppressLint("NewApi")
							@SuppressWarnings("deprecation")
							public void onClick(DialogInterface dialog, final int item) {
								if (item == fItemCharacter && fItemCharacter >= 0) {
									AOTalk.currentTargetCharacter = message.getCharacter();
									AOTalk.currentTargetChannel = "";
									
									AOTalk.setServiceTargets();
								}
								
								if (item == fItemChannel && fItemChannel >= 0) {
									AOTalk.currentTargetCharacter = "";
									
									List<Channel> tempList = new ArrayList<Channel>();
									tempList.addAll(AOTalk.channelList);
									tempList.addAll(AOTalk.privateList);
									
									for (Channel channel : tempList) {
										if (channel.getName().toLowerCase(Locale.getDefault()).equals(message.getChannel().toLowerCase(Locale.getDefault()))) {
											AOTalk.currentTargetChannel = channel.getName();
											break;
										}
									}
									
									AOTalk.setServiceTargets();
								}
								
								if (item == fItemWhois && fItemWhois >= 0) {
									AOTalk.whoIs(message.getCharacter(), false);								
								}
								
								if (item == fItemCopy) {
									if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
									    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) AOTalk.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
									    clipboard.setText(Html.fromHtml(message.getMessage()).toString());
									} else {
									    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) AOTalk.getContext().getSystemService(Context.CLIPBOARD_SERVICE); 
									    android.content.ClipData clip = android.content.ClipData.newPlainText(getText(R.string.app_name), Html.fromHtml(message.getMessage()).toString());
									    clipboard.setPrimaryClip(clip);
									}
									Logging.toast(AOTalk.getContext(), getString(R.string.copied_to_clipboard));
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
		});
		
		messageList.setPersistentDrawingCache(ListView.PERSISTENT_ALL_CACHES);
        
        ((ImageButton) fragmentChat.findViewById(R.id.channel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AOTalk.setChannel();
			}
		});
        
        ((EditText) fragmentChat.findViewById(R.id.input)).setOnKeyListener(new OnKeyListener() {
			@Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
		        	if (((EditText) v).getText().toString().length() > 0) {
						AOTalk.sendMessage(((EditText) v).getText().toString());
						((EditText) v).setText("");
		        	}
		        	
	            	AOTalk.hideSoftKeyboard(v);          	
		        	
	            	return true;
				}
				
				return false;
			}
        });
        
        if (fragmentChat.findViewById(R.id.friendsfragment) != null) {
        	//FrameLayout friendsfragment = (FrameLayout) fragmentChat.findViewById(R.id.friendsfragment);
        	
        	FragmentManager fm = getFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.friendsfragment); // You can find Fragments just like you would with a 
                                                                                   // View by using FragmentManager.
            
            // If we are using activity_fragment_xml.xml then this the fragment will not be
            // null, otherwise it will be.
            if (fragment == null) {
                
                // We alter the state of Fragments in the FragmentManager using a FragmentTransaction. 
                // FragmentTransaction's have access to a Fragment back stack that is very similar to the Activity
                // back stack in your app's task. If you add a FragmentTransaction to the back stack, a user 
                // can use the back button to undo a transaction. We will cover that topic in more depth in
                // the second part of the tutorial.
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(R.id.friendsfragment, FragmentFriends.newInstance());
                ft.commit(); // Make sure you call commit or your Fragment will not be added. 
                             // This is very common mistake when working with Fragments!
            }
        }
		
        AOTalk.updateInputHint();
        
		return fragmentChat;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		Logging.log(APP_TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
}
