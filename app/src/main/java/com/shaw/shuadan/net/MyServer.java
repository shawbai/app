package com.shaw.shuadan.net;

import com.shaw.shuadan.entity.UpLoadBean;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface MyServer {
    public String Url = "http://yun918.cn/study/public/";

    @Multipart
    @POST("file_upload.php")
    Observable<UpLoadBean> upload(@Part("key") RequestBody requestBody, @Part MultipartBody.Part
            file);
}