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
			    		//"<img src=http://www.rubi-ka.com/image/icon/" +  currentstring[3] + ".gif>&nbsp;" +
			    		"<b>" + currentstring[0] + "</b>" +
			    		flags +
			    		"<br /><br />" +
			    		"<b>Quality level:</b> " + currentstring[2] + 
			    		"<br /><br />" +
			    		"<b>Description</b><br />" + currentstring[1] +
			    		"<br /><br /><font color=#777777>Data from Xyphos.org</font>";
			    }
	        }
        }
        
		return retval;
	}
}
