/*
 * ItemXMLHandler.java
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
 
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class ItemXMLHandler extends DefaultHandler {
	protected static final String APPTAG = "--> AOTalk::ItemXMLHandler";
	
	private boolean in_outertag    = false;
	private boolean in_innertag    = false;
    private boolean in_name        = false;
    private boolean in_description = false;
    
    private ItemXMLData myParsedDataSet = new ItemXMLData();

	public ItemXMLData getParsedData() {
	    return this.myParsedDataSet;
	}

    @Override
    public void startDocument() throws SAXException {
    	this.myParsedDataSet = new ItemXMLData();
    }

    @Override
    public void endDocument() throws SAXException {}

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {   	
    	if (localName.equals("item")) {
            this.in_outertag = true;
        } else if (localName.equals("attributes")) {
            this.in_innertag = true;
        } else if (localName.equals("name")) {
            this.in_name = true;
        } else if (localName.equals("description")) {
        	this.in_description = true;
        } else if (localName.equals("attribute")) {
    		Log.d("XMLPARSER", "Found an attribute");
    		
        	if(atts.getValue("name").equals("Level")) {
        		myParsedDataSet.setQuality(atts.getValue("value"));
        		Log.d("XMLPARSER", "Found an ql-attribute : " + atts.getValue("value"));
        	}
        	
        	if(atts.getValue("name").equals("Icon")) {
        		myParsedDataSet.setIcon(atts.getValue("value"));
        		Log.d("XMLPARSER", "Found an icon-attribute : " + atts.getValue("value"));
        	}
        	
        	if(atts.getValue("name").equals("EquipmentPage")) {
        		myParsedDataSet.setType(atts.getValue("extra"));
        		Log.d("XMLPARSER", "Found an type-attribute : " + atts.getValue("extra"));
        	}
        	
        	if(atts.getValue("name").equals("Flags")) {
        		myParsedDataSet.setFlags(atts.getValue("extra"));
        		Log.d("XMLPARSER", "Found an flags-attribute : " + atts.getValue("extra"));
        	}
        }
   	}

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (localName.equals("item")) {
            this.in_outertag = false;
            myParsedDataSet.createPoint();
            myParsedDataSet.setDescription("");
        } else if (localName.equals("attributes")) {
        	this.in_innertag = false;
        } else if (localName.equals("name")) {
            this.in_name = false;
        } else if (localName.equals("description")) {
            this.in_description = false;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
    	if (this.in_name) {
        	String tempname = new String();
        	tempname = "";
        	
        	if (myParsedDataSet.getName() != null) {
        		tempname = myParsedDataSet.getName();
        	}
        	
        	myParsedDataSet.setName(tempname + new String(ch, start, length));
	    	Log.d("XMLPARSER", "ITEM : " + myParsedDataSet.getName());

        }
    	
        if (this.in_description) {
        	String tempdesc = new String();
        	tempdesc = "";
        	
        	if (myParsedDataSet.getDescription() != null) {
        		tempdesc = myParsedDataSet.getDescription();
        	}
        	
        	myParsedDataSet.setDescription(tempdesc + new String(ch, start, length));
        }
        
        if (this.in_innertag) { }
        if (this.in_outertag) { }      
    }
}