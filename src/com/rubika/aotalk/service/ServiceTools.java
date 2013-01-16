package com.rubika.aotalk.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

import com.rubika.aotalk.AOTalk;
import com.rubika.aotalk.R;
import com.rubika.aotalk.util.ImageCache;
import com.rubika.aotalk.util.ImageTools;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ServiceTools {
	private static final String APP_TAG = "--> The Leet ::ServiceTools";

	public static Bitmap getUserImage(int serverId, String charName, Context context) {
		String dataurl = String.format(Statics.BASE_CHAR_URL, serverId, charName);
		Bitmap currentUserImage = null;
		
		Logging.log(APP_TAG, "Character url: " + dataurl);
		
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 0);
		HttpResponse response = null;

		String imageName = null;
		
		if (charName != null && ClientService.databaseHandler != null) {
			imageName = ClientService.databaseHandler.getCharacterImage(charName, serverId);
		}
		
		if (imageName == null) {
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
				        	imageName = matcher.group(1).replace("http://www.anarchy-online.com/character/photos/", "").trim();
				    		Logging.log(APP_TAG, "Image path: " + imageName);
				        }
					}
				} catch (IllegalStateException e) {
					Logging.log(APP_TAG, e.getMessage());
				} catch (IOException e) {
					Logging.log(APP_TAG, e.getMessage());
				}
			}
		}
		
		File cacheDir = ImageCache.getCacheDirectory(ClientService.getContext().getPackageName(), "photos");
		
		if (imageName != null) {
			ClientService.databaseHandler.addCharacterData(charName, imageName, serverId);
			
			currentUserImage = ImageTools.cropImage(
				ImageTools.resizeImage(
					ImageCache.getImage(ClientService.getContext(), imageName, "http://people.anarchy-online.com/character/photos/", cacheDir, Bitmap.CompressFormat.JPEG),
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
	
	public static String getUserImageName(Context context, String name, int server) {
		String path = null;
		
		path = AOTalk.databaseHandler.getCharacterImage(name, server);
		
		if (path == null || path.equals("0")) {
			List<String> userData = ServiceTools.getUserData(context, name, server);
			
    		if (userData != null && !userData.isEmpty() && userData.get(2) != null) {
	    		path = userData.get(2);
    		}
		}
		
		return path;
	}
	
	public static List<String> getUserData(Context context, String username, int server) {
		String dataurl = String.format(Locale.US, Statics.BASE_CHAR_URL, server, username);
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
        
        if (xml != null && !xml.startsWith("<html>")) {
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
        
        String imageName = null;
        
        if (doc != null) {
        	Pattern pattern = Pattern.compile("<pictureurl>(.*?)</pictureurl>");
	        Matcher matcher = pattern.matcher(xml);
	        
	        while(matcher.find()) {
	        	charInfo += "<img src=\"" + matcher.group(1).replace("www.", "people.") + "\" style=\"float:right; margin:0 0 0 5px;\" />";
	        	imageName = matcher.group(1).replace("http://www.anarchy-online.com/character/photos/", "").trim();
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
		
		if (imageName == null) {
			imageName = "0";
		} else {
			Logging.log(APP_TAG, "found image name: " + imageName);
			ClientService.databaseHandler.addCharacterData(username, imageName, server);
		}
		
		charData.add(imageName);
		
		return charData;
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
