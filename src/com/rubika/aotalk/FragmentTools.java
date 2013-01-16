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
import com.rubika.aotalk.util.Logging;
import com.rubika.aotalk.util.Statics;

public class FragmentTools extends SherlockFragment {
	private static final String APP_TAG = "--> The Leet ::FragmentTools";
	private TextView title;
	private ImageButton play;
	
	final Handler handler = new Handler();
	
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logging.log(APP_TAG, "onCreateView");
		
		if (container == null) {
            return null;
        }

		View fragmentTools = inflater.inflate(R.layout.fragment_tools, container, false);
		
		GridView grid = (GridView) fragmentTools.findViewById(R.id.grid);
		
		if (!PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext()).getBoolean("enableAnimations", true)) {
			grid.setLayoutAnimation(null);
		}
		
		grid.setAdapter(AOTalk.gridAdapter);
		
		title = (TextView) fragmentTools.findViewById(R.id.gsptext);
		play = (ImageButton) fragmentTools.findViewById(R.id.gspplay);
		
		if (AOTalk.isPlaying) {
            play.setImageResource(R.drawable.ic_menu_stop);
            
			if (AOTalk.currentTrack != null) {
				title.setText(AOTalk.currentTrack);
			} else {
				title.setText(getString(R.string.gsp2));
			}
		} else {
			play.setImageResource(R.drawable.ic_menu_play);
			title.setText(getString(R.string.gsp2));
		}
		
		play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AOTalk.service != null) {
                	if (AOTalk.isPlaying) {
						Message msg = Message.obtain(null, Statics.MESSAGE_PLAYER_STOP);
		                msg.replyTo = AOTalk.serviceMessenger;
		                try {
		                	AOTalk.service.send(msg);
						} catch (RemoteException e) {
							Logging.log(APP_TAG, e.getMessage());
						}

		                play.setImageResource(R.drawable.button_play);
		                AOTalk.isPlaying = false;
						
						title.setText(getString(R.string.gsp2));
                	} else {
		                Message msg = Message.obtain(null, Statics.MESSAGE_PLAYER_PLAY);
		                msg.replyTo = AOTalk.serviceMessenger;
		                
		                try {
		                	AOTalk.service.send(msg);
						} catch (RemoteException e) {
							Logging.log(APP_TAG, e.getMessage());
						}
						
						play.setImageResource(R.drawable.button_stop);
						AOTalk.isPlaying = true;
                	}
                } else {
                	Logging.log(APP_TAG, "service is NULL");
                }
			}
		});
		
		return fragmentTools;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		Logging.log(APP_TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
	
	public void updateTitle(String text) {
		if (text != null && title != null) {
			title.setText(text);
		}
	}
}
