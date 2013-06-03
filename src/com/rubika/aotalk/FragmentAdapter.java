package com.rubika.aotalk;

import java.util.List;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.actionbarsherlock.app.SherlockFragment;

public class FragmentAdapter extends FragmentPagerAdapter {
	private List<SherlockFragment> fragments;

	public FragmentAdapter(FragmentManager fm, List<SherlockFragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public SherlockFragment getItem(int position) {
		return this.fragments.get(position);
	}

	@Override
	public int getCount() {
		return this.fragments.size();
	}

	@Override
	public String getPageTitle(int position) {
		switch (position) {
		case 0:
			return "RKN";
		case 1:
			return "TOOLS";
		case 2:
			return "CHAT";
		case 3:
			return "FRIENDS";
		}
		return null;
	}
}
