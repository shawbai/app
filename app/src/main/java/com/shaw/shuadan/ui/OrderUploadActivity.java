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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.shaw.shuadan.MainActivity;
import com.shaw.shuadan.R;
import com.shaw.shuadan.entity.UpLoadBean;
import com.shaw.shuadan.net.MyServer;
import com.shaw.shuadan.ui.user.LoginActivity;
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

public class OrderUploadActivity extends Activity {
    @BindView(R.id.topbar)
    QMUITopBar mTopBar;
    final String TAG = getClass().getSimpleName();


    private Button bt_open;
    private Button bt_tack;
    private ImageView img;
    private File mFile;
    private Uri mImageUri;

    private static final int CAMERA_CODE = 200;
    private static final int ALBUM_CODE = 100;


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

        View root = LayoutInflater.from(this).inflate(R.layout.activity_tool_order_upload, null);
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
                        Toast.makeText(OrderUploadActivity.this, "上传成功：" , Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(int code) {
                        if(code==401){

                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(OrderUploadActivity.this, "失败：" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                },getToken());
    }

    private void initTopBar() {
        initView();
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTopBar.setTitle("上传淘宝ID");
    }

    private void initView() {
        img = (ImageView) findViewById(R.id.img);
    }

    @OnClick(R.id.bt_tack)
    void takesD() {
        //权限判断
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            openAlbum();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        startActivityForResult(intent,ALBUM_CODE);
    }

    //处理权限
    @OnClick({R.id.bt_open})
    void takePhoto() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 200);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (permissions[0] == Manifest.permission.CAMERA){
                openCamera();
            }else if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE){
                openAlbum();
            }
        }
    }

    private void openCamera() {
        //创建文件用于保存图片
        mFile = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        if (!mFile.exists()) {
            try {
                mFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //适配7.0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mImageUri = Uri.fromFile(mFile);
        } else {
            //第二个参数要和清单文件中的配置保持一致
            mImageUri = FileProvider.getUriForFile(this, "com.baidu.upload.provider", mFile);
        }

        //启动相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);//将拍照图片存入mImageUri
        startActivityForResult(intent, CAMERA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    //Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
                    // .openInputStream(mImageUri));
                    //img.setImageBitmap(bitmap);

                    //处理照相之后的结果并上传
                    uploadOk(mFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if (requestCode == ALBUM_CODE){
            //相册
            if (resultCode == RESULT_OK){
                Uri imageUri = data.getData();
                //处理uri,7.0以后的fileProvider 把URI 以content provider 方式 对外提供的解析方法
                File file = getFileFromUri(imageUri, this);

                if (file.exists()){
                    updateFile(file);
                }
            }
        }
    }



    public File getFileFromUri(Uri uri, Context context) {
        if (uri == null) {
            return null;
        }
        switch (uri.getScheme()) {
            case "content":
                return getFileFromContentUri(uri, context);
            case "file":
                return new File(uri.getPath());
            default:
                return null;
        }
    }

    /**
     通过内容解析中查询uri中的文件路径
     */
    private File getFileFromContentUri(Uri contentUri, Context context) {
        if (contentUri == null) {
            return null;
        }
        File file = null;
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(contentUri, filePathColumn, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            cursor.close();

            if (!TextUtils.isEmpty(filePath)) {
                file = new File(filePath);
            }
        }
        return file;
    }

    /**
     * 文件上传
     */
    private void uploadOk(File file) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        //设置上传文件以及文件对应的MediaType类型
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);

        //MultipartBody文件上传
        /**区别：
         * addFormDataPart:   上传key:value形式
         * addPart:  只包含value数据
         */
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)//设置文件上传类型
                .addFormDataPart("key", "img")//文件在服务器中保存的文件夹路径
                .addFormDataPart("file", file.getName(), requestBody)//包含文件名字和内容
                .build();

        Request request = new Request.Builder()
                .url("http://yun918.cn/study/public/file_upload.php")
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();

                Gson gson = new Gson();
                final UpLoadBean upLoadBean = gson.fromJson(str, UpLoadBean.class);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (upLoadBean != null && upLoadBean.getCode() == 200) {
                            Log.d("xxx", upLoadBean.getData().getUrl());
                            success(upLoadBean);
                        } else {
                            Toast.makeText(OrderUploadActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    private void updateFile(File file) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MyServer.Url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        MyServer myServer = retrofit.create(MyServer.class);

        //文件封装
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpg"),file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                "file", file.getName(), requestBody);

        //文本封装
        RequestBody requestBody1 = RequestBody.create(MediaType.parse("multipart/form-data"),"img");

        final Observable<UpLoadBean> upload = myServer.upload(requestBody1,filePart);

        upload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UpLoadBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(UpLoadBean upLoadBean) {
                        if (upLoadBean != null && upLoadBean.getCode() == 200) {
                            Log.d("lzj", upLoadBean.getData().getUrl());
                            success(upLoadBean);
                        } else {
                            Toast.makeText(OrderUploadActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private void success(UpLoadBean upLoadBean) {
        Toast.makeText(OrderUploadActivity.this, upLoadBean.getRes(), Toast.LENGTH_SHORT).show();
        Glide.with(this).load(upLoadBean.getData().getUrl()).into(img);
    }



}
