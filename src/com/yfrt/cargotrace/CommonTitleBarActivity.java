package com.yfrt.cargotrace;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CommonTitleBarActivity extends Activity {
	protected TextView leftView, titleView, rightView;
	protected ViewGroup vg;
	public static final int LAYER_TYPE_SOFTWARE = 1;
	protected float mDensity;

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(R.layout.common_title_bar);
		FrameLayout content = (FrameLayout)findViewById(R.id.content);
		LayoutInflater.from(this).inflate(layoutResID, content);
		
		mDensity = getResources().getDisplayMetrics().density;
		init(getIntent());
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(R.layout.common_title_bar);
		FrameLayout content = (FrameLayout)findViewById(R.id.content);
		content.addView(view);
		
		mDensity = getResources().getDisplayMetrics().density;
		init(getIntent());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			leftView.performClick();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	private void init(Intent data) {
		titleView = (TextView)findViewById(R.id.title);	
		leftView = (TextView) findViewById(R.id.leftBuuton);
		rightView = (TextView) findViewById(R.id.rightBuuton);
		//middleView = (TextView) findViewById(R.id.reply);
	}

	public void setCustomTitle(CharSequence title) {
		titleView.setText(title);
	}
	
	public void setCustomTitleTextColor(int color) {
		titleView.setTextColor(color);
	}

	public String getTitleString() {
		String result = null;

		if (titleView != null && titleView instanceof TextView) {
			TextView titleTextView = (TextView) titleView;
			CharSequence charSeq = titleTextView.getText();

			if (charSeq != null) {
				result = charSeq.toString();
			}
		}

		return result;
	}

    public int getTitleBarHeight() {
    	return getResources().getDimensionPixelSize(R.dimen.title_bar_height);
    }

	protected boolean onBackEvent() {
		finish();
		return false;
	}



	protected void setLeftButton(Drawable leftIcon, OnClickListener l) {
		leftView.setVisibility(View.VISIBLE);
		leftView.setBackgroundDrawable(leftIcon);
		leftView.setOnClickListener(l);
	}
	
//	protected void setMiddleButton(Drawable middleIcon, OnClickListener l) 
//	{
//		middleView.setBackgroundDrawable(middleIcon);
//		middleView.setOnClickListener(l);
//	}


	protected void setRightButton(Drawable rightIcon, OnClickListener l) {
		rightView.setVisibility(View.VISIBLE);
		rightView.setBackgroundDrawable(rightIcon);
		rightView.setOnClickListener(l);
	}

	protected OnClickListener onBackListeger = new OnClickListener() {
		public void onClick(View v) {
			onBackEvent();
		}
	};
}
