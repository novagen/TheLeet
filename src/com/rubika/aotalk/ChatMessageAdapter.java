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
package com.rubika.aotalk;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatMessageAdapter extends BaseAdapter {
    private Context context;
    private List<ChatMessage> listMessages;

    public ChatMessageAdapter(Context context, List<ChatMessage> listMessages) {
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
        ChatMessage entry = listMessages.get(position);
        
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.message, null);
        }

        TextView message = (TextView) convertView.findViewById(R.id.message);
        message.setText("");
        message.append(Html.fromHtml(entry.getMessage()));
        message.setMovementMethod(LinkMovementMethod.getInstance());
                
        return convertView;
    }
}
