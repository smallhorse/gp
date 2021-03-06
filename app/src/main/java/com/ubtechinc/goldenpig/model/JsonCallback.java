package com.ubtechinc.goldenpig.model;

import com.google.gson.Gson;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.network.NetworkHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class JsonCallback<T> implements Callback {
    private Class<T> clazz;
    private Type type;

    public JsonCallback(Class<T> clazz) {
        this.clazz = clazz;
    }

    public JsonCallback(Type type) {
        this.type = type;
    }

    public void onFailure(Call call, IOException e) {
        if (NetworkHelper.sharedHelper() == null) {
            onError("当前网络异常，请检查网络设置");
        } else if (NetworkHelper.sharedHelper().isNetworkAvailable()) {
            onError("当前数据异常，请稍后重试");
        } else {
            onError("当前网络异常，请检查网络设置");
        }
        //onError(e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String result = response.body().string();
        LogUtils.d("hdf", result);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == -505) {
                onError("保存失败，已存在相同问句");
                return;
            } else if (code == -504) {
                onError("保存失败，内容中包含敏感词");
                return;
            } else if (code != 0) {
                if (NetworkHelper.sharedHelper() == null) {
                    onError("当前网络异常，请检查网络设置");
                } else if (NetworkHelper.sharedHelper().isNetworkAvailable()) {
                    onError("当前数据异常，请稍后重试");
                } else {
                    onError("当前网络异常，请检查网络设置");
                }
//                if (TextUtils.isEmpty(jsonObject.getString("errMsg"))) {
//                    onError("数据异常，请重试");
//                } else {
//                    String str = jsonObject.getString("errMsg");
//                    if (str.contains("fail") || str.contains("Fail")) {
//                        onError("网络异常，请重试");
//                    } else {
//                        onError(str);
//                    }
//                }
                return;
            }
            if (clazz == String.class) {
                //只需要String数据
                onSuccess(null);
                return;
            }
            Gson gson = new Gson();
            T data = null;
            if (clazz != null) data = gson.fromJson(jsonObject.getString("skills"), clazz);
            else if (type != null) data = gson.fromJson(jsonObject.getString("skills"), type);
            else if (clazz == null && type == null) data = (T) jsonObject;
            onSuccess(data);
        } catch (JSONException e) {
            e.printStackTrace();
            onError("当前数据异常，请稍后重试");
        }
    }

    public abstract void onSuccess(T reponse);

    public abstract void onError(String str);
}
