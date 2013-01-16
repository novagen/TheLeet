/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * (c) 2011 Sebastian Roth <sebastian.roth@gmail.com>
 */

package com.rubika.aotalk.ui.tiledscrollview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.rubika.aotalk.R;
import com.rubika.aotalk.TheLeet;
import com.rubika.aotalk.util.ImageCache;
import com.rubika.aotalk.util.Logging;

public class TiledScrollViewWorker extends TwoDScrollView {
	private static final String APP_TAG = "--> AOTalk::TiledScrollViewWorker";
	
	private static int MapCoordMinX = 31022;
	private static int MapCoordMaxX = 49770;
	private static int MapCoordMinY = 24880;
	private static int MapCoordMaxY = 48996;
	
	private static final int UPDATE_TILES = 123;
	private static final int CLEANUP_OLD_TILES = 124;
	private static final int FILL_TILES_DELAY = 50;
	private static final int ANIMATION_DURATION = 100;
	private static final int CLEANUP_DELAY = 250;
	
	private boolean animationEnabled;

	private OnZoomLevelChangedListener onZoomLevelChangedListener = null;
	private List<Marker> mMarkers = new ArrayList<Marker>();

	private OnClickListener mOnMarkerOnClickListener;
	private List<ImageView> mMarkerViews = new ArrayList<ImageView>();

	public void setMarkerOnClickListener(OnClickListener mOnMarkerOnClickListener) {
		this.mOnMarkerOnClickListener = mOnMarkerOnClickListener;
	}

	public void setOnZoomLevelChangedListener(OnZoomLevelChangedListener listener) {
		this.onZoomLevelChangedListener = listener;
	}

	public void addMarker(String zone, int x, int y, String description) {
        Logging.log(APP_TAG, "Adding marker: " + description + ", " + zone + ", " + x + ", " + y);
		mMarkers.add(new Marker(zone, x, y, description));
	}

	public enum ZoomLevel {
		DEFAULT,
		LEVEL_1,
		LEVEL_2;

		public ZoomLevel upLevel() {
			switch (this) {
			case DEFAULT:
				return LEVEL_1;
			case LEVEL_1:
				return LEVEL_2;
			case LEVEL_2:
				return LEVEL_2;
			}

			return this;
		}

		public ZoomLevel downLevel() {
			switch (this) {
			case DEFAULT:
				return DEFAULT;
			case LEVEL_1:
				return DEFAULT;
			case LEVEL_2:
				return LEVEL_1;
			}

			return this;
		}
	}


	static ZoomLevel mCurrentZoomLevel = ZoomLevel.DEFAULT;
	static Map<ZoomLevel, ConfigurationSet> mConfigurationSets = new HashMap<ZoomLevel, ConfigurationSet>();

	private static ConfigurationSet getCurrentConfigurationSet() {
		if (mConfigurationSets.containsKey(mCurrentZoomLevel)) {
			return mConfigurationSets.get(mCurrentZoomLevel);
		}

		return mConfigurationSets.get(ZoomLevel.DEFAULT);
	}

	public void addConfigurationSet(ZoomLevel level, ConfigurationSet set) {
		mConfigurationSets.put(level, set);
	}

	private FrameLayout mContainer;
	private static final String TAG = TiledScrollViewWorker.class.getSimpleName();
	//    private float mDensity;
	private Handler mHandler = new TileHandler(this);
			
	static class TileHandler extends Handler {
		private final WeakReference<TiledScrollViewWorker> worker;

		public TileHandler(TiledScrollViewWorker w) {
			this.worker = new WeakReference<TiledScrollViewWorker>(w);
		}
		
		@Override
		public void handleMessage(final Message msg) {
			if (worker.get() != null) {
				switch (msg.what) {
					case UPDATE_TILES:
						try {
							worker.get().fillTiles();
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
		            case CLEANUP_OLD_TILES:
						worker.get().cleanupOldTiles();
		                break;
				}
			}
		}
	};

	private Map<Tile, SoftReference<ImageView>> tiles = new ConcurrentHashMap<Tile, SoftReference<ImageView>>();
	private File cacheDir;
	private static Context context;

