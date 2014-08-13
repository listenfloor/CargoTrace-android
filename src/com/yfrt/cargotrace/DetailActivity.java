package com.yfrt.cargotrace;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.http.NameValuePair;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.open.HttpStatusException;
import com.tencent.open.NetworkUnavailableException;
import com.tencent.tauth.Constants;
import com.tencent.tauth.IRequestListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.net.RequestListener;
import com.yfrt.cargotrace.HttpManager.HttpQueryCallback;

import android.os.Bundle;
import android.os.Debug;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

class AccessToken {
	String access_token;
	String remind_in;
	int expires_in;
	String uid;
	String scope;
}

public class DetailActivity extends CommonTitleBarActivity{

	private RecordItem recordItem;
	AlertDialog menuDialog;// menu菜单Dialog
    GridView menuGrid;
    View menuView;

    private final int ITEM_EMAIL = 0;// 邮件
    private final int ITEM_MESSAGE = 1;// 短信
    private final int ITEM_SHARETOQQ = 2;// QQ
    private final int ITEM_SHARETOQZONE = 3;// QQ空间
    private final int ITEM_WEIXIN = 4;// 微信
    private final int ITEM_WEIXINGROUP = 5;// 微信朋友圈
    private final int ITEM_SINAWEIBO = 6;// 新浪微博
    private final int ITEM_TCWEIBO = 7;// 腾讯微博
    
    private static final String TENCENT_APP_ID = "100462165";
	private static final String SCOPE = "get_user_info,get_simple_userinfo,get_user_profile,get_app_friends,"
            + "add_share,add_topic,list_album,upload_pic,add_album,set_user_face,get_vip_info,get_vip_rich_info,get_intimate_friends_weibo,match_nick_tips_weibo";
    private Tencent mTencent;
    
    public static final String WX_APP_ID = "wxd0ad5355b067a6b7"; 
    private IWXAPI api; 
    private static final int THUMB_SIZE = 100;
    private String mCodeString;
    
    private Weibo mWeibo;
    public static Oauth2AccessToken accessToken;
    public static final String TAG = "sinasdk";
    
  //应用的key 请到官方申请正式的appkey替换APP_KEY
  	public static final String SINA_APP_KEY="4006877491";
  	//替换为开发者REDIRECT_URL
  	public static final String SINA_REDIRECT_URL = "http://www.efreight.cn";
  	//新支持scope 支持传入多个scope权限，用逗号分隔
  	public static final String SINA_SCOPE = "email,direct_messages_read,direct_messages_write";
  	
//  	//private WeiboAPI mTCWeibo;
//    public static String TC_AccessToken;
//  //应用的key 请到官方申请正式的appkey替换APP_KEY
//  	public static final String TC_APP_KEY="801369781";
//  	//替换为开发者REDIRECT_URL
//  	public static final String TC_REDIRECT_URL = "http://www.efreight.cn";
//  	//新支持scope 支持传入多个scope权限，用逗号分隔
//  	public static final String TC_SCOPE = "email,direct_messages_read,direct_messages_write," +
//  			"friendships_groups_read,friendships_groups_write,statuses_to_me_read," +
//  				"follow_app_official_microblog";
    
    /** 菜单图片 **/
    int[] menu_image_array = { R.drawable.e_detail_mailbtn,
            R.drawable.e_detail_messagebtn, R.drawable.e_detail_qqbtn,
            R.drawable.e_detail_qzonebtn, R.drawable.e_detail_weixinbtn,
            R.drawable.e_detail_weixincirclebtn, R.drawable.e_detail_weibobtn,
            R.drawable.e_detail_tencentbtn };
    /** 菜单文字 **/
    String[] menu_name_array = { "邮件", "短信", "QQ分享", "QQ空间", "微信", "微信朋友圈",
            "新浪微博", "腾讯微博"};
	
