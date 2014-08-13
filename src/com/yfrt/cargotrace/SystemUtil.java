package com.yfrt.cargotrace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.yfrt.cargotrace.HttpManager.HttpQueryCallback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.style.BulletSpan;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

public class SystemUtil{
	private static Object recordLock = new Object();
	
	public static boolean checkOrderId(String orderIdString) {
		if (orderIdString.length() != AppConstants.order_id_length) {
			return false;
		}
		int verify = Integer.parseInt(orderIdString.substring(3,
				orderIdString.length() - 1));
		if (verify % AppConstants.order_id_verify_mod != Integer
				.parseInt(orderIdString.substring(orderIdString.length() - 1))) {
			return false;
		}
		return true;
	}
	
	public static void queryOrderId(String orderIdString, HttpQueryCallback callback) {
		String postData = "<eFreightService><ServiceURL>Subscribe</ServiceURL><ServiceAction>TRANSACTION</ServiceAction><ServiceData><Subscribe><type>traceAndAlarm</type><target>"
				+ orderIdString
				+ "</target><targettype>MAWB</targettype><sync>N</sync><subscriber>none</subscriber><subscribertype>NONE</subscribertype><standard_type>3</standard_type><limit_num>-1</limit_num><systime>2013-01-01 00:00:00</systime><offflag/></Subscribe></ServiceData></eFreightService>";
		HttpManager.asyncQuery(postData, new WeakReference<HttpQueryCallback>(callback));
	}
	
	public static void subscribeOrderId(String orderIdString, String key, HttpQueryCallback callback) {
		String postData = "<eFreightService><ServiceURL>Subscribe</ServiceURL><ServiceAction>TRANSACTION</ServiceAction><ServiceData><Subscribe><type>traceAndAlarm</type><target>"
				+ orderIdString
				+ "</target><targettype>MAWB</targettype><sync>N</sync><subscriber>"
				+ key
				+ "</subscriber><subscribertype>ANDROID</subscribertype><standard_type>3</standard_type><limit_num>-1</limit_num><systime>2013-01-01 00:00:00</systime><offflag/></Subscribe></ServiceData></eFreightService>";
		HttpManager.asyncQuery(postData, new WeakReference<HttpQueryCallback>(callback));
	}
	public static void unSubscribeOrderId(String orderIdString, HttpQueryCallback callback) {
		String postData = "<eFreightService><ServiceURL>Subscribe</ServiceURL><ServiceAction>TRANSACTION</ServiceAction><ServiceData><Subscribe><type>traceAndAlarm</type><target>"
				+ orderIdString
				+ "</target><targettype>MAWB</targettype><sync>N</sync><subscriber></subscriber><subscribertype>NONE</subscribertype><standard_type>3</standard_type><limit_num>-1</limit_num><systime>2013-01-01 00:00:00</systime><offflag/></Subscribe></ServiceData></eFreightService>";
		HttpManager.asyncQuery(postData, new WeakReference<HttpQueryCallback>(callback));
	}
	
	public static void getPublicKey(HttpQueryCallback callback) {
		String urlString = "http://emall.efreight.cn/eFreightHttpEngine?getKey=true";
		HttpManager.asyncQuery(urlString, null, new WeakReference<HttpManager.HttpQueryCallback>(callback));
	}
	
