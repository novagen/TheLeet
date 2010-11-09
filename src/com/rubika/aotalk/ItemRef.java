package com.rubika.aotalk;

import java.util.ArrayList;

public class ItemRef {
	public String getData(String id, String ql) {
		String xmlpath = "http://itemxml.xyphos.com/?id=" + id + "&ql=" + ql;
		String retval = "";
		
		ItemXMLParser xmldata = new ItemXMLParser();
        xmldata.setXMLPath(xmlpath);
        
        ArrayList<String[]> xmlstrings = xmldata.getArray();
        
        if(xmlstrings != null) {
	        if(xmlstrings.size() > 0) {
			    for (final String[] currentstring : xmlstrings) {
			    	retval = 
			    		"<img src=http://www.rubi-ka.com/image/icon/" +  currentstring[3] + ".gif>" +
			    		"<b>Name</b><br />" + currentstring[0] + 
			    		"<br /><br />" +
			    		"<b>Quality</b><br />" + currentstring[2] + 
			    		"<br /><br />" +
			    		"<b>Description</b><br />" + currentstring[1] +
			    		"<br /><br /><font color=#777777>Data from Xyphos.org</font>";
			    }
	        }
        }
        
		return retval;
	}
}
