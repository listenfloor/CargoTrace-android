package com.yfrt.cargotrace;

import java.io.Serializable;
import java.util.List;

public class RecordItem implements Serializable{

	private static final long serialVersionUID = -6404514869370769544L;
	
	public String orderId = null;
	public boolean subscribe = false;
	public int state = 0; // 0 normal, 1 alert, 2 history
	public String code;
	public String from;
	public String to;
	public String occurTime;
	public String curState;
	//public String traceDetail;
}
