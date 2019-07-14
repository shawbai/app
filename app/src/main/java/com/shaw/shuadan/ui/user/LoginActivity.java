/*
 * ************************************************************
 * 文件：LoginActivity.java  模块：app  项目：WeChatGenius
 * 当前修改时间：2018年08月20日 17:50:43
 * 上次修改时间：2018年08月20日 17:50:42
 * 作者：大路
 * Copyright (c) 2018
 * ************************************************************
 */

package com.shaw.shuadan.ui.user;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.shaw.shuadan.MainActivity;
import com.shaw.shuadan.R;
import com.shaw.shuadan.ui.GridLayoutActivity;
import com.shaw.shuadan.util.BaseCallBack;
import com.shaw.shuadan.util.BaseOkHttpClient;
import com.shaw.shuadan.util.OkHttpUtils;
import com.shaw.shuadan.util.RegexUtils;
import com.shaw.shuadan.util.StringUtils;
import com.shaw.shuadan.web.WsManager;
import com.shaw.shuadan.web.WsStatusListener;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.ByteString;

public class LoginActivity extends Activity {
    @BindView(R.id.topbar) QMUITopBar mTopBar;
    private final String TAG = getClass().getSimpleName();

    public JSONObject getToken() {
        SharedPreferences user = getSharedPreferences("user_info",0);
        String userInfoStr = user.getString("user_info","");
        return JSONObject.parseObject(userInfoStr);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 沉浸式状态栏
        QMUIStatusBarHelper.translucent(this);
        View root = LayoutInflater.from(this).inflate(R.layout.activity_login, null);
        ButterKnife.bind(this, root);
        //初始化状态栏
        initTopBar();
        //设置view
        setContentView(root);
        JSONObject json = getToken();
        if(json!=null&&!StringUtils.isEmpty(json.toJSONString())){
            Intent intent = new Intent(this, GridLayoutActivity.class);
            startActivity(intent);
            //结束本页面
            finish();
        }
        ((EditText) findViewById(R.id.edittext_username)).setText("station1@station.com");
        ((EditText) findViewById(R.id.edittext_password)).setText("12345678");
        WsStatusListener wsBaseStatusListener = new WsStatusListener() {
            @Override
            public void onOpen(Response response) {
                super.onOpen(response);
                //协议初始化  心跳等
                System.out.println("!!!!!!!!!!:"+response);
            }

            @Override
            public void onMessage(String text) {
                super.onMessage(text);
                //消息处理
                System.out.println("!!!!!!!!!!1:"+text);
            }

            @Override
            public void onMessage(ByteString bytes) {
                super.onMessage(bytes);
                //消息处理
            }

            @Override
            public void onClosing(int code, String reason) {
                super.onClosing(code, reason);
            }

            @Override
            public void onClosed(int code, String reason) {
                super.onClosed(code, reason);
            }

            @Override
            public void onFailure(Throwable t, Response response) {
                super.onFailure(t, response);
            }
        };
        WsManager wsBaseManager = new WsManager.Builder(getBaseContext())
                .client(new OkHttpClient().newBuilder()
                        .pingInterval(15, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build())
                .needReconnect(true)
                .wsUrl("http://129.211.82.124:3001")
                .build();
        wsBaseManager.setWsStatusListener(wsBaseStatusListener);
        wsBaseManager.startConnect();
    }

    //初始化状态栏
    private void initTopBar() {
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_about, R.id.empty_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    }
                });
        mTopBar.setTitle(getResources().getString(R.string.activity_title_main));
    }

    //登录按钮
    @OnClick({R.id.button_login})
    void onClickButton(View view) {
        switch (view.getId()) {
            case R.id.button_login:
                onClickLogin(view);
                break;
        }
    }

    //关于、联系我们，文字点击事件处理
    @OnClick({R.id.textview_about})
    void onClickTextView(View view) {
        Intent intent = null;
//        Log.d(TAG, "登录页面，文字：" + ((TextView) view).getText() + " 被点击了。");
        switch (view.getId()) {
            case R.id.textview_about:
                intent = new Intent(LoginActivity.this, RegisterActivity.class);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    //登录按钮事件
    private void onClickLogin(View view) {
//        "admin@localhost.com"
//        "bananaiscool"
        String strName = ((EditText) findViewById(R.id.edittext_username)).getText().toString().trim();
        String strPassword = ((EditText) findViewById(R.id.edittext_password)).getText().toString().trim();

        if (!RegexUtils.isEmail(strName)) {
            Toast.makeText(this, "账号格式不正确！", Toast.LENGTH_SHORT).show();
            QMUIKeyboardHelper.showKeyboard((EditText) findViewById(R.id.edittext_username), 1500);
            return;
        }
        if (strPassword.length() < 6 || strPassword.length() > 20) {
            Toast.makeText(this, "密码长度为6到20位！", Toast.LENGTH_SHORT).show();
            QMUIKeyboardHelper.showKeyboard((EditText) findViewById(R.id.edittext_password), 1500);
            return;
        }
        Intent intent = new Intent(this, GridLayoutActivity.class);
        BaseOkHttpClient.newBuilder()
                .addParam("email", strName)
                .addParam("password", strPassword)
                .post()
                .url("http://129.211.82.124:3000/api/v1/sessions/stations")
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        System.out.println("登录成功");
                        JSONObject json = JSONObject.parseObject(o.toString());
                        SharedPreferences user = getSharedPreferences("user_info",0);
                        SharedPreferences.Editor editor =  user.edit();
                        editor.putString("user_info",json.getString("data"));
                        editor.commit();
                        startActivity(intent);
                        //结束本页面
                        finish();
//                        getSharedPreferences();
//                        Toast.makeText(LoginActivity.this, "成功：" + o.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int code) {
                        Toast.makeText(LoginActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(LoginActivity.this, "失败：" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
