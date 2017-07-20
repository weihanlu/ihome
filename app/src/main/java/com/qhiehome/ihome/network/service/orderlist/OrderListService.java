package com.qhiehome.ihome.network.service.orderlist;

import com.qhiehome.ihome.network.model.orderlist.OrderListRequest;
import com.qhiehome.ihome.network.model.orderlist.OrderListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/7/20.
 */

public interface OrderListService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("order/all")
    Call<OrderListResponse> orderlist(@Body OrderListRequest orderListRequest);
}
