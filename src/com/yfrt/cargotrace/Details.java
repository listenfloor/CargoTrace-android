package com.yfrt.cargotrace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Details implements Serializable{

	private static final long serialVersionUID = -2335556100390777512L;

	public List<DetailItem> detailList = new ArrayList<DetailItem>();
}
