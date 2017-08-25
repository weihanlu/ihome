package com.qhiehome.ihome.network.service.inquiry;

import com.qhiehome.ihome.network.model.inquiry.orderowner.OrderOwnerRequest;
import com.qhiehome.ihome.network.model.inquiry.orderowner.OrderOwnerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/8/25.
 */

public interface OrderOwnerService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("order/owner")
    Call<OrderOwnerResponse> orderOwner(@Body OrderOwnerRequest orderOwnerRequest);
}
