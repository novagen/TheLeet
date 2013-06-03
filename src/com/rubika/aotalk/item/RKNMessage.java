package com.rubika.aotalk.item;

public class RKNMessage {
    private long id;
    private String message;
    private long timestamp;
    private String character;
	private String icon;
    private boolean doAnimation = true;
    
    public String getMessage() {
    	return message;
    }
    
    public void setMessage(String message) {
    	this.message = message;
    }
    
    public long getId() {
    	return id;
    }
    
    public void setId(long id) {
    	this.id = id;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getCharacter() {
        return character;
    }

	public void setCharacter(String character) {
		this.character = character;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public boolean showAnimation() {
		return this.doAnimation;
	}
	
	public void showAnimation(boolean doAnimation) {
		this.doAnimation = doAnimation;
	}
}
