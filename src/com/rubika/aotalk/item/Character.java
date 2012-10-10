package com.rubika.aotalk.item;

import java.util.Comparator;

public class Character {
    public final String text;
    public final int icon;
    public final int type;
    
    public Character(String text, Integer icon, Integer type) {
        this.text = text;
        this.icon = icon;
        this.type = type;
    }
    
    @Override
    public String toString() {
        return text;
    }
    
    public int getType() {
    	return type;
    }
    
	public static class CustomComparator implements Comparator<Character> {
	    @Override
	    public int compare(Character character1, Character character2) {
	        return character1.toString().compareTo(character2.toString());
	    }
	}
}