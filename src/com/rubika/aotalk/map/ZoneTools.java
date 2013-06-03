package com.rubika.aotalk.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

import com.rubika.aotalk.util.Logging;

public class ZoneTools {
	private static final String APP_TAG = "--> The Leet :: ZoneTools";

	private static int MapCoordMinX = 31022;
	private static int MapCoordMaxX = 49770;
	private static int MapCoordMinY = 24880;
	private static int MapCoordMaxY = 48996;

	public static class ZoneInfo {
		public int X = 0;
		public int Y = 0;
		public float XScale = 0;
		public float YScale = 0;
		public boolean IsRubiKa;
		
		public ZoneInfo(int x, int y, float xscale, float yscale, boolean isrubika) {
			X = x;
			Y = y;
			XScale = xscale;
			YScale = yscale;
			IsRubiKa = isrubika;
		}
	}
	
	public static ZoneInfo getZoneInfo(Context context, int zone) {
		ZoneInfo result = null;
		
		InputStream istr;
		try {
			istr = context.getAssets().open("MapCoordinates.xml");
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); 
			factory.setNamespaceAware(true); 
			
			XmlPullParser xrp = factory.newPullParser(); 
			xrp.setInput(istr, "UTF-8");  
			
			while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (xrp.getEventType() == XmlPullParser.START_TAG) {
					if (xrp.getName().equals("Playfield")) {
						if (xrp.getAttributeValue(null, "id").equals(String.valueOf(zone))) {
							Logging.log(APP_TAG, xrp.getAttributeValue(null, "name"));
							result = new ZoneInfo(
									Integer.parseInt(xrp.getAttributeValue(null, "x")),
									Integer.parseInt(xrp.getAttributeValue(null, "z")),
									Float.parseFloat(xrp.getAttributeValue(null, "xscale")),
									Float.parseFloat(xrp.getAttributeValue(null, "zscale")),
									(Integer.parseInt(xrp.getAttributeValue(null, "id")) < 4000)
								);
						}
					}
				}
				
				xrp.next();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static ZoneInfo getZoneInfo(Context context, String zone) {
		ZoneInfo result = null;
		
		InputStream istr;
		try {
			istr = context.getAssets().open("MapCoordinates.xml");
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); 
			factory.setNamespaceAware(true); 
			
			XmlPullParser xrp = factory.newPullParser(); 
			xrp.setInput(istr, "UTF-8");  
			
			while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (xrp.getEventType() == XmlPullParser.START_TAG) {
					if (xrp.getName().equals("Playfield")) {
						if (xrp.getAttributeValue(null, "name").toLowerCase(Locale.getDefault()).equals(zone.toLowerCase(Locale.getDefault()))) {
							Logging.log(APP_TAG, xrp.getAttributeValue(null, "name"));
							result = new ZoneInfo(
									Integer.parseInt(xrp.getAttributeValue(null, "x")),
									Integer.parseInt(xrp.getAttributeValue(null, "z")),
									Float.parseFloat(xrp.getAttributeValue(null, "xscale")),
									Float.parseFloat(xrp.getAttributeValue(null, "zscale")),
									(Integer.parseInt(xrp.getAttributeValue(null, "id")) < 4000)
								);
						}
					}
				}
				
				xrp.next();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static Integer[] getRealPosition(Context context, int zone, int x, int y)
    {
		Logging.log(APP_TAG, "Zone: " + zone + ", x: " + x + ", y: " + y);
		return getRealPosition(context, getZoneInfo(context, zone), x, y);
    }
	
	public static Integer[] getRealPosition(Context context, String zone, int x, int y)
    {
		Logging.log(APP_TAG, "Zone: " + zone + ", x: " + x + ", y: " + y);
		return getRealPosition(context, getZoneInfo(context, zone), x, y);
    }
	
	public static Integer[] getRealPosition(Context context, ZoneInfo zoneInfo, int x, int y) {
		int mapSizeX = 7168;
		int mapSizeY = 9216;
		int diffX = 0;
		int diffY = 0;

		if (zoneInfo != null) {
			if (!zoneInfo.IsRubiKa) {
				mapSizeX = 6144;
				mapSizeY = 24576;
				diffX = 29;
				diffY = 23;
			}
			
			Integer[] worldPos = new Integer[] {
		        zoneInfo.X + x,
		        zoneInfo.Y + y
	        };
			
			float relativeX = zoneInfo.XScale * (worldPos[0] - MapCoordMinX) / (MapCoordMaxX - MapCoordMinX);
	        float relativeY = 1 - (zoneInfo.YScale * (worldPos[1] - MapCoordMinY) / (MapCoordMaxY - MapCoordMinY));
	                
	        float pixelX = relativeX * (mapSizeX - (Math.abs(diffX) * 2));
	        float pixelY = relativeY * (mapSizeY - (Math.abs(diffY) * 2));
	        
	        Logging.log(APP_TAG, "Real X: " + pixelX + ", Real Y: " + pixelY);
			
			return new Integer[] {
	            Math.round(pixelX) + diffX,
	            Math.round(pixelY) + diffY,
	            zoneInfo.IsRubiKa?1:0
	        };
		} else {
			return new Integer[] { 0, 0, 0 };
		}
	}
}
