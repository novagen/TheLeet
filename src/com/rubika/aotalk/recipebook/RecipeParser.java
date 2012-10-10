package com.rubika.aotalk.recipebook;

public class RecipeParser {
	public static String parse(String data) {
		data = data.replace("#C12", "")
				.replace("#C14", "")
				.replace("#C15", "")
				.replace("#C16", "")
				.replace("#C20", "")
				.replace("#16", "");
		
		return data;
	}
	
	public static String preProcess(String data) {
		data = data.replace("\\r\\n", "<br />")
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&quot;", "\"")
				.replace("&amp;", "&")
				.replace("( ", "(");
		
		return data;
	}
}
