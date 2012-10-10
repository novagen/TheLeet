/*
 * ChatMessage.java
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
package com.rubika.aotalk.item;

import com.rubika.aotalk.service.ServiceTools;
import com.rubika.aotalk.util.ChatParser;

public class ChatMessage {
    private String message;
    private long timestamp;
    private String character;
    private String channel;
    private long id;
    private int server;
    private boolean doAnimation = true;
    
    // Constructor for the ChatMessage class
    public ChatMessage() {
    }
    
    // Constructor for the ChatMessage class
    public ChatMessage(long timestamp, String message, String character, String channel, int id, int server) {
	    super();
	    this.message   = message;
	    this.timestamp = timestamp;
	    this.character = character;
	    this.channel   = channel;
	    this.id 	   = id;
    	this.server    = server;
    }
    
    // Getter and setter methods for all the fields.
    public String getMessage() {
    	return message;
    }
    
    public void setMessage(String message) {
    	this.message = message;
    }
    
    public long getId() {
    	return id;
    }
    
    public void setId(long id) {
    	this.id = id;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getCharacter() {
        return character;
    }

	public void setCharacter(String character) {
		this.character = character;
	}
    
    public String getChannel() {
        return channel;
    }

	public void setChannel(String channel) {
		this.channel = channel;
	}
    
    public int getType() {
    	int type = ChatParser.TYPE_GROUP_MESSAGE;
        
	    if (channel.equals(ServiceTools.CHANNEL_PRIVATE)) {
	    	type = ChatParser.TYPE_PG_MESSAGE;
	    } else if (channel.equals(ServiceTools.CHANNEL_SYSTEM)) {
	    	type = ChatParser.TYPE_SYSTEM_MESSAGE;
	    } else if (channel.equals(ServiceTools.CHANNEL_PM)) {
	    	type = ChatParser.TYPE_PRIVATE_MESSAGE;
	    } else if (channel.equals(ServiceTools.CHANNEL_FRIEND)) {
	    	type = ChatParser.TYPE_FRIEND_MESSAGE;
	    } else if (channel.equals(ServiceTools.CHANNEL_APPLICATION)) {
	    	type = ChatParser.TYPE_CLIENT_MESSAGE;
	    }
        
    	return type;
    }
    
    public int getServer() {
        return server;
    }

	public void setServer(int server) {
		this.server = server;
	}
	
	public boolean showAnimation() {
		return this.doAnimation;
	}
	
	public void showAnimation(boolean doAnimation) {
		this.doAnimation = doAnimation;
	}
}