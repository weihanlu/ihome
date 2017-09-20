package com.qhiehome.ihome.network.service.pay;

import com.qhiehome.ihome.network.model.pay.notify.PayNotifyRequest;
import com.qhiehome.ihome.network.model.pay.notify.PayNotifyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/9/19.
 */

public interface PayNotifyService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("pay/notify")
    Call<PayNotifyResponse> notify(@Body PayNotifyRequest payNotifyRequest);
}