	public TiledScrollViewWorker(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = c;

		readAttributes(attrs);
		animationEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enableAnimations", true);

		init();
	}

	public TiledScrollViewWorker(Context c, AttributeSet attrs, int defStyle) {
		super(c, attrs, defStyle);
		context = c;

		readAttributes(attrs);
		animationEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enableAnimations", true);

		init();
	}

	private void readAttributes(AttributeSet attrs) {
		final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.com_rubika_aotalk_tiledscrollview_TiledScrollView);

		int imageWidth = a.getInt(R.styleable.com_rubika_aotalk_tiledscrollview_TiledScrollView_image_width, -1);
		int imageHeight = a.getInt(R.styleable.com_rubika_aotalk_tiledscrollview_TiledScrollView_image_height, -1);
		int tileWidth = a.getInt(R.styleable.com_rubika_aotalk_tiledscrollview_TiledScrollView_tile_width, -1);
		int tileHeight = a.getInt(R.styleable.com_rubika_aotalk_tiledscrollview_TiledScrollView_tile_height, -1);
		String filePattern = a.getString(R.styleable.com_rubika_aotalk_tiledscrollview_TiledScrollView_file_pattern);

		if (imageWidth == -1 || imageHeight == -1 || tileWidth == -1 || tileHeight == -1 || filePattern == null) {
			throw new IllegalArgumentException("Please set all attributes correctly!");
		}

		mConfigurationSets.put(ZoomLevel.DEFAULT, new ConfigurationSet(filePattern, tileWidth, tileHeight, imageWidth, imageHeight));
	}

	private void init() {
		cacheDir = ImageCache.getCacheDirectory(TheLeet.getContext().getPackageName(), "maps");
		mContainer = new ZoomingFrameLayout(getContext());

		ConfigurationSet set = getCurrentConfigurationSet();

		final LayoutParams lp = new LayoutParams(set.getImageWidth(), set.getImageHeight());

		mContainer.setMinimumWidth(set.getImageWidth());
		mContainer.setMinimumHeight(set.getImageHeight());
		mContainer.setLayoutParams(lp);

		addView(mContainer, lp);

		mContainer.setBackgroundColor(getResources().getColor(android.R.color.black));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		try {
			fillTiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);

		Message msg = Message.obtain();
		msg.what = UPDATE_TILES;
		
		mHandler.removeCallbacksAndMessages(null);
		mHandler.sendMessageDelayed(msg, FILL_TILES_DELAY);
	}

	private void fillTiles() throws IOException {
		Rect visible = new Rect();
		mContainer.getDrawingRect(visible);

		final int left = visible.left + getScrollX();
		final int top = visible.top + getScrollY();

		final ConfigurationSet set = getCurrentConfigurationSet();

		final int width = (int) (getMeasuredWidth()) + getScrollX() + set.getTileWidth();
		final int height = (int) (getMeasuredHeight()) + getScrollY() + set.getTileHeight();

		new AsyncTask<Void, ImageView, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				for (int y = top; y < height; ) {
					int tileY = Double.valueOf(Math.ceil(y / set.getTileHeight())).intValue();
					
					for (int x = left; x < width; ) {
						int tileX = Double.valueOf(Math.ceil(x / set.getTileWidth())).intValue();
						Tile tile = new Tile(tileX, tileY);

						if (tileX < set.getImageWidth() / set.getTileWidth() && tileY < set.getImageHeight() / set.getTileHeight()) {
							if (!tiles.containsKey(tile) || tiles.get(tile).get() == null) {
								try {
									publishProgress(getNewTile(tile));
								} catch (IOException e) {
									Logging.log(APP_TAG, e.getMessage());
								}
							} else {
							}
						}

						x = x + set.getTileWidth();
					}
					y = y + set.getTileHeight();
				}

				return null;
			}

