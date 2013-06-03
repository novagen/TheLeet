package com.rubika.aotalk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.rubika.aotalk.ui.improvedtextview.ImprovedTextView;
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

@SuppressLint("NewApi")
public class FragmentTools extends SherlockFragment {
	private static final String APP_TAG = "--> The Leet :: FragmentTools";
	private ImprovedTextView title;
	private ImageButton play;
	private LinearLayout load;
	
	private boolean enableVibrations = false;
	private boolean enableVisualizer = true;
	private boolean cycleColor = false;
	
	private static Context context;
	private Tracker tracker;
	
	final Handler handler = new Handler();
	
    private Visualizer visualizer;
    private VisualizerView visualizerView;
    private LinearLayout visualizerHolder;

	public static FragmentTools newInstance() {
		FragmentTools f = new FragmentTools();
        return f;
    }
	
	public FragmentTools() {
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
	@Override
	public void onDestroyView() {
		if (visualizer != null) {
			visualizer.setEnabled(false);
			visualizer.release();
			visualizer = null;
		}
		
		if (visualizerView != null) {
			visualizerView = null;
		}
		
		super.onDestroyView();
	}
	
	@Override
	public void onResume() {
		super.onResume();

		enableVisualizer = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enableMusicVisualizer", true);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
			if (enableVisualizer) {
				AOTalk.getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
		        
				if (visualizer == null) {
					setupVisualizer();
				}
				
		        if (!visualizer.getEnabled() && visualizer != null) {
			        visualizer.setEnabled(true);
				}
		        
				if (AOTalk.isPlaying) {
					visualizerHolder.setVisibility(View.VISIBLE);
				} else {
					visualizerHolder.setVisibility(View.GONE);
				}
			} else {
		        if (visualizer != null) {
					visualizer.setEnabled(false);
					visualizer.release();
					visualizer = null;
		        }
		        
				visualizerHolder.setVisibility(View.GONE);
			}
		}
		
		updatePlayer(AOTalk.isPlaying, AOTalk.currentTrack);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
            return null;
        }
		
		context = AOTalk.getContext();
		
        EasyTracker.getInstance().setContext(context);
        tracker = EasyTracker.getTracker();

        View fragmentTools = inflater.inflate(R.layout.fragment_tools, container, false);
        		
		GridView grid = (GridView) fragmentTools.findViewById(R.id.grid);
		
