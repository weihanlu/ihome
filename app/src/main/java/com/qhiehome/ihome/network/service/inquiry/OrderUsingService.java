package com.qhiehome.ihome.network.service.inquiry;

import com.qhiehome.ihome.network.model.inquiry.orderusing.OrderUsingRequest;
import com.qhiehome.ihome.network.model.inquiry.orderusing.OrderUsingResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/8/25.
 */

public interface OrderUsingService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("usingorder")
    Call<OrderUsingResponse> orderUsing(@Body OrderUsingRequest orderUsingRequest);
}
