package com.rubika.aotalk.adapter;

import java.util.List;

import com.rubika.aotalk.item.Friend;
import com.rubika.aotalk.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FriendAdapter extends ArrayAdapter<Friend> {
	public FriendAdapter(Context context, int textViewResourceId, List<Friend> friends) {
		super(context, textViewResourceId, friends);
	}
	
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.list_item_friend, null);
		
		TextView t = (TextView)layout.findViewById(R.id.title);
        t.setText(getItem(arg0).getName());
        
        Drawable online = getContext().getResources().getDrawable(R.drawable.presence_invisible);
        Drawable aospeak = null;
        
        if(getItem(arg0).isOnline()) {
        	online = getContext().getResources().getDrawable(R.drawable.presence_online);
        }
        
        if(getItem(arg0).getAOSpeakStatus()) {
        	aospeak = getContext().getResources().getDrawable(R.drawable.aospeak);	
        }
        
        t.setCompoundDrawablesWithIntrinsicBounds(
        		online, 
        		null, 
        		aospeak, 
        		null
        	);

		return layout;
	}
}
