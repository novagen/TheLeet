package com.rubika.aotalk.recipebook;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

public class RecipeParser {
	private static final String APP_TAG = "--> The Leet ::RecipeParser";
	
	public static String parse(String data) {
        Pattern pattern;
        Matcher matcher;
        
		//data = data.replaceAll("\\<.*?>","");
		
		data = data.replace("\\r\\n", "<br />")
				.replace("\\r\\r", "<br />")
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&quot;", "\"")
				.replace("&amp;", "&")
				.replace("( ", "(")
				.replace("------------------------------", "<hr />")
				.replace("<hr /><br />", "<hr />");

        data = data.replace("#C12", "")
				.replace("#C14", "")
				.replace("#C15", "")
				.replace("#C16", "")
				.replace("#C20", "")
				.replace("#16", "");

        pattern = Pattern.compile("<img src=\'?rdb://([0-9]*?)\'?>");
        matcher = pattern.matcher(data);
        
        while(matcher.find()) {
        	Logging.log(APP_TAG, "found image");
        	data = data.replace(
	        	"<img src=rdb://" + matcher.group(1) + ">", 
	        	"<img src=\"" + Statics.ICON_PATH + matcher.group(1) + "\" class=\"icon clear\" />"
        	);
        	
        	data = data.replace(
		        "<img src='rdb://" + matcher.group(1) + "'>", 
		        "<img src=\"" + Statics.ICON_PATH + matcher.group(1) + "\" class=\"icon clear\" />"
	        );
        }
		
        pattern = Pattern.compile("#L \"([^/\"]*?)\" \"/tell recipebook rshow (.*?)\"");
        matcher = pattern.matcher(data);
        
        while(matcher.find()) {
        	data = data.replace(
	        	"#L \"" + matcher.group(1) + "\" \"/tell recipebook rshow " + matcher.group(2) + "\"", 
	        	"<a href=\"aorb://" + matcher.group(2) + "\">" + matcher.group(1) + "</a>"
        	);
        }
        	
        pattern = Pattern.compile("#L \"([^\"]*?)\" \"([0-9]*?)\"");
        matcher = pattern.matcher(data);
        
        while(matcher.find()) {
        	data = data.replace(
	        	"#L \"" + matcher.group(1) + "\" \"" + matcher.group(2) + "\"", 
	        	"<a href=\"itemref://" + matcher.group(2) + "/0/0\">" + matcher.group(1) + "</a>"
        	);
        }
        
        pattern = Pattern.compile("#L \"([^/\"]*?)\" \"(.*?)\"");
        matcher = pattern.matcher(data);
        
        while(matcher.find()) {
        	data = data.replace(
	        	"#L \"" + matcher.group(1) + "\" \"" + matcher.group(2) + "\"", 
	        	matcher.group(1)
        	);
        }
		
		return data;
	}
}
