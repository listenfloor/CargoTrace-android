package com.yfrt.cargotrace;

import java.util.ArrayList;
import java.util.List;


import com.yfrt.cargotrace.HttpManager.HttpQueryCallback;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class SubscribeActivity extends CommonTitleBarActivity{

	private List<RecordItem> recordList;

	private SubscribeListAdapter subscribeListAdapter;
	
	private ListView listView;
	
	private Button btnAll;
	private Button btnAlert;
	private Button btnNormal;
	private Button btnHistory;
	
	private HttpQueryCallback httpCallback = new HttpQueryCallback() {
		@Override
		public void onQueryComplete(int state, String result) {
			if(state == HttpQueryCallback.STATE_OK) {
			} else {
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_subscribe);
		
		setCustomTitle(getString(R.string.subscribe_title));
		setCustomTitleTextColor(getResources().getColor(R.color.text_white));
		
		setLeftButton(getResources().getDrawable(R.drawable.back), new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(SubscribeActivity.this, MainActivity.class);
				startActivity(i);
				finish();
			}
		});
		
		findViewById(R.id.bottom_search_btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(SubscribeActivity.this, SearchActivity.class);
				startActivity(i);
				finish();
			}
		});

//		recordList = Util.readRecord((CargoTraceApp)getApplication());
		recordList = ((CargoTraceApp)getApplication()).recordList;
		subscribeListAdapter = new SubscribeListAdapter(this,  recordList);

		listView = (ListView)findViewById(R.id.list);
		listView.setAdapter(subscribeListAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent i = new Intent(SubscribeActivity.this, DetailActivity.class);
				i.putExtra("orderId", recordList.get(position).orderId);
				if("DLV".equals(recordList.get(position).code)) {
					i.putExtra("refresh", false);
				} else {					
					i.putExtra("refresh", true);
				}
				i.putExtra("subscribe", recordList.get(position).subscribe);
				startActivity(i);
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
}
