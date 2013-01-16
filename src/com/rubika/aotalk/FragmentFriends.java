package com.rubika.aotalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;
import ao.misc.NameFormat;

import com.actionbarsherlock.app.SherlockFragment;
import com.rubika.aotalk.item.Friend;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

public class FragmentFriends extends SherlockFragment {
	private static final String APP_TAG = "--> The Leet ::FragmentFriends";

	static FragmentFriends newInstance() {
		FragmentFriends f = new FragmentFriends();
        return f;
    }
	
	public FragmentFriends() {
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logging.log(APP_TAG, "onCreateView");
		
		if (container == null) {
            return null;
        }
		
		final View fragmentFriends = inflater.inflate(R.layout.fragment_friends, container, false);
		
		((ListView) fragmentFriends.findViewById(R.id.friendlist)).setAdapter(AOTalk.friendAdapter);
		((ListView) fragmentFriends.findViewById(R.id.friendlist)).setDividerHeight(0);
		((ListView) fragmentFriends.findViewById(R.id.friendlist)).setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final Friend friend = AOTalk.friendAdapter.getItem(arg2);
				final CharSequence[] listItems = new CharSequence[3];
				
				listItems[0] = getString(R.string.send_private_message);
				listItems[1] = String.format(getString(R.string.who_is), friend.getName());
				listItems[2] = getString(R.string.remove_friend);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(AOTalk.getContext());
				builder.setTitle(friend.getName());
				builder.setItems(
					listItems,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, final int item) {
							if (item == 0) {
								AOTalk.editor.putString("lastUsedChannel", "");
								AOTalk.editor.putString("lastUsedCharacter", friend.getName());
								AOTalk.editor.commit();
								
								AOTalk.currentTargetCharacter = friend.getName();
								AOTalk.currentTargetChannel = "";

								AOTalk.actionBar.setSelectedNavigationItem(0);
								AOTalk.fragmentPager.setCurrentItem(1);
																
								AOTalk.setServiceTargets();
								
								return;
							}
							
							if (item == 1) {
								AOTalk.whoIs(friend.getName(), AOTalk.currentServerID, false);								
							}
							
							if (item == 2) {
				    	    	final String fname = friend.getName();
				    	    	
				    	    	AlertDialog acceptRemoveDialog = new AlertDialog.Builder(AOTalk.getContext()).create();
				    	    	
				    	    	acceptRemoveDialog.setTitle(String.format(getString(R.string.remove_friend_title), fname));
				    	    	acceptRemoveDialog.setMessage(String.format(getString(R.string.remove_friend_confirm), fname));
				    	    		            
				    	    	acceptRemoveDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
				    				public void onClick(DialogInterface dialog, int which) {
				    			    	Message msg = Message.obtain(null, Statics.MESSAGE_FRIEND_REMOVE);
				    			        msg.replyTo = AOTalk.serviceMessenger;
				    			        msg.obj = fname;
				    			        
				    			        try {
				    			        	AOTalk.service.send(msg);
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
		});
		
		((EditText) fragmentFriends.findViewById(R.id.input)).addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable e) {
				if(e != null && e.length() == 0){
					((ListView) fragmentFriends.findViewById(R.id.friendlist)).clearTextFilter();
		        }
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
		        AOTalk.friendAdapter.getFilter().filter(s);
			}
		});
		
		((ImageButton) fragmentFriends.findViewById(R.id.add_friend)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	if (((EditText) fragmentFriends.findViewById(R.id.input)).getText().toString().length() > 0) {
					Message msg = Message.obtain(null, Statics.MESSAGE_FRIEND_ADD);
			        msg.replyTo = AOTalk.serviceMessenger;
			        msg.obj = NameFormat.format(((EditText) fragmentFriends.findViewById(R.id.input)).getText().toString().trim());
			        
			        try {
			        	AOTalk.service.send(msg);
					} catch (RemoteException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
					
			        AOTalk.friendAdapter.getFilter().filter("");
					((EditText) fragmentFriends.findViewById(R.id.input)).setText("");
		    	}
			}
		});
		
		return fragmentFriends;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		Logging.log(APP_TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
}
