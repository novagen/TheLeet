package com.rubika.aotalk.database;

import java.util.ArrayList;
import java.util.List;

import com.rubika.aotalk.item.Account;
import com.rubika.aotalk.item.ChatMessage;
import com.rubika.aotalk.service.ServiceTools;
import com.rubika.aotalk.util.ChatParser;
import com.rubika.aotalk.util.WidgetController;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ao.protocol.DimensionAddress;

public class DatabaseHandler extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "aotalk";
	
	private static final String TABLE_MESSAGE_NAME = "message";
    private static final String TABLE_ACCOUNT_NAME = "accounts";
	
	private static final String KEY_ID = "id";
	
	private static final String KEY_MESSAGE_MESSAGE = "what";
	private static final String KEY_MESSAGE_FROM = "fromid";
	private static final String KEY_MESSAGE_CHANNEL = "channelid";
	private static final String KEY_MESSAGE_USER = "toid";
	private static final String KEY_MESSAGE_TIME = "timewhen";
	private static final String KEY_MESSAGE_SERVER = "server";

    private static final String KEY_ACCOUNT_USERNAME = "username";
    private static final String KEY_ACCOUNT_PASSWORD = "password";
    private static final String KEY_ACCOUNT_SERVER = "server";
    private static final String KEY_ACCOUNT_AUTO = "autoconnect";
    private static final String KEY_ACCOUNT_CHARACTER = "character";
    
    private static final String CREATE_MESSAGE_TABLE = "CREATE TABLE "
	    + TABLE_MESSAGE_NAME + "("
		+ KEY_ID + " INTEGER PRIMARY KEY,"
		+ KEY_MESSAGE_MESSAGE + " TEXT,"
		+ KEY_MESSAGE_FROM + " TEXT," 
		+ KEY_MESSAGE_CHANNEL + " TEXT," 
		+ KEY_MESSAGE_USER + " TEXT," 
		+ KEY_MESSAGE_TIME + " REAL," 
		+ KEY_MESSAGE_SERVER + " REAL" 
		+ ")";
	
    private static final String CREATE_ACCOUNT_TABLE = "CREATE TABLE "
	    + TABLE_ACCOUNT_NAME + "("
	    + KEY_ID + " INTEGER PRIMARY KEY,"
	    + KEY_ACCOUNT_USERNAME + " TEXT,"
	    + KEY_ACCOUNT_PASSWORD + " TEXT,"
	    + KEY_ACCOUNT_SERVER + " INTEGER,"
	    + KEY_ACCOUNT_AUTO + " INTEGER,"
	    + KEY_ACCOUNT_CHARACTER + " INTEGER"
		+ ")";
    
    private Context context;

    public DatabaseHandler(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    	this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_MESSAGE_TABLE);
		db.execSQL(CREATE_ACCOUNT_TABLE);
		
		//db.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT_NAME);
		
		//onCreate(db);
	}
	
	public void deleteAllPosts() {
		synchronized(this) {
			SQLiteDatabase db = this.getWritableDatabase();
			
			db.delete(TABLE_MESSAGE_NAME, null, null);
			db.close();
		}
	}
	
	public void deleteAllPostsForUser(int userid) {
		synchronized(this) {
			SQLiteDatabase db = this.getWritableDatabase();
	
			db.delete(TABLE_MESSAGE_NAME, KEY_MESSAGE_USER + " = ?", new String[] { String.valueOf(userid) });
			db.close();
		}
	}
	
	public void addPost(String message, String from, String channel, int user, int server) {
		synchronized(this) {
			SQLiteDatabase db = this.getWritableDatabase();
	
			ContentValues values = new ContentValues();
		    values.put(KEY_MESSAGE_MESSAGE, message.replace("\n", "<br />").replace("'", "''"));
		    values.put(KEY_MESSAGE_FROM, from);
		    values.put(KEY_MESSAGE_CHANNEL, channel);
		    values.put(KEY_MESSAGE_USER, user);
		    values.put(KEY_MESSAGE_TIME, System.currentTimeMillis());
		    values.put(KEY_MESSAGE_SERVER, server);
	
		    db.insert(TABLE_MESSAGE_NAME, null, values);
		    
		    int type = ChatParser.TYPE_GROUP_MESSAGE;
		    
		    if (channel.equals(ServiceTools.CHANNEL_PRIVATE)) {
		    	type = ChatParser.TYPE_PG_MESSAGE;
		    }
		    
		    if (channel.equals(ServiceTools.CHANNEL_SYSTEM)) {
		    	type = ChatParser.TYPE_SYSTEM_MESSAGE;
		    }
		    
		    if (channel.equals(ServiceTools.CHANNEL_PM)) {
		    	type = ChatParser.TYPE_PRIVATE_MESSAGE;
		    }
		    
		    WidgetController.setText(message, type, context);
		    WidgetController.setClearText(message, from, channel, context);
		    
		    db.close();
		}
	}
	
	public List<ChatMessage> getAllPosts() {
		synchronized(this) {
			SQLiteDatabase db = this.getReadableDatabase();
	
			List<ChatMessage> messages = new ArrayList<ChatMessage>();
		    String selectQuery = "SELECT  * FROM " + TABLE_MESSAGE_NAME + " ORDER BY " + KEY_ID + " DESC";
		 
		    Cursor cursor = db.rawQuery(selectQuery, null);
		 
		    if (cursor.moveToFirst()) {
		        do {
		        	messages.add(
		        		new ChatMessage(
		        			cursor.getLong(cursor.getColumnIndex(KEY_MESSAGE_TIME)),
		        			cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_MESSAGE)),
		        			cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_FROM)),
		        			cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_CHANNEL)),
		        			cursor.getInt(cursor.getColumnIndex(KEY_ID)),
		        			cursor.getInt(cursor.getColumnIndex(KEY_MESSAGE_SERVER))
		        		)
		        	);
		        } while (cursor.moveToNext());
		    }
	
		    cursor.close();
		    db.close();
		    
		    return messages;
		}
	}
	
	public List<ChatMessage> getAllPostsForUser(int userid, int server, String channel) {
		synchronized(this) {
			SQLiteDatabase db = this.getReadableDatabase();
	
			List<ChatMessage> messages = new ArrayList<ChatMessage>();
		    Cursor cursor = null;
		    
		    if (channel == ServiceTools.CHANNEL_MAIN) {
			    cursor = db.query(
			    		TABLE_MESSAGE_NAME,
			    		new String[] { KEY_ID, KEY_MESSAGE_MESSAGE, KEY_MESSAGE_FROM, KEY_MESSAGE_CHANNEL, KEY_MESSAGE_USER, KEY_MESSAGE_TIME, KEY_MESSAGE_SERVER },
			    		KEY_MESSAGE_USER + " = ? AND " + KEY_MESSAGE_SERVER + " = ?",
			    		new String[] { String.valueOf(userid), String.valueOf(server) },
			    		null,
			    		null,
			    		KEY_ID + " ASC",
			    		null
			    	);
		    } else {
			    cursor = db.query(
			    		TABLE_MESSAGE_NAME,
			    		new String[] { KEY_ID, KEY_MESSAGE_MESSAGE, KEY_MESSAGE_FROM, KEY_MESSAGE_CHANNEL, KEY_MESSAGE_USER, KEY_MESSAGE_TIME, KEY_MESSAGE_SERVER },
			    		KEY_MESSAGE_USER + " = ? AND " + KEY_MESSAGE_SERVER + " = ? AND " + KEY_MESSAGE_CHANNEL + " = ?",
			    		new String[] { String.valueOf(userid), String.valueOf(server), String.valueOf(channel) },
			    		null,
			    		null,
			    		KEY_ID + " ASC",
			    		null
			    	);
		    }
		    		 
		    if (cursor.moveToFirst()) {
		        do {
		        	messages.add(
		        		new ChatMessage(
		        			cursor.getLong(cursor.getColumnIndex(KEY_MESSAGE_TIME)),
		        			cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_MESSAGE)).replace("''", "'"),
		        			cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_FROM)),
		        			cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_CHANNEL)),
		        			cursor.getInt(cursor.getColumnIndex(KEY_ID)),
		        			cursor.getInt(cursor.getColumnIndex(KEY_MESSAGE_SERVER))
		        		)
		        	);
		        } while (cursor.moveToNext());
		    }
		 
		    cursor.close();
		    db.close();
		    
		    return messages;
		}
	}
	
	public List<ChatMessage> getNewPostsForUser(int userid, long postid, int server, String channel) {
		synchronized(this) {
			SQLiteDatabase db = this.getReadableDatabase();
	
			List<ChatMessage> messages = new ArrayList<ChatMessage>();
		    Cursor cursor = null;
		    
		    if (channel == ServiceTools.CHANNEL_MAIN) {
			    cursor = db.query(
			    		TABLE_MESSAGE_NAME,
			    		new String[] { KEY_ID, KEY_MESSAGE_MESSAGE, KEY_MESSAGE_FROM, KEY_MESSAGE_CHANNEL, KEY_MESSAGE_USER, KEY_MESSAGE_TIME, KEY_MESSAGE_SERVER },
			    		KEY_MESSAGE_USER + " = ? AND " + KEY_ID + " > ? AND " + KEY_MESSAGE_SERVER + " = ? ",
			    		new String[] { String.valueOf(userid), String.valueOf(postid), String.valueOf(server) },
			    		null,
			    		null,
			    		KEY_ID + " ASC",
			    		null
			    	);
		    } else {
			    cursor = db.query(
			    		TABLE_MESSAGE_NAME,
			    		new String[] { KEY_ID, KEY_MESSAGE_MESSAGE, KEY_MESSAGE_FROM, KEY_MESSAGE_CHANNEL, KEY_MESSAGE_USER, KEY_MESSAGE_TIME, KEY_MESSAGE_SERVER },
			    		KEY_MESSAGE_USER + " = ? AND " + KEY_ID + " > ? AND " + KEY_MESSAGE_SERVER + " = ? AND " + KEY_MESSAGE_CHANNEL + " = ?",
			    		new String[] { String.valueOf(userid), String.valueOf(postid), String.valueOf(server), String.valueOf(channel) },
			    		null,
			    		null,
			    		KEY_ID + " ASC",
			    		null
			    	);
		    	
		    }
		    	
		    if (cursor.moveToFirst()) {
		        do {
		        	messages.add(
		        		new ChatMessage(
			        			cursor.getLong(cursor.getColumnIndex(KEY_MESSAGE_TIME)),
			        			cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_MESSAGE)).replace("''", "'"),
			        			cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_FROM)),
			        			cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_CHANNEL)),
			        			cursor.getInt(cursor.getColumnIndex(KEY_ID)),
			        			cursor.getInt(cursor.getColumnIndex(KEY_MESSAGE_SERVER))
		        		)
		        	);
		        } while (cursor.moveToNext());
		    }
		 
		    cursor.close();
		    db.close();
		    
		    return messages;
		}
	}
	
	public void addAccount(Account account) {
		synchronized(this) {
			SQLiteDatabase db = this.getWritableDatabase();
	
			ContentValues values = new ContentValues();
		    
		    values.put(KEY_ACCOUNT_USERNAME, account.getUsername());
		    values.put(KEY_ACCOUNT_PASSWORD, account.getPassword());
		    values.put(KEY_ACCOUNT_SERVER, account.getServer().getID());
		    values.put(KEY_ACCOUNT_AUTO, (account.getAutoconnect() == true)? 1:0);
		 
		    db.insert(TABLE_ACCOUNT_NAME, null, values);
		    db.close();
		}
	}
	
	public void updateAccount(Account account) {
		synchronized(this) {
			SQLiteDatabase db = this.getWritableDatabase();
	
			ContentValues values = new ContentValues();
		    
		    values.put(KEY_ACCOUNT_USERNAME, account.getUsername());
		    values.put(KEY_ACCOUNT_PASSWORD, account.getPassword());
		    values.put(KEY_ACCOUNT_SERVER, account.getServer().getID());
		    values.put(KEY_ACCOUNT_AUTO, (account.getAutoconnect() == true)? 1:0);
		 
		    db.update(TABLE_ACCOUNT_NAME, values, KEY_ID + " = ?", new String[] { String.valueOf(account.getID()) });
		    db.close();
		}
	}
	
	public void deleteAccount(Account account) {
		synchronized(this) {
			SQLiteDatabase db = this.getWritableDatabase();
	
			db.delete(TABLE_ACCOUNT_NAME, KEY_ID + " = ?", new String[] { String.valueOf(account.getID()) });
			db.close();
		}
	}
	
	public List<Account> getAllAccounts() {
		synchronized(this) {
			SQLiteDatabase db = this.getReadableDatabase();
	
			List<Account> accountList = new ArrayList<Account>();
		    String selectQuery = "SELECT  * FROM " + TABLE_ACCOUNT_NAME;
		 
		    Cursor cursor = db.rawQuery(selectQuery, null);
		 
		    if (cursor != null && cursor.getCount() > 0) {
			    if (cursor.moveToFirst()) {
			        do {
			    	    DimensionAddress server;
			    	    
			    	    switch (cursor.getInt(cursor.getColumnIndex(KEY_ACCOUNT_SERVER))) {
			    	    case 1:
			    	    	server = DimensionAddress.RK1;
			    	    	break;
			    	    case 2:
			    	    	server = DimensionAddress.RK2;
			    	    	break;
			    	    case 0:
			    	    	server = DimensionAddress.TEST;
			    	    	break;
			    	    default:
			    	    	server = DimensionAddress.RK1;	    	
			    	    }
		
			        	Account account = new Account(
			            		cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_USERNAME)),
			            		cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_PASSWORD)),
			            		server,
			            		(cursor.getInt(cursor.getColumnIndex(KEY_ACCOUNT_AUTO)) != 0),
			            		cursor.getInt(cursor.getColumnIndex(KEY_ID))
			            	);
		
			        	accountList.add(account);
			        } while (cursor.moveToNext());
			    }
			 
			    cursor.close();
			    db.close();
			    
			    return accountList;
		    } else {
			    cursor.close();
			    db.close();
			    
		    	return null;
		    }
		}
	}
	
	public Account getAccount(int id) {
		synchronized(this) {
			SQLiteDatabase db = this.getWritableDatabase(); 
	
			Cursor cursor = db.query(
		    		TABLE_ACCOUNT_NAME, 
		    		new String[] { KEY_ID, KEY_ACCOUNT_USERNAME, KEY_ACCOUNT_PASSWORD, KEY_ACCOUNT_SERVER, KEY_ACCOUNT_AUTO }, 
		    		KEY_ID + "=?", 
		    		new String[] { String.valueOf(id) },
		    		null,
		    		null,
		    		null,
		    		null
		    	);
		    
		    if (cursor != null && cursor.getCount() > 0) {
		        cursor.moveToFirst();
		 
			    DimensionAddress server;
			    
			    switch (cursor.getInt(cursor.getColumnIndex(KEY_ACCOUNT_SERVER))) {
			    case 1:
			    	server = DimensionAddress.RK1;
			    	break;
			    case 2:
			    	server = DimensionAddress.RK2;
			    	break;
			    case 0:
			    	server = DimensionAddress.TEST;
			    	break;
			    default:
			    	server = DimensionAddress.RK1;	    	
			    }
			    
			    Account account = new Account(
			    		cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_USERNAME)),
			    		cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_PASSWORD)),
			    		server,
			            (cursor.getInt(cursor.getColumnIndex(KEY_ACCOUNT_AUTO)) != 0),
			            cursor.getInt(cursor.getColumnIndex(KEY_ID))
			    	);
		
			    cursor.close();
				db.close();
				
			    return account;
		    } else {
			    cursor.close();
				db.close();
				
		    	return null;
		    }
		}
	}
}
