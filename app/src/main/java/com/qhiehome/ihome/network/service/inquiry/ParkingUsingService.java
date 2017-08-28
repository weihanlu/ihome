package com.qhiehome.ihome.network.service.inquiry;

import com.qhiehome.ihome.network.model.inquiry.parkingusing.ParkingUsingRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingusing.ParkingUsingResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/8/24.
 */

public interface ParkingUsingService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("occupation")
    Call<ParkingUsingResponse> parkingUsingQuery(@Body ParkingUsingRequest parkingUsingRequest);
}
