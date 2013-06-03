package com.rubika.aotalk.util;

import java.util.Arrays;
import java.util.List;

import android.graphics.Color;

public class Statics {
	public static final int HANDLER_DELAY = 250;
	
	public static final String GCMSenderID = "145786202037";
	
	public static final int SEARCH_TYPE_CHARACTER 	= 1;
	public static final int SEARCH_TYPE_GUIDE 		= 2;
	public static final int SEARCH_TYPE_RECIPE 		= 3;

	public static final String BOTNAME 				= "Anarchytalk";
	public static final String WHOIS_MESSAGE 		= "!aotalk_whois %s";
	public static final String WHOIS_START 			= "<font color=#DEDE42><font color=#DEDE42><a href=\"text://";
	public static final String WHOIS_END   			= "\">Details</a></font></font>";
	public static final String PREFIX_PRIVATE_GROUP = "PG: ";
	
	private static final String CSS_BODY =
	//		"body { background-image: url(data:image/png;base64," +
	//		"iVBORw0KGgoAAAANSUhEUgAAAJYAAACWCAIAAACzY+a1AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA2lpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wUmlnaHRzPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvcmlnaHRzLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcFJpZ2h0czpNYXJrZWQ9IkZhbHNlIiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjdEOEZENEM0NUZGODExRTI4N0JCRTg1ODMwMjVBMkU4IiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjdEOEZENEMzNUZGODExRTI4N0JCRTg1ODMwMjVBMkU4IiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCBDUzIgV2luZG93cyI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ1dWlkOjE2NDdCMjRBMjI4MUUwMTFBNjNDQjI2N0Y0MTg2MkJBIiBzdFJlZjpkb2N1bWVudElEPSJ1dWlkOjg5NzZBQTg2MjA4MUUwMTFBNjNDQjI2N0Y0MTg2MkJBIi8+IDwvcmRmOkRlc2Ny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" + 
	//		"); " +
			"body, html { background-color: transparent; } " + 
			"body { color:#ffffff; font-size:0.9em; text-shadow: 1px 1px 3px rgba(0,0,0,0.8); } ";
	private static final String CSS_CLEAR = ".clear { background-color:transparent; box-shadow:none; border:0; padding:5px; } ";
	private static final String HTML_HEAD_START = "<!DOCTYPE html><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title></title><style type=\"text/css\">";
	private static final String HTML_HEAD_END   = "</style><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\" /></head><body>";
	
	public static final String HTML_END   = "<div style=\"clear:both;\"></div></body></html>";
	
	public static final String HTML_START = 
			HTML_HEAD_START + CSS_BODY +
			"a { color:#9FBCFF; } " +
			".item { float:right; } " +
			"hr { height:0px; overflow:hidden; border-bottom:1px solid #2b4751; } " + 
			"img { box-shadow: 0px 2px 7px 0px rgba(0, 0, 0, 0.3); border:1px solid #222222; padding:1px; background-color:#FFFFFF; } " +
			".icon { margin:0 5px 0 0; position:relative; top:-2px; vertical-align:middle; } " +
			".item { background-color:#FFFFFF; } " +
			".noshadow { text-shadow: none; } " +
			CSS_CLEAR + HTML_HEAD_END;
	
	public static final String GSP_HTML_START = 
			HTML_HEAD_START + CSS_BODY +
			"a { color:#9FBCFF; text-decoration:none; } " +
			".GSPEventDone { text-decoration:line-through; } " +
			CSS_CLEAR + HTML_HEAD_END;
	
	public static final String GUIDE_HTML_START = 
			HTML_HEAD_START + CSS_BODY +
			"body, html { padding: 0; margin: 0; } " + 
			"a { color:#9FBCFF; } " +
			"img { max-width: 100%; border: none; } " +
			"table { font-size: 0.8em; } " +
			"td { vertical-align: top; } " +
			CSS_CLEAR + HTML_HEAD_END;
	
	// From client
	public static final int MESSAGE_CONNECT 			 = 0;
	public static final int MESSAGE_CHARACTER 			 = 2;
	public static final int MESSAGE_DISCONNECT 			 = 3;
	public static final int MESSAGE_CLIENT_REGISTER 	 = 4;
	public static final int MESSAGE_CLIENT_UNREGISTER 	 = 5;
	public static final int MESSAGE_STATUS 				 = 20;
	public static final int MESSAGE_SEND 				 = 24;
	public static final int MESSAGE_SET_CHANNEL 		 = 25;
	public static final int MESSAGE_SET_CHARACTER 		 = 26;
	public static final int MESSAGE_SET_SHOW 			 = 28;
	public static final int MESSAGE_FRIEND_ADD 			 = 30;
	public static final int MESSAGE_FRIEND_REMOVE 		 = 29;
	public static final int MESSAGE_MUTED_CHANNELS 		 = 36;
	public static final int MESSAGE_PRIVATE_CHANNEL_JOIN = 38;
	public static final int MESSAGE_PRIVATE_CHANNEL_DENY = 39;
	
	// To client
	public static final int MESSAGE_CLIENT_ERROR 		= 7;
	public static final int MESSAGE_STARTED 			= 11;
	public static final int MESSAGE_CONNECTION_ERROR 	= 6;
	public static final int MESSAGE_DISCONNECTED 		= 13;
	public static final int MESSAGE_CHARACTERS 			= 14;
	public static final int MESSAGE_LOGIN_ERROR 		= 15;
	public static final int MESSAGE_FRIEND 				= 16;
	public static final int MESSAGE_UPDATE 				= 18;
	public static final int MESSAGE_IS_CONNECTED 		= 21;
	public static final int MESSAGE_IS_DISCONNECTED 	= 19;
	public static final int MESSAGE_REGISTERED 			= 22;
	public static final int MESSAGE_CHANNEL 			= 23;
	public static final int MESSAGE_WHOIS 				= 27;
	public static final int MESSAGE_PRIVATE_CHANNEL 	= 37;
	public static final int MESSAGE_PRIVATE_INVITATION 	= 40;
	
