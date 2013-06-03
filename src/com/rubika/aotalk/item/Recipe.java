package com.rubika.aotalk.item;

import java.text.Collator;
import java.util.Comparator;

import android.graphics.drawable.Drawable;

import com.rubika.aotalk.recipebook.FragmentActivityRecipes.ListLoader;

public class Recipe {
	private ListLoader loader;
	private String desc;
	private String id;
	private int type;
	private int icon;
	
	public Recipe(ListLoader loader, String desc, String id, int icon, int type) {
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
    
    public static final Comparator<Recipe> ALPHA_COMPARATOR = new Comparator<Recipe>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(Recipe object1, Recipe object2) {
            return sCollator.compare(object1.getLabel(), object2.getLabel());
        }
    };
}