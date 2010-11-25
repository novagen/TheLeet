/*
 * ItemXMLParser.java
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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.util.Log;

public class ItemXMLParser {
	protected static final String APPTAG = "--> AOTalk::ItemXMLParser";
	
	private ArrayList<String[]> points = new ArrayList<String[]>();
	private String xmlpath = "";
	
	public ArrayList<String[]> getArray() {      
		try {
			URL url = new URL(xmlpath);
			ReadXML(url);
		} catch (MalformedURLException e) {
			Log.d("ItemXMLParser::getArray", "Bad URL");
		}
		
		return points;
	}
	
	public void setXMLPath(String xml) {
		this.xmlpath = xml;
	}
	
	public String getXMLPath() {
		return this.xmlpath;
	}
	
	private void ReadXML(URL url) {
		try {	        
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
	
			XMLReader xr = sp.getXMLReader();
			
			ItemXMLHandler xmlHandler = new ItemXMLHandler();
			xr.setContentHandler(xmlHandler);
			
			InputStream xmldata = url.openStream();
			
	        xr.parse(new InputSource(xmldata));
	        ItemXMLData parsedXMLDataSet = xmlHandler.getParsedData();
			
	        points = parsedXMLDataSet.getPoints();
	    } catch (Exception e) {
	    	Log.d("ItemXMLParser::ReadXML", "Error parsing : " + e.getMessage());
	    	e.printStackTrace();
		}
	}
}
