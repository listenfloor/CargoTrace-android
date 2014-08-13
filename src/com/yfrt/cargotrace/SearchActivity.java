package com.yfrt.cargotrace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.yfrt.cargotrace.HttpManager.HttpQueryCallback;

import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends CommonTitleBarActivity{

	private EditText company;
	private EditText orderId;
	private String orderIdString;
	private View progress;
	private Handler handler = new Handler();
	private List<AirCompany> airCompanyList = new ArrayList<AirCompany>();
	
	private HttpQueryCallback httpCallback = new HttpQueryCallback() {
		@Override
		public void onQueryComplete(int state, String result) {
			if(state == HttpQueryCallback.STATE_OK) {
				RecordItem item = new RecordItem();
				item.orderId = orderIdString;
				SystemUtil.parseRecord(SearchActivity.this, item, result);
				SystemUtil.addRecord((CargoTraceApp)getApplication(), item);
				handler.post(new Runnable() {
					@Override
					public void run() {
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(orderId,InputMethodManager.HIDE_IMPLICIT_ONLY);
						imm.hideSoftInputFromWindow(orderId.getWindowToken(), 0);
						finish();
					}
				});
				
				Intent i = new Intent(SearchActivity.this, DetailActivity.class);
				i.putExtra("orderId", orderIdString);
				i.putExtra("refresh", false);
				i.putExtra("subscribe", false);
				startActivity(i);
			} else {
				runOnUiThread(new Runnable() {
					public void run() {	
						Toast.makeText(SearchActivity.this, getString(R.string.query_order_error), Toast.LENGTH_SHORT).show();
					}
				});
			}
			runOnUiThread(new Runnable() {
				public void run() {
					progress.setVisibility(View.GONE);	
				}
			});
		}
	};
	
	private void initCompanyList() {
		String filePath = getFilesDir() + AppConstants.record_air_company;
		File air = new File(filePath);
		if (air.exists()) {
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
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		initCompanyList();
		
		setCustomTitle(getString(R.string.search_title));
		setCustomTitleTextColor(getResources().getColor(R.color.text_white));
		
		setLeftButton(getResources().getDrawable(R.drawable.back), new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				CargoTraceApp app = (CargoTraceApp)getApplication();
				if(app.hasRecord()) {
					Intent i = new Intent(SearchActivity.this, SubscribeActivity.class);
					startActivity(i);
				} else {
					Intent i = new Intent(SearchActivity.this, MainActivity.class);
					startActivity(i);
				}
			}
		});

		getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		
		company = (EditText) findViewById(R.id.company_code);
		orderId = (EditText) findViewById(R.id.order_id);
		final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		String airCompanyId = null;
		if(getIntent().getExtras() != null) {
			airCompanyId = getIntent().getExtras().getString(AppConstants.air_company_id);
		}
		if(airCompanyId != null && !"".equals(airCompanyId)) {
			company.setText(airCompanyId);
			showCompanyName();
			orderId.setText("");
			orderId.requestFocusFromTouch();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					imm.showSoftInput(orderId,InputMethodManager.SHOW_FORCED);					
				}
			}, 600);
		} else {
			company.requestFocusFromTouch();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					imm.showSoftInput(company,InputMethodManager.SHOW_FORCED);					
				}
			}, 600);

		}
		
		orderId.setKeyListener(new NumberKeyListener() {
			public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_DEL && orderId.getText().toString().length() == 0) {
					String companyName = company.getText().toString();
					if(companyName != null && companyName.length() > 1) {
						companyName = companyName.substring(0, companyName.length() - 1);
						company.setText(companyName);
					}
					company.requestFocus();
					company.setSelection(company.getText().toString().length());
					return true;
				}
				return super.onKeyDown(view, content, keyCode, event);				
			}
			@Override
			public int getInputType() {
				// TODO Auto-generated method stub
				return InputType.TYPE_CLASS_NUMBER;
			}
			
			@Override
			protected char[] getAcceptedChars() {
				char numberChars[] ={'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
		        return numberChars;
			}
		});
		
		company.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().length() == 3) {
					orderId.requestFocus();
					showCompanyName();
				} else {
					findViewById(R.id.company_name).setVisibility(View.GONE);
				}
			}
		});
		
		
		// search button
		Button search = (Button)findViewById(R.id.search_btn);
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				orderIdString = company.getText().toString() + orderId.getText().toString(); 
				if(SystemUtil.checkOrderId(orderIdString)) {
					progress.setVisibility(View.VISIBLE);
					SystemUtil.queryOrderId(orderIdString.substring(0, 3) + "-" + orderIdString.substring(3), httpCallback);
				} else {
					Toast.makeText(SearchActivity.this, getString(R.string.order_id_error), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		progress = findViewById(R.id.progress);
	}
	
	private void showCompanyName() {
		findViewById(R.id.company_name).setVisibility(View.VISIBLE);
		for (int i = 0; i < this.airCompanyList.size(); i++) {
			AirCompany com = airCompanyList.get(i);
			if(company.getText().toString() != null && company.getText().toString().equals(com.ac_code3c)) {
				((TextView)findViewById(R.id.company_name)).setText(com.cnname);
				return;
			}
		}
		((TextView)findViewById(R.id.company_name)).setText(R.string.air_company_not_support);
	}
}
