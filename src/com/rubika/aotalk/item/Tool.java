package com.rubika.aotalk.item;

import android.view.View.OnClickListener;

public class Tool {
	private String name;
	private int icon;
	private OnClickListener onclick;
	
	public Tool(String name, int icon, OnClickListener onclick) {
		this.name = name;
		this.icon = icon;
		this.onclick = onclick;
	}
	
	public String getName() {
		return name;
	}
	
	public int getIcon() {
		return icon;
	}
	
	public OnClickListener getOnClick() {
		return onclick;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setIcon(int icon) {
		this.icon = icon;
	}
	
	public void setOnClick(OnClickListener onclick) {
		this.onclick = onclick;
	}
	
}
