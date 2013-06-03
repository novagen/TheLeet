package com.rubika.aotalk.map;

import android.view.View;

public class MarkerView {
	public View view;
	public double X;
	public double Y;
	
	public MarkerView(View view, double X, double Y) {
		this.view = view;
		this.X = X;
		this.Y = Y;
	}
}