	private HttpQueryCallback refreshCallback = new HttpQueryCallback() {
		@Override
		public void onQueryComplete(final int state, final String result) {
			if(state == HttpQueryCallback.STATE_OK) {
				SystemUtil.parseRecord(DetailActivity.this, recordItem, result);
				SystemUtil.saveRecord((CargoTraceApp)getApplication());

				refreshListView();
				if(!isFinishing()){
					SystemUtil.showToast(DetailActivity.this, getString(R.string.refresh_success), Toast.LENGTH_SHORT);
				}
			} else {
				if(!isFinishing()){
					SystemUtil.showToast(DetailActivity.this, getString(R.string.refresh_failed), Toast.LENGTH_SHORT);
				}
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {	
					findViewById(R.id.progress).setVisibility(View.GONE);
				}
			});
		}
	};
	
	private HttpQueryCallback subscribeCallback = new HttpQueryCallback() {
		@Override
		public void onQueryComplete(final int state, final String result) {
			if(state == HttpQueryCallback.STATE_OK) {
				recordItem.subscribe = true;
				SystemUtil.saveRecord((CargoTraceApp)getApplication());
				
				if(!isFinishing()){
					SystemUtil.showToast(DetailActivity.this, getString(R.string.subscribe_success), Toast.LENGTH_SHORT);
				}
			} else {
				if(!isFinishing()){
					SystemUtil.showToast(DetailActivity.this, getString(R.string.subscribe_failed), Toast.LENGTH_SHORT);
				}
			}

			runOnUiThread(new Runnable() {
				@Override
				public void run() {	
					findViewById(R.id.progress).setVisibility(View.GONE);					
				}
			});
		}
	};
	private HttpQueryCallback unsubscribeCallback = new HttpQueryCallback() {
		@Override
		public void onQueryComplete(final int state, final String result) {
			if(state == HttpQueryCallback.STATE_OK) {
				recordItem.subscribe = false;
				SystemUtil.saveRecord((CargoTraceApp)getApplication());

				if(!isFinishing()){
					SystemUtil.showToast(DetailActivity.this, getString(R.string.unsubscribe_success), Toast.LENGTH_SHORT);
				}
			} else {
				if(!isFinishing()){
					SystemUtil.showToast(DetailActivity.this, getString(R.string.unsubscribe_failed), Toast.LENGTH_SHORT);
				}
			}

			runOnUiThread(new Runnable() {
				@Override
				public void run() {	
					findViewById(R.id.progress).setVisibility(View.GONE);					
				}
			});
		}
	};
	@Override
	protected void onStop() {
		Debug.stopMethodTracing();
		super.onStop();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Debug.startMethodTracing();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		mWeibo = Weibo.getInstance(ConstantS.APP_KEY, ConstantS.REDIRECT_URL,ConstantS.SCOPE);
		
		mTencent = Tencent.createInstance(TENCENT_APP_ID, DetailActivity.this);
		api = WXAPIFactory.createWXAPI(DetailActivity.this, null);
    	api.registerApp(WX_APP_ID); 
    	
		menuView = View.inflate(this, R.layout.gridview_menu, null);
        // 创建AlertDialog
        menuDialog = new AlertDialog.Builder(this).create();
        menuDialog.setView(menuView);
        menuDialog.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU)// 监听按键
                    dialog.dismiss();
                return false;
            }
        });

        menuGrid = (GridView) menuView.findViewById(R.id.gridview);
        menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
        /** 监听menu选项 **/
        menuGrid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                switch (arg2) {
                case ITEM_EMAIL:// 邮件
                	sendMailByIntent();
                    break;
                case ITEM_MESSAGE:// 短信
                	sendMessage();
                    break;
                case ITEM_SHARETOQQ:// QQ
    		        if (!mTencent.isSessionValid()) {
    		            IUiListener listener = new BaseUiListener();
    		            mTencent.login(DetailActivity.this, SCOPE, listener);
    		        }
    		        else 
    		        {
    		        	onClickShareToQQ();
					}
                    break;
                case ITEM_SHARETOQZONE:// QQ空间
                	if (!mTencent.isSessionValid()) {
    		            IUiListener listener = new BaseUiListener();
    		            mTencent.login(DetailActivity.this, SCOPE, listener);
    		        }
    		        else 
    		        {
    		        	onClickAddShare();
					}
                	
                    break;
                case ITEM_WEIXIN:// 微信
                	onClickShareToWeixin();
                    break;
                case ITEM_WEIXINGROUP:// 微信朋友圈
                	onClickShareToWeixinGroup();
                    break;
                case ITEM_SINAWEIBO:// 新浪微博
                	accessToken = AccessTokenKeeper.readAccessToken(DetailActivity.this);
                	if (accessToken.isSessionValid())
                	{
                		onClickShareToSinaWeibo();
					}
                	else
                	{
                		mWeibo.anthorize(DetailActivity.this, new AuthDialogListener());
					}
                    break;
                case ITEM_TCWEIBO:// 腾讯微博
                	//TC_AccessToken = Util.getSharePersistent(getApplicationContext(),"ACCESS_TOKEN");
//            		if (TC_AccessToken == null || "".equals(TC_AccessToken)) 
//            		{
//            			auth(801369781,"2cc2e8653197eb271b3719c4f3f6f302");
//            		}
//            		else 
//            		{
//            			onClickShareToTCWeibo();
//					}
                    break;
                }
            }
        });
        
		setCustomTitle(getString(R.string.order_detail_title));
		setCustomTitleTextColor(getResources().getColor(R.color.text_white));
		
		setLeftButton(getResources().getDrawable(R.drawable.back), new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				Intent i = new Intent(DetailActivity.this, SubscribeActivity.class);
				startActivity(i);
			}
		});
		
