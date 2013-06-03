package com.rubika.aotalk.item;

import java.util.Comparator;

public class MonitorSite {
	private int id;
	private String name;
	private boolean enabled;
	
	public MonitorSite() {
	}
	
	public MonitorSite(int id, String name, boolean enabled) {
		this.id = id;
		this.name = name;
		this.enabled = enabled;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean getEnabled() {
		return this.enabled;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public static class SitenameComparator implements Comparator<MonitorSite> {
	    @Override
	    public int compare(MonitorSite site1, MonitorSite site2) {
	        return site1.getName().compareTo(site2.getName());
	    }
	}

}
