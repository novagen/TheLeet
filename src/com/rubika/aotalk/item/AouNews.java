package com.rubika.aotalk.item;

public class AouNews {
	private String label;
	private String desc;
	private String date;
	private String link;
	private String target;
	
	public AouNews(String label, String desc, String date, String link, String target) {
		this.desc = desc;
		this.date = date;
		this.label = label;
		this.link = link;
		this.target = target;
	}

	public String getLink() {
		return link;
	}
	
    public String getLabel() {
        return label;
    }
    
    public String getDesc() {
    	return desc;
    }
    
    public String getDate() {
    	return date;
    }
    
    public String getTarget() {
    	return target;
    }
    
    @Override public String toString() {
        return desc;
    }
}
