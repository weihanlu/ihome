package com.qhiehome.ihome.network.service.inquiry;

import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/7/21.
 */

public interface ParkingOwnedService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("parking/all")
    Call<ParkingOwnedResponse> parkingOwned(@Body ParkingOwnedRequest parkingOwnedRequest);
}
