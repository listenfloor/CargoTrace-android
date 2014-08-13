package com.yfrt.cargotrace;

import com.yfrt.cargotrace.R.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class MyProgressBar extends RelativeLayout {

	private ImageView backgroundBar;
	private ImageView progressBar;
	private ImageView indicator;
	private float progress; // from 0 to 1.0
	
	public MyProgressBar(Context c) {
		super(c);
	}
	public MyProgressBar(Context c, AttributeSet attr) {
		this(c, attr, 0);
	}
	public MyProgressBar(Context c, AttributeSet attr, int defStyle) {
		super(c, attr, defStyle);
		TypedArray ta = c.obtainStyledAttributes(attr, R.styleable.yfrt);
		
		backgroundBar = new ImageView(c);
		backgroundBar.setBackgroundColor(ta.getColor(R.styleable.yfrt_progress_bg, 0xffffff));
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, 10);
		lp.addRule(ALIGN_PARENT_BOTTOM);
		backgroundBar.setLayoutParams(lp);
		addView(backgroundBar);
		
		progressBar = new ImageView(c);
		lp = new LayoutParams(0, 10);
		lp.addRule(ALIGN_PARENT_BOTTOM);
		progressBar.setLayoutParams(lp);
		progressBar.setBackgroundColor(ta.getColor(R.styleable.yfrt_progress_highlight, 0x000000));
		addView(progressBar);
		
		indicator = new ImageView(c);
//		lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		indicator.setLayoutParams(lp);
		Drawable dr = ta.getDrawable(R.styleable.yfrt_progress_indicator);
		indicator.setBackgroundDrawable(dr);
//		indicator.setBackgroundDrawable(ta.getDrawable(R.styleable.yfrt_progress_bg));
		addView(indicator);
	}
	
	public void setProgress(float p) {
		progress = p;
		progress = progress < 0 ? 0.0f : progress;
		progress = progress > 1.0f ? 1.0f : progress;

		requestLayout();
	}
		
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		int width = (int)(MeasureSpec.getSize(widthMeasureSpec) * progress);
//		progressBar.measure(width, progressBar.getMeasuredHeight());
//	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int width = (int)(progress * (right - left));
		LayoutParams lp = (LayoutParams) progressBar.getLayoutParams();
		lp.width = width; 
		
		lp = (LayoutParams)indicator.getLayoutParams();
		lp.setMargins(width - MeasureSpec.getSize(indicator.getMeasuredWidth()) / 2, 0, 0, 0);

		super.onLayout(changed, left, top, right, bottom);
}
}
