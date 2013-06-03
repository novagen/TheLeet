package com.rubika.aotalk;

import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.rubika.aotalk.adapter.RKNMessageAdapter;
import com.rubika.aotalk.item.RKNMessage;
import com.rubika.aotalk.rkn.CallSoap;
import com.rubika.aotalk.util.Logging;

public class FragmentRKN extends SherlockFragment {
	private static final String APP_TAG = "--> The Leet :: FragmentRKN";
	private AccountManager accountManager;
	private Account[] accounts;
	private RelativeLayout Overlay;
	private ListView messageList;
	public static RKNMessageAdapter messageAdapter;
	private static SharedPreferences settings;
	private List<RKNMessage> messages;
	
	public static FragmentRKN newInstance() {
		FragmentRKN f = new FragmentRKN();
        return f;
    }
	
	public FragmentRKN() {
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		CheckRKNAccount();
	}
	
	private void CheckRKNAccount() {
        accountManager = AccountManager.get(AOTalk.getContext());
		accounts = accountManager.getAccountsByType(AOTalk.getContext().getString(R.string.account_type));

        if (accounts.length <= 0) {
        	Overlay.setVisibility(View.VISIBLE);
        }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
            return null;
        }
				
        settings = PreferenceManager.getDefaultSharedPreferences(AOTalk.getContext());
        messages = new ArrayList<RKNMessage>();
        
        View fragmentRKN = inflater.inflate(R.layout.fragment_rkn, container, false);
        
        Overlay = (RelativeLayout) fragmentRKN.findViewById(R.id.overlay);
        
        Button RKNLogin = (Button) fragmentRKN.findViewById(R.id.rknlogin);
        RKNLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (android.os.Build.VERSION.SDK_INT > 7) {
					accountManager.addAccount(AOTalk.getContext().getString(R.string.account_type), null, null, null, getActivity(), null, null);
				} else {
					Logging.toast(AOTalk.getContext(), getString(R.string.unsupported_version));
				}
			}
		});
        
        CheckRKNAccount();
        
        messageAdapter = new RKNMessageAdapter(AOTalk.getContext(), android.R.layout.simple_dropdown_item_1line, messages, settings.getBoolean("enableAnimations", true));
		messageList = (ListView) fragmentRKN.findViewById(R.id.postlist);
		
		messageList.setAdapter(messageAdapter);
		messageList.setDividerHeight(0);
		messageList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				//final RKNMessage message = messageAdapter.getItem(arg2);
				return true;
			}
		});
		
		messageList.setPersistentDrawingCache(ListView.PERSISTENT_ALL_CACHES);
 
        
        return fragmentRKN;
	}
	
    public class GetRKNetFeed extends AsyncTask<Void, Void, String> {
    	//private List<RKNMessage> tempMessages = new ArrayList<RKNMessage>();
    	
        @Override    
        protected void onPreExecute() {
        }

        @Override 
		protected void onPostExecute(String result) {
	    }

		@Override
		protected String doInBackground(Void... params) {			
			String Username = accounts[0].name;
			String Password = accountManager.getPassword(accounts[0]);
			
			CallSoap soap = new CallSoap("GetFeedForUser");
			
			List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
			
    		PropertyInfo pi = new PropertyInfo();
			pi.setName("Username");
		    pi.setValue(Username);
		    pi.setType(String.class);
		    properties.add(pi);
		    
    		pi = new PropertyInfo();
			pi.setName("Password");
		    pi.setValue(Password);
		    pi.setType(String.class);
		    properties.add(pi);
		    
		    Logging.log(APP_TAG, soap.Call(properties).toString());
			
			return null;
		}
    }
}
