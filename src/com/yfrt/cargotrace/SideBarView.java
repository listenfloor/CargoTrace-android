package com.yfrt.cargotrace;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class SideBarView extends FrameLayout {

	private View mainView;
	private View sideBarView;
	private Scroller scroller;
	private int sideWidth = 50; // dp
	private DisplayMetrics metrics;
	
	public SideBarView(Context c) {
		super(c);
	}
	
	public void init(View main, View sideBar, DisplayMetrics m) {
		mainView = main;
		addView(mainView);

		sideBarView = sideBar;
		addView(sideBar);

		metrics = m;

		scroller = new Scroller(getContext());
		
		postInvalidate();
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mainView.layout(0, 0, metrics.widthPixels, metrics.heightPixels);
		sideBarView.layout((int)(sideWidth * metrics.density - metrics.widthPixels), 0, 0, metrics.heightPixels);
	}

	@Override
	public void computeScroll() {
		if(scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
		}
	}
	
	public void showSideBar() {
		scroller.startScroll(0, 0, (int)(sideWidth * metrics.density - metrics.widthPixels), 0);
		invalidate();
	}
	public void hideSideBar() {
		scroller.startScroll((int)(sideWidth * metrics.density - metrics.widthPixels), 0, (int)(metrics.widthPixels - sideWidth * metrics.density), 0);
		//scroller.startScroll(0, 0, (int)(metrics.widthPixels - sideWidth * metrics.density), 0);
		invalidate();
	}
}
