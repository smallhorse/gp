package com.ubtechinc.goldenpig.main;

import android.content.Context;

import com.ubtechinc.goldenpig.BuildConfig;
import com.ubtechinc.goldenpig.comm.net.CookieInterceptor;
import com.ubtechinc.goldenpig.net.URestSigner;
import com.ubtechinc.nets.utils.DeviceUtils;

import java.util.HashMap;

import static com.ubtechinc.goldenpig.main.CommonWebActivity.KEY_IMMERSE_STATUSBAR;
import static com.ubtechinc.goldenpig.main.CommonWebActivity.KEY_NEED_ACTIONBAR;
import static com.ubtechinc.goldenpig.main.CommonWebActivity.KEY_URL;

/**
 * @Description: ${DESCRIPTION}
 * @Author: zhijunzhou
 * @CreateDate: 2019/3/21 18:36
 */
public class UbtWebHelper {

    /**
     * 帮助与反馈
     *
     * @param context
     * @return
     */
    public static HashMap<String, Object> getFeedBackWebviewData(Context context) {
        HashMap<String, Object> map = new HashMap<>();
        String baseUrl = BuildConfig.H5_URL + "/small/smallComment.html?";
        String deviceId = DeviceUtils.getDeviceId(context);
        String url = baseUrl + "appId=" + BuildConfig.APP_ID + "&sign=" + URestSigner.sign(context, deviceId).replace(" ", "%20")
                + "&product=" + BuildConfig.product + "&deviceId=" + deviceId;
        map.put(KEY_URL, url);
        map.put(KEY_IMMERSE_STATUSBAR, true);
        map.put(KEY_NEED_ACTIONBAR, false);
        return map;
    }

    /**
     * QQ音乐
     *
     * @param context
     * @return
     */
    public static HashMap<String, Object> getQQMusicWebviewData(Context context) {
        HashMap<String, Object> map = new HashMap<>();
        String baseUrl = BuildConfig.H5_URL + "/small/smallqqMusic.html?";
        String deviceId = DeviceUtils.getDeviceId(context);
        String url = baseUrl + "appId=" + BuildConfig.APP_ID + "&sign=" + URestSigner.sign(context, deviceId).replace(" ", "%20")
                + "&product=" + BuildConfig.product + "&deviceId=" + deviceId + "&authorization=" + CookieInterceptor.get().getToken();
        map.put(KEY_URL, url);
        map.put(KEY_IMMERSE_STATUSBAR, false);
        map.put(KEY_NEED_ACTIONBAR, true);
        return map;
    }

    /**
     * 蓝牙音箱
     *
     * @param context
     * @return
     */
    public static HashMap<String, Object> getBleWebviewData(Context context) {
        HashMap<String, Object> map = new HashMap<>();
        String baseUrl = BuildConfig.H5_URL + "/small/smallBlue.html";
        map.put(KEY_URL, baseUrl);
        map.put(KEY_IMMERSE_STATUSBAR, false);
        map.put(KEY_NEED_ACTIONBAR, true);
        return map;
    }

    /**
     * 服务条款
     *
     * @param context
     * @return
     */
    public static HashMap<String, Object> getServicePolicyWebviewData(Context context) {
        HashMap<String, Object> map = new HashMap<>();
        String baseUrl = BuildConfig.H5_URL + "/small/smallService.html";
        map.put(KEY_URL, baseUrl);
        map.put(KEY_IMMERSE_STATUSBAR, false);
        map.put(KEY_NEED_ACTIONBAR, true);
        return map;
    }

    /**
     * 隐私协议
     *
     * @param context
     * @return
     */
    public static HashMap<String, Object> getPrivacyPolicyWebviewData(Context context) {
        HashMap<String, Object> map = new HashMap<>();
        String baseUrl = BuildConfig.H5_URL + "/small/smallProcy.html";
        map.put(KEY_URL, baseUrl);
        map.put(KEY_IMMERSE_STATUSBAR, false);
        map.put(KEY_NEED_ACTIONBAR, true);
        return map;
    }

    /**
     * 更新
     * @param context
     * @return
     */
    public static HashMap<String, Object> getUpdateInfoWebviewData(Context context, String targetUrl) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(KEY_URL, targetUrl);
        map.put(KEY_IMMERSE_STATUSBAR, false);
        map.put(KEY_NEED_ACTIONBAR, true);
        return map;
    }

}
