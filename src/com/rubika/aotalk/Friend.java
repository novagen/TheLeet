/*
 * Friend.java
 *
 *************************************************************************
 * Copyright 2010 Christofer Engel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rubika.aotalk;

public class Friend {
	private boolean online;
	private String name;
	private int id;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public void setOnline(boolean online) {
		this.online = online;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getID() {
		return this.id;
	}
	
	public boolean isOnline() {
		return this.online;
	}
}
