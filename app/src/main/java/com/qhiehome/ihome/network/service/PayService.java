package com.qhiehome.ihome.network.service;

import com.qhiehome.ihome.network.model.pay.PayRequest;
import com.qhiehome.ihome.network.model.pay.PayResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/7/20.
 */

public interface PayService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("pay")
    Call<PayResponse> pay(@Body PayRequest payRequest);
}