//		setMiddleButton(getResources().getDrawable(R.drawable.reply), new OnClickListener() {
//			@Override
//			public void onClick(View v) 
//			{
//				onMenuOpened(0,null);
//			}
//		});
		
		setRightButton(getResources().getDrawable(R.drawable.refresh), new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(findViewById(R.id.progress).getVisibility() == View.VISIBLE) {
					return;
				}
				findViewById(R.id.progress).setVisibility(View.VISIBLE);
				SystemUtil.queryOrderId(recordItem.orderId.substring(0, 3) + "-" + recordItem.orderId.substring(3), refreshCallback);
			}
		});
		
		String orderId = getIntent().getExtras().getString("orderId");
		for(RecordItem item : ((CargoTraceApp)getApplication()).recordList) {
			if(item.orderId.equals(orderId)) {
				recordItem = item;
				break;
			}
		}
		initControls();
		
		boolean needRefresh = getIntent().getExtras().getBoolean("refresh");
		if(needRefresh) {
			SystemUtil.queryOrderId(orderId.substring(0, 3) + "-" + orderId.substring(3), refreshCallback);
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("menu");// 必须创建一项
        return super.onCreateOptionsMenu(menu);
    }
    
    private SimpleAdapter getMenuAdapter(String[] menuNameArray,
            int[] imageResourceArray) {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < menuNameArray.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImage", imageResourceArray[i]);
            map.put("itemText", menuNameArray[i]);
            data.add(map);
        }
        SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
                R.layout.item_menu, new String[] { "itemImage", "itemText" },
                new int[] { R.id.item_image, R.id.item_text });
        return simperAdapter;
    }
    
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menuDialog == null) {
            menuDialog = new AlertDialog.Builder(this).setView(menuView).show();
        } else {
            menuDialog.show();
        }
        return false;// 返回为true 则显示系统menu
    }
	
	private void initControls(){
		View moreDetail = findViewById(R.id.more_detail);
//		if(((CargoTraceApp)getApplication()).isLogin()) {
//			moreDetail.setVisibility(View.GONE);
//		} else {
			moreDetail.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Builder builder = new AlertDialog.Builder(DetailActivity.this);
					builder.setTitle("登录");
					builder.setMessage("查看详细信息需要登录，是否确定登录？");
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent i = new Intent(DetailActivity.this, LoginActivity.class);
							startActivity(i);
						}
					});
					builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
			});
		//}
			
		View shareView = findViewById(R.id.reply);
		if (menuDialog == null) {
            menuDialog = new AlertDialog.Builder(this).setView(menuView).show();
		}
		shareView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				menuDialog.show();	
			}
		});
		
		View subscribeIcon = findViewById(R.id.subscribe);
		final String key = getSharedPreferences(
				AppConstants.default_preference, Context.MODE_PRIVATE)
				.getString(C2dmReceiver.REGISTRATION_KEY, "");
