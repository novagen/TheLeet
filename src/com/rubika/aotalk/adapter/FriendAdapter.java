package com.rubika.aotalk.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.rubika.aotalk.item.Friend;
import com.rubika.aotalk.util.ImageLoader;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;
import com.rubika.aotalk.R;
import com.rubika.aotalk.TheLeet;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendAdapter extends ArrayAdapter<Friend> {
	private static final String APP_TAG = "--> The Leet :: FriendAdapter";

	private boolean iconsEnabled;
	private SharedPreferences settings;
    private ImageLoader imageLoader;
    private List<Friend> friendsOrg = new ArrayList<Friend>();
    private List<Friend> friendsFlt = new ArrayList<Friend>();
    private AdapterFilter filter;

    public FriendAdapter(Context context, int textViewResourceId, List<Friend> friends, boolean enableIcons) {
		super(context, textViewResourceId, friends);
		iconsEnabled = enableIcons;
		settings = PreferenceManager.getDefaultSharedPreferences(context);
		imageLoader = new ImageLoader(context);
		
		friendsOrg = friends;
		friendsFlt = friends;
	}
    
    @Override
    public void add(Friend object) {
    	friendsOrg.add(object);
    	super.add(object);
    }
    
    @Override
    public void addAll(Collection<? extends Friend> friends) {
    	friendsOrg.clear();
    	friendsOrg.addAll(friends);
    }
    
    @Override
    public void clear() {
    	friendsOrg.clear();
    	friendsFlt.clear();
    	super.clear();
    }
    
    @Override
    public Filter getFilter() {
        if (filter == null){
          filter  = new AdapterFilter();
        }
        return filter;
      }
    
    private class AdapterFilter extends Filter{
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
			Logging.log(APP_TAG, "filtering results: " + constraint);

			FilterResults results = new FilterResults();
            ArrayList<Friend> i = new ArrayList<Friend>();
            
            if (constraint!= null && constraint.toString().length() > 0) {
            	for (int index = 0; index < friendsOrg.size(); index++) {
                    Friend si = friendsOrg.get(index);
                    
                    if(si.toString().startsWith(constraint.toString().toLowerCase())){
                      i.add(si);  
                    }
                }
            	
                results.values = i;
                results.count = i.size(); 
            } else {
            	synchronized (friendsOrg) {
                    results.values = friendsOrg;
                    results.count = friendsOrg.size();
                }
            }
            
        	return results;
        }

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			friendsFlt = (List<Friend>)results.values;
	        notifyDataSetChanged();
			Logging.log(APP_TAG, "returning filtered results: " + friendsFlt.size());
		}
    }
    
    @Override
    public int getCount() {
    	return friendsFlt.size();
    }
    
    @Override
    public Friend getItem(int position) {
        return friendsFlt.get(position);
    }   
	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_friend, null);
        }
	
        
		TextView t = (TextView)convertView.findViewById(R.id.title);
        t.setText(getItem(position).getName());
        
        if(getItem(position).isOnline()) {
        	t.setTextColor(Color.parseColor(getContext().getString(R.color.mainwhite)));
        } else {
        	t.setTextColor(Color.parseColor(getContext().getString(R.color.subgrey)));
        }
        
        
        ImageView i = (ImageView)convertView.findViewById(R.id.icon);
        
        if (iconsEnabled && settings.getBoolean("enableFaces", true)) {
    		if (getItem(position).getIcon() != null) {
    			imageLoader.DisplayImage(Statics.PHOTO_PATH + getItem(position).getIcon(), i);
    		} else {
    			i.setImageDrawable(TheLeet.getContext().getResources().getDrawable(R.drawable.ic_notification_old));
    		}
	        
	    	i.setVisibility(View.VISIBLE);
        	t.setPadding((int)Math.round(t.getPaddingRight() * 1.5), 0, t.getPaddingRight(), 0);
        } else {
        	i.setVisibility(View.GONE);
        	t.setPadding(t.getPaddingRight(), t.getPaddingRight(), t.getPaddingRight(), t.getPaddingRight());
        }
        
        
        ImageView s = (ImageView)convertView.findViewById(R.id.aospeak);

        if (iconsEnabled && getItem(position).getAOSpeakStatus()) {
        	s.setImageDrawable(getContext().getResources().getDrawable(R.drawable.aospeak));
        	s.setVisibility(View.VISIBLE);
        } else {
        	s.setVisibility(View.GONE);
        }

		return convertView;
	}
}
