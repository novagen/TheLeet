package com.rubika.aotalk.item;

public class Account {
	private String username;
	private String password;
	private boolean autoconnect;
	private int id;
	
	public Account(String username, String password, boolean autoconnect, int id) {
		this.username = username;
		this.password = password;
		this.autoconnect = autoconnect;
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean getAutoconnect() {
		return autoconnect;
	}
	
	public void setAutoconnect(boolean autoconnect) {
		this.autoconnect = autoconnect;
	}
	
	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		this.id = id;
	}
}
