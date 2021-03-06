package com.ubtechinc.goldenpig.pigmanager.mypig;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ubt.qrcodelib.Constants;
import com.ubt.qrcodelib.ZxingUtils;
import com.ubtech.utilcode.utils.ToastUtils;
import com.ubtechinc.goldenpig.BuildConfig;
import com.ubtechinc.goldenpig.R;
import com.ubtechinc.goldenpig.base.BaseToolBarActivity;
import com.ubtechinc.goldenpig.comm.net.CookieInterceptor;
import com.ubtechinc.goldenpig.comm.widget.UBTSubTitleDialog;
import com.ubtechinc.goldenpig.pigmanager.register.GetAddMemberQRHttpProxy;
import com.ubtechinc.goldenpig.route.ActivityRoute;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * @auther :hqt
 * @email :qiangta.huang@ubtrobot.com
 * @description :添加成员界面
 * @time :2018/9/21 17:39
 * @change :
 * @changetime :2018/9/21 17:39
 */
public class QRCodeActivity extends BaseToolBarActivity implements View.OnClickListener {
    private ImageView mQRImg; //二维码
    private int mQRSize;
    private long mQRClickTime;
    private boolean isPair; //用于区分两种配对八戒和添加成员功能，显示不同文字或导航栏按钮
    private String singa;
    private int mQRLogoSize;

    private UBTSubTitleDialog dialog;

    @Override
    protected int getConentView() {
        return R.layout.activity_qrcode;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        setTitleBack(true);
        initViews();
        initContent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            initContent(intent);
        }
    }

    private void initViews() {
        mQRImg = findViewById(R.id.ubt_img_qrcode);
        mQRImg.setOnClickListener(this);

        mQRSize = getResources().getDimensionPixelOffset(R.dimen.dp_250);
        mQRLogoSize = getResources().getDimensionPixelOffset(R.dimen.dp_83);
    }

    private void initContent(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (null != extras && extras.containsKey("isPair")) {
                isPair = extras.getBoolean("isPair", false);
            }
        }
        if (isPair) {
            setToolBarTitle(R.string.ubt_pair_pig);
            ((TextView) findViewById(R.id.ubt_tv_qrcode_sub_title)).setText("配对攻略");
            ((TextView) findViewById(R.id.ubt_tv_qrcode_desc)).setText(R.string.ubt_pair_pig_desc);
            mToolbarRightBtn = findViewById(R.id.ubt_imgbtn_add);
            mToolbarRightBtn.setImageResource(R.drawable.ic_shaoyishao);
            mToolbarRightBtn.setVisibility(View.VISIBLE);
            mToolbarRightBtn.setOnClickListener(this);

        } else {
            setToolBarTitle(R.string.ubt_add_member);
            ((TextView) findViewById(R.id.ubt_tv_qrcode_sub_title)).setText(R.string.ubt_add_member);
            ((TextView) findViewById(R.id.ubt_tv_qrcode_desc)).setText(R.string.ubt_addmember_desc);
            findViewById(R.id.ubt_imgbtn_add).setVisibility(View.GONE);

        }
        createQR();
    }

    private void showQRErrorToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showLongToast("生成二维码失败，请点击重试");
            }
        });
    }

    private void createQR() {
        GetAddMemberQRHttpProxy httpProxy = new GetAddMemberQRHttpProxy();
        httpProxy.getMemberQR(CookieInterceptor.get().getToken(), BuildConfig.APP_ID, BuildConfig.product, new GetAddMemberQRHttpProxy.GetMemberQRCallBack() {
            @Override
            public void onError(String error) {

                showQRErrorToast();
            }

            @Override
            public void onSuccess(String response) {
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("sign")) {
                            singa = jsonObject.getString("sign");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mQRImg != null) {
//                                        mQRImg.setImageBitmap(ZxingUtils.createBitmap(singa, mQRSize, mQRSize, 1));
                                        mQRImg.setImageBitmap(ZxingUtils.createQRCodeWithLogo(singa, mQRSize,BitmapFactory.decodeResource(getResources(), R.drawable.img_hulian)));
                                    }
                                }
                            });

                        } else {
                            showQRErrorToast();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showQRErrorToast();
                    }
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.QR_PAIR_PIG_REQUEST:
                if (resultCode == Constants.QR_PAIR_PIG_SUCCESS && data != null) {
                    doPairPig(data.getDataString());
                } else {
//                    ToastUtils.showShortToast(R.string.ubt_get_qr_pair_pig_fialure);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ubt_img_qrcode:
                if (mQRClickTime == 0 || System.currentTimeMillis() - mQRClickTime > 5000) {
                    mQRClickTime = System.currentTimeMillis();
                    createQR();
                } else {
                    ToastUtils.showLongToast("生成二维码过于频繁，请稍后重试");
                }

                break;
            case R.id.ubt_imgbtn_add:
                if (isPair) {
                    goToPairQRScannerActivity();
                }
                break;
                default:
        }
    }

    private void doPairPig(String sign) {

    }

    private void goToPairQRScannerActivity() {
        if (Build.VERSION.SDK_INT >= 23) {
            AndPermission.with(this)
                    .requestCode(0x1101)
                    .permission(Permission.CAMERA)
                    .callback(new PermissionListener() {
                        @Override
                        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                            if (cameraIsCanUse()) {
                                ActivityRoute.toAnotherActivity(QRCodeActivity.this, PairQRScannerActivity.class, Constants.QR_PAIR_PIG_REQUEST, false);
                            } else {
                                showPermissionDialog(Permission.CAMERA);
                            }
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionDialog(Permission.CAMERA);
                        }
                    })
                    .rationale((requestCode, rationale) -> rationale.resume())
                    .start();

        } else {
            if (cameraIsCanUse()) {
                ActivityRoute.toAnotherActivity(QRCodeActivity.this, PairQRScannerActivity.class, Constants.QR_PAIR_PIG_REQUEST, false);
            } else {
                showPermissionDialog(Permission.CAMERA);
            }
        }
    }


}
