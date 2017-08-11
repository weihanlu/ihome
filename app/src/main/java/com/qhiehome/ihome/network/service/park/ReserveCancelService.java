package com.qhiehome.ihome.network.service.park;

import com.qhiehome.ihome.network.model.park.reservecancel.ReserveCancelRequest;
import com.qhiehome.ihome.network.model.park.reservecancel.ReserveCancelResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/8/11.
 */

public interface ReserveCancelService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("order/cancel")
    Call<ReserveCancelResponse> reserve(@Body ReserveCancelRequest reserveRequest);
}
