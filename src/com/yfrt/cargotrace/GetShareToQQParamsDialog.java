/**
 * @author azraellong
 * @date 2013-2-18
 */

package com.yfrt.cargotrace;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * @author azraellong
 */
public class GetShareToQQParamsDialog extends Dialog implements
        View.OnClickListener {
    TextView imageUrl;
    TextView targetUrl;
    TextView summary;
    TextView siteUrl;
    TextView appName;//app名称，用于手Q显示返回

    ShareToQQParamsListener paramsListener;
    
    interface ShareToQQParamsListener{
        void onComplete(Bundle params);
    }
    
    /**
     * @param context
     */
    public GetShareToQQParamsDialog(Context context, ShareToQQParamsListener listener) {
        super(context);
        paramsListener = listener;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.shareqq_commit){
            Bundle bundle = new Bundle();
            bundle.putString("title", "指尖货运");
            bundle.putString("imageUrl", "http://img0.ph.126.net/HKZ1Gg7H8k_vL81r_rvcmQ==/1873497445086270291.png" + "");
            bundle.putString("targetUrl", "http://www.eft.cn/");
            bundle.putString("summary", summary.getText() + "");
            bundle.putString("site", "");
            bundle.putString("appName", "指尖货运" + "");
            
            if(null != paramsListener){
                paramsListener.onComplete(bundle);
            }
            this.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.get_shareqq_params_dialog);
        summary = (TextView) findViewById(R.id.shareqq_summary);
        
        findViewById(R.id.shareqq_commit).setOnClickListener(this);
    }

}
