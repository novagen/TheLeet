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
