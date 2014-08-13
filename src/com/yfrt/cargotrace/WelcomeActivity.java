package com.yfrt.cargotrace;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class WelcomeActivity extends Activity {

	private Handler handler = new Handler();
	private long beginTime;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		System.out.println("欢迎界面");
		String version = pref.getString(AppConstants.version, null);
		final Intent intent = new Intent();
		if(!AppConstants.current_version.equals(version)) {
			Editor editor = pref.edit();
			editor.putString(AppConstants.version, AppConstants.current_version);
			editor.commit();
			intent.setClass(this, UserGuideActivity.class);
		} else {
			intent.setClass(this, MainActivity.class);
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				beginTime = System.currentTimeMillis();
				((CargoTraceApp)getApplication()).recordList = SystemUtil.readRecord(((CargoTraceApp)getApplication()).recordFile);
				long time = System.currentTimeMillis() - beginTime;
				time = time < 1200 ? 1200 - time : 0;
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						startActivity(intent);
						finish();
					}
				}, time);
			}
		}).start();
	}
}
