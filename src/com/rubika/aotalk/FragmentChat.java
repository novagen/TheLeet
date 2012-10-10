package com.rubika.aotalk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.rubika.aotalk.adapter.ChatMessageAdapter;
import com.rubika.aotalk.util.Logging;

public class FragmentChat extends SherlockFragment {
	private ChatMessageAdapter messageAdapter;
	private OnItemLongClickListener clickListener;
	private View.OnClickListener channelListener;
	private OnKeyListener keyListener;
	
	static FragmentChat newInstance(ChatMessageAdapter messageAdapter, OnItemLongClickListener clickListener, View.OnClickListener channelListener, OnKeyListener keyListener) {
		FragmentChat f = new FragmentChat(messageAdapter, clickListener, channelListener, keyListener);
        return f;
    }
	
	public FragmentChat() {
	}
	
	public FragmentChat(ChatMessageAdapter messageAdapter, OnItemLongClickListener clickListener, View.OnClickListener channelListener, OnKeyListener keyListener) {
		this.messageAdapter = messageAdapter;
		this.clickListener = clickListener;
		this.channelListener = channelListener;
		this.keyListener = keyListener;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logging.log("FragmentChat", "onCreateView");
		
		if (container == null) {
            return null;
        }
		
		View fragmentChat = inflater.inflate(R.layout.fragment_chat, container, false);
		
		((ListView) fragmentChat.findViewById(R.id.messagelist)).setAdapter(messageAdapter);
		((ListView) fragmentChat.findViewById(R.id.messagelist)).setOnItemLongClickListener(clickListener);
		
		((ListView) fragmentChat.findViewById(R.id.messagelist)).setPersistentDrawingCache(ListView.PERSISTENT_ALL_CACHES);
		/*
		((ListView) fragmentChat.findViewById(R.id.messagelist)).setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				((ListView) view).startLayoutAnimation();
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
		});
		
		AnimationSet set = new AnimationSet(true);
		
		Animation animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, (float)0, Animation.RELATIVE_TO_SELF, (float)0.5);
		animation.setDuration(200);
		set.addAnimation(animation);
		
		LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
		
		((ListView) fragmentChat.findViewById(R.id.messagelist)).setLayoutAnimation(controller);
		((ListView) fragmentChat.findViewById(R.id.messagelist)).startLayoutAnimation();
		*/
        
        ((ImageButton) fragmentChat.findViewById(R.id.channel)).setOnClickListener(channelListener);
        ((EditText) fragmentChat.findViewById(R.id.input)).setOnKeyListener(keyListener);
		
		return fragmentChat;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		Logging.log("FragmentChat", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
}
