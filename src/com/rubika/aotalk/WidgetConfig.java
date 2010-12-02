package com.rubika.aotalk;

import java.util.ArrayList;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class WidgetConfig extends Activity {
	int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private static final String PREFS_NAME = "AOTalkWidget";
    private static final String PREF_PREFIX_KEY = "widget_";
    private ListView iconlist;
    
    private String[] icons = {"Default", "Atrox", "Clan", "ICC", "Omni"};
	
	public WidgetConfig() {
        super();
    }
	
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.widget_config);

        iconlist = (ListView) findViewById(R.id.icons);
        iconlist.setAdapter(new IconAdapter(WidgetConfig.this, R.layout.icon_row, icons));
        
        iconlist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
	            final Context context = WidgetConfig.this;

	            // When the button is clicked, save the string in our prefs and return that they
	            // clicked OK.
	            String icon = iconlist.getItemAtPosition(position).toString();
	            saveIconPref(context, widgetId, icon);

	            // Push widget update to surface with newly set prefix
	            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	            WidgetSmall.updateAppWidget(context, appWidgetManager, widgetId, icon);

	            // Make sure we pass back the original appWidgetId
	            Intent resultValue = new Intent();
	            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
	            setResult(RESULT_OK, resultValue);
	            finish();
			}
        });

        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
        	widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        //mAppWidgetPrefix.setText(loadTitlePref(ExampleAppWidgetConfigure.this, mAppWidgetId));
    }
	
	static void saveIconPref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.commit();
    }
    
    static String loadIconPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String prefix = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (prefix != null) {
            return prefix;
        } else {
            return "Default";
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
    }

    static void loadAllTitlePrefs(Context context, ArrayList<Integer> appWidgetIds, ArrayList<String> texts) {
    }
        
    public class IconAdapter extends ArrayAdapter<String> {
    	public IconAdapter(Context context, int textViewResourceId, String[] objects) {
    		super(context, textViewResourceId, objects);
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
	    	LayoutInflater inflater = getLayoutInflater();
	    	
	    	View row = inflater.inflate(R.layout.icon_row, parent, false);
	    	
	    	TextView label = (TextView)row.findViewById(R.id.label);
	    	label.setText(icons[position]);
	    	
	    	ImageView icon=(ImageView)row.findViewById(R.id.icon);
	
	    	if (icons[position].equals("Default")) {
	    		icon.setImageResource(R.drawable.widget_button_aotalk);
	    	} else if(icons[position].equals("Atrox")) {
	    		icon.setImageResource(R.drawable.widget_button_atrox);
	    	} else if(icons[position].equals("Clan")) {
	    		icon.setImageResource(R.drawable.widget_button_clan);
	    	} else if(icons[position].equals("ICC")) {
	    		icon.setImageResource(R.drawable.widget_button_icc);
	    	} else if(icons[position].equals("Omni")) {
	    		icon.setImageResource(R.drawable.widget_button_omni);
	    	}
	
	    	return row;
    	}
    }
}


