package com.qhiehome.ihome.network.service;

import com.qhiehome.ihome.network.model.charge.ChargeRequest;
import com.qhiehome.ihome.network.model.charge.ChargeResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/7/20.
 */

public interface ChargeService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("chargeing")
    Call<ChargeResponse> charge(@Body ChargeRequest chargeRequest);
}
