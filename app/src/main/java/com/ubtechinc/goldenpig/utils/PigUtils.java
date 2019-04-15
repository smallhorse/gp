package com.ubtechinc.goldenpig.utils;

import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.tencent.ai.tvs.business.UniAccessInfo;
import com.tencent.ai.tvs.info.DeviceManager;
import com.ubt.imlibv2.bean.UbtTIMManager;
import com.ubtechinc.commlib.log.UbtLogger;
import com.ubtechinc.goldenpig.BuildConfig;
import com.ubtechinc.goldenpig.comm.net.CookieInterceptor;
import com.ubtechinc.goldenpig.eventbus.EventBusUtil;
import com.ubtechinc.goldenpig.eventbus.modle.Event;
import com.ubtechinc.goldenpig.login.observable.AuthLive;
import com.ubtechinc.goldenpig.pigmanager.bean.PigInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PigUtils {
    public static String TVS_APP_KEY = "ffb54fb08efe11e8a377658d0db82adb";

    public static void getPigList(String response, String userId, ArrayList<PigInfo> pigInfos) {
        if (!TextUtils.isEmpty(response)) {
            try {
                // [{"serialNumber":"040002KFK2222222221","userId":812882,"isAdmin":1,
                // "relationDate":"2018-09-13 15:10:43","bindingId":4512}]
                JSONArray jsonArray = new JSONArray(response);
                if (jsonArray != null) {
                    pigInfos.clear();
                    int length = jsonArray.length();
                    for (int index = 0; index < length; index++) {
                        JSONObject object = jsonArray.getJSONObject(index);
                        if (object.has("userId")) {

                            PigInfo pigInfo = new PigInfo();
                            pigInfo.setRobotUserId(userId);
                            pigInfo.setMasterUserId(userId);
                            pigInfo.setRobotName(object.getString("serialNumber"));
                            pigInfo.setBindingId(object.getInt("bindingId"));
                            if (object.getInt("isAdmin") == 1) {
                                pigInfo.isAdmin = true;
                                pigInfos.add(0, pigInfo);
                            } else {
                                pigInfos.add(pigInfo);
                            }

                        }
                    }

                    EventBusUtil.sendEvent(new Event<>(EventBusUtil.USER_PIG_UPDATE));

                    PigInfo pigInfo = AuthLive.getInstance().getCurrentPig();
                    if (pigInfo != null) {
                        UbtTIMManager ubtTIMManager = UbtTIMManager.getInstance();
                        ubtTIMManager.setPigAccount(pigInfo.getRobotName());
                        if (pigInfo.isAdmin && !ubtTIMManager.isLoginedTIM()) {
                            ubtTIMManager.loginTIM(userId, pigInfo.getRobotName(), com.ubt.imlibv2.BuildConfig
                                    .IM_Channel);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static UniAccessInfo getAlarmUniAccessinfo(int eCloud_type, int eRepeatType, long
            lAlarmId, long lStartTimeStamp) {
        UniAccessInfo info = new UniAccessInfo();
        info.domain = "alarm";
        info.intent = "cloud_manager";
        JSONObject obj = new JSONObject();
        try {
            obj.put("eType", 0);
            JSONObject stCloudAlarmReq = new JSONObject();
            JSONObject stAccountBaseInfo = new JSONObject();
            stAccountBaseInfo.put("strAcctId", CookieInterceptor.get().getThridLogin().getOpenId());
            stCloudAlarmReq.put("stAccountBaseInfo", stAccountBaseInfo);
            stCloudAlarmReq.put("eCloud_type", eCloud_type);//0,为查看; 1为添加;2为删除;3为更新
            stCloudAlarmReq.put("sPushInfo", "");
            JSONArray vCloudAlarmData = new JSONArray();
            JSONObject vCloudAlarmData0 = new JSONObject();
            JSONObject stAIDeviceBaseInfo = new JSONObject();
            stAIDeviceBaseInfo.put("strGuid", "");//AuthLive.getInstance().getCurrentPig() == null ?"hdfeng" :
            // AuthLive.getInstance().getCurrentPig().getGuid()
            stAIDeviceBaseInfo.put("strAppKey", TVS_APP_KEY);
            vCloudAlarmData0.put("stAIDeviceBaseInfo", stAIDeviceBaseInfo);
            vCloudAlarmData0.put("eRepeatType", eRepeatType);
            //0为异常类型，1为一次性，2为每天，3为每周，4为每月，5为工作日，6为节假日
            vCloudAlarmData0.put("lAlarmId", lAlarmId);
            vCloudAlarmData0.put("lStartTimeStamp", (long) (lStartTimeStamp / 1000));
            vCloudAlarmData0.put("vRingId", null);
            vCloudAlarmData.put(vCloudAlarmData0);
            stCloudAlarmReq.put("vCloudAlarmData", vCloudAlarmData);
            obj.put("stCloudAlarmReq", stCloudAlarmReq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        info.jsonBlobInfo = obj.toString();
        return info;
    }

    public static UniAccessInfo getSmartHomeUniAccessinfo(int eCloud_type, int eRepeatType, long
            lAlarmId, long lStartTimeStamp) {
        String skillId = "8c62fb66-d05f-11e8-8130-68cc6ea8c1f8";
        UniAccessInfo info = new UniAccessInfo();
        info.domain = "smarthome";
        info.intent = "query_skills";
        JSONObject obj = new JSONObject();
        try {
            obj.put("operType", "query_skills");
//            obj.put("skill_id", skillId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        info.jsonBlobInfo = obj.toString();
        return info;
    }

    public static UniAccessInfo getRemindUniAccessinfo(String sNote, int eCloud_type, int eRepeatType, long
            lReminderId, long lStartTimeStamp) {
        UniAccessInfo info = new UniAccessInfo();
        info.domain = "reminder_v2";
        info.intent = "cloud_manager";
        JSONObject obj = new JSONObject();
        try {
            obj.put("eType", 0);
            JSONObject stCloudAlarmReq = new JSONObject();
            JSONObject stAccountBaseInfo = new JSONObject();
            stAccountBaseInfo.put("strAcctId", CookieInterceptor.get().getThridLogin().getOpenId());
            stCloudAlarmReq.put("stAccountBaseInfo", stAccountBaseInfo);
            stCloudAlarmReq.put("eCloud_type", eCloud_type);//0,为查看; 1为添加;2为删除;3为更新
            //stCloudAlarmReq.put("sPushInfo", "");
            JSONArray vCloudAlarmData = new JSONArray();
            JSONObject vCloudAlarmData0 = new JSONObject();
            JSONObject stAIDeviceBaseInfo = new JSONObject();
            stAIDeviceBaseInfo.put("strGuid", "");//AuthLive
            // .getInstance().getCurrentPig() == null ?
            //"hdfeng" : AuthLive.getInstance().getCurrentPig().getRobotName()
            stAIDeviceBaseInfo.put("strAppKey", TVS_APP_KEY);
            vCloudAlarmData0.put("stAIDeviceBaseInfo", stAIDeviceBaseInfo);
            stCloudAlarmReq.put("stAIDeviceBaseInfo", stAIDeviceBaseInfo);
            vCloudAlarmData0.put("eRepeatType", eRepeatType);
            //0为异常类型，1为一次性，2为每天，3为每周，4为每月，5为工作日，6为节假日
            vCloudAlarmData0.put("lReminderId", lReminderId);
            vCloudAlarmData0.put("lStartTimeStamp", (long) (lStartTimeStamp / 1000));
            vCloudAlarmData0.put("sNote", sNote);
            vCloudAlarmData0.put("vRingId", null);
            vCloudAlarmData.put(vCloudAlarmData0);
            stCloudAlarmReq.put("vCloudReminderData", vCloudAlarmData);
            obj.put("stCloudAlarmReq", stCloudAlarmReq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        info.jsonBlobInfo = obj.toString();
        return info;
    }

    public static DeviceManager getAlarmDeviceMManager() {
        DeviceManager deviceManager = new DeviceManager();
        deviceManager.productId = BuildConfig.PRODUCT_ID;
        deviceManager.dsn = AuthLive.getInstance().getCurrentPig().getRobotName();
        return deviceManager;
    }


    public static UniAccessInfo setChildMode(int mode){
        UniAccessInfo info = new UniAccessInfo();
        info.domain = "child_ctrl";
        info.intent = "set_child_mode_info";
        JSONObject obj = new JSONObject();
        try {
            JSONObject param = new JSONObject();
            param.put("eActionType", 1);
            param.put("deviceId",/*AuthLive.getInstance().getCurrentPig().getGuid()*/ "");
            param.put("deviceAppkey", TVS_APP_KEY);
            param.put("ePlatformType", mode);
            obj.put("childControlInfo", param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        info.jsonBlobInfo = obj.toString();
        UbtLogger.d("setChildMode", "info:" + info.toString());
        return info;
    }


    public static UniAccessInfo getChildMode() {
        UniAccessInfo info = new UniAccessInfo();
        info.domain = "child_ctrl";
        info.intent = "get_child_mode_info";
        JSONObject obj = new JSONObject();
        try {
            JSONObject param = new JSONObject();
            param.put("deviceId",/*AuthLive.getInstance().getCurrentPig().getGuid()*/ "");
            obj.put("childModeInfo", param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        info.jsonBlobInfo = obj.toString();
        UbtLogger.d("getChildMode", "info:" + info.toString());
        return info;
    }





}
