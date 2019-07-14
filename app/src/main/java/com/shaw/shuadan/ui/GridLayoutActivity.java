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
import com.shaw.shuadan.ui.user.RegisterActivity;
import com.shaw.shuadan.util.BaseCallBack;
import com.shaw.shuadan.util.BaseOkHttpClient;
import com.shaw.shuadan.util.RegexUtils;
import com.shaw.shuadan.util.StringUtils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class GridLayoutActivity extends Activity {
    @BindView(R.id.topbar)
    QMUITopBar mTopBar;
    final String TAG = getClass().getSimpleName();

    public String getToken() {
        SharedPreferences user = getSharedPreferences("user_info",0);
        String userInfoStr = user.getString("user_info","");
        JSONObject json = JSONObject.parseObject(userInfoStr);
        if(json!=null&&!StringUtils.isEmpty(json.toJSONString())) {
            return json.getString("tk");
        }else{
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 沉浸式状态栏
        QMUIStatusBarHelper.translucent(this);

        View root = LayoutInflater.from(this).inflate(R.layout.activity_grid_layout, null);
        ButterKnife.bind(this, root);
        String jsonStr = getToken();
        if(jsonStr==null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            //结束本页面
            finish();
        }

        //初始化状态栏
        initTopBar();

        setContentView(root);
    }

    private void initTopBar() {
        mTopBar.addRightImageButton(R.mipmap.icon_error, R.id.empty_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(GridLayoutActivity.this, LoginActivity.class);
                        SharedPreferences user = getSharedPreferences("user_info",0);
                        SharedPreferences.Editor editor =  user.edit();
                        editor.putString("user_info","");
                        editor.commit();
                        startActivity(intent);
                        finish();
                    }
                });
        mTopBar.setTitle("首页");
    }

    //登录按钮
    @OnClick({R.id.button_one})
    void onClickOneButton(View view) {
        Intent intent = null;
//        Log.d(TAG, "登录页面，文字：" + ((TextView) view).getText() + " 被点击了。");
        switch (view.getId()) {
            case R.id.button_one:
                intent = new Intent(this, TaobaoActivity.class);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    //登录按钮
    @OnClick({R.id.buttone_three})
    void onClickThreeButton(View view) {
        Intent intent = null;
//        Log.d(TAG, "登录页面，文字：" + ((TextView) view).getText() + " 被点击了。");
        switch (view.getId()) {
            case R.id.buttone_three:
                intent = new Intent(this, OrderUploadActivity.class);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    //登录按钮
    @OnClick({R.id.button_six})
    void onClickSixButton(View view) {
        Intent intent = null;
//        Log.d(TAG, "登录页面，文字：" + ((TextView) view).getText() + " 被点击了。");
        switch (view.getId()) {
            case R.id.button_six:
                intent = new Intent(this, OrderListActivity.class);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }


}
