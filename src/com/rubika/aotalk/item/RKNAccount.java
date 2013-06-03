package com.rubika.aotalk.item;

public class RKNAccount {
	private int AccountId;
	private String Username;
	private String Password;
	
	public RKNAccount(int AccountId, String Username, String Password) {
		this.AccountId = AccountId;
		this.Username = Username;
		this.Password = Password;
	}
	
	public int getAccountId() {
		return this.AccountId;
	}
	
	public String getUsername() {
		return this.Username;
	}
	
	public String getPassword() {
		return this.Password;
	}
	
	public void setAccountId(int AccountId) {
		this.AccountId = AccountId;
	}
	
	public void setUsername(String Username) {
		this.Username = Username;
	}
	
	public void setPassword(String Password) {
		this.Password = Password;
	}
}
