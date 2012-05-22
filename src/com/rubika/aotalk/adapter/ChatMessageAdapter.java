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

import com.rubika.aotalk.item.ChatMessage;
import com.rubika.aotalk.ui.colorpicker.ColorPickerPreference;
import com.rubika.aotalk.util.ChatParser;
import com.rubika.aotalk.Preferences;
import com.rubika.aotalk.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
	private SharedPreferences settings;
	
	private int COLOR_APP;
	private int COLOR_SYS;
	private int COLOR_PRV;
	private int COLOR_GRP;
	private int COLOR_ORG;
	private int COLOR_FRN;
	
	public ChatMessageAdapter(Context context, int textViewResourceId, List<ChatMessage> objects) {
		super(context, textViewResourceId, objects);
		settings = PreferenceManager.getDefaultSharedPreferences(context);
	}

	protected static final String APP_TAG = "--> ChatMessageAdapter";

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ChatMessage entry = getItem(position);
        
        COLOR_APP = settings.getInt("color_app", Preferences.COLOR_ORG_APP);
    	COLOR_SYS = settings.getInt("color_system", Preferences.COLOR_ORG_SYS);
    	COLOR_PRV = settings.getInt("color_tell", Preferences.COLOR_ORG_PRV);
    	COLOR_GRP = settings.getInt("color_group", Preferences.COLOR_ORG_GRP);
    	COLOR_ORG = settings.getInt("color_org", Preferences.COLOR_ORG_ORG);
    	COLOR_FRN = settings.getInt("color_frn", Preferences.COLOR_ORG_FRN);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chatmessage, null);
        }

        TextView message = (TextView) convertView.findViewById(R.id.message);
        
        String text = "";
        text += entry.getMessage().replaceFirst("] 0:", "]");

    	String color = ColorPickerPreference.convertToARGB(COLOR_GRP);
       
		switch(entry.getType()) {
		case ChatParser.TYPE_SYSTEM_MESSAGE:
			color = ColorPickerPreference.convertToARGB(COLOR_SYS);
			break;
		case ChatParser.TYPE_PRIVATE_MESSAGE:
			color = ColorPickerPreference.convertToARGB(COLOR_PRV);
			break;
		case ChatParser.TYPE_CLIENT_MESSAGE:
			color = ColorPickerPreference.convertToARGB(COLOR_APP);
			break;
		case ChatParser.TYPE_GROUP_MESSAGE:
			color = ColorPickerPreference.convertToARGB(COLOR_GRP);
			break;
		case ChatParser.TYPE_ORG_MESSAGE:
			color = ColorPickerPreference.convertToARGB(COLOR_ORG);
			break;
		case ChatParser.TYPE_FRIEND_MESSAGE:
			color = ColorPickerPreference.convertToARGB(COLOR_FRN);
			break;
		}

        if (settings.getBoolean("showMessageColors", true)) {
        	text = "<font color=\"#" + color.substring(2) + "\">" + text + "</font>";
        }
        
        if (settings.getBoolean("showTimestamp", true)) {
        	text = ChatParser.getFormattedTimeFromLong(entry.getTimestamp()) + " " + text;
        }
				
        message.setText(Html.fromHtml(text));
        message.setMovementMethod(LinkMovementMethod.getInstance());
                
        return convertView;
    }
}
