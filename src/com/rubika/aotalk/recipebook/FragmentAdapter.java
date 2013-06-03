package com.rubika.aotalk.recipebook;

import java.util.List;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

public class FragmentAdapter extends FragmentPagerAdapter {
	private List<SherlockListFragment> fragments;

	public FragmentAdapter(FragmentManager fm, List<SherlockListFragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public SherlockListFragment getItem(int position) {
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
			return "RECIPES";
		}
		
		return null;
	}
}
