package com.ubtechinc.goldenpig.about;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ubtechinc.commlib.utils.ContextUtils;
import com.ubtechinc.goldenpig.R;
import com.ubtechinc.goldenpig.app.UBTPGApplication;
import com.ubtechinc.goldenpig.base.BaseToolBarActivity;
import com.ubtechinc.goldenpig.route.ActivityRoute;

import butterknife.BindView;
import butterknife.OnClick;

public class UbtAboutActivtiy extends BaseToolBarActivity {

    TextView mVersionTv;
    Button mPrivacyBtn;
    int debug_open=5;
    int click_times=0;
    @Override
    protected int getConentView() {
        return R.layout.activity_about;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        setTitleBack(true);
        setToolBarTitle(R.string.ubt_about);
        mVersionTv=findViewById(R.id.ubt_tv_about_version);
        mPrivacyBtn=findViewById(R.id.ubt_btn_privacy_policy);
        mVersionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click_times++;
                if(click_times>debug_open){
                    UBTPGApplication.voiceMail_debug=true;
                    Toast.makeText(UBTPGApplication.getContext(),"debug open",Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (mVersionTv!=null){
            mVersionTv.setText(String.format(getString(R.string.ubt_version_format),ContextUtils.getVerName(this)));
        }
        mPrivacyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityRoute.toAnotherActivity(UbtAboutActivtiy.this,PrivacyPolicyActivity.class,false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}

