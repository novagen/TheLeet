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

import java.util.Calendar;
import java.util.List;

import com.rubika.aotalk.R;
import com.rubika.aotalk.item.MarketMessage;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MarketMessageAdapter extends BaseAdapter {
    private Context context;
    private List<MarketMessage> listMessages;

    public MarketMessageAdapter(Context context, List<MarketMessage> listMessages) {
        this.context = context;
        this.listMessages = listMessages;
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
        
        String name = entry.getCharacter();
        String namecolor = "fff";
        
        if(entry.getSide() == 1) {
        	namecolor = "48b4ff";
        }
        
        if(entry.getSide() == 2) {
        	namecolor = "f86868";
        }
        
        if(entry.getSide() == 3) {
        	namecolor = "f1f16e";
        }
        
        name = "<font color=#" + namecolor + ">" + name + "</font>";
        
        //Calendar date = new Calendar(entry.getTimestamp() * 1000);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(entry.getTimestamp() * 1000);
        
        String time_h = "" + date.get(Calendar.HOUR_OF_DAY);
        String time_m = "" + date.get(Calendar.MINUTE);
        
        if(time_h.length() < 2) {
        	time_h = "0" + time_h;
        }
        
        if(time_m.length() < 2) {
        	time_m = "0" + time_m;
        }
        
        String time = "<b>[" + time_h + ":" + time_m + "]</b> ";
        
        name = time + name;
               
        TextView from = (TextView) convertView.findViewById(R.id.from);
        from.setText(Html.fromHtml(name));
        
        TextView message = (TextView) convertView.findViewById(R.id.message);
        message.setText("");
        
        message.append(Html.fromHtml(entry.getMessage()));
        message.setMovementMethod(LinkMovementMethod.getInstance());
        
        return convertView;
    }
}
