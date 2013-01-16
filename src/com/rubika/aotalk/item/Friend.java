/*
 * Friend.java
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

import java.util.Comparator;

public class Friend {
	private boolean online;
	private String name;
	private int id;
	private boolean aospeakstatus;
	//private Bitmap iconimage;
	private String icon;
	
	public Friend() {
	}
	
	public Friend(String name, int id, boolean online, String icon) {
		this.name = name;
		this.id = id;
		this.online = online;
		this.icon = icon;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public void setOnline(boolean online) {
		this.online = online;
	}
	
	public void setAOSpeakStatus(boolean aospeakstatus) {
		this.aospeakstatus = aospeakstatus;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getID() {
		return this.id;
	}
	
	public boolean isOnline() {
		return this.online;
	}
	
	public boolean getAOSpeakStatus() {
		return this.aospeakstatus;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	/*
	public void setIconBitmap(Bitmap bitmap) {
		this.iconimage = bitmap;
	}
	
	public Bitmap getIconBitmap() {
		return this.iconimage;
	}
	*/
	
	@Override
	public String toString() {
		return this.name.toLowerCase();
	}
	
	public static class CustomComparator implements Comparator<Friend> {
	    @Override
	    public int compare(Friend friend1, Friend friend2) {
	        return friend1.getName().compareTo(friend2.getName());
	    }
	}
}
