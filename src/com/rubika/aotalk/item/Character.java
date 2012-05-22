package com.rubika.aotalk.item;

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
}