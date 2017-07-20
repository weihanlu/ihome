package com.qhiehome.ihome.network.service.park;

import com.qhiehome.ihome.network.model.park.enter.EnterParkingRequest;
import com.qhiehome.ihome.network.model.park.enter.EnterParkingResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/7/20.
 */

public interface EnterParkingService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("enter")
    Call<EnterParkingResponse> enterParking(@Body EnterParkingRequest enterParkingRequest);
}