			@Override
			protected void onProgressUpdate(ImageView... ivs) {
				//Animation animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float)0.5); 
				Animation animation = new AlphaAnimation(0.0f, 1.0f);
				
				if (animationEnabled) {
					animation.setDuration(ANIMATION_DURATION);
					animation.setFillAfter(true);
				}

				for (ImageView iv : ivs) {
					if (iv == null) {
						continue;
					}

					Tile tile = (Tile) iv.getTag();

					iv.setId(new Random().nextInt());
					FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

					lp2.leftMargin = tile.x * set.getTileWidth();
					lp2.topMargin = tile.y * set.getTileHeight();
					lp2.gravity = Gravity.TOP | Gravity.LEFT;
					iv.setLayoutParams(lp2);

					if (animationEnabled) {
						animation.reset();
						iv.setAnimation(animation);
					}
					
					mContainer.addView(iv, lp2);
					
					if (animationEnabled) {
						iv.startAnimation(animation);
					}

					tiles.put(tile, new SoftReference<ImageView>(iv));
				}
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				Logging.log(APP_TAG, "Updated tiles");
				
				if (mMarkerViews.isEmpty()) {
					Bitmap b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_maps_indicator_current_position);

					for (Marker m : mMarkers) {
						if (m.getZone() != null) {
							Logging.log(TAG, "Adding: " + m);
							FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
							lp.leftMargin = m.getX() - (b.getWidth() / 2);
							lp.topMargin = m.getY() - (b.getHeight() / 2);
							lp.gravity = Gravity.TOP | Gravity.LEFT;
	
							ImageView iv = new ImageView(getContext());
							iv.setTag(m);
							iv.setImageResource(R.drawable.ic_maps_indicator_current_position);
							iv.setLayoutParams(lp);
	
							if (mOnMarkerOnClickListener != null) {
								iv.setOnClickListener(mOnMarkerOnClickListener);
							}
	
							mMarkerViews.add(iv);
						}
					}
					
					b.recycle();
				}
				
				for (ImageView iv : mMarkerViews) {
					if (iv.getParent() == null) {
						Logging.log(TAG, "Drawing marker");
						mContainer.addView(iv, iv.getLayoutParams());
					} else {
						mContainer.removeView(iv);
						mContainer.addView(iv, iv.getLayoutParams());
					}
				}
				
