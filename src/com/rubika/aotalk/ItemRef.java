/*
 * ItemRef.java
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

import android.util.Log;

public class ItemRef {
	protected static final String APPTAG = "--> AOTalk::ItemRef";
	
	public String getData(String id, String ql) {
		String xmlpath = "http://itemxml.xyphos.com/?id=" + id + "&ql=" + ql;
		String retval = "";
		
		ItemXMLParser xmldata = new ItemXMLParser();
        xmldata.setXMLPath(xmlpath);
        
        Log.d(APPTAG, "PATH : " + xmlpath.toString());
        
        ArrayList<String[]> xmlstrings = xmldata.getArray();
        
        if(xmlstrings != null) {
	        if(xmlstrings.size() > 0) {
			    for (final String[] currentstring : xmlstrings) {
			    	String flags = "";
			    	if(currentstring[5].contains("NoDrop")) {
			    		flags += "NODROP ";
			    	}
			    	
			    	if(currentstring[5].contains("Unique")) {
			    		flags += "UNIQUE ";
			    	}
			    	
			    	if(!flags.equals("")) {
			    		flags = "<br /><font color=#777777>" + flags + "</font>";
			    	}
			    	
			    	retval += 
			    		"<img src=\"http://www.rubi-ka.com/image/icon/" +  currentstring[3] + ".gif\" class=\"item\">" +
			    		"<b>" + currentstring[0] + "</b>" +
			    		flags +
			    		"<br /><br />" +
			    		"<b>Quality level:</b> " + currentstring[2] + 
			    		"<br /><br />" +
			    		"<b>Description</b><br />" + currentstring[1];
			    	
			    	/*
			    	if(currentstring[6] != null) {
			    		retval += currentstring[6];
			    	}
			    	
			    	if(currentstring[7] != null) {
			    		retval += "<br />";
			    		retval += "<b>Attributes</b><br />";
			    		retval += currentstring[7];
			    	}
			    	*/
			    	
			    	retval += "<br /><br />";
			    	retval += "<font color=#777777>Data from Xyphos.org</font>";
			    }
	        }
        }
        
        if(retval.startsWith("<br />")) {
        	retval = retval.replaceFirst("<br />", "");
        }
        
		return retval;
	}
}
