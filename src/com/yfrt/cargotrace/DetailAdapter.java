package com.yfrt.cargotrace;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DetailAdapter extends BaseAdapter{

	enum TYPE {
		SEPERATE,
		DETAIL
	}
	private Context context;
	private List<Pair<TYPE, DetailItem>> mList = new ArrayList<Pair<TYPE,DetailItem>>();
	public DetailAdapter(Context c, List<DetailItem> list) {
		context = c;
		parseList(list);
	}
	private void parseList(List<DetailItem> list) {
		String location = "";
		String date = "";
		for(DetailItem item : list) {
			if(!item.location.equals(location) || !item.date.equals(date)) {
				location = item.location;
				date = item.date;
				mList.add(new Pair<TYPE, DetailItem>(TYPE.SEPERATE, item));
			}
			mList.add(new Pair<TYPE, DetailItem>(TYPE.DETAIL, item));
		}
	}
	@Override
	public int getCount() {
		if(mList == null){
			return 0;
		} else {
			return mList.size();
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
    public int getItemViewType(int position) {
        return mList.get(position).first == TYPE.SEPERATE ? 0 : 1;
    }
    
	@Override
    public int getViewTypeCount() {
        return 2;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(mList.get(position).first == TYPE.SEPERATE) {
			if(convertView == null) {
				convertView = View.inflate(context, R.layout.detail_location_item, null);
			}				
			DetailItem item = mList.get(position).second;
			TextView desc = (TextView)convertView.findViewById(R.id.desc);
			desc.setText(item.location + " " + item.date);
		} else {
			if(convertView == null) {
				convertView = View.inflate(context, R.layout.detail_item, null);
			}
			
			DetailItem item = mList.get(position).second;
			
			TextView state = (TextView)convertView.findViewById(R.id.state);
			state.setText(item.code);
			
			TextView tv1 = (TextView)convertView.findViewById(R.id.desc1);
			String orderId = item.orderId;
			orderId = orderId.substring(0, 3) + "-" + orderId.substring(3, 7) + " " + orderId.substring(7);
			tv1.setText(orderId);
			
			TextView tv2 = (TextView)convertView.findViewById(R.id.desc2);
			tv2.setText(item.from + "--" + item.to);
			
			TextView tv3 = (TextView)convertView.findViewById(R.id.desc3);
			tv3.setText(item.curState);
		}
		
		if(mList.get(position).first == TYPE.SEPERATE) {
			View v = convertView.findViewById(R.id.upLine);
			v.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
		}
		
		return convertView;
	}
}