				mHandler.removeMessages(CLEANUP_OLD_TILES);
				mHandler.sendMessageDelayed(Message.obtain(mHandler, CLEANUP_OLD_TILES), CLEANUP_DELAY);
			}
		}.execute((Void[]) null);
	}

	private ImageView getNewTile(Tile tile) throws IOException {
		ImageView iv = new ImageView(getContext());

		ConfigurationSet set = getCurrentConfigurationSet();

		String path = set.getFilePattern()
				.replace("%col%", Integer.valueOf(tile.y + 1).toString())
				.replace("%row%", Integer.valueOf(tile.x + 1).toString());
		
		Bitmap bm = ImageCache.getImage(TheLeet.getContext(), path, "http://109.74.0.178/maps/", cacheDir, Bitmap.CompressFormat.JPEG);
		
		if (bm != null && iv != null) {
			iv.setImageBitmap(bm);
			iv.setMinimumWidth(bm.getWidth());
			iv.setMinimumHeight(bm.getHeight());
			iv.setMaxWidth(bm.getWidth());
			iv.setMaxHeight(bm.getHeight());
		} else {
			Logging.log(APP_TAG, "Tiles failed to load");
		}

		iv.setTag(tile);

		return iv;
	}
	
	public void cleanupOldTiles() {
		Logging.log(TAG, "Cleanup old tiles (" + tiles.size() + ")");
		
		Rect actualRect = new Rect(
				getScrollX(), getScrollY(),
				getWidth() + getScrollX(),
				getHeight() + getScrollY()
				);

		for (Tile tile : tiles.keySet()) {
			final ImageView v = tiles.get(tile).get();
			Rect r = new Rect();
			v.getHitRect(r);

			if (!Rect.intersects(actualRect, r)) {
				mContainer.removeView(v);
				tiles.remove(tile);
			}
		}
		
		Logging.log(TAG, "Result (" + tiles.size() + ")");
		System.gc();
	}

	private boolean inZoomMode = false;
	private boolean ignoreLastFinger = false;
	private float mOrigSeparation;
	private static final float ZOOMJUMP = 75f;

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		int action = e.getAction() & MotionEvent.ACTION_MASK;
		
		if (e.getPointerCount() == 2) {
			inZoomMode = true;
		} else {
			inZoomMode = false;
		}
		
		if (inZoomMode) {
			switch (action) {
			case MotionEvent.ACTION_POINTER_DOWN:
				// We may be starting a new pinch so get ready
				mOrigSeparation = calculateSeparation(e);
				break;
			case MotionEvent.ACTION_POINTER_UP:
				// We're ending a pinch so prepare to
				// ignore the last finger while it's the
				// only one still down.
				ignoreLastFinger = true;
				break;
			case MotionEvent.ACTION_MOVE:
				// We're in a pinch so decide if we need to change
				// the zoom level.
				float newSeparation = calculateSeparation(e);
				ZoomLevel next = mCurrentZoomLevel;
				if (newSeparation - mOrigSeparation > ZOOMJUMP) {
					Logging.log(TAG, "Zoom In!");

					next = mCurrentZoomLevel.upLevel();
					mOrigSeparation = newSeparation;
				} else if (mOrigSeparation - newSeparation > ZOOMJUMP) {
					Logging.log(TAG, "Zoom Out!");

					next = mCurrentZoomLevel.downLevel();
					mOrigSeparation = newSeparation;
				}

				changeZoomLevel(next);

				break;
			}
			
			// Don't pass these events to Android because we're
			// taking care of them.
			return true;
		} else {
			// cleanup if necessary from zooming logic
		}
		
		// Throw away events if we're on the last finger
		// until the last finger goes up.
		if (ignoreLastFinger) {
			if (action == MotionEvent.ACTION_UP) {
				ignoreLastFinger = false;
			}
			
			return true;
		}
		
		return super.onTouchEvent(e);
	}
	
	public void setCenter(String zone, int x, int y) {
		int fixX = this.getWidth() / 2;
		int fixY = this.getHeight() / 2;
		
		Integer[] realCoords = getRealPosition(zone, x, y);
		
		scrollTo(realCoords[0] - fixX, realCoords[1] - fixY);
	}

	public void redraw() {
		tiles.clear();

		removeAllViews();
		mMarkerViews.clear();

		init();		
		
		try {
			fillTiles();
		} catch (IOException e1) {
			Log.e(TAG, "Problem loading new tiles.", e1);
		}
	}
	
	private void changeZoomLevel(ZoomLevel next) {
		if (next != mCurrentZoomLevel && mConfigurationSets.containsKey(next)) {
			int direction = next.compareTo(mCurrentZoomLevel);
			
			mCurrentZoomLevel = next;
			Logging.log(TAG, "new zoom level: " + mCurrentZoomLevel);

			tiles.clear();

			double x = getScrollX();
			double y = getScrollY();
			double w = mContainer.getWidth();
			double h = mContainer.getHeight();

			removeAllViews();
			mMarkerViews.clear();

			init();

			double newW = getCurrentConfigurationSet().getImageWidth();
			double newH = getCurrentConfigurationSet().getImageHeight();

			Logging.log(TAG, "1: " + x + ", " + y);
			Logging.log(TAG, "2: " + w + ", " + h);
			Logging.log(TAG, "3: " + newW + ", " + newH);

			Logging.log(TAG, "new sX: " + (int) x / w * newW);
			Logging.log(TAG, "new sY: " + (int) y / h * newH);
			
			Logging.log(TAG, "h: " + com.rubika.aotalk.map.Map.viewHeight() + ", w: " + com.rubika.aotalk.map.Map.viewWidth());

			int fixX = (com.rubika.aotalk.map.Map.viewWidth() / 2) * direction;
			int fixY = (com.rubika.aotalk.map.Map.viewHeight() / 2) * direction;
			
			if (direction < 0) {
				fixX /= 2;
				fixY /= 2;
			}

			jumpTo(
				(int) (x / w * newW) + fixX, 
				(int) (y / h * newH) + fixY
			);

			if (onZoomLevelChangedListener != null) {
				onZoomLevelChangedListener.onZoomLevelChanged(mCurrentZoomLevel);
			}
			
			try {
				fillTiles();
			} catch (IOException e1) {
				Log.e(TAG, "Problem loading new tiles.", e1);
			}
		}
	}

	private float calculateSeparation(MotionEvent e) {
		float x = e.getX(0) - e.getX(1);
		float y = e.getY(0) - e.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	public boolean canZoomFurtherDown() {
		return mCurrentZoomLevel.downLevel() != mCurrentZoomLevel &&
				mConfigurationSets.containsKey(mCurrentZoomLevel.downLevel());
	}

	public void zoomDown() {
		changeZoomLevel(mCurrentZoomLevel.downLevel());
	}

	public boolean canZoomFurtherUp() {
		return mCurrentZoomLevel.upLevel() != mCurrentZoomLevel &&
				mConfigurationSets.containsKey(mCurrentZoomLevel.upLevel());
	}

	public void zoomUp() {
		changeZoomLevel(mCurrentZoomLevel.upLevel());
	}
		
	public static class ZoneInfo {
		public int X = 0;
		public int Y = 0;
		public float XScale = 0;
		public float YScale = 0;
		
		public ZoneInfo(int x, int y, float xscale, float yscale) {
			X = x;
			Y = y;
			XScale = xscale;
			YScale = yscale;
		}
	}
	
	public static ZoneInfo getZoneInfo(int zone) {
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
									Float.parseFloat(xrp.getAttributeValue(null, "zscale"))
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
	
	public static ZoneInfo getZoneInfo(String zone) {
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
						if (xrp.getAttributeValue(null, "name").toLowerCase(Locale.US).equals(zone.toLowerCase())) {
							Logging.log(APP_TAG, xrp.getAttributeValue(null, "name"));
							result = new ZoneInfo(
									Integer.parseInt(xrp.getAttributeValue(null, "x")),
									Integer.parseInt(xrp.getAttributeValue(null, "z")),
									Float.parseFloat(xrp.getAttributeValue(null, "xscale")),
									Float.parseFloat(xrp.getAttributeValue(null, "zscale"))
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
	
	public static Integer[] getRealPosition(String zone, int x, int y)
    {
		Logging.log(APP_TAG, "Zone: " + zone + ", x: " + x + ", y: " + y);
		
		ZoneInfo zoneInfo = getZoneInfo(zone);
		
		if (zoneInfo == null) {
			return new Integer[] { 0, 0 };
		}
		
		Integer[] worldPos = new Integer[] {
                zoneInfo.X + x,
                zoneInfo.Y + y
        };
		
		int diffX = 0;
		int diffY = 0;
		
		if (mCurrentZoomLevel.ordinal() == 0) {
			diffX = 29;
			diffY = 23;
		} else if (mCurrentZoomLevel.ordinal() == 1) {
			diffX = 58;
			diffY = 46;
		} else {
			diffX = 116;
			diffY = 92;
		}
				
		float relativeX = zoneInfo.XScale * (worldPos[0] - MapCoordMinX) / (MapCoordMaxX - MapCoordMinX);
        float relativeY = 1 - (zoneInfo.YScale * (worldPos[1] - MapCoordMinY) / (MapCoordMaxY - MapCoordMinY));
                
        float pixelX = relativeX * (getCurrentConfigurationSet().getImageWidth() - (Math.abs(diffX) * 2));
        float pixelY = relativeY * (getCurrentConfigurationSet().getImageHeight() - (Math.abs(diffY) * 2));
        
        Logging.log(APP_TAG, "Real X: " + pixelX + ", Real Y: " + pixelY);
		
		return new Integer[] {
            Math.round(pixelX) + diffX,
            Math.round(pixelY) + diffY
        };
    }

	public class Marker {
		private String zone;
		private int x;
		private int y;
		private String description;

		public Marker(String zone, int x, int y, String description) {
			this.zone = zone;
			this.x = x;
			this.y = y;
			this.description = description;
		}

		public int getX() {
			return getRealPosition(zone, x, y)[0];
		}

		public int getY() {
			return getRealPosition(zone, x, y)[1];
		}

		public String getDescription() {
			return description;
		}
		
		public String getZone() {
			return zone;
		}

		@Override
		public String toString() {
			return "Marker{ " +
					"zone= " + zone +
					", x= " + x +
					", y= " + y +
					", description='" + description + "'" +
					" }";
		}
	}
}
