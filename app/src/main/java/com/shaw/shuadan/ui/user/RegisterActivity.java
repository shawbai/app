package com.shaw.shuadan.ui.user;

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
import com.shaw.shuadan.util.BaseCallBack;
import com.shaw.shuadan.util.BaseOkHttpClient;
import com.shaw.shuadan.util.RegexUtils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class RegisterActivity extends Activity {
    @BindView(R.id.topbar)
    QMUITopBar mTopBar;
    final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 沉浸式状态栏
        QMUIStatusBarHelper.translucent(this);

        View root = LayoutInflater.from(this).inflate(R.layout.activity_register, null);
        ButterKnife.bind(this, root);
        //初始化状态栏
        initTopBar();

        setContentView(root);
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTopBar.setTitle("注册账号");
    }


    //登录按钮
    @OnClick({R.id.button_register})
    void onClickButton(View view) {
        switch (view.getId()) {
            case R.id.button_register:
                onClickRegister(view);
                break;
        }
    }

    //登录按钮事件
    private void onClickRegister(View view) {
//        "admin@localhost.com"
//        "bananaiscool"
        String strName = ((EditText) findViewById(R.id.edittext_name)).getText().toString().trim();
        String strPassword = ((EditText) findViewById(R.id.edittext_password)).getText().toString().trim();
        String strPosition = ((EditText) findViewById(R.id.edittext_position)).getText().toString().trim();
        String strPaymentCode = ((EditText) findViewById(R.id.edittext_payment_code)).getText().toString().trim();

        if (!RegexUtils.isEmail(strName)) {
            Toast.makeText(this, "账号格式不正确！", Toast.LENGTH_SHORT).show();
            QMUIKeyboardHelper.showKeyboard((EditText) findViewById(R.id.edittext_name), 1500);
            return;
        }
        if (strPassword.length() < 6 || strPassword.length() > 20) {
            Toast.makeText(this, "密码长度为6到20位！", Toast.LENGTH_SHORT).show();
            QMUIKeyboardHelper.showKeyboard((EditText) findViewById(R.id.edittext_password), 1500);
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
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
                        Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(RegisterActivity.this, "失败：" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



}
