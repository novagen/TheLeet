package com.rubika.aotalk.map;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.Preferences;
import com.rubika.aotalk.R;
import com.rubika.aotalk.adapter.MapTypeAdapter;
import com.rubika.aotalk.item.MapType;
import com.rubika.aotalk.ui.tiledscrollview.ConfigurationSet;
import com.rubika.aotalk.ui.tiledscrollview.TiledScrollView;
import com.rubika.aotalk.ui.tiledscrollview.TiledScrollViewWorker;
import com.rubika.aotalk.util.Logging;

public class Map extends SherlockFragmentActivity {
	private static final String APP_TAG = "--> AOTalk::Map";
	public static int viewHeight = 0;
	public static int viewWidth = 0;
	
	private boolean center = false;
	private int centerOnX = 0;
	private int centerOnY = 0;
	private String centerZone = "";

	public static int viewHeight() {
		return viewHeight;
	}
	
	public static int viewWidth() {
		return viewWidth;
	}
	
	private TiledScrollView view;
	private Context context;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout main = (LinearLayout) this.getLayoutInflater().inflate(R.layout.activity_map, null);
        context = this;
        
        setContentView(main);
        view = (TiledScrollView) findViewById(R.id.map);

        List<MapType> list = new ArrayList<MapType>();
    	list.add(new MapType("Atlas of Rubi-Ka", 0));
    	//list.add(new MapType("Atlas of Shadowlands", 0));
    	
    	MapTypeAdapter mapAdapter = new MapTypeAdapter(this, R.id.messagelist, list); 
       
        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.abbg));
		bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(mapAdapter, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition , long itemId) {
				if (itemPosition == 0) {
					view.addConfigurationSet(TiledScrollViewWorker.ZoomLevel.DEFAULT, new ConfigurationSet(
			        		"aork/1/map_%col%_%row%.jpg", 185, 235, 1850, 2350));
	
			        view.addConfigurationSet(TiledScrollViewWorker.ZoomLevel.LEVEL_1, new ConfigurationSet(
			        		"aork/2/map_%col%_%row%.jpg", 185, 235, 3700, 4700));

			        view.addConfigurationSet(TiledScrollViewWorker.ZoomLevel.LEVEL_2, new ConfigurationSet(
			                "aork/3/map_%col%_%row%.jpg", 185, 235, 7400, 9400));
					
				} else if (itemPosition == 1) {
					view.addConfigurationSet(TiledScrollViewWorker.ZoomLevel.DEFAULT, new ConfigurationSet(
			        		"aosl/1/map_%col%_%row%.jpg", 185, 235, 1850, 2350));
	
			        view.addConfigurationSet(TiledScrollViewWorker.ZoomLevel.LEVEL_1, new ConfigurationSet(
			        		"aosl/2/map_%col%_%row%.jpg", 185, 235, 3700, 4700));
	
			        view.addConfigurationSet(TiledScrollViewWorker.ZoomLevel.LEVEL_2, new ConfigurationSet(
			                "aosl/3/map_%col%_%row%.jpg", 185, 235, 7400, 9400));
				}
				
				view.invalidate();
				view.redraw();
				
		        return true;
			}
        });
        
        view.addConfigurationSet(TiledScrollViewWorker.ZoomLevel.LEVEL_1, new ConfigurationSet(
        		"aork/2/map_%col%_%row%.jpg", 185, 235, 3700, 4700));

        view.addConfigurationSet(TiledScrollViewWorker.ZoomLevel.LEVEL_2, new ConfigurationSet(
                "aork/3/map_%col%_%row%.jpg", 185, 235, 7400, 9400));
        
        Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        if (extras != null) {
        	Logging.log(APP_TAG, "Got coordinates from extras");
        	
        	view.addMarker(extras.getString("zone"), extras.getInt("x"), extras.getInt("y"), extras.getString("name"));
        	
        	center = true;
        	centerOnX = extras.getInt("x");
        	centerOnY = extras.getInt("y");
        	centerZone = extras.getString("zone");

        	view.setMarkerOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Logging.toast(context, extras.getString("name"));
				}
			});
        }

        if (getIntent().getData() != null) {
	        if(getIntent().getData().toString().startsWith("aomap")) {
	        	Logging.log(APP_TAG, "Got coordinates from intent");
	        	
	        	final String values[] = Uri.decode(getIntent().getData().toString()).replace("aomap://", "").trim().split("/");
	        	view.addMarker(values[1], Integer.parseInt(values[2]), Integer.parseInt(values[3]), values[0]);
	        	
	        	center = true;
	        	centerOnX = Integer.parseInt(values[2]);
	        	centerOnY = Integer.parseInt(values[3]);
	        	centerZone = values[1];

	        	view.setMarkerOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Logging.toast(context, values[0]);
					}
				});
	        }
        }
        
        main.getViewTreeObserver().addOnGlobalLayoutListener(
        	new ViewTreeObserver.OnGlobalLayoutListener() {
        		public void onGlobalLayout() {
        			DisplayLayoutDimensions();
        			
        			if (center) {
        				center = false;
        				view.setCenter(centerZone, centerOnX, centerOnY);
        			}
        		}
        	}
        );
	}
	
	public void DisplayLayoutDimensions() {
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        
        viewHeight = container.getHeight();
        viewWidth = container.getWidth();
	}
	
	@Override
	protected void onPause() {
		System.gc();
		super.onPause();
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
			case R.id.preferences:
				Intent intent = new Intent(this, Preferences.class);
				startActivity(intent);
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
