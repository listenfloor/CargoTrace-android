package com.yfrt.cargotrace;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CompanyListAdapter extends BaseAdapter {

	private Context context;
	private List<AirCompany> companyList;
	
	public CompanyListAdapter(Context c) {
		context = c;
	}

	public void setCompanyList(List<AirCompany> source) {
		companyList = source;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return companyList.size();
	}

	@Override
	public Object getItem(int position) {
		return companyList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			View v = View.inflate(context, R.layout.logo_item, null);
			convertView = v;
		}
		View logo = convertView.findViewById(R.id.logo);
		logo.setBackgroundDrawable(companyList.get(position).icon);
		
		return convertView;
	}

}
