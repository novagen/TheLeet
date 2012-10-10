package com.rubika.aotalk.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rubika.aotalk.R;
import com.rubika.aotalk.item.Tool;

public class GridAdapter extends ArrayAdapter<Tool> {
	private Context context;
	
	public GridAdapter(Context context, int textViewResourceId, List<Tool> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        
		if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_item, null);
        }

        Tool item = getItem(position);
        TextView tv = (TextView) convertView.findViewById(R.id.grid_item_text);
       
		tv.setText(item.getName());
		tv.setCompoundDrawablesWithIntrinsicBounds(null, context.getResources().getDrawable(item.getIcon()), null, null);
		tv.setOnClickListener(item.getOnClick());
		
		return convertView;
	}
}
