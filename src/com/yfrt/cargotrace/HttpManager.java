package com.yfrt.cargotrace;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpManager {

	static private int timeoutConnection = 3000;
	static private int timeoutSocket = 5000;

	static public String syncQuery(final String postData) {
		return syncQuery(AppConstants.service_url, postData, false);
	}
	static public String syncQuery(final String postData, boolean encrypt) {
		return syncQuery(AppConstants.service_url, postData, encrypt);
	}
	static public String syncQuery(final String urlString, final String postData, boolean encrypt) {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
		HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
		/* 建立HTTP Post连线 */
		HttpPost post = new HttpPost(urlString);
		// Post运作传送变数必须用NameValuePair[]阵列储存
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("serviceXml", postData));
		try {
			// 发出HTTP request
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//			if(encrypt){
//				post.setHeader("ecrypt", "true");
//			}
			// 取得HTTP response
			HttpResponse httpResponse = new DefaultHttpClient(httpParams).execute(post);

				// 若状态码为200 ok
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
			// 取出回应字串
				String strResult = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
				return strResult;
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static public void asyncQuery(final String postData,
			final WeakReference<HttpQueryCallback> weakCallback) {
		asyncQuery(AppConstants.service_url, postData, weakCallback);
	}

	static public void asyncQuery(final String urlString,
			final String postData,
			final WeakReference<HttpQueryCallback> weakCallback) {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams,
						timeoutConnection);
				HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
				/* 建立HTTP Post连线 */
				HttpPost post = new HttpPost(urlString);
				// Post运作传送变数必须用NameValuePair[]阵列储存
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("serviceXml", postData));
				try {
					// 发出HTTP request
					post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					// 取得HTTP response
					HttpResponse httpResponse = new DefaultHttpClient(httpParams).execute(post);
					HttpQueryCallback callback = weakCallback.get();
					if (callback != null) {
						// 若状态码为200 ok
						if (httpResponse.getStatusLine().getStatusCode() == 200) {
							// 取出回应字串
							String strResult = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
							if (callback != null) {
								callback.onQueryComplete(HttpQueryCallback.STATE_OK, strResult);
							}
						} else {
							if (callback != null) {
								callback.onQueryComplete(HttpQueryCallback.STATE_ERROR, null);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					HttpQueryCallback callback = weakCallback.get();
					if (callback != null) {
						callback.onQueryComplete(HttpQueryCallback.STATE_ERROR, null);
					}
				}
			}
		})).start();
	}
	
	private static X509TrustManager xtm = new X509TrustManager() {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	};
	private static HostnameVerifier hnv = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	
	static public String syncHttpsPost(String urlString, final List<NameValuePair> postData) {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");
			X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };
			sslContext.init(null, xtmArray, new java.security.SecureRandom());
		} catch (GeneralSecurityException gse) {
		}

		// 为javax.net.ssl.HttpsURLConnection设置默认的SocketFactory和HostnameVerifier
		if (sslContext != null) {
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		}

		HttpsURLConnection.setDefaultHostnameVerifier(hnv);

		StringBuilder data = new StringBuilder();
		if(postData != null && postData.size() > 0) {
			for(int i = 0; i < postData.size(); i++) {
				NameValuePair pair = postData.get(i);
				if(i > 0) {
					data.append('&');
				}
				data.append(pair.getName());
				data.append('=');
				data.append(pair.getValue());
			}
		}
		String query = data.toString();	
		 
		byte[] entitydata = query.getBytes();// 得到实体数据
		HttpsURLConnection urlCon = null;
		try {
			urlCon = (HttpsURLConnection) (new URL(urlString)).openConnection();
			urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			urlCon.setRequestProperty("Content-Length", String.valueOf(entitydata.length));
			urlCon.setRequestMethod("POST");
			urlCon.setDoOutput(true);
			urlCon.setDoInput(true);
			urlCon.connect();

			// 把封装好的实体数据发送到输出流
			OutputStream outStream = urlCon.getOutputStream();
			outStream.write(entitydata);
			outStream.flush();
			outStream.close();

			// 服务器返回输入流并读写
			BufferedReader in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
			String line;
			StringBuilder ret = new StringBuilder();
			while ((line = in.readLine()) != null) {
				ret.append(line);
			}
			in.close();
			
			return ret.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	interface HttpQueryCallback {
		int STATE_OK = 0;
		int STATE_ERROR = 1;

		void onQueryComplete(int state, String result);
	}
}