//		if("".equals(key)) {
//			subscribeIcon.setVisibility(View.GONE);
//		} else {			
			subscribeIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					findViewById(R.id.progress).setVisibility(View.VISIBLE);
					if(recordItem.subscribe) {
						SystemUtil.unSubscribeOrderId(recordItem.orderId.substring(0, 3) + "-" + recordItem.orderId.substring(3), unsubscribeCallback);
					} else {
						SystemUtil.subscribeOrderId(recordItem.orderId.substring(0, 3) + "-" + recordItem.orderId.substring(3), key, subscribeCallback);
					}
				}
			});
		//}
		
		(new Thread(new Runnable() {
			@Override
			public void run() {	
				refreshListView();
			}
		})).start();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		View moreDetail = findViewById(R.id.more_detail);
		if(((CargoTraceApp)getApplication()).isLogin()) {
			moreDetail.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v("rz", "destroy : " +System.currentTimeMillis());
	}
	
	private void refreshListView() {
		final ListView listView = (ListView) findViewById(R.id.list);
		FileInputStream fis;
		try {
			fis = new FileInputStream(getFilesDir() + "/" + recordItem.orderId);
			ObjectInputStream ois = new ObjectInputStream(fis);
			@SuppressWarnings("unchecked")
			final List<String> alarmList = (List<String>)ois.readObject();
			@SuppressWarnings("unchecked")
			final List<DetailItem> detailList = (List<DetailItem>)ois.readObject();
			ois.close();
			final DetailAdapter adapter = new DetailAdapter(this, detailList);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					for(int i = 0; i < listView.getChildCount(); i++) {						
						listView.removeHeaderView(listView.getChildAt(i));
					}
					if(alarmList != null && alarmList.size() > 0) {
						for(String str : alarmList) {
							View v = getLayoutInflater().inflate(R.layout.detail_alarm_item, null);
							TextView content = (TextView)v.findViewById(R.id.content);
							content.setText(str);
							listView.addHeaderView(v);
						}
					}
					setTraceProgress(detailList);
					listView.setAdapter(adapter);
					adapter.notifyDataSetChanged();		

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setTraceProgress(List<DetailItem> detailList) {
		for(DetailItem item : detailList) {
			if ("BKD".equals(item.code) || "FWB".equals(item.code)
					|| "RCS".equals(item.code)
					|| "PRE".equals(item.code)
					|| "MAN".equals(item.code)
					|| "MAN-SPLIT".equals(item.code)) {
				((TextView)findViewById(R.id.depart_text)).setTextColor(0xff009bde);
				findViewById(R.id.depart_bar).setBackgroundColor(0xff009bde);
			} else if("DLV".equals(item.code)) {
				((TextView)findViewById(R.id.arrive_text)).setTextColor(0xff009bde);
				findViewById(R.id.arrive_bar).setBackgroundColor(0xff009bde);						
			} else {
				((TextView)findViewById(R.id.ongoing_text)).setTextColor(0xff009bde);
				findViewById(R.id.ongoing_bar).setBackgroundColor(0xff009bde);
			}
		}
	}
	
	/** 发送邮件 **/
	private int sendMailByIntent()
	{   
        String[] mySbuject = new String[] {"指尖货运"};  
        String mybody = "我正在使用指尖货运Android客户端查询订阅我的运单状态，扫描下面的二维码也可通过微信订阅，还有更多功能等您发现！";  
        Intent myIntent = new Intent(android.content.Intent.ACTION_SEND);  
        myIntent.setType("plain/text");  
        myIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mySbuject);  
        myIntent.putExtra(android.content.Intent.EXTRA_TEXT, mybody);  
        startActivity(Intent.createChooser(myIntent, "mail test"));  
  
        return 1;  
    }

	/** 发送短信 **/
	private void sendMessage()
	{
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.putExtra("sms_body", "分享自指尖货运，微信搜索\"指尖货运\"添加公众账号");
		sendIntent.setType("vnd.android-dir/mms-sms");
		startActivity(Intent.createChooser(sendIntent, "messgae test")); 
	}
	
	/** QQ分享 **/
	private void onClickShareToQQ() 
	{
		final Activity context = DetailActivity.this;
        new GetShareToQQParamsDialog(context,
                new GetShareToQQParamsDialog.ShareToQQParamsListener() {

                    @Override
                    public void onComplete(Bundle params) {
                    	
                    	shareParams = params;
                    	Thread thread = new Thread(shareThread);
            			thread.start();
            			
                    	
                    }
                }).show();
	}
	
	Bundle shareParams = null;;
        
    
    //线程类，该类使用匿名内部类的方式进行声明
    Runnable shareThread = new Runnable(){ 	    	 
    	
		public void run() {	 
			doShareToQQ(shareParams);
		}
    };
    
    private void doShareToQQ(Bundle params)
    {
    	mTencent.shareToQQ(DetailActivity.this, params, new BaseUiListener(){
       	 protected void doComplete(JSONObject values) 
       	 {
            }

            @Override
            public void onError(UiError e) {
            }
            @Override
            public void onCancel() {
            }
       });    	
    }
    
    /** QQ空间分享 **/
    private void onClickAddShare() 
    {
    	Bundle parmas = new Bundle();
        parmas.putString("title", "指尖货运");// 必须。feeds的标题，最长36个中文字，超出部分会被截断。
        parmas.putString("url","http://www.eft.cn");// 必须。分享所在网页资源的链接，点击后跳转至第三方网页，// 请以http://开头。
        parmas.putString("summary", "我正在使用指尖货运Android客户端查询订阅我的运单状态，扫描下面的二维码也可通过微信订阅!");// 所分享的网页资源的摘要内容，或者是网页的概要描述。最长80个中文字，超出部分会被截断。
        parmas.putString("images", "http://img0.ph.126.net/HKZ1Gg7H8k_vL81r_rvcmQ==/1873497445086270291.png");// 所分享的网页资源的代表性图片链接"，请以http://开头，长度限制255字符。多张图片以竖线（|）分隔，目前只有第一张图片有效，图片规格100*100为佳。
        parmas.putString("type", "4");// 分享内容的类型。

        mTencent.requestAsync(Constants.GRAPH_ADD_SHARE, parmas, Constants.HTTP_POST, new BaseApiListener("add_share", true), null);
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          mTencent.onActivityResult(requestCode, resultCode, data);
    }
    
    private class BaseUiListener implements IUiListener
    {
        @Override
        public void onComplete(JSONObject response) 
        {
            doComplete(response);
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e)
        {
        }

        @Override
        public void onCancel() 
        {
        }
    }
    
    private class BaseApiListener implements IRequestListener {
        private String mScope = "all";
        private Boolean mNeedReAuth = false;

        public BaseApiListener(String scope, boolean needReAuth) {
            mScope = scope;
            mNeedReAuth = needReAuth;
        }

        @Override
        public void onComplete(final JSONObject response, Object state) {
            doComplete(response, state);
        }

        protected void doComplete(JSONObject response, Object state) {
            try {
                int ret = response.getInt("ret");
                if (ret == 100030) {
                    if (mNeedReAuth) {
                        Runnable r = new Runnable() {
                            public void run() {
                                mTencent.reAuth(DetailActivity.this, mScope, new BaseUiListener());
                            }
                        };
                        DetailActivity.this.runOnUiThread(r);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("toddtest", response.toString());
            }

        }

        @Override
        public void onIOException(final IOException e, Object state) {
        }

        @Override
        public void onMalformedURLException(final MalformedURLException e,
                Object state) {
        }

        @Override
        public void onJSONException(final JSONException e, Object state) {
        }

        @Override
        public void onConnectTimeoutException(ConnectTimeoutException arg0,
                Object arg1) {

        }

        @Override
        public void onSocketTimeoutException(SocketTimeoutException arg0,
                Object arg1) {
        }

        @Override
        public void onUnknowException(Exception arg0, Object arg1) {
        }

        @Override
        public void onHttpStatusException(HttpStatusException arg0, Object arg1) {
        }

        @Override
        public void onNetworkUnavailableException(NetworkUnavailableException arg0, Object arg1) {
        }
    }
    
    /** 微信分享 **/
    private void onClickShareToWeixin() 
    {
    	Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.send_img);
    	WXWebpageObject ext = new WXWebpageObject();
        ext.webpageUrl = "www.eft.cn";
        
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
		bmp.recycle();
        
		WXMediaMessage msg = new WXMediaMessage();
		msg.title = "指尖货运"; 
    	msg.description = "我正在使用指尖货运Android客户端查询订阅我的运单状态，扫描下面的二维码也可通过微信订阅，还有更多功能等您发现！";
        msg.mediaObject = ext;
		msg.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("appdata");
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
    }
    
    /** 微信朋友圈分享 **/
    private void onClickShareToWeixinGroup() 
    {
    	Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.send_img);
		WXImageObject imgObj = new WXImageObject(bmp);
		
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		msg.title = "指尖货运"; 
    	msg.description = "我正在使用指尖货运Android客户端查询订阅我的运单状态，扫描下面的二维码也可通过微信订阅，还有更多功能等您发现！"; 
		
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
		bmp.recycle();
		msg.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("appdata");
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneTimeline;
		api.sendReq(req);
    }
    
    private byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
    
    private String buildTransaction(final String type) 
    { 
    	return (type == null) ? String.valueOf(System.currentTimeMillis()) :type + System.currentTimeMillis(); 
    }
    
    /** 新浪微博分享 **/
    private void onClickShareToSinaWeibo() 
    {
    	new Thread(new Runnable() {
			@Override
			public void run() {
	    		try{
	        		String urlString = "https://api.weibo.com/oauth2/access_token";
	        		List<NameValuePair> postData = new ArrayList<NameValuePair>();
	        		postData.add(new BasicNameValuePair("client_id", "3675955695"));
	        		postData.add(new BasicNameValuePair("client_secret", "514a242498ae10213fb91b76bebc791f"));
	        		postData.add(new BasicNameValuePair("grant_type", "authorization_code"));
	        		postData.add(new BasicNameValuePair("code", mCodeString));
	        		postData.add(new BasicNameValuePair("redirect_uri", "http://www.ycpai.com"));

	        		String result = HttpManager.syncHttpsPost(urlString, postData);
	        		AccessToken token = new Gson().fromJson(result, AccessToken.class);
	        		
	        		urlString = "https://api.weibo.com/2/statuses/update.json";
	        		postData = new ArrayList<NameValuePair>();
	        		postData.add(new BasicNameValuePair("access_token", token.access_token));
	        		postData.add(new BasicNameValuePair("status", URLEncoder.encode("我在使用Cargotrace，推荐给你们吧:http://www.ycpai.com")));
	        		postData.add(new BasicNameValuePair("visible", "0"));
	        		result = HttpManager.syncHttpsPost(urlString, postData);

	        		JsonObject postRet = new Gson().fromJson(result, JsonObject.class);
	        		if(postRet.get("id") != null) {
	        			Toast.makeText(getApplicationContext(), "分享成功",Toast.LENGTH_LONG).show();
	        		} else {
	        			Toast.makeText(getApplicationContext(), "分享失败",Toast.LENGTH_LONG).show();
	        		}
	    		} catch(Exception e) {
	    			Toast.makeText(getApplicationContext(), "分享失败",Toast.LENGTH_LONG).show();
	    		}
			}
		}).start();
//    	WeiboParameters bundle = new WeiboParameters();
//        bundle.add("pic", null);
//        bundle.add("status", "test");
//        String url = "https://upload.api.weibo.com/2/statuses/upload.json";
//        AsyncWeiboRunner.request(url, bundle, "post", new ShareListener());
    }
    
    class AuthDialogListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
        	mCodeString = values.getString("code");
        	onClickShareToSinaWeibo();
//            String token = values.getString("access_token");
//            String expires_in = values.getString("expires_in");
//            accessToken = new Oauth2AccessToken(token, expires_in);
//            if (accessToken.isSessionValid()) {
//                AccessTokenKeeper.keepAccessToken(DetailActivity.this,
//                        accessToken);
//                Toast.makeText(DetailActivity.this, "认证成功", Toast.LENGTH_SHORT)
//                        .show();
//            }
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
    
    class ShareListener implements RequestListener
    {
		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onComplete4binary(ByteArrayOutputStream arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			
		}
    } 
    
    /**腾讯微博分享 **/
//    private void onClickShareToTCWeibo() 
//    {
//    	TC_AccessToken = Util.getSharePersistent(getApplicationContext(),"ACCESS_TOKEN");
//		AccountModel account = new AccountModel(TC_AccessToken);
//		mTCWeibo = new WeiboAPI(account);
//		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.send_img);
//		//mTCWeibo.addWeibo(DetailActivity.this, "test", "json", 0, 0, 0, 0, this, null, BaseVO.TYPE_JSON);
//		mTCWeibo.addPic(DetailActivity.this, "我正在使用指尖货运Android客户端查询订阅我的运单状态，扫描下面的二维码也可通过微信订阅，还有更多功能等您发现！", "json", 0, 0, bmp, 0, 0, this, null,BaseVO.TYPE_JSON);
//    }
//    
//    @Override
//	public void onResult(Object object)
//    {
//    	if (object != null) {
//			ModelResult result = (ModelResult) object;
//			if (result.isExpires()) {
//				Toast.makeText(DetailActivity.this,
//						result.getError_message(), Toast.LENGTH_SHORT)
//						.show();
//			} else {
//				if (result.isSuccess()) {
//					Toast.makeText(DetailActivity.this, "发送成功", 4000)
//							.show();
//					Log.d("发送成功", object.toString());
//				} else {
//					Toast.makeText(DetailActivity.this,
//							((ModelResult) object).getError_message(), 4000)
//							.show();
//				}
//			}
//
//		}
//	}
//    
//    private void auth(long appid, String app_secket) {
//		final Context context = this.getApplicationContext();
//		
//		AuthHelper.register(this, appid, app_secket, new OnAuthListener() {
//
//			@Override
//			public void onWeiBoNotInstalled() {
//				Intent i = new Intent(DetailActivity.this,Authorize.class);
//				startActivity(i);
//			}
//
//			@Override
//			public void onWeiboVersionMisMatch() {
//				Toast.makeText(DetailActivity.this, "onWeiboVersionMisMatch",
//						1000).show();
//				Intent i = new Intent(DetailActivity.this,Authorize.class);
//				startActivity(i);
//			}
//
//			@Override
//			public void onAuthFail(int result, String err) {
//				Toast.makeText(DetailActivity.this, "result : " + result, 1000)
//						.show();
//			}
//
//			@Override
//			public void onAuthPassed(String name, WeiboToken token) {
//				Util.saveSharePersistent(context, "ACCESS_TOKEN", token.accessToken);
//				Util.saveSharePersistent(context, "EXPIRES_IN", String.valueOf(token.expiresIn));
//				Util.saveSharePersistent(context, "OPEN_ID", token.openID);
//				Util.saveSharePersistent(context, "REFRESH_TOKEN", "");
//				Util.saveSharePersistent(context, "CLIENT_ID", Util.getConfig().getProperty("APP_KEY"));
//				Util.saveSharePersistent(context, "AUTHORIZETIME",String.valueOf(System.currentTimeMillis() / 1000l));
//			}
//		});
//
//		AuthHelper.auth(this, "");
//	}
}
