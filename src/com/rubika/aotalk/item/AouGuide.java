package com.rubika.aotalk.item;

import android.graphics.drawable.Drawable;

import com.rubika.aotalk.aou.FragmentGuides.ListLoader;

public class AouGuide {
	private ListLoader loader;
	private String desc;
	private String id;
	private int type;
	private int icon;
	
	public AouGuide(ListLoader loader, String desc, String id, int icon, int type) {
		this.loader = loader;
		this.desc = desc;
		this.id = id;
		this.icon = icon;
		this.type = type;
	}
	
	public String getLabel() {
	    return desc;
	}
	
	public Drawable getIcon() {
		return loader.getContext().getResources().getDrawable(icon);
	}
	
	public String getID() {
		return id;
	}
	
	public ListLoader getLoader() {
		return loader;
	}
	
	public int getType() {
		return type;
	}
	
	@Override public String toString() {
	    return desc;
	}
}