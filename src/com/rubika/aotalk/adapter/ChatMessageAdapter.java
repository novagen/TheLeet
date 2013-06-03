/*
 * ChatMessageAdapter.java
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
import java.util.Locale;

import com.rubika.aotalk.item.ChatMessage;
import com.rubika.aotalk.util.ChatParser;
import com.rubika.aotalk.util.ImageLoader;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;
import com.rubika.aotalk.AOTalk;
import com.rubika.aotalk.R;
import com.rubika.aotalk.TheLeet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
	protected static final String APP_TAG  = "--> The Leet :: ChatMessageAdapter";
	private static SharedPreferences settings;
	
    private boolean animationEnabled;
    private ImageLoader imageLoader; 
	
	public ChatMessageAdapter(Context context, int textViewResourceId, List<ChatMessage> objects, boolean enableAnimations) {
		super(context, textViewResourceId, objects);
		settings = PreferenceManager.getDefaultSharedPreferences(context);
		animationEnabled = enableAnimations;
		imageLoader = new ImageLoader(context);
	}

    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ChatMessage entry = getItem(position);
        
		boolean enableTimestamp = settings.getBoolean("showTimestamp", true);
		boolean enableFaces = settings.getBoolean("enableFaces", true);
		boolean enableAnimations = settings.getBoolean("enableAnimations", true);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
           	convertView = inflater.inflate(R.layout.message_chat, null);
        }

        TextView t = (TextView) convertView.findViewById(R.id.message);
        
        String text = entry.getMessage().replaceFirst("] 0:", "]");
        
        if (enableTimestamp) {
        	if (enableFaces) {
        		text = ChatParser.getFormattedTimeFromLong(entry.getTimestamp()) + "<br />" + text;
        	} else {
        		text = ChatParser.getFormattedTimeFromLong(entry.getTimestamp()) + " " + text;
        	}
        }
				
        t.setText(Html.fromHtml(text));
        t.setTextColor(getColor(entry));
        t.setMovementMethod(LinkMovementMethod.getInstance());
        
        ImageView i1 = (ImageView)convertView.findViewById(R.id.icon);
        ImageView i2 = (ImageView)convertView.findViewById(R.id.icon2);
        LinearLayout h1 = (LinearLayout)convertView.findViewById(R.id.iconholder);
        LinearLayout h2 = (LinearLayout)convertView.findViewById(R.id.iconholder2);
                
        if (enableFaces) {
        	if (entry.getCharacter() != null && (entry.getMessage().startsWith("to [") || entry.getCharacter().equals(AOTalk.currentCharacterName))) {
    	        i1.setImageBitmap(null);
        		
        		if (getItem(position).getIcon() != null) {
        			imageLoader.DisplayImage(Statics.PHOTO_PATH + getItem(position).getIcon(), i2);
        		} else {
        			Logging.log(APP_TAG, "Image is null");
        			i2.setImageDrawable(TheLeet.getContext().getResources().getDrawable(R.drawable.ic_notification_old));
        		}

    	        h1.setVisibility(View.GONE);
            	h2.setVisibility(View.VISIBLE);
            } else {
    	        i2.setImageBitmap(null);
    	        
        		if (getItem(position).getIcon() != null) {
        			imageLoader.DisplayImage(Statics.PHOTO_PATH + getItem(position).getIcon(), i1);
        		} else {
        			Logging.log(APP_TAG, "Image is null");
        			i1.setImageDrawable(TheLeet.getContext().getResources().getDrawable(R.drawable.ic_notification_old));
        		}

    	        h1.setVisibility(View.VISIBLE);
            	h2.setVisibility(View.GONE);
            }
        } else {
        	h1.setVisibility(View.GONE);
        	h2.setVisibility(View.GONE);
        }
                
		if (entry.showAnimation() && animationEnabled && enableAnimations) {
	        Animation animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f); 
	        
			animation.setDuration(200);
			animation.setFillAfter(true);
			
			convertView.setAnimation(animation);
			convertView.startAnimation(animation);
			
			entry.showAnimation(false);
		}
		
        return convertView;
    }
    
    private static int getColor(ChatMessage entry) {
		int colorInt;
		
    	switch(entry.getType()) {
		case ChatParser.MESSAGE_TYPE_SYSTEM:
			colorInt = settings.getInt("color_system", Statics.COLOR_ORG_SYS);
			break;
		case ChatParser.MESSAGE_TYPE_PRIVATE:
			colorInt = settings.getInt("color_prv", Statics.COLOR_ORG_PRV);
			break;
		case ChatParser.MESSAGE_TYPE_CLIENT:
			colorInt = settings.getInt("color_app", Statics.COLOR_ORG_APP);
			break;
		case ChatParser.MESSAGE_TYPE_GROUP:
			if (isOrgChannel(entry.getChannel())) {
				colorInt = settings.getInt("color_ocn", Statics.COLOR_ORG_OCN);
			} else {
				colorInt = settings.getInt("color_group", Statics.COLOR_ORG_GRP);
			}

			break;
		case ChatParser.MESSAGE_TYPE_FRIEND:
			colorInt = settings.getInt("color_frn", Statics.COLOR_ORG_FRN);
			break;
		default:
			colorInt = settings.getInt("color_group", Statics.COLOR_ORG_GRP);
			break;
		}
   	
    	return colorInt;
    }
    
    private static boolean isOrgChannel(String channel) {
		boolean isOrg = false;
		String mainorg = settings.getString("mainorg", "");
		
		if (mainorg.equals("")) {
			mainorg = "clan (name unknown)";
		} else {
			mainorg = "clan (name unknown)," + mainorg;
		}
		
		String[] orgs = mainorg.split(",");
		
		for (int i = 0; i < orgs.length; i++) {
			if (channel.trim().toLowerCase(Locale.getDefault()).equals(orgs[i].trim().toLowerCase(Locale.getDefault()))) {
				isOrg = true;
			}
		}
		
		return isOrg;
    }
}
