package com.yfrt.cargotrace;

import com.weibo.sdk.android.util.Utility;

public interface ConstantS {
	
	//应用的key 请到官方申请正式的appkey替换APP_KEY
	public static final String APP_KEY="4006877491";
	
	//替换为开发者REDIRECT_URL
	public static final String REDIRECT_URL = "http://www.efreight.cn";
	    
	//新支持scope 支持传入多个scope权限，用逗号分隔
	public static final String SCOPE = "email,direct_messages_read,direct_messages_write," +
			"friendships_groups_read,friendships_groups_write,statuses_to_me_read," +
				"follow_app_official_microblog";
	
	public static final String CLIENT_ID = "client_id";
	
	public static final String RESPONSE_TYPE = "response_type";
	
	public static final String USER_REDIRECT_URL = "redirect_uri";
	
	public static final String DISPLAY = "display";
	
	public static final String USER_SCOPE = "scope";
	
	public static final String PACKAGE_NAME = "packagename";
	
	public static final String KEY_HASH = "key_hash";

	   
}
