package com.qhiehome.ihome.network.service.estate;

import com.qhiehome.ihome.network.model.estate.map.DownloadEstateMapRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/9/8.
 */

public interface DownloadEstateMapService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })

    @POST("pic/nav")
    Call<ResponseBody> downloadMap(@Body DownloadEstateMapRequest downloadEstateMapRequest);
}
