package com.rubika.aotalk.adapter;

import java.util.List;

import com.rubika.aotalk.item.Character;
import com.rubika.aotalk.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CharacterAdapter extends BaseAdapter {
	private Context context;
	private List<Character> listItems;
	
	public CharacterAdapter(Context context, int textViewResourceId, List<Character> listItems) {
		this.context = context;
		this.listItems = listItems;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = new View(context);
		
		if(listItems.get(position) != null) {
       		layout = inflater.inflate(R.layout.list_item, null);
	
	        TextView t = (TextView)layout.findViewById(R.id.title);
	        t.setText(listItems.get(position).text);
		}
		
        return layout;
    }

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int position) {
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
