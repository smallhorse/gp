package com.ubtechinc.goldenpig.pigmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.ubtechinc.goldenpig.R;
import com.ubtechinc.goldenpig.base.BaseToolBarActivity;
import com.ubtechinc.goldenpig.main.MainActivity;
import com.ubtechinc.goldenpig.route.ActivityRoute;
import com.ubtechinc.tvlloginlib.utils.SharedPreferencesUtils;

import pl.droidsonroids.gif.GifImageView;

/**
 * @auther :hqt
 * @email :qiangta.huang@ubtrobot.com
 * @description :配网确认页面
 * @time :2018/8/23 10:03
 * @change :
 * @changetime :2018/8/23 10:03
 */
public class SetNetWorkEnterActivity extends BaseToolBarActivity implements View.OnClickListener {
    private GifImageView mTipsImg;             //提示动画
    private View mBtnStartSetNet;       //开始配网按钮

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        udQPAB();
    }

    private void udQPAB() {
        boolean firstEnter = SharedPreferencesUtils.getBoolean(this, "firstEnter", true);
        boolean backable = firstEnter ? false : true;
        boolean skipable = firstEnter ? true : false;
        setTitleBack(backable);
        if (skipable) {
            mTvSkip.setVisibility(View.VISIBLE);
        } else {
            mTvSkip.setVisibility(View.GONE);
        }
    }

    @Override
    protected int getConentView() {
        return R.layout.activity_set_net_enter;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        hiddleTitle();
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesUtils.putBoolean(this, "firstEnter", false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        udQPAB();
    }

    private void initViews() {
        mTipsImg = (GifImageView) findViewById(R.id.ubt_img_set_net_logo);
        mTipsImg.post(new Runnable() {
            @Override
            public void run() {
                //ImageUtils.showGif(SetNetWorkEnterActivity.this,mTipsImg,R.drawable.open_pig);
                mTipsImg.setImageResource(R.drawable.open_pig);
            }
        });

        mBtnStartSetNet = findViewById(R.id.ubt_btn_start_set_net);
        mBtnStartSetNet.setOnClickListener(this);

        mTvSkip = findViewById(R.id.ubt_tv_set_net_skip);
        mTvSkip.setVisibility(View.VISIBLE);
        mTvSkip.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ubt_btn_start_set_net:
                ActivityRoute.toAnotherActivity(this, SearchPigActivity.class, false);
                break;
            case R.id.ubt_tv_set_net_skip:
                ActivityRoute.toAnotherActivity(this, MainActivity.class, true);
                break;
            default:
                break;
        }
    }
}
