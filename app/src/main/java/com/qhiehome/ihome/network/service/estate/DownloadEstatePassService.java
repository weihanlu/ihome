package com.qhiehome.ihome.network.service.estate;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by YueMa on 2017/9/8.
 */

public interface DownloadEstatePassService {
    @GET("pic/pass/{pass}")
    Call<ResponseBody> downloadPass(@Path("pass") String PassPath);
}
