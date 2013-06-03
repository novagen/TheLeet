package com.rubika.aotalk.ui.improvedtextview;

import java.util.ArrayList;
import java.util.WeakHashMap;

import com.rubika.aotalk.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.TextView;

public class ImprovedTextView extends TextView {
	private ArrayList<Shadow> outerShadows;
	
	private WeakHashMap<String, Pair<Canvas, Bitmap>> canvasStore;
	
	private float strokeWidth;
	private Integer strokeColor;
	private Join strokeJoin;
	private float strokeMiter;
	
	private int[] lockedCompoundPadding;
	private boolean frozen = false;

	public ImprovedTextView(Context context) {
		super(context);
		init(null);
	}
	public ImprovedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}
	public ImprovedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}
	
	@SuppressWarnings("deprecation")
	public void init(AttributeSet attrs){
		outerShadows = new ArrayList<Shadow>();

		if(canvasStore == null){
		    canvasStore = new WeakHashMap<String, Pair<Canvas, Bitmap>>();
		}
	
		if(attrs != null){
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView);
			
            String typefaceName = a.getString( R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvTypeface);
            if(typefaceName != null) {
                Typeface tf = Typeface.createFromAsset(getContext().getAssets(), String.format("fonts/%s.ttf", typefaceName));
                setTypeface(tf);
            }
		
			if(a.hasValue(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvBackground)){
				Drawable background = a.getDrawable(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvBackground);
				if(background != null){
					this.setBackgroundDrawable(background);
				}else{
					this.setBackgroundColor(a.getColor(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvBackground, 0xff000000));
				}
			}
			
			if(a.hasValue(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvOuterShadowColor)){
				this.addOuterShadow(a.getFloat(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvOuterShadowRadius, 0), 
									a.getFloat(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvOuterShadowDx, 0), 
									a.getFloat(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvOuterShadowDy, 0),
									a.getColor(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvOuterShadowColor, 0xff000000));
			}
			
			if(a.hasValue(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvStrokeColor)){
				float strokeWidth = a.getFloat(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvStrokeWidth, 1);
				int strokeColor = a.getColor(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvStrokeColor, 0xff000000);
				float strokeMiter = a.getFloat(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvStrokeMiter, 10);
				Join strokeJoin = null;
				switch(a.getInt(R.styleable.com_rubika_aotalk_improvedtextview_ImprovedTextView_mtvStrokeJoinStyle, 0)){
				case(0): strokeJoin = Join.MITER; break;
				case(1): strokeJoin = Join.BEVEL; break;
				case(2): strokeJoin = Join.ROUND; break;
				}
				this.setStroke(strokeWidth, strokeColor, strokeJoin, strokeMiter);
			}
		}
	}
	
	public void setStroke(float width, int color, Join join, float miter){
		strokeWidth = width;
		strokeColor = color;
		strokeJoin = join;
		strokeMiter = miter;
	}
	
	public void setStroke(float width, int color){
		setStroke(width, color, Join.MITER, 10);
	}
	
	public void addOuterShadow(float r, float dx, float dy, int color){
		if(r == 0){ r = 0.0001f; }
		outerShadows.add(new Shadow(r,dx,dy,color));
	}
	
	public void clearOuterShadows(){
		outerShadows.clear();
	}

	PorterDuffXfermode xmode1 = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
	PorterDuffXfermode xmode2 = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
	Rect r = new Rect();
	
	@SuppressWarnings("deprecation")
	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		freeze();
		Drawable restoreBackground = this.getBackground();
		Drawable[] restoreDrawables = this.getCompoundDrawables();
		int restoreColor = this.getCurrentTextColor();
		
		this.setCompoundDrawables(null,  null, null, null);

		for(Shadow shadow : outerShadows){
			this.setShadowLayer(shadow.r, shadow.dx, shadow.dy, shadow.color);
			super.onDraw(canvas);
		}
		
		this.setShadowLayer(0,0,0,0);
		this.setTextColor(restoreColor);
		
		if(strokeColor != null){
			TextPaint paint = this.getPaint();
			paint.setStyle(Style.STROKE);
			paint.setStrokeJoin(strokeJoin);
			paint.setStrokeMiter(strokeMiter);
			this.setTextColor(strokeColor);
			paint.setStrokeWidth(strokeWidth);
			super.onDraw(canvas);
			paint.setStyle(Style.FILL);
			this.setTextColor(restoreColor);
		}
		
		if(restoreDrawables != null){
			this.setCompoundDrawablesWithIntrinsicBounds(restoreDrawables[0], restoreDrawables[1], restoreDrawables[2], restoreDrawables[3]);
		}
		
		this.setBackgroundDrawable(restoreBackground);
		this.setTextColor(restoreColor);

		unfreeze();
	}
	
	// Keep these things locked while onDraw in processing
	public void freeze(){
		lockedCompoundPadding = new int[]{
				getCompoundPaddingLeft(),
				getCompoundPaddingRight(),
				getCompoundPaddingTop(),
				getCompoundPaddingBottom()
		};
		frozen = true;
	}
	
	public void unfreeze(){
		frozen = false;
	}
	
    
    @Override
    public void requestLayout(){
        if(!frozen) super.requestLayout();
    }
	
	@Override
	public void postInvalidate(){
		if(!frozen) super.postInvalidate();
	}
	
   @Override
    public void postInvalidate(int left, int top, int right, int bottom){
        if(!frozen) super.postInvalidate(left, top, right, bottom);
    }
	
	@Override
	public void invalidate(){
		if(!frozen)	super.invalidate();
	}
	
	@Override
	public void invalidate(Rect rect){
		if(!frozen) super.invalidate(rect);
	}
	
	@Override
	public void invalidate(int l, int t, int r, int b){
		if(!frozen) super.invalidate(l,t,r,b);
	}
	
	@Override
	public int getCompoundPaddingLeft(){
		return !frozen ? super.getCompoundPaddingLeft() : lockedCompoundPadding[0];
	}
	
	@Override
	public int getCompoundPaddingRight(){
		return !frozen ? super.getCompoundPaddingRight() : lockedCompoundPadding[1];
	}
	
	@Override
	public int getCompoundPaddingTop(){
		return !frozen ? super.getCompoundPaddingTop() : lockedCompoundPadding[2];
	}
	
	@Override
	public int getCompoundPaddingBottom(){
		return !frozen ? super.getCompoundPaddingBottom() : lockedCompoundPadding[3];
	}
	
	public static class Shadow{
		float r;
		float dx;
		float dy;
		int color;
		public Shadow(float r, float dx, float dy, int color){
			this.r = r;
			this.dx = dx;
			this.dy = dy;
			this.color = color;
		}
	}
}
