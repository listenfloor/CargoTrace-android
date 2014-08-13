package com.yfrt.cargotrace;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SubscribeListAdapter extends BaseAdapter{

	private Context context;
	private List<RecordItem> mList;
	public SubscribeListAdapter(Context c, List<RecordItem> list) {
		context = c;
		mList = list;
	}
	public void setRecordList(List<RecordItem> list) {
		mList = list;
	}
	@Override
	public int getCount() {
		if(mList != null) {
			return mList.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = View.inflate(context, R.layout.cargo_item, null);
		}
			RecordItem item = mList.get(position);
			TextView state = (TextView)convertView.findViewById(R.id.state);
			state.setText(item.code);
			
			TextView tv1 = (TextView)convertView.findViewById(R.id.desc1);
			String orderId = item.orderId;
			orderId = orderId.substring(0, 3) + "-" + orderId.substring(3, 7) + " " + orderId.substring(7);
			tv1.setText(orderId);
			
			TextView tv2 = (TextView)convertView.findViewById(R.id.desc2);
			tv2.setText(item.from + "--" + item.to + "  " + item.occurTime);

			TextView tv3 = (TextView)convertView.findViewById(R.id.desc3);
			tv3.setText(item.curState);
		
		return convertView;
	}
}
