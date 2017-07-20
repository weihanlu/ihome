package com.qhiehome.ihome.network.service;

import com.qhiehome.ihome.network.model.parkinglist.ParkingListRequest;
import com.qhiehome.ihome.network.model.parkinglist.ParkingListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/7/20.
 */

public interface ParkingListService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("parking/all")
    Call<ParkingListResponse> parkinglist(@Body ParkingListRequest parkingListRequest);
}
