package com.yfrt.cargotrace;



import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class SupportActivity extends CommonTitleBarActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_support);
		
		setCustomTitle(getString(R.string.support_title));
		setCustomTitleTextColor(getResources().getColor(R.color.text_white));
		
		setLeftButton(getResources().getDrawable(R.drawable.back), new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