	public static String login(String user, String password, String publicKey, HttpQueryCallback callback) {
		String loginData = "<eFreightService><ServiceURL>eFreightUser</ServiceURL><ServiceAction>login</ServiceAction><ServiceData><eFreightUser><username>"
				+ user
				+ "</username><password>"
				+ password
				+ "</password></eFreightUser></ServiceData></eFreightService>";
		
		try {
			loginData = new String(RSA.encryptByPublicKey(loginData.getBytes(), publicKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return HttpManager.syncQuery(AppConstants.login_url, loginData, true);
	}
	
	public static void register(String userName, String password, HttpQueryCallback callback) {
		String dateString = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		String postData = "<eFreightService><ServiceURL>eFreightUser</ServiceURL><ServiceAction>register</ServiceAction><ServiceData><eFreightUser><username>"
				+ userName
				+ "</username><password>"
				+ password
				+ "</password><name>昵称</name><companyname>公司名</companyname><telephone>固定电话</telephone><mobile>手机</mobile><email>邮箱</email><title>简介</title><address>地址</address><handler></handler><createtime>"
				+ dateString
				+ "</createtime><modifytime>"
				+ dateString
				+ "</modifytime></eFreightUser></ServiceData></eFreightService>";
		HttpManager.asyncQuery(AppConstants.login_url, postData, new WeakReference<HttpManager.HttpQueryCallback>(callback));
	}
	

	public static void updateRecord(CargoTraceApp app, RecordItem item) {
		synchronized (recordLock) {
			
		}
	}
	public static void addRecord(CargoTraceApp app, RecordItem item) {
		synchronized (recordLock) {
			List<RecordItem> tempList = null; 
			if(app.recordList != null) {
				tempList =  new ArrayList<RecordItem>(app.recordList);
			} else {
				tempList =  new ArrayList<RecordItem>();
			}
			for(RecordItem tempItem : tempList) {
				if(tempItem.orderId.equals(item.orderId)) {
					tempList.remove(tempItem);
					break;
				}
			}
			tempList.add(0, item);
			app.recordList = tempList;

			saveRecord(app);
		}
	}
	
	public static void saveRecord(CargoTraceApp app) {
		synchronized (recordLock) {
			File record = new File(app.recordFile);
			try {
				FileOutputStream fos = new FileOutputStream(record);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(app.recordList);
				oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static List<RecordItem> readRecord(String filePath) {
		synchronized (recordLock) {
			List<RecordItem> records = null;
			File record = new File(filePath);
			try {
				FileInputStream fis = new FileInputStream(record);
				ObjectInputStream ois = new ObjectInputStream(fis);
				records = (List<RecordItem>)ois.readObject();
				ois.close();
			} catch (Exception e) {
				record.delete();
				records = new ArrayList<RecordItem>();
				e.printStackTrace();
			}
			return records;
		}
	}
	public static void parseRecord(Context context, RecordItem item, String detail) {
		Element root = SystemUtil.getDocumentElement(detail);
		
		NodeList nodes = root.getElementsByTagName("origin_airport");
		if(nodes != null && nodes.getLength() > 0) {
			item.from = nodes.item(0).getTextContent();
		}
		nodes = root.getElementsByTagName("destination_airport");
		if(nodes != null && nodes.getLength() > 0) {
			item.to = nodes.item(0).getTextContent();								
		}
		nodes = root.getElementsByTagName("OP_TIME");
		if(nodes != null && nodes.getLength() > 0) {
			item.occurTime = nodes.item(nodes.getLength() - 1).getTextContent();
		}
		nodes = root.getElementsByTagName("STARDARD_DATA");
		if(nodes != null && nodes.getLength() > 0) {
			item.curState = nodes.item(nodes.getLength() - 1).getTextContent();
		}
		// 判断是否有预警<FreightAlarm><alarmcontent>
		List<String> alarmList = new ArrayList<String>();
		nodes = root.getElementsByTagName("alarmcontent");
		if(nodes != null && nodes.getLength() > 0) {
			item.state = 1;
			for(int i = 0; i < nodes.getLength(); i++) {
				alarmList.add(nodes.item(i).getTextContent());
			}
		}
		
		nodes = root.getElementsByTagName("CARGO_CODE");
		if(nodes != null && nodes.getLength() > 0) {
			item.code = nodes.item(nodes.getLength() - 1).getTextContent();
		}
		// 判断是否完成
		for(int i = 0; i < nodes.getLength(); i++) {
			if("DLV".equals(nodes.item(i).getTextContent())) {
				item.state = 2; // finish
				break;
			}
		}

		List<DetailItem> detailList = new ArrayList<DetailItem>();
		NodeList traceNodes = root.getElementsByTagName("TraceTranslate");
		for(int i = traceNodes.getLength() - 1; i >= 0; i--) {
			DetailItem detailItem = new DetailItem();
			NodeList children = traceNodes.item(i).getChildNodes();
			detailItem.orderId = item.orderId;
			detailItem.code = children.item(5).getTextContent();
			detailItem.location = children.item(10).getTextContent();
			detailItem.date = children.item(16).getTextContent();
			detailItem.from = children.item(11).getTextContent();
			detailItem.to = children.item(12).getTextContent();
			detailItem.curState = children.item(15).getTextContent();
			
			detailList.add(detailItem);
		}
		File record = new File(context.getFilesDir() + "/" + item.orderId);
		FileOutputStream ofs = null;
		ObjectOutputStream oos = null;
		try {
			ofs = new FileOutputStream(record);
			oos = new ObjectOutputStream(ofs);
			oos.writeObject(alarmList);
			oos.writeObject(detailList);
			oos.close();
			ofs.close();
		} catch (Exception e1) {
			try{
				if(oos != null)
					oos.close();
				if(ofs != null)
					ofs.close();
				record.delete();
			} catch(Exception e2) {
				e2.printStackTrace();
			}
			e1.printStackTrace();
		}
	}
	
	public enum RecordFilter {
		ALL,
		ALERT,
		NORMAL,
		HISTORY
	}
	public static List<RecordItem> filerRecord(List<RecordItem> origin, RecordFilter filter) {
		List<RecordItem> result = new ArrayList<RecordItem>();
		switch (filter) {
		case ALL:
			result = origin;
			break;

		case ALERT:
			for(RecordItem item : origin) {
				if(item.state == 1) {
					result.add(item);
				}
			}
			break;
		case NORMAL:
			for(RecordItem item : origin) {
				if(item.state == 0) {
					result.add(item);
				}
			}
			break;
		case HISTORY:
			for(RecordItem item : origin) {
				if(item.state == 2) {
					result.add(item);
				}
			}
			break;
		default:
			break;
		}
		return result;
	}
	
	public static boolean checkPassword(String password) {
		if(password.length() < 6) {
			return false;
		} else {
			return true;
		}
	}
	public static Element getDocumentElement(File file) {
		Element root = null;
		try {			
			if(file.exists()) {
				FileInputStream fin = new FileInputStream(file);
				byte[] data = new byte[fin.available()];
				fin.read(data);
				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder(); 
				Document doc = builder.parse(fin);
				
				root = doc.getDocumentElement();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return root;
	}
	
	public static Element getDocumentElement(String xmlString) {
		Element root = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder(); 
		     if (xmlString != null) {  
		             Pattern p = Pattern.compile("\\s*|\t|\r|\n");  
		             Matcher m = p.matcher(xmlString);  
		             xmlString = m.replaceAll("");  
		     } 
			InputStream is = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			Document doc = builder.parse(is);
			
			root = doc.getDocumentElement();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return root;
	}
	
	public static void showToast(final Activity activity, final String text, final int time) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, text, time).show();
			}
		});
	}
}
