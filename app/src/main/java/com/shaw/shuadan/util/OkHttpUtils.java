/*
 * ************************************************************
 * 文件：OkHttpUtils.java  模块：app  项目：WeChatGenius
 * 当前修改时间：2018年08月20日 16:05:59
 * 上次修改时间：2018年08月20日 16:05:59
 * 作者：大路
 * Copyright (c) 2018
 * ************************************************************
 */

package com.shaw.shuadan.util;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public final class OkHttpUtils {
    private static final long DEFAULT_READ_TIMEOUT_MILLIS = 15 * 1000;
    private static final long DEFAULT_WRITE_TIMEOUT_MILLIS = 20 * 1000;
    private static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = 20 * 1000;
    private static volatile OkHttpUtils sInstance;
    private final OkHttpClient mOkHttpClient;
    private final Handler handler;

    private OkHttpUtils() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        handler = new Handler(Looper.getMainLooper());
        mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .build();
    }

    public static OkHttpUtils getInstance() {
        if (sInstance == null) {
            synchronized (OkHttpUtils.class) {
                if (sInstance == null) {
                    sInstance = new OkHttpUtils();
                }
            }
        }
        return sInstance;
    }

    //单例获取OkHttpClient实例
    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    //设置get请求
    public void doGet(String url, final onCallBack onCallBack) {

        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (onCallBack != null) {
                    onCallBack.onFaild(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {
                    if (response != null && response.isSuccessful()) {
                        final String json = response.body().string();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (onCallBack != null) {
                                    onCallBack.onResponse(json);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (onCallBack != null) {
                    onCallBack.onFaild(new Exception("有异常"));
                }
            }
        });


    }


    //设置post请求

    public void doPost(String url, Map<String, String> map, final onCallBack onCallBack) {

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : map.keySet()) {
            builder.add(key, map.get(key));
        }
        FormBody formBody = builder.build();
        Request request = new Request.Builder()
                .post(formBody)
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (onCallBack != null) {
                    onCallBack.onFaild(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {
                    if (response != null && response.isSuccessful()) {
                        final String json = response.body().string();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (onCallBack != null) {
                                    onCallBack.onResponse(json);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (onCallBack != null) {
                    onCallBack.onFaild(new Exception("有异常"));
                }
            }
        });
    }

    //设置接口
    public interface onCallBack {
        void onFaild(Exception e);

        void onResponse(String json);
    }

    public static void main(String[] args) {

        Map<String,String> map = new HashMap<>();
        map.put("email ","admin@localhost.com");
        map.put("password  ","bananaiscool");
        OkHttpUtils.getInstance().doPost("http://129.211.82.124:3000//v1​/sessions​/stations",map,new onCallBack()
        {
            @Override
            public void onFaild(Exception e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(String response)
            {
                System.out.println(response);
            }
        });



    }

}
