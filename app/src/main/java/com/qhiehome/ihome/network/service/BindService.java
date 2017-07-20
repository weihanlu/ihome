package com.qhiehome.ihome.network.service;

import com.qhiehome.ihome.network.model.bind.BindRequest;
import com.qhiehome.ihome.network.model.bind.BindResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/7/20.
 */

public interface BindService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("bind")
    Call<BindResponse> bind(@Body BindRequest bindRequest);
}
