package com.qhiehome.ihome.network.service.alipay;


import com.qhiehome.ihome.network.model.alipay.AliPayRequest;
import com.qhiehome.ihome.network.model.alipay.AliPayResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AliPayService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("pay/alipay")
    Call<AliPayResponse> aliPay(@Body AliPayRequest payRequest);
}
