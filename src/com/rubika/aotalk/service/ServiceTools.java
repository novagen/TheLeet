package com.rubika.aotalk.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.rubika.aotalk.R;
import com.rubika.aotalk.util.Logging;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ServiceTools {
	private static final String APP_TAG = "--> AnarchyTalk::ServiceTools";
	
	public static final String BASE_CHAR_URL = "http://people.anarchy-online.com/character/bio/d/%d/name/%s/bio.xml";

	public static final String BOTNAME = "Anarchytalk";
	public static final String WHOIS_MESSAGE = "!aotalk_whois %s";
	public static final String WHOIS_START = "<font color=#DEDE42><font color=#DEDE42><a href=\"text://";
	public static final String WHOIS_END   = "\">Details</a></font></font>";
	
	public static final String HTML_START = 
		"<html><head></head><style type=\"text/css\">" +
		"body { background-color:#466C7A; color:#ffffff; font-size:0.9em; overflow:hidden; text-shadow: 1px 1px 3px rgba(0,0,0,0.8); }" +
		"a { color:#9FBCFF; }" +
		".item { float:right; }" +
		"hr { height:0px; overflow:hidden; border-bottom:1px solid #2b4751; }" + 
		"img { box-shadow: 0px 2px 7px 0px rgba(0, 0, 0, 0.3); border:1px solid #222222; padding:1px; background-color:#FFFFFF; }" +
		".icon { margin:0 5px 0 0; position:relative; top:-2px; vertical-align:middle; }" +
		".item { background-color:#FFFFFF; }" +
		"</style><body>";
	public static final String HTML_END   = "<div style=\"clear:both;\"></div></body></html>";
	
	public static final String PREFIX_PRIVATE_GROUP = "PG: ";
	
	// From client
	public static final int MESSAGE_CONNECT = 0;
	public static final int MESSAGE_CHARACTER = 2;
	public static final int MESSAGE_DISCONNECT = 3;
	public static final int MESSAGE_CLIENT_REGISTER = 4;
	public static final int MESSAGE_CLIENT_UNREGISTER = 5;
	public static final int MESSAGE_STATUS = 20;
	public static final int MESSAGE_SEND = 24;
	public static final int MESSAGE_SET_CHANNEL = 25;
	public static final int MESSAGE_SET_CHARACTER = 26;
	public static final int MESSAGE_SET_SHOW = 28;
	public static final int MESSAGE_FRIEND_ADD = 30;
	public static final int MESSAGE_FRIEND_REMOVE = 29;
	public static final int MESSAGE_MUTED_CHANNELS = 36;
	public static final int MESSAGE_PRIVATE_CHANNEL_JOIN = 38;
	public static final int MESSAGE_PRIVATE_CHANNEL_DENY = 39;
	
	// To client
	public static final int MESSAGE_CLIENT_ERROR = 7;
	public static final int MESSAGE_STARTED = 11;
	public static final int MESSAGE_CONNECTION_ERROR = 6;
	public static final int MESSAGE_DISCONNECTED = 13;
	public static final int MESSAGE_CHARACTERS = 14;
	public static final int MESSAGE_LOGIN_ERROR = 15;
	public static final int MESSAGE_FRIEND = 16;
	public static final int MESSAGE_UPDATE = 18;
	public static final int MESSAGE_IS_CONNECTED = 21;
	public static final int MESSAGE_IS_DISCONNECTED = 19;
	public static final int MESSAGE_REGISTERED = 22;
	public static final int MESSAGE_CHANNEL = 23;
	public static final int MESSAGE_WHOIS = 27;
	public static final int MESSAGE_PRIVATE_CHANNEL = 37;
	public static final int MESSAGE_PRIVATE_CHANNEL_INVITATION = 40;
	
	// Player messages
	public static final int MESSAGE_PLAYER_ERROR = 31;
	public static final int MESSAGE_PLAYER_STARTED = 32;
	public static final int MESSAGE_PLAYER_STOPPED = 33;
	public static final int MESSAGE_PLAYER_PLAY = 34;
	public static final int MESSAGE_PLAYER_STOP = 35;
	public static final int MESSAGE_PLAYER_TRACK = 41;
	
	// Channel types
	public static final String CHANNEL_MAIN = "main";
	public static final String CHANNEL_PM = "pm";
	public static final String CHANNEL_SYSTEM = "sys";
	public static final String CHANNEL_PRIVATE = "priv";
	public static final String CHANNEL_FRIEND = "frnd";
	public static final String CHANNEL_APPLICATION = "app";
	
	public static final List<String> channelsDisabled = Arrays.asList(
			"Tower Battle Outcome", "Tour Announcements", "IRRK News Wire", "Org Msg", "All Towers"
		);

	public static Bitmap getUserImage(String dataurl, Context context) {
		Logging.log("ServiceTools", "Character url: " + dataurl);
		
		Bitmap currentUserImage = null;
		
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 0);
		HttpResponse response = null;

		String imagepath = null;
		
		try {
			HttpGet get = new HttpGet(dataurl);
			response = client.execute(get);
		} catch (Exception e) {
			Logging.log(APP_TAG, e.getMessage());
		}

		if (response != null) {
			InputStream in;

			try {
				in = response.getEntity().getContent();
				String result = convertStreamToString(in);

				if (result != null) {
					Pattern pattern = Pattern.compile("<pictureurl>(.*?)</pictureurl>");
			        Matcher matcher = pattern.matcher(result);
			        
			        while(matcher.find()) {
			        	imagepath = matcher.group(1).replace("www.", "people.").trim();
			    		Logging.log("ServiceTools", "Image path: " + imagepath);
			        }
				}
			} catch (IllegalStateException e) {
				Logging.log(APP_TAG, e.getMessage());
			} catch (IOException e) {
				Logging.log(APP_TAG, e.getMessage());
			}
		}

		if (imagepath != null) {
			currentUserImage = cropImage(
				resizeImage(
					downloadImage(context, imagepath), 
					(int)Math.round(context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height) * 1.5), 
					context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
				), 
				context
			);
		}
		
		if (currentUserImage != null) {
			return currentUserImage;
		} else {
			return ((BitmapDrawable)context.getResources().getDrawable(R.drawable.ic_notification)).getBitmap();
		}
	}
	
	public static boolean intToBoolean(int intValue)
	{
		return (intValue != 0);
	}
	
	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
	
	public static List<String> getUserData(Context context, String username, int server) {
		String dataurl = String.format(BASE_CHAR_URL, server, username);
		String charInfo = "";
		String charName = "";
		
		List<String> charData = new ArrayList<String>();
		
		String xml = null;
        Document doc = null;

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(dataurl);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);
            Logging.log(APP_TAG, xml);
        } catch (UnsupportedEncodingException e) {
			Logging.log(APP_TAG, e.getMessage());
        } catch (ClientProtocolException e) {
			Logging.log(APP_TAG, e.getMessage());
        } catch (IOException e) {
			Logging.log(APP_TAG, e.getMessage());
        }
        
        if (xml != null) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
     
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is); 
            } catch (ParserConfigurationException e) {
				Logging.log(APP_TAG, e.getMessage());
                return null;
            } catch (SAXException e) {
				Logging.log(APP_TAG, e.getMessage());
                return null;
            } catch (IOException e) {
				Logging.log(APP_TAG, e.getMessage());
                return null;
            }
        }
        
        int level = 0;
        int defender_rank_id = 0;
        String breed = "";
        String gender = "";
        String faction = "";
        String profession = "";
        String profession_title = "";
        String defender_rank = "";
        
        String organization_name = "";
        String rank = "";
        
        if (doc != null) {
        	Pattern pattern = Pattern.compile("<pictureurl>(.*?)</pictureurl>");
	        Matcher matcher = pattern.matcher(xml);
	        
	        while(matcher.find()) {
	        	charInfo += "<img src=\"" + matcher.group(1).replace("www.", "people.") + "\" style=\"float:right; margin:0 0 0 5px;\" />";
	        }

	        NodeList nl = doc.getElementsByTagName("name");
	        NodeList nt;
        	
            for (int i = 0; i < nl.getLength(); i++) {
                nt = nl.item(i).getChildNodes();
                
                for (int x = 0; x < nt.getLength(); x++) {
                	Node t = nt.item(x);
                	
	                if (!t.getNodeName().equals("#text")) {
	                	if (t.getNodeName().equals("nick")) {
		                	if (charName.length() > 0) {
		                		charName += " ";
		                	}
		                	charName += "\'";
		                }
		                
		                if (t.getFirstChild() != null) {
		                	if (t.getFirstChild().getNodeValue() != null) {
			                	charName += t.getFirstChild().getNodeValue().trim();
			                }
		                }
		                
	                	if (t.getNodeName().equals("nick")) {
		                	charName += "\' ";
		                }
	                }
                }
            }
            
        	nl = doc.getElementsByTagName("basic_stats");

            for (int i = 0; i < nl.getLength(); i++) {
                nt = nl.item(i).getChildNodes();
                
                for (int x = 0; x < nt.getLength(); x++) {
                	Node t = nt.item(x);

                	if (t.getNodeName().equals("level")) {
            			level = Integer.parseInt(t.getFirstChild().getNodeValue());
            		}
            		
            		if (t.getNodeName().equals("breed")) {
            			breed = t.getFirstChild().getNodeValue();
            		}
            		
            		if (t.getNodeName().equals("gender")) {
            			gender = t.getFirstChild().getNodeValue();
            			if (gender.equals("Nano")) {
            				gender = "Nanomage";
            			}
            		}
            		
            		if (t.getNodeName().equals("faction")) {
            			faction = t.getFirstChild().getNodeValue();
            		}
            		
            		if (t.getNodeName().equals("profession")) {
            			profession = t.getFirstChild().getNodeValue();
            		}
            		
            		if (t.getNodeName().equals("profession_title")) {
            			profession_title = t.getFirstChild().getNodeValue();
            		}
            		
            		if (t.getNodeName().equals("defender_rank")) {
            			defender_rank = t.getFirstChild().getNodeValue();
            		}
            		
            		if (t.getNodeName().equals("defender_rank_id")) {
            			defender_rank_id = Integer.parseInt(t.getFirstChild().getNodeValue());
            		}
                }
            }
            
        	nl = doc.getElementsByTagName("organization_membership");
        	
            for (int i = 0; i < nl.getLength(); i++) {
                nt = nl.item(i).getChildNodes();

                for (int x = 0; x < nt.getLength(); x++) {
                	Node t = nt.item(x);
                		
            		if (t.getNodeName().equals("organization_name")) {
            			organization_name = t.getFirstChild().getNodeValue();
            		}
            		
            		if (t.getNodeName().equals("rank")) {
            			rank = t.getFirstChild().getNodeValue();
            		}
                }
            }
        }
		
        if (charName.equals("")) {
        	charName = context.getString(R.string.no_char_title);
        }
        
		if (charInfo.equals("")) {
			charInfo = context.getString(R.string.no_char_data);
		} else {
			charInfo += String.format(
					context.getString(R.string.character_info),
					faction,
					profession,
					profession_title,
					gender,
					breed,
					level,
					defender_rank,
					defender_rank_id
				);
		}
		
		if (rank.length() > 0 && organization_name.length() > 0) {
			charInfo += String.format(
					context.getString(R.string.character_info_org),
					rank,
					organization_name
				);
		}
		
		charData.add(charName);
		charData.add(charInfo);
		
		return charData;
	}
	
	public static Bitmap cropImage(Bitmap bitmap, Context context) {
		Logging.log(APP_TAG, "cropImage");
		
	    if (bitmap != null) {
	    	if (bitmap.getWidth() > 0 && bitmap.getHeight() > 0 && bitmap.getHeight() > bitmap.getWidth()) {
		    	int startY = 0;
		    	
		    	if (bitmap.getHeight() - bitmap.getWidth()  > 1) {
		    		startY = Math.round((bitmap.getHeight() - bitmap.getWidth()) / 2);
		    	}
		    	
		    	if (startY + bitmap.getWidth() > bitmap.getHeight()) {
		    		startY = 0;
		    	}
		    	
		    	Logging.log(APP_TAG, "startY: " + startY + ", w: " + bitmap.getWidth() + ", h: " + bitmap.getHeight());
	    		
	    		bitmap = Bitmap.createBitmap(
		    			bitmap, 
		    			0, 
		    			startY, 
		    			bitmap.getWidth(),
		    			bitmap.getWidth()
		    		);
	    	}
	    	
	    	return bitmap;
	    } else {
	    	return null;
	    }
	}
	
	
	public static Bitmap resizeImage(Bitmap bitmap, int height, int width) {
		Logging.log(APP_TAG, "resizeImage");

		if (bitmap != null) {
	    	if (bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
				int currentWidth = bitmap.getWidth();
				int currentHeight = bitmap.getHeight();
		
				float scaleWidth = ((float) width) / currentWidth;
				float scaleHeight = ((float) height) / currentHeight;
				
				if (width > currentWidth || height > currentHeight) {
					scaleWidth = 1;
					scaleHeight = 1;
				}
		
				Matrix matrix = new Matrix();
				matrix.postScale(scaleWidth, scaleHeight);
		
				Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, currentWidth, currentHeight, matrix, false);
				return resizedBitmap;
	    	} else {
	    		return null;
	    	}
		} else {
			return null;
		}
	}
	
	
	private static Bitmap downloadImage(Context context, String path) {
		Logging.log(APP_TAG, "downloadImage");

		try {
			URL url = new URL(path);
			InputStream is = (InputStream) url.getContent();
			Drawable d = Drawable.createFromStream(is, "src");
			return ((BitmapDrawable)d).getBitmap();
		} catch (MalformedURLException e) {
			Logging.log(APP_TAG, e.getMessage());
			return null;
		} catch (IOException e) {
			Logging.log(APP_TAG, e.getMessage());
			return null;
		}
	}

	
	public static boolean isOnline(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo info = cm.getActiveNetworkInfo();
	    
	    if (info != null) {
	        return info.isConnected();
	    } else {
	        return false;
	    }
	}
}
