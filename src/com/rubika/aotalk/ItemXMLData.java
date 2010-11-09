package com.rubika.aotalk;

import java.util.ArrayList;

public class ItemXMLData {
	private String name        = null;
	private String description = null;
	private String quality	   = null;
	private String icon  	   = null;

	private ArrayList<String[]> pointlist = new ArrayList<String[]>();
	
	public void createPoint() {
		String[] point = new String[4];
		point[0] = this.name;
		point[1] = this.description;
		point[2] = this.quality;
		point[3] = this.icon;
		
		this.pointlist.add(point);
	}
	
	public ArrayList<String[]> getPoints() {
		return this.pointlist;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setQuality(String quality) {
		this.quality = quality;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getQuality() {
		return this.quality;
	}
	
	public String getIcon() {
		return this.icon;
	}
}