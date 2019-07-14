package com.shaw.shuadan.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.shaw.shuadan.MainActivity;
import com.shaw.shuadan.R;
import com.shaw.shuadan.ui.user.LoginActivity;
import com.shaw.shuadan.util.BaseCallBack;
import com.shaw.shuadan.util.BaseOkHttpClient;
import com.shaw.shuadan.util.RegexUtils;
import com.shaw.shuadan.util.StringUtils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class TaobaoActivity extends Activity {
    @BindView(R.id.topbar)
    QMUITopBar mTopBar;
    final String TAG = getClass().getSimpleName();

    public String getToken() {
        JSONObject json = getUserInfo();
        if(json!=null&&!StringUtils.isEmpty(json.toJSONString())) {
            return json.getString("tk");
        }else{
            return null;
        }
    }


    public JSONObject getUserInfo() {
        SharedPreferences user = getSharedPreferences("user_info",0);
        String userInfoStr = user.getString("user_info","");
        return JSONObject.parseObject(userInfoStr);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 沉浸式状态栏
        QMUIStatusBarHelper.translucent(this);

        View root = LayoutInflater.from(this).inflate(R.layout.activity_tool_taobao, null);
        ButterKnife.bind(this, root);
        //初始化状态栏
        initTopBar();
        setContentView(root);
        BaseOkHttpClient.newBuilder()
                .get()
                .url("http://129.211.82.124:3000/api/v1/kycs/"+getUserInfo().getJSONObject("admin").getInteger("id"))
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(TaobaoActivity.this, "上传成功：" , Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(int code) {
                        if(code==401){

                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(TaobaoActivity.this, "失败：" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                },getToken());
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTopBar.setTitle("上传淘宝ID");
    }


    //登录按钮
    @OnClick({R.id.button_tool_taobao})
    void onClickButton(View view) {
        switch (view.getId()) {
            case R.id.button_tool_taobao:
                onClickTaobao(view);
                break;
        }
    }

    //登录按钮事件
    private void onClickTaobao(View view) {

        String id = ((EditText) findViewById(R.id.edittext_taobao_id)).getText().toString().trim();


        if (id.length() < 6 || id.length() > 20) {
            Toast.makeText(this, "密码长度为6到20位！", Toast.LENGTH_SHORT).show();
            QMUIKeyboardHelper.showKeyboard((EditText) findViewById(R.id.edittext_taobao_id), 1500);
            return;
        }
        Intent loginIntent = new Intent(this, LoginActivity.class);
        Intent homeIntent = new Intent(this, GridLayoutActivity.class);
        BaseOkHttpClient.newBuilder()
                .addParam("taobao_id", id)
                .post()
                .url("http://129.211.82.124:3000/api/v1/kycs")
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(TaobaoActivity.this, "上传成功：" , Toast.LENGTH_SHORT).show();
                        startActivity(homeIntent);
                        finish();
                    }

                    @Override
                    public void onError(int code) {
                        if(code==401){
                            startActivity(loginIntent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(TaobaoActivity.this, "失败：" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                },getToken());
    }



}
