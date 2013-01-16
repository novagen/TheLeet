/*
 * MarketMessageAdapter.java
 *
 *************************************************************************
 * Copyright 2010 Christofer Engel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rubika.aotalk.adapter;

import java.util.List;

import com.rubika.aotalk.R;
import com.rubika.aotalk.item.MarketMessage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MarketMessageAdapter extends BaseAdapter {
    private Context context;
    private List<MarketMessage> listMessages;
    private boolean animationEnabled;
	private SharedPreferences settings;

    public MarketMessageAdapter(Context context, List<MarketMessage> listMessages, boolean enableAnimations) {
        this.context = context;
        this.listMessages = listMessages;
		animationEnabled = enableAnimations;
		settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getCount() {
        return listMessages.size();
    }

    public Object getItem(int position) {
        return listMessages.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
    	MarketMessage entry = listMessages.get(position);
        
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.message_market, null);
        }
        
        int namecolor = context.getResources().getColor(R.color.mainwhite);
        
        if(entry.getSide() == 1) {
        	namecolor = context.getResources().getColor(R.color.omni_blue);
        }
        
        if(entry.getSide() == 2) {
        	namecolor = context.getResources().getColor(R.color.clan_red);
        }
        
        if(entry.getSide() == 3) {
        	namecolor = context.getResources().getColor(R.color.neutral_yellow);
        }
        
        TextView from = (TextView) convertView.findViewById(R.id.from);
        from.setText(Html.fromHtml(entry.getCharacter()));
        from.setTextColor(namecolor);
        
        TextView stamp = (TextView) convertView.findViewById(R.id.time);
        stamp.setText(DateUtils.getRelativeTimeSpanString(entry.getTimestamp() * 1000));
        
        TextView message = (TextView) convertView.findViewById(R.id.message);
        message.setText("");
        
        message.append(Html.fromHtml(entry.getMessage()));
        message.setMovementMethod(LinkMovementMethod.getInstance());
        
		if (entry.showAnimation() && animationEnabled && settings.getBoolean("enableAnimations", true)) {
	        Animation animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float)1); 
	        
			animation.setDuration(200);
			animation.setFillAfter(true);
			
			convertView.setAnimation(animation);
			convertView.startAnimation(animation);
			
			entry.showAnimation(false);
		}

		return convertView;
    }
}
