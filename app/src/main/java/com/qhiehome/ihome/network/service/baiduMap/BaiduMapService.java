package com.qhiehome.ihome.network.service.baiduMap;

import com.qhiehome.ihome.network.model.baiduMap.BaiduMapResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by YueMa on 2017/8/11.
 */

public interface BaiduMapService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })

    @GET("geocoder/v2/")
    Call<BaiduMapResponse> queryLatLnt(@QueryMap Map<String, String> option);

}
