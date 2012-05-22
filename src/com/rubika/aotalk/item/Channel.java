package com.rubika.aotalk.item;

import java.util.Comparator;

public class Channel {
	private String name;
	private int id;
	private boolean enabled;
	private boolean muted;
	
	public Channel(String name, int id, boolean enabled, boolean muted) {
		this.name = name;
		this.id = id;
		this.enabled = enabled;
		this.muted = muted;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean getMuted() {
		return muted;
	}
	
	public void setMuted(boolean muted) {
		this.muted = muted;
	}
	
	public static class CustomComparator implements Comparator<Channel> {
	    @Override
	    public int compare(Channel channel1, Channel channel2) {
	        return channel1.getName().compareTo(channel2.getName());
	    }
	}
}
