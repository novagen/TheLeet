package com.rubika.aotalk.map;

public class Marker {
	private String title;
	private String zone;
	private boolean onRK;
	private int x;
	private int y;
	private int zoneid;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getZone() {
		return zone;
	}
	public void setZone(String zone) {
		this.zone = zone;
	}
	public boolean isOnRK() {
		return onRK;
	}
	public void setOnRK(boolean onRK) {
		this.onRK = onRK;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getZoneId() {
		return zoneid;
	}
	public void setZoneId(int zoneid) {
		this.zoneid = zoneid;
	}
}
