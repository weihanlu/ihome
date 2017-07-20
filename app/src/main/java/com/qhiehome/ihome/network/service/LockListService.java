package com.qhiehome.ihome.network.service;

import com.qhiehome.ihome.network.model.locklist.LockListRequest;
import com.qhiehome.ihome.network.model.locklist.LockListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/7/20.
 */

public interface LockListService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("lock/all")
    Call<LockListResponse> locklist(@Body LockListRequest lockListRequest);
}
