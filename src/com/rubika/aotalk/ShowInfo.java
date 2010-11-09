package com.rubika.aotalk;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class ShowInfo extends Activity {
	protected static final String CMD_START = "/start";
	protected static final String CMD_TELL  = "/tell";
	protected static final String SDPATH    = "/aotalk/icons/";

        
	static ImageGetter imageLoader = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
        	try {
        		InputStream is = (InputStream) new URL(source).getContent();
      
        		Drawable drawable = Drawable.createFromStream(is, "src name");
        		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        		return drawable;
        	} catch (Exception e) {
        		Log.d("IMAGELOADER", e.getMessage());
        		return null;
        	}
        }
	};
    	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showinfo);
        
    	final TextView info = (TextView) findViewById(R.id.showinfo);
        
        if(getIntent().getData().toString().startsWith("text://")) {
	        String text = getIntent().getData().toString().replace("\n", "<br />").replaceFirst("text://", "");
	                
	        //Removes all images until cache has been coded, takes too long to load them every time
	        Pattern pattern = Pattern.compile("<img src=\'?rdb://([0-9]*?)\'?>");
	        Matcher matcher = pattern.matcher(text);
	        while(matcher.find()) {
	        	text = text.replace(
	        		"<img src=rdb://" + matcher.group(1) + ">", ""
	        		//"<img src=http://www.rubi-ka.com/image/icon/" + matcher.group(1) + ".gif>"
	        	);
	        	text = text.replace(
		        	"<img src='rdb://" + matcher.group(1) + "'>", ""
		        	//"<img src=http://www.rubi-ka.com/image/icon/" + matcher.group(1) + ".gif>"
		        );
	        }
	        
	        pattern = Pattern.compile("<img src=\'?tdb://(.*?)\'?>");
	        matcher = pattern.matcher(text);
	        while(matcher.find()) {
	        	text = text.replace("<img src=tdb://" + matcher.group(1) + ">","");
	        }	        
	        
	        info.setText(Html.fromHtml(text, imageLoader, null));
	        info.setMovementMethod(LinkMovementMethod.getInstance());
        }
        
        if(getIntent().getData().toString().startsWith("chatcmd://")) {
	        String command = getIntent().getData().toString().replace("chatcmd://", "");
	        String chatcmd = command.substring(0, command.indexOf(" ")).trim();
	        
	        if(chatcmd.equals(CMD_START)) {
	        	String url = command.replace(chatcmd, "").trim();
	        	Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	        	startActivity(i);
	        	this.finish();
	        } else if(chatcmd.equals(CMD_TELL)) {
	        	String target = command.replace(chatcmd, "").trim().substring(0, command.indexOf(" ")).trim();
	        	String message = command.replace(chatcmd, "").trim().replace(target, "").trim();
	        	
		        info.setText("this chatcmd is not implemented yet");
		        info.append("\n'" + chatcmd + " " + target + " " + message + "'");	        	
	        } else {
		        info.setText("this chatcmd is not implemented yet");
		        info.append("\n'" + chatcmd + "'");
	        }
        }
        
        if(getIntent().getData().toString().startsWith("itemref://")) {
	        String itemref = getIntent().getData().toString().replace("itemref://", "").trim();
        	String lowid = itemref.substring(0, itemref.indexOf("/"));
        	
        	String ql = "";
        	
        	itemref = itemref.replace(lowid + "/", "");
        	if(itemref.indexOf("/") >= 0) {
        		String highid = itemref.substring(0, itemref.indexOf("/"));
        		ql = itemref.replace(highid + "/", "");
        	}
        	
        	if(ql.equals("")) {
        		ql = "1";
        	}
       	
        	ItemRef iref = new ItemRef();
        	String text = iref.getData(lowid, ql);
	        
	        info.setText(Html.fromHtml(text, imageLoader, null));
        }        
	}
    
    @Override
    protected void onPause(){
    	super.onPause();
    	this.finish();
	} 
    
    @Override
    protected void onStop(){
    	super.onStop();
    	this.finish();
	}
    
    /*
	public boolean makeNomedia(String path) {
		boolean retval = false;
		
		File imageNomedia = new File(path + "/.nomedia");
    	if(!imageNomedia.exists()) {
    		try {
				Log.d("makeNomedia", "Created .nomedia : " + imageNomedia.toString());
				imageNomedia.createNewFile();
				retval = true;
			} catch (IOException e) {
				Log.d("makeNomedia", "Unable to save .nomedia");
				retval = false;
			}
    	}
    	
    	return retval;
   	}
	
	public static Drawable getIcon(String icon) {
		if (icon.length() > 1) {
			boolean mExternalStorageAvailable = false;
			boolean mExternalStorageWriteable = false;
	
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
			    mExternalStorageAvailable = mExternalStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    mExternalStorageAvailable = true;
			    mExternalStorageWriteable = false;
			} else {
			    mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
	
			Drawable bitmap = null;
			String fileName = icon + ".gif";
			
			if(mExternalStorageAvailable && mExternalStorageWriteable) {
				File image = new File(Environment.getExternalStorageDirectory().toString() + SDPATH + fileName);
				
				if (!image.exists()) {
		    		downloadImage("http://www.rubi-ka.com/image/icon/" + fileName, fileName);
		    	}
			}
	        
			//bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + SDPATH + fileName);
			bitmap = BitmapDrawable.createFromPath(Environment.getExternalStorageDirectory().toString() + SDPATH + fileName);
	        
	        return bitmap;
		} else {
			return null;
		}
    }
	
	public static boolean downloadImage(String URL, String fileName) {        
	    Bitmap bitmap = null;
	       
	    boolean fetched = false;
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
	    
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
	    InputStream in = null;
		
	    try {
	    	in = OpenHttpConnection(URL);
	    	if (in != null) {
	    		bitmap = BitmapFactory.decodeStream(in);
	    		in.close();
		    	fetched = true;
	    	}
	    } catch (IOException e1) {
	    	e1.printStackTrace();
	    }
	        
	    if (fetched && mExternalStorageAvailable && mExternalStorageWriteable) {
	    	File imageDirectory = new File(Environment.getExternalStorageDirectory().toString() + SDPATH);
	    	imageDirectory.mkdirs();
	    	
	    	OutputStream outStream = null;
		    File file = new File(Environment.getExternalStorageDirectory().toString() + SDPATH, fileName);
		    try {
				outStream = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
				outStream.flush();
				outStream.close();	    	   
		    } catch (FileNotFoundException e) {
				e.printStackTrace();
		    } catch (IOException e) {
				e.printStackTrace();
		    }
	    }

	    System.gc();
	    return true;             
	}
	
	private static InputStream OpenHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;
	               
		URL url = new URL(urlString); 
		URLConnection conn = url.openConnection();
	                 
		if(!(conn instanceof HttpURLConnection)) {
			Log.d("OpenHttpConnection", "Not an HTTP connection");
		}
		
		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			response = httpConn.getResponseCode();
			
			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		} catch (Exception ex) {
			Log.d("OpenHttpConnection", "Error connecting");
		}
		
		return in;     
	}
	*/
}
