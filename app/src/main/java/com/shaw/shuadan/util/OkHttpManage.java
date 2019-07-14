package com.shaw.shuadan.util;


import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.shaw.shuadan.MainActivity;
import com.shaw.shuadan.ui.user.LoginActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;

import static android.content.ContentValues.TAG;

/**
 * OkHttpMange管理类
 * Created by leict on 2017/6/6.
 */

public class OkHttpManage {
    private static OkHttpManage mInstance;
    private OkHttpClient mClient;
    private Handler mHnadler;
    private Gson mGson;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 单例
     *
     * @return
     */
    public static synchronized OkHttpManage getInstance() {
        if (mInstance == null) {
            mInstance = new OkHttpManage();
        }
        return mInstance;
    }

    /**
     * 构造函数
     */
    private OkHttpManage() {
        initOkHttp();
        mHnadler = new Handler(Looper.getMainLooper());
        mGson = new Gson();
    }

    /**
     * 初始化OkHttpClient
     */
    private void initOkHttp() {
        mClient = new OkHttpClient().newBuilder()
                .readTimeout(30000, TimeUnit.SECONDS)
                .connectTimeout(30000, TimeUnit.SECONDS)
                .writeTimeout(30000, TimeUnit.SECONDS)
                .addNetworkInterceptor(tokenInterceptor)
                .addInterceptor(loggingInterceptor)//使用上面的拦截器
//                .authenticator(authenticator)
                .build();
    }

    Authenticator authenticator = new Authenticator() {//当服务器返回的状态码为401时，会自动执行里面的代码，也就实现了自动刷新token
        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            Log.d(TAG,"==========>   重新刷新了token");//这里可以进行刷新 token 的操作

            return response.request().newBuilder()
                    .addHeader("token", "")
                    .build();
        }
    };


    Interceptor tokenInterceptor = new Interceptor() {//全局拦截器，往请求头部添加 token 字段，实现了全局添加 token
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();//获取请求
            Request tokenRequest = null;
            if (TextUtils.isEmpty(getToken())) {//对 token 进行判空，如果为空，则不进行修改
                return chain.proceed(originalRequest);
            }
            tokenRequest = originalRequest.newBuilder()//往请求头中添加 token 字段
                    .header("Authorization", "Bearer "+getToken())
                    .build();
            return chain.proceed(tokenRequest);
        }
    };
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {//log拦截器，打印所有的log
        @Override
        public void log(String message) {
            Log.d(TAG,message);
        }
    });

    /**
     * 请求
     *
     * @param client
     * @param callBack
     */
    public void request(BaseOkHttpClient client, final BaseCallBack callBack) {
        if (callBack == null) {
            throw new NullPointerException(" callback is null");
        }
        mClient.newCall(client.buildRequest()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendonFailureMessage(callBack, call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    if (callBack.mType == null || callBack.mType == String.class) {
                        sendonSuccessMessage(callBack, result);
                    } else {
                        sendonSuccessMessage(callBack, mGson.fromJson(result, callBack.mType));
                    }
                    if (response.body() != null) {
                        response.body().close();
                    }
                } else {
                    sendonErrorMessage(callBack, response.code());
                }
            }
        });
    }

    /**
     * 成功信息
     *
     * @param callBack
     * @param result
     */
    private void sendonSuccessMessage(final BaseCallBack callBack, final Object result) {
        mHnadler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onSuccess(result);
            }
        });
    }

    /**
     * 失败信息
     *
     * @param callBack
     * @param call
     * @param e
     */
    private void sendonFailureMessage(final BaseCallBack callBack, final Call call, final IOException e) {
        mHnadler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onFailure(call, e);
            }
        });
    }

    /**
     * 错误信息
     *
     * @param callBack
     * @param code
     */
    private void sendonErrorMessage(final BaseCallBack callBack, final int code) {
        mHnadler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onError(code);
            }
        });
    }
}