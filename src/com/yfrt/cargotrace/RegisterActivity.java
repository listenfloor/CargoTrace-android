package com.yfrt.cargotrace;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.yfrt.cargotrace.HttpManager.HttpQueryCallback;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends CommonTitleBarActivity{

	private Handler handler = new Handler();
	
	private HttpQueryCallback httpCallback = new HttpQueryCallback() {
		@Override
		public void onQueryComplete(int state, String result) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					findViewById(R.id.progress).setVisibility(View.GONE);	
				}
			});
			if(state == HttpQueryCallback.STATE_OK) {
				Element root = SystemUtil.getDocumentElement(result);
				if(root != null) {
					NodeList nodes = root.getChildNodes();
					if(nodes != null && nodes.item(0) != null) {
						if("1".equals(nodes.item(0).getTextContent())) {	
							((CargoTraceApp)getApplication()).saveLoginState("user", "session");
							SystemUtil.showToast(RegisterActivity.this, getString(R.string.register_success), Toast.LENGTH_SHORT);
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									finish();
								}
							}, 2000);
						} else if("-301".equals(nodes.item(0).getTextContent())) {
							SystemUtil.showToast(RegisterActivity.this, getString(R.string.register_failed_already_register), Toast.LENGTH_SHORT);
						}
						return;
					}
				}
			}
				
			SystemUtil.showToast(RegisterActivity.this, getString(R.string.register_failed), Toast.LENGTH_SHORT);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		setCustomTitle(getString(R.string.register_title));
		setCustomTitleTextColor(getResources().getColor(R.color.text_white));
		
		setLeftButton(getResources().getDrawable(R.drawable.back), new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		findViewById(R.id.login).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				
				Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
				startActivity(i);
				}
		});

		getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				EditText user = (EditText)findViewById(R.id.mobile_no);
				imm.showSoftInput(user, InputMethodManager.SHOW_FORCED);					
			}
		}, 600);
		
		findViewById(R.id.register).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText user = (EditText)findViewById(R.id.mobile_no);
				EditText pw1 = (EditText)findViewById(R.id.password);
				EditText pw2 = (EditText)findViewById(R.id.confirm);
				String pwStr1 = pw1.getEditableText().toString();
				String pwStr2 = pw2.getEditableText().toString();
				if(pwStr1 != null && pwStr2 != null && SystemUtil.checkPassword(pwStr1) && 
						SystemUtil.checkPassword(pwStr2) && pwStr1.equals(pwStr2)) {
					findViewById(R.id.progress).setVisibility(View.VISIBLE);
					SystemUtil.register(user.getEditableText().toString(), pwStr1, httpCallback);
				} else {
					Toast.makeText(RegisterActivity.this, getString(R.string.register_password_format_error), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