	// Player messages
	public static final int MESSAGE_PLAYER_ERROR 	= 31;
	public static final int MESSAGE_PLAYER_STARTED 	= 32;
	public static final int MESSAGE_PLAYER_STOPPED 	= 33;
	public static final int MESSAGE_PLAYER_PLAY 	= 34;
	public static final int MESSAGE_PLAYER_STOP 	= 35;
	public static final int MESSAGE_PLAYER_TRACK 	= 41;
	
	// Channel types
	public static final String CHANNEL_MAIN 		= "main";
	public static final String CHANNEL_PM 			= "pm";
	public static final String CHANNEL_SYSTEM 		= "sys";
	public static final String CHANNEL_PRIVATE 		= "priv";
	public static final String CHANNEL_FRIEND 		= "frnd";
	public static final String CHANNEL_APPLICATION 	= "app";
	public static final String CHANNEL_DNET			= "Dnet";
	public static final String CHANNEL_NEUTNET		= "Neutnet";
	
	public static final String TOWER_WARS_SITES 	= "http://towerwars.info/m/TowerDistribution.php?d=5&minlevel=1&maxlevel=300&output=json";
	public static final String TOWER_WARS_ATTACKS 	= "http://towerwars.info/m/HistorySearch.php?d=5&type=attacks&limit=50&sortorder=desc&chopmethod=last&output=json";
	
	public static final List<String> channelsDisabled = Arrays.asList(
			"Tower Battle Outcome", 
			"Tour Announcements", 
			"IRRK News Wire", 
			"Org Msg", 
			"All Towers"
		);
	
	public static int COLOR_ORG_APP = Color.parseColor("#CC99CC");
	public static int COLOR_ORG_SYS = Color.parseColor("#FFCC33");
	public static int COLOR_ORG_PRV = Color.parseColor("#88FF88");
	public static int COLOR_ORG_GRP = Color.parseColor("#FFFFFF");
	public static int COLOR_ORG_FRN = Color.parseColor("#FFEE55");
	public static int COLOR_ORG_OCN = Color.parseColor("#D8D8FF");

	public static final String NEWS_URL    	       = "http://www.ao-universe.com/files/_xml/news_1_3.xml";
	public static final String CALENDAR_URL 	   = "http://www.ao-universe.com/files/_xml/calendar_1_38_bot.xml";
	public static final String GUIDES_FOLDERS_URL  = "http://www.ao-universe.com/mobile/parser.php?mode=list&bot=aotalk&output=html";
	public static final String GUIDES_FOLDER_URL   = "http://www.ao-universe.com/mobile/parser.php?mode=list&id=%s&bot=aotalk&output=html";
	public static final String GUIDES_INFO_URL     = "http://www.ao-universe.com/mobile/parser.php?mode=view&id=%s&bot=aotalk&output=html";
	public static final String GUIDES_SEARCH_URL   = "http://www.ao-universe.com/mobile/parser.php?mode=search&search=%s&bot=aotalk&output=html";
	public static final String GUIDES_LOCATION_URL = "http://www.ao-universe.com/mobile/waypoints.php?mode=list&bot=aotalk";

	public static final String CHAR_PATH  = "http://people.anarchy-online.com/character/bio/d/5/name/%s/bio.xml";
	public static final String ICON_PATH  = "http://static.aodevs.com/icon/";
	public static final String PHOTO_PATH = "http://people.anarchy-online.com/character/photos/";
		
	public static final String CIDB_SEARCH_URL    = "http://cidb.botsharp.net/?bot=aotalk&output=xml&version=1.2&search=%s";
	public static final String XYPHOS_ITEM_URL    = "http://itemxml.xyphos.com/?id=%s";
	public static final String XYPHOS_ITEM_QL_URL = "http://itemxml.xyphos.com/?id=%s&ql=%s";
	public static final String AOSPEAK_API_PATH   = "http://api.aospeak.com/online/%d/";
	public static final String GSP_PLAYLIST_PATH  = "http://community.loudcity.com/stations/gridstream-productions/files/show/gsp.pls";
	
	public static final String RECIPES_CATEGORIES_URL = "http://aodevnet.com/recipes/api/cats/format/xml/bot/aotalk";
	public static final String RECIPES_CATEGORY_URL   = "http://aodevnet.com/recipes/api/catlist/format/xml/id/%s/bot/aotalk";
	public static final String RECIPES_INFO_URL       = "http://aodevnet.com/recipes/api/show/id/%s/format/xml/bot/aotalk";
	public static final String RECIPES_SEARCH_URL     = "http://aodevnet.com/recipes/api/search/kw/%s/format/xml/bot/aotalk";
	public static final String RECIPES_BY_ITEM_URL    = "http://aodevnet.com/recipes/api/byitem/id/%s/format/xml/bot/aotalk";
	
	public static final String BROADCAST_KEY_PLAY = "com.rubika.aotalk.service.ClientService.gspPlay";
	public static final String BROADCAST_KEY_STOP = "com.rubika.aotalk.service.ClientService.gspStop";
}
