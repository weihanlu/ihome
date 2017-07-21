package com.qhiehome.ihome.network.service.inquiry;

import com.qhiehome.ihome.network.model.inquiry.order.OrderRequest;
import com.qhiehome.ihome.network.model.inquiry.order.OrderResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/7/21.
 */

public interface OrderService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("order/all")
    Call<OrderResponse> order(@Body OrderRequest orderRequest);
}
