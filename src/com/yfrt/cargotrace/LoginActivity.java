package com.yfrt.cargotrace;

import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.yfrt.cargotrace.HttpManager.HttpQueryCallback;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends CommonTitleBarActivity{
	private static final String weiboApiKey = "4006877491";
	private static final String weiboRedirectURL = "http://www.efreight.cn";
	private static Oauth2AccessToken accessToken;
	public static final String SINA_SCOPE = "email,direct_messages_read,direct_messages_write," +
  			"friendships_groups_read,friendships_groups_write,statuses_to_me_read," +
  				"follow_app_official_microblog";
	
	private static final String TENCENT_APP_ID = "100462165";
	private static final String SCOPE = "get_user_info,get_simple_userinfo,get_user_profile,get_app_friends,"
            + "add_share,add_topic,list_album,upload_pic,add_album,set_user_face,get_vip_info,get_vip_rich_info,get_intimate_friends_weibo,match_nick_tips_weibo";

	private Tencent mTencent;
	
	private Handler handler = new Handler();
	
	private HttpQueryCallback keyCallback = new HttpQueryCallback() {
		@Override
		public void onQueryComplete(int state, String result) {
			if(state == HttpQueryCallback.STATE_OK) {
				Element root = SystemUtil.getDocumentElement(result);
				NodeList nodes = null;
				String module = null;
				String exponent = null;
				if(root != null) {
					nodes = root.getChildNodes();
				}
				nodes = nodes.item(0).getChildNodes();
				module = nodes.item(0).getTextContent();
				exponent = nodes.item(1).getTextContent();
				String key =null;
				try {
					key = RSA.getPublicKey(module, exponent, 16);
				} catch (Exception e) {
					e.printStackTrace();
					SystemUtil.showToast(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT);
					return;
				}
				
				EditText user = (EditText)findViewById(R.id.user_name);
				EditText password = (EditText)findViewById(R.id.password);
				String pwString = password.getText().toString();

				String loginResult = SystemUtil.login(user.getText().toString(), pwString, key, loginCallback);
				if(loginResult != null) {
					root = SystemUtil.getDocumentElement(loginResult);
					if(root != null) {
						nodes = root.getChildNodes();
						if(nodes != null && nodes.item(0) != null) {
							if("1".equals(nodes.item(0).getTextContent())) {	
								((CargoTraceApp)getApplication()).saveLoginState("user", "session");
								SystemUtil.showToast(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT);
								handler.postDelayed(new Runnable() {
									@Override
									public void run() {
										finish();
									}
								}, 2000);
//							} else if("-302".equals(nodes.item(0).getTextContent())) {
							} else {
								SystemUtil.showToast(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT);
							}
							return;
						}
					}
				}
			}
			
			SystemUtil.showToast(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT);
		}
	};
	
	private HttpQueryCallback loginCallback = new HttpQueryCallback() {
		@Override
		public void onQueryComplete(int state, String result) {
			if(state == HttpQueryCallback.STATE_OK) { 
				Element root = SystemUtil.getDocumentElement(result);
				if(root != null) {
					NodeList nodes = root.getChildNodes();
					if(nodes != null && nodes.item(0) != null) {
						if("1".equals(nodes.item(0).getTextContent())) {	
							((CargoTraceApp)getApplication()).saveLoginState("user", "session");
							SystemUtil.showToast(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT);
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									finish();
								}
							}, 2000);
						} else if("-302".equals(nodes.item(0).getTextContent())) {
							SystemUtil.showToast(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT);
						}
						return;
					}
				}
			}
			
			SystemUtil.showToast(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		setCustomTitle(getString(R.string.login_title));
		setCustomTitleTextColor(getResources().getColor(R.color.text_white));
		
		setLeftButton(getResources().getDrawable(R.drawable.back), new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		findViewById(R.id.login).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				EditText user = (EditText)findViewById(R.id.user_name);
				EditText password = (EditText)findViewById(R.id.password);
				String pwString = password.getText().toString();
				if(SystemUtil.checkPassword(pwString)) {
					SystemUtil.getPublicKey(keyCallback);					
				} else {
					SystemUtil.showToast(LoginActivity.this, getString(R.string.login_password_format_error), Toast.LENGTH_SHORT);
				}
			}
		});

		findViewById(R.id.register).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				
				Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivity(i);
			}
		});
		
		findViewById(R.id.weibo).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startWeiboOAuth();
			}
		});
		
		findViewById(R.id.qq).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTencent = Tencent.createInstance(TENCENT_APP_ID, LoginActivity.this);
		        if (!mTencent.isSessionValid()) {
		            IUiListener listener = new BaseUiListener();
		            mTencent.login(LoginActivity.this, SCOPE, listener);
		        } else {
		            mTencent.logout(LoginActivity.this);
		        }
			}
		});
	}
	
	private void startWeiboOAuth() {
		Weibo weibo = Weibo.getInstance(weiboApiKey, weiboRedirectURL, SINA_SCOPE);
		weibo.anthorize(this, new WeiboAuthDialogListener());
	}
	
    class WeiboAuthDialogListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            String token = values.getString("access_token");
            String expires_in = values.getString("expires_in");
            accessToken = new Oauth2AccessToken(token, expires_in);
            if (accessToken.isSessionValid()) {
            	loginComplete();
            }
        }

        @Override
        public void onError(WeiboDialogError e) {
            Toast.makeText(getApplicationContext(),
                    "Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(getApplicationContext(), "Auth cancel",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(getApplicationContext(),
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }
    
    private void loginComplete() {
		Toast.makeText(LoginActivity.this,("登录成功"), Toast.LENGTH_SHORT).show();
	      
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				((CargoTraceApp) getApplication()).saveLoginState("user", "session");
			}
		}, 2000);
    }
    private void loginError() {
        Toast.makeText(getApplicationContext(), "登录失败 ", Toast.LENGTH_LONG).show();
    }
    private void loginCancel() {
        Toast.makeText(getApplicationContext(), "登录取消", Toast.LENGTH_LONG).show();
    }
    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(JSONObject response) {
        	loginComplete();
        }

        @Override
        public void onError(UiError e) {
        	loginError();
        }

        @Override
        public void onCancel() {
        	loginCancel();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          mTencent.onActivityResult(requestCode, resultCode, data);
    }
}
