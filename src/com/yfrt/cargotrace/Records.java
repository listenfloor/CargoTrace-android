package com.yfrt.cargotrace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Records implements Serializable{

	private static final long serialVersionUID = -6404514869370769544L;
	
	public List<RecordItem> recordList = new ArrayList<RecordItem>();
}
