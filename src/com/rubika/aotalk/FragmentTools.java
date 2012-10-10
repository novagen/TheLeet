package com.rubika.aotalk;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.rubika.aotalk.adapter.GridAdapter;
import com.rubika.aotalk.service.ServiceTools;
import com.rubika.aotalk.util.Logging;

public class FragmentTools extends SherlockFragment {
	private static final String APP_TAG = "--> AnarchyTalk::FragmentTools";
	private AOTalk activity;
	private TextView title;
	private GridAdapter adapter;
	private ImageButton play;
	
	final Handler handler = new Handler();
	
	public static FragmentTools newInstance(AOTalk activity, GridAdapter adapter) {
		FragmentTools f = new FragmentTools(activity, adapter);
        return f;
    }
	
	public FragmentTools() {
	}
	
	public FragmentTools(AOTalk activity, GridAdapter adapter) {
		this.activity = activity;
		this.adapter = adapter;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logging.log(APP_TAG, "onCreateView");
		
		if (activity == null) {
			getActivity().finish();
		}
		
		if (container == null) {
            return null;
        }

		View fragmentTools = inflater.inflate(R.layout.fragment_tools, container, false);
		
		GridView grid = (GridView) fragmentTools.findViewById(R.id.grid);
		
		if (!PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext()).getBoolean("enableAnimations", true)) {
			grid.setLayoutAnimation(null);
		}
		
		grid.setAdapter(adapter);
		
		title = (TextView) fragmentTools.findViewById(R.id.gsptext);
		play = (ImageButton) fragmentTools.findViewById(R.id.gspplay);
		
		
        if (activity != null) {
			if (activity.isPlaying) {
	            play.setImageResource(R.drawable.ic_menu_stop);
	            
				if (activity.currentTrack != null) {
					title.setText(activity.currentTrack);
				} else {
					title.setText(getString(R.string.gsp2));
				}
			} else {
				play.setImageResource(R.drawable.ic_menu_play);
				title.setText(getString(R.string.gsp2));
			}
        } else {
        	Logging.log(APP_TAG, "activity is NULL");
        }
		
		play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                if (activity != null) {
					if (activity.service != null) {
	                	if (activity.isPlaying) {
							Message msg = Message.obtain(null, ServiceTools.MESSAGE_PLAYER_STOP);
			                msg.replyTo = activity.serviceMessenger;
			                try {
			                	activity.service.send(msg);
							} catch (RemoteException e) {
								Logging.log(APP_TAG, e.getMessage());
							}
	
			                play.setImageResource(R.drawable.button_play);
			                activity.isPlaying = false;
							
							title.setText(getString(R.string.gsp2));
	                	} else {
			                Message msg = Message.obtain(null, ServiceTools.MESSAGE_PLAYER_PLAY);
			                msg.replyTo = activity.serviceMessenger;
			                
			                try {
			                	activity.service.send(msg);
							} catch (RemoteException e) {
								Logging.log(APP_TAG, e.getMessage());
							}
							
							play.setImageResource(R.drawable.button_stop);
							activity.isPlaying = true;
	                	}
	                } else {
	                	Logging.log(APP_TAG, "service is NULL");
	                }
                } else {
                	Logging.log(APP_TAG, "activity is NULL");
                }
			}
		});
		
		return fragmentTools;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		Logging.log("FragmentChat", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
	
	public void updateTitle(String text) {
		title.setText(text);
	}
}
