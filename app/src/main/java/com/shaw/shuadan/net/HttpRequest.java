/*
 * ************************************************************
 * 文件：HttpRequest.java  模块：app  项目：WeChatGenius
 * 当前修改时间：2018年08月20日 16:12:37
 * 上次修改时间：2018年08月20日 16:12:37
 * 作者：大路
 * Copyright (c) 2018
 * ************************************************************
 */

package com.shaw.shuadan.net;

import com.shaw.shuadan.util.OkHttpUtils;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class HttpRequest {
    //调试版接口
    private String mServerUrlDebug = "http://129.211.82.124:3000/";
    //正式版接口
    private String mServerUrlRelease = "http://www.baidu.com";


    public String getData() {
        return getData(mServerUrlDebug);
    }

    //get方法获取数据
    public String getData(String url) {
        Response response;
        String strResponse;

        //准备网络请求
        OkHttpClient client = OkHttpUtils.getInstance().getOkHttpClient();
        Request request = new Request.Builder()
                .url(mServerUrlDebug)
                .build();
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                return "网络请求失败";
            }
            ResponseBody body = response.body();
            if (body == null) {
                return "网络请求返回body为空";
            }
            strResponse = body.string();
            //解析返回的JSON字符串
        } catch (IOException e) {
            e.printStackTrace();
            return "网络请求异常" + e.getMessage();
        }
        return strResponse;
    }









}
