package com.yfrt.cargotrace;

import com.yfrt.cargotrace.Workspace.OnScreenChangeListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class UserGuideActivity extends Activity {

	private Workspace workspace;
	private LinearLayout index;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
//        index = (LinearLayout)findViewById(R.id.index);

        workspace = (Workspace)findViewById(R.id.workspace);
        workspace.setOnScreenChangeListener(new OnScreenChangeListener() {
			@Override
			public void onScrrenChangeEnd(int whichScreen) {
				
			}
			
			@Override
			public void onScreenChangeStart(int whichScreen) {
			}
		});
        workspace.setCurrentScreen(0);
        
        Button start = (Button)findViewById(R.id.start);
        start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UserGuideActivity.this, MainActivity.class);
				startActivity(intent);
				UserGuideActivity.this.finish();
			}
		});
    }
}
