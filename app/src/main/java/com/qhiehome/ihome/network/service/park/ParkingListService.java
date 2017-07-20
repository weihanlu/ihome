package com.qhiehome.ihome.network.service.park;

import com.qhiehome.ihome.network.model.park.list.ParkingListRequest;
import com.qhiehome.ihome.network.model.park.list.ParkingListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ParkingListService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("parking/all")
    Call<ParkingListResponse> list(@Body ParkingListRequest parkingListRequest);
}
