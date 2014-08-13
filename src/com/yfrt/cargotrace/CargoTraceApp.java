package com.yfrt.cargotrace;

import java.io.File;
import java.util.List;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

public class CargoTraceApp extends Application {
	private String session;
	private String userName;
	
	public String recordFile;
	
	public List<RecordItem> recordList;
	
	public boolean canPush = false;
	
//	public List<RecordItem> orderList;
	
	public boolean isLogin() {
		if(session != null) {
			return true;
		}
		
		SharedPreferences sp = getSharedPreferences(AppConstants.default_preference, 0);
		session = sp.getString(AppConstants.session, null);
		if(session == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public void saveLogoutState() {
		session = null;
		SharedPreferences sp = getSharedPreferences(AppConstants.default_preference, 0);
		sp.edit().remove(AppConstants.session).remove(AppConstants.user).commit();
	}
	
	public void saveLoginState(String userName, String session) {
		this.userName = userName;
		this.session = session;
		SharedPreferences sp = getSharedPreferences(AppConstants.default_preference, 0);
		sp.edit().putString(AppConstants.session, session).putString(AppConstants.user, userName).commit();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		recordFile = getFilesDir() + AppConstants.record_file_name;
		
		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
		registrationIntent.putExtra("sender", "galaxy0000@gmail.com");
		try {
			this.startService(registrationIntent);
			canPush = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasRecord() {
		File record = new File(recordFile);
		return record.exists();
	}
}
