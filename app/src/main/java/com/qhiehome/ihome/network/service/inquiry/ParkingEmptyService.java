package com.qhiehome.ihome.network.service.inquiry;

import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/7/21.
 */

public interface ParkingEmptyService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("parking/query")
    Call<ParkingEmptyResponse> parkingEmpty(@Body ParkingEmptyRequest parkingEmptyRequest);
}
