package com.qhiehome.ihome.network.service.park;

import com.qhiehome.ihome.network.model.park.reserve.ReserveRequest;
import com.qhiehome.ihome.network.model.park.reserve.ReserveResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/7/28.
 */

public interface ReserveService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("parking/reserve")
    Call<ReserveResponse> reserve(@Body ReserveRequest reserveRequest);
}
