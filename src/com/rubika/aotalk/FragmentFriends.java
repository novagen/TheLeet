package com.rubika.aotalk;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;
import ao.misc.NameFormat;

import com.actionbarsherlock.app.SherlockFragment;
import com.rubika.aotalk.adapter.FriendAdapter;
import com.rubika.aotalk.service.ServiceTools;
import com.rubika.aotalk.util.Logging;

public class FragmentFriends extends SherlockFragment {
	private static final String APP_TAG = "--> AnarchyTalk::FragmentFriends";
	private FriendAdapter friendAdapter;
	private OnItemLongClickListener clickListener;
	private TextWatcher keyListener;
	private AOTalk activity;
	
	static FragmentFriends newInstance(AOTalk activity, FriendAdapter friendAdapter, OnItemLongClickListener clickListener, TextWatcher keyListener) {
		FragmentFriends f = new FragmentFriends(activity, friendAdapter, clickListener, keyListener);
        return f;
    }
	
	public FragmentFriends() {
	}
	
	public FragmentFriends(AOTalk activity, FriendAdapter friendAdapter, OnItemLongClickListener clickListener, TextWatcher keyListener) {
		this.activity = activity;
		this.friendAdapter = friendAdapter;
		this.clickListener = clickListener;
		this.keyListener = keyListener;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logging.log("FragmentFriends", "onCreateView");
		
		if (container == null) {
            return null;
        }
		
		final View fragmentFriends = inflater.inflate(R.layout.fragment_friends, container, false);
		((ListView) fragmentFriends.findViewById(R.id.friendlist)).setAdapter(friendAdapter);
		((ListView) fragmentFriends.findViewById(R.id.friendlist)).setOnItemLongClickListener(clickListener);
		
		((EditText) fragmentFriends.findViewById(R.id.input)).addTextChangedListener(keyListener);
		((ImageButton) fragmentFriends.findViewById(R.id.add_friend)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	if (((EditText) fragmentFriends.findViewById(R.id.input)).getText().toString().length() > 0) {
					Message msg = Message.obtain(null, ServiceTools.MESSAGE_FRIEND_ADD);
			        msg.replyTo = activity.serviceMessenger;
			        msg.obj = NameFormat.format(((EditText) fragmentFriends.findViewById(R.id.input)).getText().toString().trim());
			        
			        try {
			        	activity.service.send(msg);
					} catch (RemoteException e) {
						Logging.log(APP_TAG, e.getMessage());
					}
					
					((EditText) fragmentFriends.findViewById(R.id.input)).setText("");
		    	}
			}
		});
		
		return fragmentFriends;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		Logging.log("FragmentFriends", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
}
