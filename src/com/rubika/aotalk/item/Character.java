package com.rubika.aotalk.item;

import java.util.Comparator;

public class Character {
    public final String text;
    public final int icon;
    public final int type;
    public int order;
    
    public Character(String text, Integer icon, Integer type) {
        this.text = text;
        this.icon = icon;
        this.type = type;
        this.order = 0;
    }
    
    public Character(String text, Integer icon, Integer type, Integer order) {
        this.text = text;
        this.icon = icon;
        this.type = type;
        this.order = order;
    }
    
    @Override
    public String toString() {
        return text;
    }
    
    public int getType() {
    	return type;
    }
    
    public int getOrder() {
    	return order;
    }
    
    public void setOrder(int order) {
    	this.order = order;
    }
    
	public static class CustomComparator implements Comparator<Character> {
	    @Override
	    public int compare(Character character1, Character character2) {
	        return character1.toString().compareTo(character2.toString());
	    }
	}
}