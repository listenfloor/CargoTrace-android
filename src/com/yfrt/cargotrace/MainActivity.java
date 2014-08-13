package com.yfrt.cargotrace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.yfrt.cargotrace.HttpManager.HttpQueryCallback;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private List<AirCompany> airCompanyList = new ArrayList<AirCompany>();

	private CompanyListAdapter companyListAdapter;
	private SideBarView sideBarView;
	private boolean isShow = false;
	private String orderIdString;
	private Handler handler = new Handler();
	private View progress;
	private SubscribeListAdapter subscribeListAdapter;
	private List<RecordItem> recordList;
	
	private ListView subscribeListView;
	
	private Button btnAll;
	private Button btnAlert;
	private Button btnNormal;
	private Button btnHistory;
	
	private HttpQueryCallback httpCallback = new HttpQueryCallback() {
		@Override
		public void onQueryComplete(final int state, final String result) {
			if(state == HttpQueryCallback.STATE_OK) {
				RecordItem item = new RecordItem();
				item.orderId = orderIdString;
				SystemUtil.parseRecord(MainActivity.this, item, result);
				SystemUtil.addRecord((CargoTraceApp)getApplication(), item);
				
				finish();
						
				Intent i = new Intent(MainActivity.this, DetailActivity.class);
				i.putExtra("orderId", orderIdString);
				i.putExtra("refresh", false);
				i.putExtra("subscribe", false);
				startActivity(i);
			} else {
				SystemUtil.showToast(MainActivity.this, getString(R.string.query_order_error), Toast.LENGTH_SHORT);
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progress.setVisibility(View.GONE);
				}
			});
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(isShow) {
				sideBarView.hideSideBar();
				isShow = !isShow;
			} else {
				finish();
			}
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		View main = View.inflate(this, R.layout.activity_main, null);
		View sideBar = View.inflate(this, R.layout.setting_layout, null);
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		sideBarView = new SideBarView(this);
		sideBarView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		sideBarView.init(main, sideBar, metrics);
		
		setContentView(sideBarView);
		
		progress = findViewById(R.id.progress);
		
		initMainPage();
		initSettingPage();
	}
	
	private void initMainPage() {
		TextView title = (TextView)findViewById(R.id.title);
		title.setText("指尖货运");
		title.setTextColor(Color.WHITE);
		
		// setting button
		Button leftTitleBtn = (Button)findViewById(R.id.left);
		leftTitleBtn.setBackgroundResource(R.drawable.setting);
		leftTitleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isShow) {
					sideBarView.hideSideBar();
				} else {
					sideBarView.showSideBar();
					refreshSettingView();
				}
				isShow = !isShow;
			}
		});
		
		// search button
		Button search = (Button)findViewById(R.id.search_btn);
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText orderEdit = (EditText)findViewById(R.id.cargo_id);
				orderIdString = orderEdit.getText().toString(); 
				if(SystemUtil.checkOrderId(orderIdString)) {
					progress.setVisibility(View.VISIBLE);
					SystemUtil.queryOrderId(orderIdString.substring(0, 3) + "-" + orderIdString.substring(3), httpCallback);
				} else {
					Toast.makeText(MainActivity.this, getString(R.string.order_id_error), Toast.LENGTH_SHORT).show();
				}
			}
		});
		//bottom search button
		Button bottomSearch = (Button)findViewById(R.id.bottom_search_btn);
		bottomSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				
				Intent i = new Intent(MainActivity.this, SearchActivity.class);
				startActivity(i);
			}
		});
		
		CargoTraceApp app = (CargoTraceApp)getApplication();
		if(app.hasRecord()) {
			initSubscribe();
		} else {
			initAirCompany();
		}
	}
	private void initAirCompany() {
		findViewById(R.id.air_company).setVisibility(View.VISIBLE);
		findViewById(R.id.subscribe).setVisibility(View.GONE);		

		String filePath = getFilesDir() + AppConstants.record_air_company;
		File air = new File(filePath);
		if (!air.exists()) {
			progress.setVisibility(View.VISIBLE);
			new Thread(new Runnable() {
				@Override
				public void run() {
					getAirCompany();
					downloadLogo();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							loadLogos();
							progress.setVisibility(View.GONE);							
						}
					});
				}
			}).start();
		} else {
			FileInputStream fis;
			String airCompany = "";
			try {
				fis = new FileInputStream(air);
				byte[] data = new byte[fis.available()];
				while(fis.read(data) != -1) {
					airCompany += new String(data);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			parseAirCompany(airCompany);
			loadLogos();
		}
	}
	private void initSubscribe() {
		findViewById(R.id.air_company).setVisibility(View.GONE);
		findViewById(R.id.subscribe).setVisibility(View.VISIBLE);
		
		recordList = ((CargoTraceApp)getApplication()).recordList;
		subscribeListAdapter = new SubscribeListAdapter(this, recordList);
		
		subscribeListView = (ListView)findViewById(R.id.list);
		subscribeListView.setAdapter(subscribeListAdapter);
		subscribeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent i = new Intent(MainActivity.this, DetailActivity.class);
				i.putExtra("orderId", recordList.get(position).orderId);
				i.putExtra("refresh", true);
				i.putExtra("subscribe", recordList.get(position).subscribe);
				startActivity(i);
				
				finish();
			}
		});
		
		btnAll = (Button)findViewById(R.id.all);
		btnAll.setBackgroundResource(R.drawable.subscribe_title_bg_select);
		btnAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnAll.setBackgroundResource(R.drawable.subscribe_title_bg_select);
				btnAlert.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				btnNormal.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				btnHistory.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				subscribeListAdapter.setRecordList(SystemUtil.filerRecord(recordList, SystemUtil.RecordFilter.ALL));
				subscribeListAdapter.notifyDataSetChanged();
			}
		});
		btnAlert = (Button)findViewById(R.id.alert);
		btnAlert.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnAll.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				btnAlert.setBackgroundResource(R.drawable.subscribe_title_bg_select);
				btnNormal.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				btnHistory.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				subscribeListAdapter.setRecordList(SystemUtil.filerRecord(recordList, SystemUtil.RecordFilter.ALERT));
				subscribeListAdapter.notifyDataSetChanged();

			}
		});
		btnNormal = (Button)findViewById(R.id.normal);
		btnNormal.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnAll.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				btnAlert.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				btnNormal.setBackgroundResource(R.drawable.subscribe_title_bg_select);
				btnHistory.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				subscribeListAdapter.setRecordList(SystemUtil.filerRecord(recordList, SystemUtil.RecordFilter.NORMAL));
				subscribeListAdapter.notifyDataSetChanged();

			}
		});
		btnHistory = (Button)findViewById(R.id.history);
		btnHistory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnAll.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				btnAlert.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				btnNormal.setBackgroundResource(R.drawable.subscribe_title_btn_bg_selector);
				btnHistory.setBackgroundResource(R.drawable.subscribe_title_bg_select);
				subscribeListAdapter.setRecordList(SystemUtil.filerRecord(recordList, SystemUtil.RecordFilter.HISTORY));
				subscribeListAdapter.notifyDataSetChanged();

			}
		});
	}

	private void initSettingPage() {
		OnClickListener l = new OnClickListener() {
			@Override
			public void onClick(View v) {
				sideBarView.hideSideBar();
				isShow = !isShow;
			}
		};
		findViewById(R.id.home).setOnClickListener(l);
		findViewById(R.id.homeIcon).setOnClickListener(l);
		findViewById(R.id.homeBtn).setOnClickListener(l);
		
		l = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
				if(!((CargoTraceApp)getApplication()).canPush) {
					SystemUtil.showToast(MainActivity.this, getResources().getString(R.string.push_not_support), Toast.LENGTH_SHORT);
				}
			}
		};
		findViewById(R.id.subscribe).setOnClickListener(l);
		findViewById(R.id.subscribeIcon).setOnClickListener(l);
		findViewById(R.id.subscribeBtn).setOnClickListener(l);
		
		l = new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, SupportActivity.class));
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						sideBarView.hideSideBar();
						isShow = !isShow;						
					}
				}, 500);
			}
		};
		findViewById(R.id.support).setOnClickListener(l);
		findViewById(R.id.supportIcon).setOnClickListener(l);
		findViewById(R.id.supportBtn).setOnClickListener(l);
		
		l = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(((CargoTraceApp)getApplication()).isLogin()) {
					((CargoTraceApp)getApplication()).saveLogoutState();
					((TextView)findViewById(R.id.loginBtn)).setText(R.string.login);
					Toast.makeText(MainActivity.this, "注销成功", Toast.LENGTH_SHORT).show();
				} else {
					startActivity(new Intent(MainActivity.this, LoginActivity.class));
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							sideBarView.hideSideBar();
							isShow = !isShow;							
						}
					}, 500);
				}
				
			}
		};
		findViewById(R.id.login).setOnClickListener(l);
		findViewById(R.id.loginIcon).setOnClickListener(l);
		findViewById(R.id.loginBtn).setOnClickListener(l);
		
	}
	
	private void loadLogos() {
		File dir = new File(this.getFilesDir() + "/logo");
		for(AirCompany company : airCompanyList) {
			String logoPath = dir + company.logourl.substring(company.logourl.lastIndexOf('/'));
			Bitmap bitmap = BitmapFactory.decodeFile(logoPath);
			company.icon = new BitmapDrawable(bitmap);
		}
		
		GridView grid = (GridView)findViewById(R.id.logos);
		
		companyListAdapter = new CompanyListAdapter(this);
		companyListAdapter.setCompanyList(airCompanyList);
		
		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(MainActivity.this, SearchActivity.class);
				i.putExtra(AppConstants.air_company_id, airCompanyList.get(arg2).ac_code3c);
				startActivity(i);
			}
		});
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		grid.setNumColumns(3);
		grid.setColumnWidth(metrics.widthPixels / 3);
		grid.setPadding(0, 0, 0, 0);
		grid.setAdapter(companyListAdapter);
		grid.invalidate();
	}
	
	private void downloadLogo() {
		File destDir = new File(this.getFilesDir() + "/logo");
		if(!destDir.exists()) {
			destDir.mkdir();
		}
		for (AirCompany company : airCompanyList) {
			FileOutputStream fOut = null;
			try {
				HttpGet httpGet = new HttpGet(company.logourl);
				HttpResponse response = new DefaultHttpClient().execute(httpGet);
				if(response.getStatusLine().getStatusCode() == 200) {
					byte[] data = EntityUtils.toByteArray(response.getEntity());
					
					String imageName = company.logourl.substring(company.logourl.lastIndexOf('/'));
					fOut = new FileOutputStream(destDir + imageName);
					fOut.write(data);
					fOut.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void saveAirCompanyList(String info) {
		byte[] data = info.getBytes();
		String filePath = getFilesDir() + AppConstants.record_air_company;
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(filePath);
			fOut.write(data);
			fOut.close();		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getAirCompany() {
		String requestString = "<Service><ServiceURL>TraceTranslate</ServiceURL><ServiceAction>querySupportAirCompany</ServiceAction><ServiceData></ServiceData></Service>";
		String result = HttpManager.syncQuery(requestString);
		if(result != null) {
			saveAirCompanyList(result);		
			parseAirCompany(result);
		} else {
			SystemUtil.showToast(this, getString(R.string.query_air_company_err), Toast.LENGTH_SHORT);
		}
	}
	
	private void parseAirCompany(String src) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder(); 
	         if (src != null) {  
	                 Pattern p = Pattern.compile("\\s*|\t|\r|\n");  
	                 Matcher m = p.matcher(src);  
	                 src = m.replaceAll("");  
	         } 
			InputStream is = new ByteArrayInputStream(src.getBytes("UTF-8"));
			Document doc = builder.parse(is);
			
			Element root = doc.getDocumentElement();
			
			NodeList nodeList = root.getChildNodes();
			if(nodeList.getLength() == 3) {
				if("1".equals(nodeList.item(0).getTextContent())) {
					NodeList airCompanyNodeList = nodeList.item(2).getFirstChild().getChildNodes();
					for(int i = 0; i < airCompanyNodeList.getLength(); i++) {
						AirCompany company = new AirCompany();
						NodeList children = airCompanyNodeList.item(i).getChildNodes();
						if(children.getLength() == 6) {
							company.ac_code2c 		= children.item(0).getTextContent();
							company.ac_code3c 		= children.item(1).getTextContent();
							company.cnname 			= children.item(2).getTextContent();
							company.cnname_short 	= children.item(3).getTextContent();
							company.enname 			= children.item(4).getTextContent();
//							company.logourl 		= children.item(5).getTextContent();
							company.logourl 		= "http://efreight.cn/airline.logo/" + company.ac_code2c + ".logo.png";
							airCompanyList.add(company);
						}
					}
				} else {
					Toast.makeText(MainActivity.this, R.string.query_air_company_err, Toast.LENGTH_SHORT).show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void refreshSettingView() {
		if(((CargoTraceApp)getApplication()).canPush) {
			TextView tv = (TextView)findViewById(R.id.subscribeBtn);
			tv.setText(R.string.start_alert);
			List<RecordItem> items = ((CargoTraceApp)getApplication()).recordList;
			for(RecordItem item : items) {
				if(item.subscribe == true) {
					tv.setText(R.string.close_alert);
					break;
				}
			}
		}
		
		if(((CargoTraceApp)getApplication()).isLogin()) {
			((TextView)findViewById(R.id.loginBtn)).setText(R.string.logout);
		} else {
			((TextView)findViewById(R.id.loginBtn)).setText(R.string.login);
		}
	}
}
