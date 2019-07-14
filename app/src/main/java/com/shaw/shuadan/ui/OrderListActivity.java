package com.shaw.shuadan.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.shaw.shuadan.R;
import com.shaw.shuadan.entity.UpLoadBean;
import com.shaw.shuadan.net.MyServer;
import com.shaw.shuadan.util.BaseCallBack;
import com.shaw.shuadan.util.BaseOkHttpClient;
import com.shaw.shuadan.util.StringUtils;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrderListActivity extends Activity {
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

        View root = LayoutInflater.from(this).inflate(R.layout.activity_tool_order_list, null);
        ButterKnife.bind(this, root);
        //初始化状态栏
        initTopBar();
        setContentView(root);
        BaseOkHttpClient.newBuilder()
                .get()
                .addParam("status",1)
                .addParam("page",1)
                .addParam("limit",10)
                .url("http://129.211.82.124:3000/api/v1/orders?status=1&page=1&limit=10")
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(OrderListActivity.this, "上传成功：" , Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(int code) {
                        if(code==401){

                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(OrderListActivity.this, "失败：" + e.toString(), Toast.LENGTH_SHORT).show();
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

}
