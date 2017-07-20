package com.qhiehome.ihome.network.service.lock;

import com.qhiehome.ihome.network.model.lock.bind.BindRequest;
import com.qhiehome.ihome.network.model.lock.bind.BindResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface BindService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("bind")
    Call<BindResponse> bind(@Body BindRequest bindRequest);
}