		if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enableAnimations", true)) {
			grid.setLayoutAnimation(null);
		}
		
		enableVibrations = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enableMusicVibrations", false);
		enableVisualizer = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enableMusicVisualizer", true);
		
		grid.setAdapter(AOTalk.gridAdapter);
		
		title = (ImprovedTextView) fragmentTools.findViewById(R.id.gsptext);
		
		play = (ImageButton) fragmentTools.findViewById(R.id.gspplay);
		load = (LinearLayout) fragmentTools.findViewById(R.id.gspload);
		
        visualizerHolder = (LinearLayout) fragmentTools.findViewById(R.id.visualizer);
        
		updatePlayer(AOTalk.isPlaying, AOTalk.currentTrack);
		
		play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AOTalk.service != null) {
	                play.setVisibility(View.GONE);
	                load.setVisibility(View.VISIBLE);

	                if (AOTalk.isPlaying) {
						Message msg = Message.obtain(null, Statics.MESSAGE_PLAYER_STOP);
		                msg.replyTo = AOTalk.serviceMessenger;
		                try {
		                	AOTalk.service.send(msg);
						} catch (RemoteException e) {
							Logging.log(APP_TAG, e.getMessage());
						}
		                
						enableVibrations = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enableMusicVibrations", false);
						tracker.sendEvent("Player", "Play", "", 0L);
                	} else {
                		Message msg = Message.obtain(null, Statics.MESSAGE_PLAYER_PLAY);
		                msg.replyTo = AOTalk.serviceMessenger;
		                
		                try {
		                	AOTalk.service.send(msg);
						} catch (RemoteException e) {
							Logging.log(APP_TAG, e.getMessage());
						}
		                
						enableVibrations = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enableMusicVibrations", false);
						tracker.sendEvent("Player", "Stop", "", 0L);
                	}
                } else {
                	Logging.log(APP_TAG, "service is NULL");
                }
			}
		});
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
			if (enableVisualizer) {
				AOTalk.getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
		        
				if (visualizer == null) {
					setupVisualizer();
				}
				
		        if (visualizer != null) {
			        if (!visualizer.getEnabled()) {
			        	visualizer.setEnabled(true);
			        }
				}
			} else {
		        if (visualizer != null) {
					visualizer.setEnabled(false);
					visualizer.release();
					visualizer = null;
		        }
			}
		}

        ImageView gspLogo = (ImageView) fragmentTools.findViewById(R.id.gsplogo);
        gspLogo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, Gridstream.class);
				startActivity(intent);
			}
		});
		
		return fragmentTools;
	}
	
    private void setupVisualizer() {
        // Create a VisualizerView (defined below), which will render the simplified audio wave form to a Canvas.
    	if (visualizerHolder != null) {
	    	visualizerView = new VisualizerView(AOTalk.getContext());
	        visualizerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	        visualizerHolder.addView(visualizerView);
	
	        // Create the Visualizer object and attach it to our media player.
	        try {
		        visualizer = new Visualizer(0);
		        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
		        visualizer.setDataCaptureListener(
		    		new Visualizer.OnDataCaptureListener() {
			            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
			                if (visualizerView != null) {
			                	visualizerView.updateVisualizer(bytes);
			                }
			            }
		
			            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
			            }
		    		}, 
		    		Visualizer.getMaxCaptureRate() / 2, 
		    		true, 
		    		false
		    	);
	        } catch (IllegalStateException e) {
	        	Logging.log(APP_TAG, e.getMessage());
	        } catch (RuntimeException e) {
	        	Logging.log(APP_TAG, e.getMessage());
	        }
    	}
    }
	
	public void updatePlayer(boolean isPlaying, String text) {
		AOTalk.isPlaying = isPlaying;
				
		if (context != null && title != null) {
			if (text != null) {
				title.setText(text);
			} else {
				title.setText(context.getText(R.string.gsp2));
			}
		}
		
		if (load != null) {
			load.setVisibility(View.GONE);
		}
		
		if (play != null) {
			play.setVisibility(View.VISIBLE);
			
			if (!isPlaying) {
	            play.setImageResource(R.drawable.icon_play);
			} else {
	            play.setImageResource(R.drawable.icon_stop);
			}
		}
		
		if (visualizerHolder != null) {
			if (!isPlaying) {
				visualizerHolder.setVisibility(View.GONE);
			} else {
				if (enableVisualizer) {
					visualizerHolder.setVisibility(View.VISIBLE);
				} else {
					visualizerHolder.setVisibility(View.GONE);
				}
			}
		}
	}
	
	private class VisualizerView extends View {
	    private byte[] mBytes;
	    private float[] mPoints;
	    private Rect mRect = new Rect();

	    private Paint paint0 = new Paint();
	    private Paint paint1 = new Paint();
	    private Paint paint2 = new Paint();

	    private float amplitude = 0;
	    private Vibrator vibrator;
	    private float colorCounter = 0;
	    
	    public VisualizerView(Context context) {
	        super(context);
	        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	        init();
	    }

	    private void init() {
	        mBytes = null;

	        paint0.setStrokeWidth(0);
	        paint0.setAntiAlias(true);
	        paint0.setColor(Color.argb(255, 51, 181, 229));
	        paint0.setStyle(Paint.Style.FILL);
	        paint0.setStrokeJoin(Paint.Join.ROUND);
	        paint0.setStrokeCap(Paint.Cap.ROUND);
	        
	        paint1 = new Paint();
	        paint1.set(paint0);
	        paint1.setColor(Color.argb(32, 51, 181, 229));
	        paint1.setStrokeWidth(10f);
	        
	        paint2 = new Paint();
	        paint2.set(paint1);
	        paint2.setStrokeWidth(20f);
	    }

	    public void updateVisualizer(byte[] bytes) {
	        mBytes = bytes;
	        invalidate();
	    }

	    @Override
	    protected void onDraw(Canvas canvas) {
	        super.onDraw(canvas);

	        if (mBytes == null) {
	            return;
	        }

	        if (mPoints == null || mPoints.length < mBytes.length * 4) {
	            mPoints = new float[mBytes.length * 4];
	        }

	        mRect.set(0, 0, getWidth(), getHeight());

	        for (int i = 0; i < mBytes.length - 1; i++) {
	            mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
	            mPoints[i * 4 + 1] = mRect.height() / 2 + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
	            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
	            mPoints[i * 4 + 3] = mRect.height() / 2 + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
	        }
	        
	        if (enableVibrations) {
		        float accumulator = 0;
		        
		        for (int i = 0; i < mBytes.length - 1; i++) {
		        	accumulator += Math.abs(mBytes[i]);
		        }
	
	        	float amp = accumulator/(128 * mBytes.length);
		        if(amp > amplitude && accumulator < 130000)
		        {
		        	vibrator.vibrate(20);
		        	amplitude = amp;
		        }
		        else
		        {
		        	amplitude *= 0.99;
		        }
	        }
	        
	        if (cycleColor) {
	        	generateColor();
	        }
	        
	        canvas.drawLines(mPoints, paint2);
	        canvas.drawLines(mPoints, paint1);
	        canvas.drawLines(mPoints, paint0);
	    }
	    
	    private void generateColor() {
			int r = (int)Math.floor(128*(Math.sin(colorCounter) + 3));
			int g = (int)Math.floor(128*(Math.sin(colorCounter + 1) + 1));
			int b = (int)Math.floor(128*(Math.sin(colorCounter + 7) + 1));
			paint0.setColor(Color.argb(255, r, g, b));
			paint1.setColor(Color.argb(32, r, g, b));
			paint2.setColor(Color.argb(32, r, g, b));
			colorCounter += 0.03;
	    }
	}
}
