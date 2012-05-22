package com.rubika.aotalk.adapter;

import java.util.List;

import com.rubika.aotalk.item.Channel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class ChannelAdapter extends ArrayAdapter<Channel> implements SpinnerAdapter {
	public ChannelAdapter(Context context, int textViewResourceId, List<Channel> objects) {
		super(context, textViewResourceId, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

		TextView title = (TextView)convertView.findViewById(android.R.id.text1);
		title.setPadding(0, 0, 0, 0);
        title.setText(getItem(position).getName());

		return convertView;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

		TextView title = (TextView)convertView.findViewById(android.R.id.text1);
		title.setPadding(title.getPaddingLeft(), title.getPaddingLeft(), title.getPaddingRight(), title.getPaddingLeft());
        title.setText(getItem(position).getName());
        
        return convertView;
	}
}
