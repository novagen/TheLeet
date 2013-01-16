package com.rubika.aotalk.item;

import android.graphics.Bitmap;

public class Item {
	private String name;
	private String id;
	private String icon;
	private Bitmap iconimage;
	
	public Item() {
	}
	
	public Item(String name, String id, String icon) {
		this.name = name;
		this.id = id;
		this.icon = icon;
	}
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setIconBitmap(Bitmap bitmap) {
		this.iconimage = bitmap;
	}
	
	public Bitmap getIconBitmap() {
		return this.iconimage;
	}
}
