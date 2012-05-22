package com.rubika.aotalk.aou;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.rubika.aotalk.util.Logging;

public class XyphosData {
	private static final String APP_TAG = "--> AnarchyTalk::XyphosData";

	public static String insertData(String string) {
    	String xml = null;

        Pattern pattern = Pattern.compile("<a href=\"itemref://([0-9]*?)/0/0\">(.*?)</a>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(string);
        
        while(matcher.find()) {
            Document itemdoc = null;

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(String.format("http://itemxml.xyphos.com/?id=%s", matcher.group(1)));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
            } catch (UnsupportedEncodingException ex) {
				Logging.log(APP_TAG, ex.getMessage());
            } catch (ClientProtocolException ex) {
				Logging.log(APP_TAG, ex.getMessage());
            } catch (IOException ex) {
				Logging.log(APP_TAG, ex.getMessage());
            }
            
            if (xml != null) {
	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            try {
	                DocumentBuilder db = dbf.newDocumentBuilder();
	     
	                InputSource is = new InputSource();
	                is.setCharacterStream(new StringReader(xml));
	                itemdoc = db.parse(is); 
	            } catch (ParserConfigurationException ex) {
					Logging.log(APP_TAG, ex.getMessage());
	                return null;
	            } catch (SAXException ex) {
					Logging.log(APP_TAG, ex.getMessage());
	                return null;
	            } catch (IOException ex) {
					Logging.log(APP_TAG, ex.getMessage());
	                return null;
	            }
            }
            
            String icon = "";
            String name = "";
            
            if (itemdoc != null) {
            	NodeList itemnl = itemdoc.getElementsByTagName("attribute");
	            
	            for (int x = 0; x < itemnl.getLength(); x++) {
	                Element item = (Element) itemnl.item(x);
	                
	                if (item != null) {
	                	if (item.getAttribute("stat") != null) {
    		                if (item.getAttribute("stat").equals("79")) {
    		                	icon = item.getAttribute("value");
    		                }
	                	}
	                }
	            }
	            
            	itemnl = itemdoc.getElementsByTagName("item");
	            
	            for (int x = 0; x < itemnl.getLength(); x++) {
	                Element item = (Element) itemnl.item(x);
	                name = getValue(item, "name");
	            }
            }
        	
            string = string.replace(
	        	"<a href=\"itemref://" + matcher.group(1) + "/0/0\">" + matcher.group(2) + "</a>", 
	        	"<a href=\"itemref://" + matcher.group(1) + "/0/0\"><img src=\"http://109.74.0.178/icon/" + icon + ".gif\" height=\"42\" width=\"42\" /> " + name + "</a>" 
        	);
        }
        
        pattern = Pattern.compile("<a href=\"itemref://([0-9]*?)/0/0\" class=\"icononly\">(.*?)</a>", Pattern.DOTALL);
        matcher = pattern.matcher(string);
        
        while(matcher.find()) {
            Document itemdoc = null;

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(String.format("http://itemxml.xyphos.com/?id=%s", matcher.group(1)));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
            } catch (UnsupportedEncodingException ex) {
				Logging.log(APP_TAG, ex.getMessage());
            } catch (ClientProtocolException ex) {
				Logging.log(APP_TAG, ex.getMessage());
            } catch (IOException ex) {
				Logging.log(APP_TAG, ex.getMessage());
            }
            
            if (xml != null) {
	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            try {
	                DocumentBuilder db = dbf.newDocumentBuilder();
	     
	                InputSource is = new InputSource();
	                is.setCharacterStream(new StringReader(xml));
	                itemdoc = db.parse(is); 
	            } catch (ParserConfigurationException ex) {
					Logging.log(APP_TAG, ex.getMessage());
	                return null;
	            } catch (SAXException ex) {
					Logging.log(APP_TAG, ex.getMessage());
	                return null;
	            } catch (IOException ex) {
					Logging.log(APP_TAG, ex.getMessage());
	                return null;
	            }
            }
            
            String icon = "";
            
            if (itemdoc != null) {
            	NodeList itemnl = itemdoc.getElementsByTagName("attribute");
	            
	            for (int x = 0; x < itemnl.getLength(); x++) {
	                Element item = (Element) itemnl.item(x);
	                
	                if (item != null) {
	                	if (item.getAttribute("stat") != null) {
    		                if (item.getAttribute("stat").equals("79")) {
    		                	icon = item.getAttribute("value");
    		                }
	                	}
	                }
	            }
            }
        	
            string = string.replace(
	        	"<a href=\"itemref://" + matcher.group(1) + "/0/0\" class=\"icononly\">" + matcher.group(2) + "</a>", 
	        	"<a href=\"itemref://" + matcher.group(1) + "/0/0\"><img src=\"http://109.74.0.178/icon/" + icon + ".gif\" height=\"42\" width=\"42\" /></a>" 
        	);
        }
        
        pattern = Pattern.compile("<a href=\"itemref://([0-9]*?)/0/0\" class=\"nameonly\">(.*?)</a>", Pattern.DOTALL);
        matcher = pattern.matcher(string);
        
        while(matcher.find()) {
            Document itemdoc = null;

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(String.format("http://itemxml.xyphos.com/?id=%s", matcher.group(1)));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
            } catch (UnsupportedEncodingException ex) {
				Logging.log(APP_TAG, ex.getMessage());
            } catch (ClientProtocolException ex) {
				Logging.log(APP_TAG, ex.getMessage());
            } catch (IOException ex) {
				Logging.log(APP_TAG, ex.getMessage());
            }
            
            if (xml != null) {
	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            try {
	                DocumentBuilder db = dbf.newDocumentBuilder();
	     
	                InputSource is = new InputSource();
	                is.setCharacterStream(new StringReader(xml));
	                itemdoc = db.parse(is); 
	            } catch (ParserConfigurationException ex) {
					Logging.log(APP_TAG, ex.getMessage());
	                return null;
	            } catch (SAXException ex) {
					Logging.log(APP_TAG, ex.getMessage());
	                return null;
	            } catch (IOException ex) {
					Logging.log(APP_TAG, ex.getMessage());
	                return null;
	            }
            }
            
            String name = "";
            
            if (itemdoc != null) {
            	NodeList itemnl = itemdoc.getElementsByTagName("attribute");
	            
            	itemnl = itemdoc.getElementsByTagName("item");
	            
	            for (int x = 0; x < itemnl.getLength(); x++) {
	                Element item = (Element) itemnl.item(x);
	                name = getValue(item, "name");
	            }
            }
        	
            string = string.replace(
	        	"<a href=\"itemref://" + matcher.group(1) + "/0/0\" class=\"nameonly\">" + matcher.group(2) + "</a>", 
	        	"<a href=\"itemref://" + matcher.group(1) + "/0/0\">" + name + "</a>" 
        	);
        }
		
		return string;
	}
	
    private static String getValue(Element item, String str) {
    	NodeList n = item.getElementsByTagName(str);
    	return getElementValue(n.item(0));
    }

    private static final String getElementValue( Node elem ) {
    	Node child;

    	if (elem != null) {
    		if (elem.hasChildNodes()) {
    			for (child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
    				if (child.getNodeType() == Node.TEXT_NODE) {
    					return child.getNodeValue();
    				}
    			}
    		}
    	}

    	return "";
    }
}
