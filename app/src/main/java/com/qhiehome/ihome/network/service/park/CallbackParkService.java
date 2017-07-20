package com.qhiehome.ihome.network.service.park;

import com.qhiehome.ihome.network.model.park.callback.CallbackParkRequest;
import com.qhiehome.ihome.network.model.park.callback.CallbackParkResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface CallbackParkService {

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("parking/callback")
    Call<CallbackParkResponse> callback(@Body CallbackParkRequest callbackParkRequest);
}
