package com.rubika.aotalk.util;

public class RKNet {
	private static final String RKNET_API_BASE			= "https://rubi-ka.net/API/v%s/%s.asmx/%s";
	private static final String RKNET_API_VERSION		= "1";
	
	public static final String RKNET_ACCOUNT_LOGIN		= "Login";
	public static final String RKNET_ACCOUNT_GETSITES	= "GetSitesForAccount";
	public static final String RKNET_ACCOUNT_SETSITES	= "SetSitesForAccount";
	public static final String RKNET_ACCOUNT_SETKEYS	= "SetKeyForDeviceOnAccount";
	public static final String RKNET_ACCOUNT_DELKEYS	= "RemoveKey";
	
	public static final String RKNET_MAP_CHAR			= "";
	public static final String RKNET_MAP_ALLCHARS		= "GetCharacterLocationsByAccount";
	public static final String RKNET_MAP_FRIENDS		= "";
	
	public static final String RKNET_REGISTER_PATH		= "https://www.rubi-ka.net/TheLeet/Register.aspx";
	public static final String RKNET_MAP_BASE_PATH		= "http://www.rubi-ka.net/LeetContent/maps/";
	public static final String RKNET_HELP_PATH			= "http://www.rubi-ka.net/LeetContent/help.html";
	public static final String RKNET_MARKET_PATH		= "http://www.rubi-ka.net/LeetContent/market.php?mode=json&order=desc&time=%d&server=0%s";
	
	public static String getApiAccountPath(String function) {
		return String.format(RKNET_API_BASE, RKNET_API_VERSION, "Account", function);
	}
	
	public static String getApiMapPath(String function) {
		return String.format(RKNET_API_BASE, RKNET_API_VERSION, "Map", function);
	}
}
