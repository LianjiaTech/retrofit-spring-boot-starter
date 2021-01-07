package com.github.lianjiatech.retrofit.spring.boot.test.http;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * 文件下载API
 *
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
