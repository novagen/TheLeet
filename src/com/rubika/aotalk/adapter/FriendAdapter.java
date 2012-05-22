package com.rubika.aotalk.adapter;

import java.util.List;

import com.rubika.aotalk.item.Friend;
import com.rubika.aotalk.R;

import android.content.Context;
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
		View layout = inflater.inflate(R.layout.friend, null);
		
		TextView t = (TextView)layout.findViewById(R.id.title);
        t.setText(getItem(arg0).getName());
        
        if(getItem(arg0).isOnline()) {
        	t.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.presence_online), null, null, null);
        } else {
        	t.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.presence_invisible), null, null, null);
        }

		return layout;
	}
}
