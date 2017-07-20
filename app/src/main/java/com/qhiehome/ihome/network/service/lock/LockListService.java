package com.qhiehome.ihome.network.service.lock;

import com.qhiehome.ihome.network.model.lock.list.LockListRequest;
import com.qhiehome.ihome.network.model.lock.list.LockListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface LockListService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("lock/all")
    Call<LockListResponse> list(@Body LockListRequest lockListRequest);
}
