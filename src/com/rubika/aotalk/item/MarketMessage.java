/*
 * MarketMessage.java
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

public class MarketMessage {
    private long timestamp;
    private String message;
    private String character;
    private String channel;
    private int side;
    
    // Constructor for the MarketMessage class
    public MarketMessage(long timestamp, String message, String character, String channel, int side) {
            super();
            this.message   = message;
            this.timestamp = timestamp;
            this.character = character;
            this.channel   = channel;
            this.side 	   = side;
    }
    
    // Getter and setter methods for all the fields.
    public String getMessage() {
            return message;
    }
    
    public void setMessage(String message) {
            this.message = message;
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
    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }
}