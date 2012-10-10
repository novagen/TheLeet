package com.rubika.aotalk.map;

import android.os.Bundle;
import asia.ivity.android.tiledscrollview.ConfigurationSet;
import asia.ivity.android.tiledscrollview.TiledScrollView;
import asia.ivity.android.tiledscrollview.TiledScrollViewWorker;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.rubika.aotalk.R;

public class Map extends SherlockFragmentActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        final ActionBar bar = getSupportActionBar();
        
		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setDisplayHomeAsUpEnabled(true);
        
        TiledScrollView view = (TiledScrollView) findViewById(R.id.map);

        view.addConfigurationSet(TiledScrollViewWorker.ZoomLevel.LEVEL_1, new ConfigurationSet(
        		"aork/2/map_%col%_%row%.jpg", 185, 235, 3700, 4700));

        view.addConfigurationSet(TiledScrollViewWorker.ZoomLevel.LEVEL_2, new ConfigurationSet(
                "aork/3/map_%col%_%row%.jpg", 185, 235, 7400, 9400));
	}
	
	@Override
	protected void onPause() {
		System.gc();
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getSupportMenuInflater().inflate(R.menu.menu_aou, menu);
    	return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
