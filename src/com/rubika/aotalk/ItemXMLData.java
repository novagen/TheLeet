/*
 * ItemXMLData.java
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

import java.util.ArrayList;

public class ItemXMLData {
	private String name        = null;
	private String description = null;
	private String quality	   = null;
	private String icon  	   = null;
	private String type        = null;
	private String flags	   = null;
	private String reqs		   = null;
	private String attr		   = null;

	private ArrayList<String[]> pointlist = new ArrayList<String[]>();
	
	public void createPoint() {
		String[] point = new String[8];
		point[0] = this.name;
		point[1] = this.description;
		point[2] = this.quality;
		point[3] = this.icon;
		point[4] = this.type;
		point[5] = this.flags;
		point[6] = this.reqs;
		point[7] = this.attr;
		
		this.pointlist.add(point);
	}
	
	public ArrayList<String[]> getPoints() {
		return this.pointlist;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setQuality(String quality) {
		this.quality = quality;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setFlags(String flags) {
		this.flags = flags;
	}
	
	public void setReqs(String reqs) {
		this.reqs = reqs;
	}
	
	public void setAttr(String attr) {
		this.attr = attr;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getQuality() {
		return this.quality;
	}
	
	public String getIcon() {
		return this.icon;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getFlags() {
		return this.flags;
	}
	
	public String getReqs() {
		return this.reqs;
	}
	
	public String getAttr() {
		return this.attr;
	}
}