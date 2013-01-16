package com.rubika.aotalk.util;

public class StringUtils {
	public static String upperCaseFirst(String s) {
		if (s != null && s.length() > 0) {
			final StringBuilder result = new StringBuilder(s.length());
			String[] words = s.split("\\s");
			for(int i=0,l=words.length;i<l;++i) {
			  if(i>0) result.append(" ");      
			  result.append(Character.toUpperCase(words[i].charAt(0)))
			        .append(words[i].substring(1));
			}
			
			return result.toString();
		} else {
			return "";
		}
	}
}
