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

public class ItemXMLHandler extends DefaultHandler {
	protected static final String APPTAG = "--> AOTalk::ItemXMLHandler";
	
	private boolean in_outertag    = false;
	private boolean in_innertag    = false;
    private boolean in_name        = false;
    private boolean in_description = false;
    private boolean in_req		   = false;
    
    private String last_op;
      
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
        } else if (localName.equals("action")) {
        	this.in_req = true;
        	if(myParsedDataSet.getReqs() != null) {
        		myParsedDataSet.setReqs(myParsedDataSet.getReqs() + "<br /><br /><b>" + atts.getValue("name") + "</b><br />");
        	} else {
        		myParsedDataSet.setReqs("<br /><br /><b>" + atts.getValue("name") + "</b><br />");
        	}
        } else if (localName.equals("name")) {
            this.in_name = true;
        } else if (localName.equals("description")) {
        	this.in_description = true;
        } else if (localName.equals("stat")) {
        	myParsedDataSet.setReqs(myParsedDataSet.getReqs() + atts.getValue("name") + " : ");
        } else if (localName.equals("op")) {
        	last_op = atts.getValue("name");
        } else if (localName.equals("value")) {
        	int num = new Integer(atts.getValue("num")).intValue();
        	if(last_op.equals("GreaterThan")) {
        		num++;
        	}
        	myParsedDataSet.setReqs(myParsedDataSet.getReqs() + num + "<br />");
        } else if (localName.equals("attribute")) {
        	if(atts.getValue("name").equals("Level")) {
        		myParsedDataSet.setQuality(atts.getValue("value"));
        	}
        	
        	if(atts.getValue("name").equals("Icon")) {
        		myParsedDataSet.setIcon(atts.getValue("value"));
        	}
        	
        	if(atts.getValue("name").equals("EquipmentPage")) {
        		myParsedDataSet.setType(atts.getValue("extra"));
        	}
        	
        	if(atts.getValue("name").equals("Flags")) {
        		myParsedDataSet.setFlags(atts.getValue("extra"));
        	} 
        	
        	if(atts.getValue("stat").length() > 2) {
        		if(!(
        			atts.getValue("stat").equals("209") || 
        			atts.getValue("stat").equals("353") || 
        			atts.getValue("stat").equals("298")
        		)) {
	        		String stat;
	        		
	        		stat = atts.getValue("name") + " : ";
	        		
	    			if(!atts.getValue("extra").equals("")) {
	    				stat += atts.getValue("extra");
	    			} else {
	    				stat += atts.getValue("value");
	    			}
	    			
	        		stat += "<br />";
	        		
	        		if(myParsedDataSet.getAttr() != null) {
	        			myParsedDataSet.setAttr(myParsedDataSet.getAttr() + stat);
	        		} else {
	        			myParsedDataSet.setAttr(stat);
	        		}
        		}
        	}
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (localName.equals("item")) {
            this.in_outertag = false;
            myParsedDataSet.createPoint();
            myParsedDataSet.setDescription("");
        } else if(localName.equals("action")) {
        	this.in_req = false;
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
    	if (this.in_req) {
    		//...
    	}
    	
    	if (this.in_name) {
        	String tempname = new String();
        	tempname = "";
        	
        	if (myParsedDataSet.getName() != null) {
        		tempname = myParsedDataSet.getName();
        	}
        	
        	myParsedDataSet.setName(tempname + new String(ch, start, length));
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