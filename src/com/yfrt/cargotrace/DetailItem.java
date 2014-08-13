package com.yfrt.cargotrace;

import java.io.Serializable;

public class DetailItem implements Serializable{
	private static final long serialVersionUID = -3393018398890237832L;
	
	public String orderId;
	public String code;
	public String location;
	public String date;
	public String from;
	public String to;
	public String curState;
}
